package com.example.controller

import com.auth0.jwt.interfaces.DecodedJWT
import com.example.Configs
import com.example.data.db.entity.VerificationOtpEntity
import com.example.data.db.table.OtpPurpose
import com.example.data.db.table.VerificationOtp
import com.example.data.repository.KeyStoreRepository
import com.example.data.repository.UserRepository
import com.example.model.ForbiddenException
import com.example.model.JWTPrincipalExtended
import com.example.model.NotFoundException
import com.example.model.RateLimitReachedException
import com.example.model.UnauthorizedException
import com.example.model.request.LoginRequest
import com.example.model.request.RefreshTokenRequest
import com.example.model.request.VerifyOtpRequest
import com.example.model.response.AccessTokenResponse
import com.example.model.response.LoginResponse
import com.example.model.response.MessageResponse
import com.example.model.response.UserResponse
import com.example.util.CodeUtils
import com.example.util.TokenUtils
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class AuthController(private val userRepository: UserRepository, private val keyStoreRepository: KeyStoreRepository) {

    suspend fun login(call: ApplicationCall) {
        val loginRequest = call.receive<LoginRequest>()
        val userEntity = userRepository.findUserByEmail(email = loginRequest.email)
        if (userEntity == null) {
            throw NotFoundException("User with email ${loginRequest.email} not found.")
        } else {
            val tokens = keyStoreRepository.createTokens(userEntity)
            call.respond(LoginResponse(user = UserResponse.from(userEntity), tokens = tokens))
        }
    }

    suspend fun refreshToken(call: ApplicationCall) {
        val authHeader = call.request.headers["Authorization"]
        if (authHeader == null) {
            call.respond(HttpStatusCode.Unauthorized)
        } else {
            val refreshTokenRequest = call.receive<RefreshTokenRequest>()
            val accessToken = authHeader.replace("Bearer\\s+".toRegex(), "")
            val refreshToken = refreshTokenRequest.refreshToken

            val decodedAccessToken: DecodedJWT
            val decodedRefreshToken: DecodedJWT

            try {
                decodedAccessToken = TokenUtils.verifyAccessTokenIgnoreExpiry(accessToken)
            } catch (t: Throwable) {
                throw UnauthorizedException("Invalid access token.")
            }

            try {
                decodedRefreshToken = TokenUtils.verifyRefreshToken(refreshToken)
            } catch (_: Throwable) {
                throw UnauthorizedException("Invalid refresh token.")
            }

            val accessKey = decodedAccessToken.getClaim(Configs.JWT_CLAIM_KEY).asString()
            val refreshKey = decodedRefreshToken.getClaim(Configs.JWT_CLAIM_KEY).asString()

            if (accessKey == null || refreshKey == null) {
                throw UnauthorizedException("Invalid token.")
            }

            val userEntity = userRepository.findUserByKey(accessKey = accessKey, refreshKey = refreshKey)
                ?: throw UnauthorizedException("Invalid token.")

            val newAccessToken = keyStoreRepository.createAccessToken(userEntity = userEntity, refreshKey = refreshKey)

            call.respond(AccessTokenResponse(accessToken = newAccessToken))
        }
    }

    suspend fun logout(call: ApplicationCall) {
        val userEntity = checkNotNull(call.principal<JWTPrincipalExtended>()).user
        val logoutRequest = call.receive<RefreshTokenRequest>()
        val refreshToken = logoutRequest.refreshToken

        val decodedRefreshToken: DecodedJWT

        try {
            decodedRefreshToken = TokenUtils.verifyRefreshToken(refreshToken)
        } catch (_: Throwable) {
            throw UnauthorizedException("Invalid refresh token.")
        }

        val refreshKey = decodedRefreshToken.getClaim(Configs.JWT_CLAIM_KEY).asString()
            ?: throw UnauthorizedException("Invalid token.")

        keyStoreRepository.deleteRefreshToken(userEntity = userEntity, refreshKey = refreshKey)

        call.respond(MessageResponse(message = "Logged out successfully!"))
    }

    suspend fun hello(call: ApplicationCall) {
        val principal = checkNotNull(call.principal<JWTPrincipalExtended>())
        val username = principal.payload.getClaim(Configs.JWT_CLAIM_USER_EMAIL).asString()
        val expiresAt =
            TimeUnit.MILLISECONDS.toMinutes(principal.expiresAt?.time?.minus(System.currentTimeMillis())!!)

        call.respond(
            mapOf(
                "message" to "Hello, $username! Token will expire at $expiresAt min.",
                "verificationStatus" to if (principal.user.verified) "Email Verified" else "Email Not Verified"
            )
        )
    }

    suspend fun sendVerificationOTP(call: ApplicationCall) {
        val userEntity = checkNotNull(call.principal<JWTPrincipalExtended>()).user

        val otpEntity = transaction {
            VerificationOtpEntity.find {
                VerificationOtp.user eq userEntity.id
                VerificationOtp.otpPurpose eq OtpPurpose.ACCOUNT_VERIFICATION
            }.singleOrNull()
        }

        if (otpEntity == null) {
            val code = CodeUtils.generateOtp()
            transaction {
                VerificationOtpEntity.new {
                    user = userEntity
                    otp = code
                    otpPurpose = OtpPurpose.ACCOUNT_VERIFICATION
                    resendCount = 0
                    incorrectAttemptCount = 0
                    issuedAt = LocalDateTime.now()
                    expireAt = LocalDateTime.now().plusMinutes(Configs.OTP_EXPIRY_MIN)
                }
            }
            //TODO: send otp via mailing function

            call.respond(MessageResponse(message = "An otp has been sent to the ${userEntity.email}. [${code}]"))
        } else {
            var sentCount = otpEntity.resendCount
            if (sentCount >= Configs.OTP_MAX_RESEND_COUNT) {
                // block if more than X otp requested with in Y min
                if (LocalDateTime.now().isBefore(otpEntity.issuedAt.plusMinutes(Configs.OTP_RATE_LIMIT_RESET_MIN))) {
                    throw RateLimitReachedException("OTP rate limit exceeded because of too many resend attempts, please try again later.")
                } else {
                    sentCount = 0
                }
            }
            sentCount++
            transaction {
                val isExpired = LocalDateTime.now().isAfter(otpEntity.expireAt)
                if (isExpired) {
                    otpEntity.incorrectAttemptCount = 0
                    otpEntity.otp = CodeUtils.generateOtp()
                }

                otpEntity.resendCount = sentCount
                otpEntity.issuedAt = LocalDateTime.now()
                otpEntity.expireAt = LocalDateTime.now().plusMinutes(5)
            }

            //TODO: send otp via mailing function
            call.respond(MessageResponse(message = "An otp has been resent to the [${otpEntity.otp}]"))
        }
    }

    suspend fun verifyOTP(call: ApplicationCall) {
        val userEntity = checkNotNull(call.principal<JWTPrincipalExtended>()).user
        val request = call.receive<VerifyOtpRequest>()

        val otpEntity = transaction {
            VerificationOtpEntity.find {
                VerificationOtp.user eq userEntity.id
                VerificationOtp.otpPurpose eq OtpPurpose.ACCOUNT_VERIFICATION
            }.singleOrNull()
        } ?: throw ForbiddenException("Invalid OTP, please use a valid one.")



        if (otpEntity.otp != request.otp) {
            if (otpEntity.incorrectAttemptCount >= Configs.OTP_VERIFY_INCORRECT_ATTEMPT) {
                throw ForbiddenException("OTP rate limit exceeded because of too many failed verify attempts, please try again later.")
            }
            transaction {
                otpEntity.incorrectAttemptCount++
            }

            throw ForbiddenException("Invalid OTP, please use a valid one.")
        }

        if (LocalDateTime.now().isAfter(otpEntity.expireAt)) {
            throw ForbiddenException("Oops! OTP has been expired.")
        }

        transaction {
            userEntity.verified = true
        }

        call.respond(MessageResponse(message = "OTP verified successfully!"))
    }
}
