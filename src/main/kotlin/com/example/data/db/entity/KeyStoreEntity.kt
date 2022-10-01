package com.example.data.db.entity

import com.example.data.db.table.KeyStores
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class KeyStoreEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<KeyStoreEntity>(KeyStores)

    var user by UserEntity referencedOn KeyStores.user
    var accessKey by KeyStores.accessKey
    var refreshKey by KeyStores.refreshKey
    var createdAt by KeyStores.createdAt
}
