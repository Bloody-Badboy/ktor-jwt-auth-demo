package com.example.route

import com.example.controller.UserController
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.configureUserRoute() {
    val userController: UserController by inject()

    routing {
        route("/user") {
            post("/register") { userController.register(call) }
            authenticate {
                get { userController.getUser(call) }
                delete { userController.deleteUser(call) }
            }
        }
    }
}
