package com.example

import com.example.data.db.table.KeyStores
import com.example.data.db.table.Users
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
        SchemaUtils.create(Users, KeyStores)
    }
    embeddedServer(Netty, port = 9000) {
        configureKoin()
        installFeatures()
        configureAuthRouting()
        configureUserRoute()
    }.start(wait = true)
}
