package com.example.data.db.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

enum class OtpPurpose {
    ACCOUNT_VERIFICATION
}

object VerificationOtp : IntIdTable(name = "verification_otp") {
    val user = reference("user", User)
    val otp = varchar("otp", 256)
    val otpPurpose = customEnumeration<OtpPurpose>(name = "otp_purpose",
        sql = "ENUM (" + OtpPurpose.values().joinToString { "'" + it.name + "'" } + ")",
        fromDb = {
            enumValueOf(it as String)
        }, toDb = { type ->
            type.name
        })
    val resendCount = integer("resend_count")
    val incorrectAttemptCount = integer("incorrect_attempt_count")
    val issuedAt = datetime("issued_at")
    val expireAt = datetime("expire_at")
}
