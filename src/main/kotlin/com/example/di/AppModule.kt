package com.example.di

import com.example.data.repository.DefaultKeyStoreRepository
import com.example.data.repository.DefaultUserRepository
import com.example.data.repository.KeyStoreRepository
import com.example.data.repository.UserRepository
import org.koin.dsl.module

val appModule = module {
    single<KeyStoreRepository> {
        DefaultKeyStoreRepository()
    }
    single<UserRepository> {
        DefaultUserRepository()
    }
}
