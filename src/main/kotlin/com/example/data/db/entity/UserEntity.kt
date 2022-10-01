package com.example.data.db.entity

import com.example.data.db.table.Users
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class UserEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserEntity>(Users)

    var firstName by Users.firstName
    var lastName by Users.lastName
    var email by Users.email
    var password by Users.password
    var createdAt by Users.createdAt
    var updatedAt by Users.updatedAt
}
