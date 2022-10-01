package com.example.data.repository

import com.example.data.db.entity.KeyStoreEntity
import com.example.data.db.entity.UserEntity
import com.example.data.db.table.KeyStore
import com.example.model.UnauthorizedException
import com.example.model.response.TokenResponse
import com.example.toHex
import com.example.util.TokenUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.random.Random

interface KeyStoreRepository {

    fun createTokens(userEntity: UserEntity): TokenResponse

    fun createAccessToken(userEntity: UserEntity, refreshKey: String): String

    fun deleteRefreshToken(userEntity: UserEntity, refreshKey: String)
}

class DefaultKeyStoreRepository : KeyStoreRepository {

    override fun createTokens(userEntity: UserEntity): TokenResponse {
        val randAccessKey = Random.nextBytes(64).toHex()
        val randRefreshKey = Random.nextBytes(64).toHex()

        transaction {
            KeyStoreEntity.new {
                user = userEntity
                accessKey = randAccessKey
                refreshKey = randRefreshKey
            }
        }

        val accessToken = TokenUtils.generateAccessToken(key = randAccessKey, userEntity = userEntity)
        val refreshToken = TokenUtils.generateRefreshToken(key = randRefreshKey, userEntity = userEntity)

        return TokenResponse(accessToken, refreshToken)
    }

    override fun createAccessToken(userEntity: UserEntity, refreshKey: String): String {
        val keyStoreEntity = transaction {
            KeyStoreEntity.find {
                KeyStore.user eq userEntity.id
                KeyStore.refreshKey eq refreshKey
            }.singleOrNull() ?: throw UnauthorizedException("Invalid refresh token.")
        }

        val randAccessKey = Random.nextBytes(64).toHex()
        transaction {
            keyStoreEntity.accessKey = randAccessKey
        }
        return TokenUtils.generateAccessToken(key = randAccessKey, userEntity = userEntity)
    }

    override fun deleteRefreshToken(userEntity: UserEntity, refreshKey: String) {
        transaction {
            val keyStoreEntity = KeyStoreEntity.find {
                KeyStore.user eq userEntity.id
                KeyStore.refreshKey eq refreshKey
            }.singleOrNull() ?: throw UnauthorizedException("Invalid refresh token.")

            keyStoreEntity.delete()
        }
    }
}
