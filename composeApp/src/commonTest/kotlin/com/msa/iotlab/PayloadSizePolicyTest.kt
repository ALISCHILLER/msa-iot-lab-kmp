package com.msa.iotlab

import com.msa.iotlab.payload.PayloadSizePolicy
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * Unit tests for payload size safety limits before send operations reach protocol clients.
 */
class PayloadSizePolicyTest {
    @Test
    fun rejectsOversizedPayload() {
        assertFailsWith<IllegalArgumentException> {
            PayloadSizePolicy.validate(sizeBytes = 11, maxBytes = 10)
        }
    }
}
