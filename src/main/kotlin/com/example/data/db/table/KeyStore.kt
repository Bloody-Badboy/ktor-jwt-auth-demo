package com.example.data.db.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object KeyStore : IntIdTable(name = "key_store") {
    val user = reference("user", User)
    val accessKey = varchar("access_key", 256)
    val refreshKey = varchar("refresh_key", 256)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}
