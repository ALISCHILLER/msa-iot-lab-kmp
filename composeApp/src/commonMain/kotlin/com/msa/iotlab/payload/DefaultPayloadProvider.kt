package com.msa.iotlab.payload

import com.msa.iotlab.protocol.PayloadEncoding

/**
 * Provides safe starter payloads for each supported encoding.
 * Keeping defaults in one place prevents UI screens from duplicating protocol-demo strings.
 */
object DefaultPayloadProvider {
    fun forEncoding(encoding: PayloadEncoding): String = when (encoding) {
        PayloadEncoding.TEXT -> "ping {counter}"
        PayloadEncoding.JSON -> "{\"cmd\":\"status\",\"ts\":{timestamp},\"id\":\"{uuid}\"}"
        PayloadEncoding.HEX -> "70 69 6E 67"
        PayloadEncoding.BASE64 -> "cGluZw=="
    }
}
