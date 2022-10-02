package com.example

import com.example.data.db.table.KeyStore
import com.example.data.db.table.User
import com.example.data.db.table.VerificationOtp
import com.example.di.configureKoin
import com.example.route.configureAuthRouting
import com.example.route.configureUserRoute
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

fun main() {
    Database.connect("jdbc:h2:./db/ktor", driver = "org.h2.Driver", user = "root", password = "")

    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.drop(User, KeyStore, VerificationOtp)
        SchemaUtils.createMissingTablesAndColumns(User, KeyStore, VerificationOtp)
    }
    embeddedServer(Netty, port = 8080) {
        configureKoin()
        installFeatures()
        configureAuthRouting()
        configureUserRoute()
    }.start(wait = true)
}
