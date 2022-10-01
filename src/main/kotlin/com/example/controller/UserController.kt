package com.example.controller

import com.example.data.db.entity.UserEntity
import com.example.data.repository.UserRepository
import com.example.model.ConflictException
import com.example.model.JWTPrincipalExtended
import com.example.model.request.UserRequest
import com.example.model.response.UserResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond

class UserController(private val userRepository: UserRepository) {

    suspend fun register(call: ApplicationCall) {
        val request = call.receive<UserRequest>()
        val user = userRepository.findUserByEmail(email = request.email)
        if (user != null) {
            throw ConflictException("User with email ${request.email} already exists.")
        }
        userRepository.createUser(request = request)
        call.respond(HttpStatusCode.Created)
    }

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
