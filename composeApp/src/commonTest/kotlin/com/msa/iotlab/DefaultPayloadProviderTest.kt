package com.msa.iotlab

import com.msa.iotlab.payload.DefaultPayloadProvider
import com.msa.iotlab.protocol.PayloadEncoding
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Tests starter payload generation so UI defaults remain valid for every encoding.
 */
class DefaultPayloadProviderTest {
    @Test
    fun jsonDefaultContainsRuntimeVariables() {
        val payload = DefaultPayloadProvider.forEncoding(PayloadEncoding.JSON)

        assertTrue(payload.contains("{timestamp}"))
        assertTrue(payload.contains("{uuid}"))
    }
}
