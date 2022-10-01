package com.example.data.repository

import com.example.data.db.entity.KeyStoreEntity
import com.example.data.db.entity.UserEntity
import com.example.model.response.TokenResponse
import com.example.toHex
import com.example.util.TokenUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.random.Random

interface KeyStoreRepository {

    fun createTokens(userEntity: UserEntity): TokenResponse
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
}
