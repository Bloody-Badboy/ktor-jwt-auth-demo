package com.example.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(@SerialName("refresh_token") val refreshToken: String)
