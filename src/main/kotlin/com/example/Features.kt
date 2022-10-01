package com.example

import com.example.controller.UserController
import com.example.model.HttpException
import com.example.model.JWTPrincipalExtended
import com.example.util.TokenUtils
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respond
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import org.slf4j.event.Level

fun Application.installFeatures() {
    val userController: UserController by inject()

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(DefaultHeaders) {
        header(HttpHeaders.Server, "Ktor/Netty")
    }

    install(StatusPages) {
        exception<HttpException> { call, cause ->
            cause.respond(call)
        }
        exception<Exception> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.stackTraceToString())
        }
    }

    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            }
        )
    }
    install(Authentication) {
        jwt {
            verifier(TokenUtils.accessTokenVerifier())
            validate { credential ->
                val payload = credential.payload
                val email = payload.getClaim(Configs.JWT_CLAIM_USER_EMAIL).asString()
                val accessKey = payload.getClaim(Configs.JWT_CLAIM_KEY).asString()
                val user = userController.findUserForAccessKey(accessKey = accessKey)
                if (user != null && user.email == email) {
                    JWTPrincipalExtended(payload, user)
                } else {
                    null
                }
            }
        }
    }
}
