package com.example

import java.time.LocalDateTime
import java.time.ZoneId

fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

fun LocalDateTime.toEpochMilli() = atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
