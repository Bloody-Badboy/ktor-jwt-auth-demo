package com.example.data.repository

import com.example.data.db.entity.KeyStoreEntity
import com.example.data.db.entity.UserEntity
import com.example.data.db.table.KeyStores
import com.example.data.db.table.Users
import com.example.model.NotFoundException
import com.example.model.request.UserRequest
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

interface UserRepository {

    fun createUser(request: UserRequest)

    fun findUserByEmail(email: String): UserEntity?

    fun findUserByKey(accessKey: String): UserEntity?

    fun findUserByKey(accessKey: String, refreshKey: String): UserEntity?

    fun deleteUser(email: String)
}

class DefaultUserRepository : UserRepository {

    override fun createUser(request: UserRequest) {
        transaction {
            UserEntity.new {
                firstName = request.firstName
                lastName = request.lastName
                email = request.email
                password = request.password
            }
        }
    }

    override fun findUserByEmail(email: String): UserEntity? {
        return transaction {
            UserEntity.find {
                Users.email eq email
            }.firstOrNull()
        }
    }

    override fun findUserByKey(accessKey: String): UserEntity? {
        return transaction {
            KeyStoreEntity.find {
                KeyStores.accessKey eq accessKey
            }.firstOrNull()?.user
        }
    }

    override fun findUserByKey(accessKey: String, refreshKey: String): UserEntity? {
        return transaction {
            KeyStoreEntity.find {
                KeyStores.accessKey eq accessKey
                KeyStores.refreshKey eq refreshKey
            }.firstOrNull()?.user
        }
    }

    override fun deleteUser(email: String) {
        transaction {
            val entity = UserEntity.find {
                Users.email eq email
            }.firstOrNull() ?: throw NotFoundException("User with $email not found")
            KeyStores.deleteWhere {
                KeyStores.user eq entity.id
            }
            entity.delete()
        }
    }
}
