package com.example.data.db.entity

import com.example.data.db.table.VerificationOtp
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class VerificationOtpEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<VerificationOtpEntity>(VerificationOtp)

    var user by UserEntity referencedOn VerificationOtp.user
    var otp by VerificationOtp.otp
    var otpPurpose by VerificationOtp.otpPurpose
    var resendCount by VerificationOtp.resendCount
    var incorrectAttemptCount by VerificationOtp.incorrectAttemptCount
    var issuedAt by VerificationOtp.issuedAt
    var expireAt by VerificationOtp.expireAt
}
