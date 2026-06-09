package com.msa.iotlab.payload

import com.msa.iotlab.protocol.PayloadEncoding
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Converts protocol payloads between user-facing text formats and transport bytes.
 * The object is intentionally stateless so it is safe to use from protocol clients and tests.
 */
object PayloadCodec {
    fun encode(text: String, encoding: PayloadEncoding): ByteArray {
        return when (encoding) {
            PayloadEncoding.TEXT, PayloadEncoding.JSON -> text.encodeToByteArray()
            PayloadEncoding.HEX -> hexToBytes(text)
            PayloadEncoding.BASE64 -> decodeBase64(text)
        }
    }

    fun preview(bytes: ByteArray): String = bytes.decodeToString()

    fun previewSafely(bytes: ByteArray): String = runCatching { preview(bytes) }.getOrElse { toHex(bytes) }

    fun toHex(bytes: ByteArray): String = bytes.joinToString(" ") { byte ->
        (byte.toInt() and 0xFF).toString(16).padStart(2, '0').uppercase()
    }

    fun hexToBytes(input: String): ByteArray {
        val clean = input
            .replace(" ", "")
            .replace("\n", "")
            .replace("\t", "")
            .replace("0x", "", ignoreCase = true)
        require(clean.length % 2 == 0) { "Hex payload must have an even number of characters." }
        require(clean.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) { "Hex payload contains invalid characters." }
        return ByteArray(clean.length / 2) { index ->
            clean.substring(index * 2, index * 2 + 2).toInt(16).toByte()
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun encodeBase64(bytes: ByteArray): String = Base64.encode(bytes)

    @OptIn(ExperimentalEncodingApi::class)
    fun decodeBase64(value: String): ByteArray = Base64.decode(value.trim())
}
