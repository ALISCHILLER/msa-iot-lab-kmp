package com.msa.iotlab

import com.msa.iotlab.protocol.ConnectionState
import com.msa.iotlab.protocol.canSendPayload
import com.msa.iotlab.protocol.displayName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for connection-state presentation helpers used by console send guards.
 */
class ConnectionStateTest {
    @Test
    fun onlyConnectedStateCanSendPayload() {
        assertTrue(ConnectionState.Connected.canSendPayload)
        assertFalse(ConnectionState.Idle.canSendPayload)
        assertFalse(ConnectionState.Connecting.canSendPayload)
        assertFalse(ConnectionState.Disconnected("closed").canSendPayload)
        assertFalse(ConnectionState.Failed("offline").canSendPayload)
    }

    @Test
    fun displayNameIsStableForUserFacingDiagnostics() {
        assertEquals("Connected", ConnectionState.Connected.displayName)
        assertEquals("Failed", ConnectionState.Failed("offline").displayName)
    }
}
