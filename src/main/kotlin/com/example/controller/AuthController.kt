package com.example.controller

import com.auth0.jwt.interfaces.DecodedJWT
import com.example.Configs
import com.example.data.repository.KeyStoreRepository
import com.example.data.repository.UserRepository
import com.example.model.ConflictException
import com.example.model.JWTPrincipalExtended
import com.example.model.NotFoundException
import com.example.model.UnauthorizedException
import com.example.model.request.LoginRequest
import com.example.model.request.RefreshTokenRequest
import com.example.model.request.UserRequest
import com.example.model.response.LoginResponse
import com.example.model.response.UserResponse
import com.example.util.TokenUtils
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.concurrent.TimeUnit

class AuthController(private val userRepository: UserRepository, private val keyStoreRepository: KeyStoreRepository) {
    suspend fun register(call: ApplicationCall) {
        val request = call.receive<UserRequest>()
        val user = userRepository.findUserByEmail(email = request.email)
        if (user != null) {
            throw ConflictException("User with email ${request.email} already exists.")
        }
        userRepository.createUser(request = request)
        call.respond(HttpStatusCode.Created)
    }

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

            val accessTokenEmail = decodedAccessToken.getClaim(Configs.JWT_CLAIM_USER_EMAIL).asString()
            val refreshTokenEmail = decodedRefreshToken.getClaim(Configs.JWT_CLAIM_USER_EMAIL).asString()
            val accessKey = decodedAccessToken.getClaim(Configs.JWT_CLAIM_KEY).asString()
            val refreshKey = decodedRefreshToken.getClaim(Configs.JWT_CLAIM_KEY).asString()

            if (accessTokenEmail == null || refreshTokenEmail == null || accessKey == null || refreshKey == null) {
                throw UnauthorizedException("Invalid token.")
            }

            val userEntity = userRepository.findUserByKey(accessKey = accessKey, refreshKey = refreshKey)
                ?: throw UnauthorizedException("Invalid token.")

            call.respond(keyStoreRepository.createTokens(userEntity))
        }
    }

    suspend fun hello(call: ApplicationCall) {
        val principal = checkNotNull(call.principal<JWTPrincipalExtended>())
        val username = principal.payload.getClaim(Configs.JWT_CLAIM_USER_EMAIL).asString()
        val expiresAt =
            TimeUnit.MILLISECONDS.toMinutes(principal.expiresAt?.time?.minus(System.currentTimeMillis())!!)

        call.respond(
            mapOf(
                "message" to "Hello, $username! Token will expire at $expiresAt min."
            )
        )
    }
}
