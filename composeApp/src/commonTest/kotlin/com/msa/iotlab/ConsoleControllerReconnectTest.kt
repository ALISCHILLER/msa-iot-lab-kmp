package com.msa.iotlab

import com.msa.iotlab.console.ConnectionRetryPolicy
import com.msa.iotlab.console.ConsoleController
import com.msa.iotlab.console.ConsoleSessionManager
import com.msa.iotlab.protocol.ConnectionState
import com.msa.iotlab.protocol.SessionStatus
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Regression tests for reconnect session lifecycle and auto-reconnect behavior.
 */
class ConsoleControllerReconnectTest {
    @Test
    fun reconnectAfterManualDisconnectStartsNewSession() = runTest {
        val client = FakeProtocolClient()
        val gateway = RecordingConsoleHistoryGateway()
        val controller = ConsoleController(
            profile = TestDomainFactory.profile(),
            client = client,
            sessionManager = ConsoleSessionManager(gateway),
            retryPolicy = ConnectionRetryPolicy.singleAttempt(),
            dispatchers = TestAppDispatchers(StandardTestDispatcher(testScheduler)),
            scope = backgroundScope,
            ownsScope = false
        )

        controller.connect()
        advanceUntilIdle()
        controller.disconnect()
        advanceUntilIdle()
        controller.connect()
        advanceUntilIdle()

        assertEquals(2, gateway.starts.size)
        assertEquals("session-1", gateway.finishes.single().sessionId)
    }

    @Test
    fun reconnectAfterFailedConnectionStartsFreshSession() = runTest {
        val client = FakeProtocolClient(mutableListOf(ConnectionState.Failed("offline"), ConnectionState.Connected))
        val gateway = RecordingConsoleHistoryGateway()
        val controller = ConsoleController(
            profile = TestDomainFactory.profile(),
            client = client,
            sessionManager = ConsoleSessionManager(gateway),
            retryPolicy = ConnectionRetryPolicy.singleAttempt(),
            dispatchers = TestAppDispatchers(StandardTestDispatcher(testScheduler)),
            scope = backgroundScope,
            ownsScope = false
        )

        controller.connect()
        advanceUntilIdle()
        controller.connect()
        advanceUntilIdle()

        assertEquals(2, gateway.starts.size)
        assertEquals(SessionStatus.FAILED, gateway.finishes.single().status)
        assertEquals(ConnectionState.Connected, controller.connectionState.value)
    }

    @Test
    fun autoReconnectAfterRemoteDisconnectUsesActiveSessionAndReconnects() = runTest {
        val client = FakeProtocolClient(mutableListOf(ConnectionState.Connected, ConnectionState.Connected))
        val gateway = RecordingConsoleHistoryGateway()
        val controller = ConsoleController(
            profile = TestDomainFactory.profile(autoReconnect = true),
            client = client,
            sessionManager = ConsoleSessionManager(gateway),
            retryPolicy = ConnectionRetryPolicy(maxAttempts = 1, delayMillis = 0),
            dispatchers = TestAppDispatchers(StandardTestDispatcher(testScheduler)),
            scope = backgroundScope,
            ownsScope = false
        )

        controller.connect()
        advanceUntilIdle()
        client.emitRemoteDisconnect("network drop")
        advanceUntilIdle()

        assertEquals(1, gateway.starts.size)
        assertEquals(2, client.connectedProfiles.size)
        assertTrue(gateway.logs.any { it.payloadText.contains("Auto reconnect scheduled") })
    }

}
