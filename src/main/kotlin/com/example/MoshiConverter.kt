package com.example

import com.squareup.moshi.Moshi
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.ktor.http.withCharset
import io.ktor.serialization.ContentConverter
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.buffer
import okio.source

class MoshiConverter(private val moshi: Moshi) : ContentConverter {
    override suspend fun deserialize(charset: Charset, typeInfo: TypeInfo, content: ByteReadChannel): Any? {
        val result = runCatching {
            withContext(Dispatchers.IO) {
                moshi.adapter(typeInfo.type.javaObjectType).fromJson(content.toInputStream().source().buffer())
            }
        }
        return result.getOrNull()
    }

    override suspend fun serializeNullable(
        contentType: ContentType,
        charset: Charset,
        typeInfo: TypeInfo,
        value: Any?
    ) = TextContent(
        moshi.adapter(
            value?.javaClass
                ?: Any::class.java
        ).nullSafe().toJson(value),
        contentType.withCharset(charset)
    )
}
