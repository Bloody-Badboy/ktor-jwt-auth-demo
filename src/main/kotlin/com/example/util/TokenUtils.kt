package com.example.util

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.DecodedJWT
import com.example.Configs
import com.example.data.db.entity.UserEntity
import java.util.Date
import java.util.concurrent.TimeUnit

object TokenUtils {
    private val ACCESS_TOKEN_ALGORITHM = Algorithm.HMAC256(Configs.JWT_ACCESS_TOKEN_SECRET)
    private val REFRESH_TOKEN_ALGORITHM = Algorithm.HMAC256(Configs.JWT_REFRESH_TOKEN_SECRET)

    private val ACCESS_TOKEN_VERIFIER: JWTVerifier = JWT.require(ACCESS_TOKEN_ALGORITHM)
        .withAudience(Configs.JWT_AUDIENCE)
        .withIssuer(Configs.JWT_AUDIENCE)
        .build()

    private val REFRESH_TOKEN_VERIFIER: JWTVerifier = JWT.require(REFRESH_TOKEN_ALGORITHM)
        .withAudience(Configs.JWT_AUDIENCE)
        .withIssuer(Configs.JWT_AUDIENCE)
        .build()

    fun accessTokenVerifier() = ACCESS_TOKEN_VERIFIER

    fun verifyRefreshToken(token: String): DecodedJWT = REFRESH_TOKEN_VERIFIER.verify(token)

    fun verifyAccessTokenIgnoreExpiry(token: String): DecodedJWT {
        return try {
            ACCESS_TOKEN_VERIFIER.verify(token)
        } catch (_: TokenExpiredException) {
            JWT.decode(token)
        } catch (t: Throwable) {
            throw t
        }
    }

    fun generateAccessToken(key: String, userEntity: UserEntity): String = JWT.create()
        .withAudience(Configs.JWT_AUDIENCE)
        .withIssuer(Configs.JWT_ISSUER)
        .withClaim(Configs.JWT_CLAIM_USER_EMAIL, userEntity.email)
        .withClaim(Configs.JWT_CLAIM_KEY, key)
        .withIssuedAt(Date())
        .withExpiresAt(Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30)))
        .sign(ACCESS_TOKEN_ALGORITHM)

    fun generateRefreshToken(key: String, userEntity: UserEntity): String = JWT.create()
        .withAudience(Configs.JWT_AUDIENCE)
        .withIssuer(Configs.JWT_ISSUER)
        .withClaim(Configs.JWT_CLAIM_USER_EMAIL, userEntity.email)
        .withClaim(Configs.JWT_CLAIM_KEY, key)
        .withIssuedAt(Date())
        .withExpiresAt(Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(180)))
        .sign(REFRESH_TOKEN_ALGORITHM)
}
