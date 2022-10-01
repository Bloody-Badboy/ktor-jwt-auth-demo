package com.example.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    @SerialName("user") val user: UserResponse,
    @SerialName("tokens") val tokens: TokenResponse
)
