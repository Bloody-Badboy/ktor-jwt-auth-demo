package com.example.di

import com.example.controller.AuthController
import com.example.controller.UserController
import org.koin.dsl.module

val controllerModule = module {
    single {
        UserController(get())
    }

    single {
        AuthController(get(), get())
    }
}
