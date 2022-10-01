package com.example.model.response

import com.example.data.db.entity.UserEntity
import com.example.toEpochMilli
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("email") val email: String,
    @SerialName("verified") val verified: Boolean,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long
) {
    companion object {
        fun from(entity: UserEntity) = UserResponse(
            firstName = entity.firstName,
            lastName = entity.lastName,
            email = entity.email,
            verified = entity.verified,
            createdAt = entity.createdAt.toEpochMilli(),
            updatedAt = entity.updatedAt.toEpochMilli()
        )
    }
}
