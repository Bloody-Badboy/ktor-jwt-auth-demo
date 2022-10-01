package com.example.data.db.entity

import com.example.data.db.table.KeyStore
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class KeyStoreEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<KeyStoreEntity>(KeyStore)

    var user by UserEntity referencedOn KeyStore.user
    var accessKey by KeyStore.accessKey
    var refreshKey by KeyStore.refreshKey
    var createdAt by KeyStore.createdAt
}
