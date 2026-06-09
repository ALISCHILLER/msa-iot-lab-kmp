package com.msa.iotlab

import com.msa.iotlab.payload.PayloadCodec
import com.msa.iotlab.protocol.PayloadEncoding
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

/**
 * Unit tests for payload encoding/decoding utilities used by every protocol client.
 */
class PayloadCodecTest {
    @Test
    fun hexRoundTrip() {
        val bytes = PayloadCodec.encode("70 69 6E 67", PayloadEncoding.HEX)
        assertEquals("ping", bytes.decodeToString())
        assertEquals("70 69 6E 67", PayloadCodec.toHex(bytes))
    }

    @Test
    fun base64RoundTrip() {
        val bytes = PayloadCodec.encode("cGluZw==", PayloadEncoding.BASE64)
        assertContentEquals("ping".encodeToByteArray(), bytes)
    }
}
