package com.example.route

import com.example.controller.AuthController
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.configureAuthRouting() {
    val authController: AuthController by inject()

    routing {
        route("/auth") {
            post("/login") { authController.login(call) }
            post("/refreshToken") { authController.refreshToken(call) }

            authenticate {
                post("/logout") { authController.logout(call) }
                post("/sendOtp") { authController.sendVerificationOTP(call) }
                post("/verifyOtp") { authController.verifyOTP(call) }
                get("/hello") { authController.hello(call) }
            }
        }
    }
}
