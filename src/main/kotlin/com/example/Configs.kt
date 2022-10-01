package com.example

object Configs {
    const val JWT_ACCESS_TOKEN_SECRET = "s3cret"
    const val JWT_REFRESH_TOKEN_SECRET = "s3cret"
    const val JWT_AUDIENCE = "http://0.0.0.0:8080"
    const val JWT_ISSUER = "http://0.0.0.0:8080"

    const val JWT_CLAIM_USER_EMAIL = "email"
    const val JWT_CLAIM_KEY = "key"

    const val OTP_MAX_RESEND_COUNT = 3
    const val OTP_EXPIRY_MIN = 10L
    const val OTP_RATE_LIMIT_RESET_MIN = 60L
    const val OTP_VERIFY_INCORRECT_ATTEMPT = 3
}
