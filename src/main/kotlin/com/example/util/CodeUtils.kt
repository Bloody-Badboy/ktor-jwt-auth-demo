package com.example.util

object CodeUtils {
    fun generateOtp(): String {
        val chars = ('A'..'Z') + ('0'..'9')
        return List(6) {
            chars.random()
        }.joinToString("")
    }
}
