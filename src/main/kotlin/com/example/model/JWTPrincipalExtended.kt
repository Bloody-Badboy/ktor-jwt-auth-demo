package com.example.model

import com.auth0.jwt.interfaces.Payload
import com.example.data.db.entity.UserEntity
import io.ktor.server.auth.Principal
import io.ktor.server.auth.jwt.JWTPayloadHolder

class JWTPrincipalExtended(payload: Payload, val user: UserEntity) : Principal, JWTPayloadHolder(payload)
