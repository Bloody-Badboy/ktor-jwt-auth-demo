package com.example.data.db.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object Users : IntIdTable(name = "users") {
    val firstName = varchar("first_name", 256)
    val lastName = varchar("last_name", 256)
    val email = varchar("email", 256).uniqueIndex()
    val password = varchar("password", 256)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}
