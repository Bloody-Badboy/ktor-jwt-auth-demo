package com.example.model

import com.example.model.response.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond

abstract class HttpException(msg: String?) : Exception(msg) {
    abstract val statusCode: HttpStatusCode

    suspend fun respond(call: ApplicationCall) {
        call.respond(statusCode, ErrorResponse(message = message ?: statusCode.description))
    }
}

class NotFoundException(msg: String? = null) : HttpException(msg) {
    override val statusCode = HttpStatusCode.NotFound
}

class ConflictException(msg: String? = null) : HttpException(msg) {
    override val statusCode = HttpStatusCode.Conflict
}

class UnauthorizedException(msg: String? = null) : HttpException(msg) {
    override val statusCode = HttpStatusCode.Unauthorized
}

class ForbiddenException(msg: String? = null) : HttpException(msg) {
    override val statusCode = HttpStatusCode.Forbidden
}

class RateLimitReachedException(msg: String? = null) : HttpException(msg) {
    override val statusCode = HttpStatusCode.Forbidden
}
