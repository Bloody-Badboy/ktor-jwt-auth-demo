package com.example.data.repository

import com.example.data.db.entity.KeyStoreEntity
import com.example.data.db.entity.UserEntity
import com.example.data.db.table.KeyStore
import com.example.data.db.table.User
import com.example.data.db.table.VerificationOtp
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
                User.email eq email
            }.singleOrNull()
        }
    }

    override fun findUserByKey(accessKey: String): UserEntity? {
        return transaction {
            KeyStoreEntity.find {
                KeyStore.accessKey eq accessKey
            }.singleOrNull()?.user
        }
    }

    override fun findUserByKey(accessKey: String, refreshKey: String): UserEntity? {
        return transaction {
            KeyStoreEntity.find {
                KeyStore.accessKey eq accessKey
                KeyStore.refreshKey eq refreshKey
            }.singleOrNull()?.user
        }
    }

    override fun deleteUser(email: String) {
        transaction {
            val entity = UserEntity.find {
                User.email eq email
            }.singleOrNull() ?: throw NotFoundException("User with $email not found")
            VerificationOtp.deleteWhere {
                VerificationOtp.user eq entity.id
            }
            KeyStore.deleteWhere {
                KeyStore.user eq entity.id
            }
            entity.delete()
        }
    }
}
