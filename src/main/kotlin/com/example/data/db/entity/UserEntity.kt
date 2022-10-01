package com.example.data.db.entity

import com.example.data.db.table.User
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class UserEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserEntity>(User)

    var firstName by User.firstName
    var lastName by User.lastName
    var email by User.email
    var password by User.password
    var verified by User.verified
    var createdAt by User.createdAt
    var updatedAt by User.updatedAt
}
