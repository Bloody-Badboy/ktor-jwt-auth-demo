package com.example.controller

import com.example.data.db.entity.UserEntity
import com.example.data.repository.UserRepository
import com.example.model.JWTPrincipalExtended
import com.example.model.response.UserResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal
import io.ktor.server.response.respond

class UserController(private val userRepository: UserRepository) {

    fun findUserForAccessKey(accessKey: String): UserEntity? {
        return userRepository.findUserByKey(accessKey = accessKey)
    }

    suspend fun getUser(call: ApplicationCall) {
        val principal = checkNotNull(call.principal<JWTPrincipalExtended>())
        call.respond(UserResponse.from(principal.user))
    }

    suspend fun deleteUser(call: ApplicationCall) {
        val principal = checkNotNull(call.principal<JWTPrincipalExtended>())
        userRepository.deleteUser(email = principal.user.email)
        call.respond(HttpStatusCode.OK)
    }
}
