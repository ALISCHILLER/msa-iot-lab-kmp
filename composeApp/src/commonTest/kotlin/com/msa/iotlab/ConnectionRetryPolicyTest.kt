package com.msa.iotlab

import com.msa.iotlab.console.ConnectionRetryPolicy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for retry policy defaults used by console connection orchestration.
 */
class ConnectionRetryPolicyTest {
    @Test
    fun defaultPolicyRetriesMoreThanOnce() {
        val policy = ConnectionRetryPolicy.default()

        assertTrue(policy.maxAttempts > 1)
        assertTrue(policy.delayMillis > 0)
    }

    @Test
    fun singleAttemptPolicyHasNoDelay() {
        val policy = ConnectionRetryPolicy.singleAttempt()

        assertEquals(1, policy.maxAttempts)
        assertEquals(0, policy.delayMillis)
    }
}
