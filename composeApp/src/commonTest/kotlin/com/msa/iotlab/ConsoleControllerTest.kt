package com.msa.iotlab

import com.msa.iotlab.console.ConnectionRetryPolicy
import com.msa.iotlab.console.ConsoleController
import com.msa.iotlab.console.ConsoleSessionManager
import com.msa.iotlab.protocol.ConnectionState
import com.msa.iotlab.protocol.PayloadEncoding
import com.msa.iotlab.protocol.SessionStatus
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for console orchestration, retry behavior and persistence side effects.
 */
class ConsoleControllerTest {
    @Test
    fun connectStartsSessionAndPersistsLifecycleLogs() = runTest {
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

        assertEquals(1, gateway.starts.size)
        assertEquals(1, client.connectedProfiles.size)
        assertTrue(gateway.logs.any { it.payloadText == "Connected" })
    }

    @Test
    fun retryPolicyRetriesFailedConnectionBeforeSuccess() = runTest {
        val client = FakeProtocolClient(
            mutableListOf(ConnectionState.Failed("first failed"), ConnectionState.Connected)
        )
        val gateway = RecordingConsoleHistoryGateway()
        val controller = ConsoleController(
            profile = TestDomainFactory.profile(),
            client = client,
            sessionManager = ConsoleSessionManager(gateway),
            retryPolicy = ConnectionRetryPolicy(maxAttempts = 2, delayMillis = 0),
            dispatchers = TestAppDispatchers(StandardTestDispatcher(testScheduler)),
            scope = backgroundScope,
            ownsScope = false
        )

        controller.connect()
        advanceUntilIdle()

        assertEquals(2, client.connectedProfiles.size)
        assertEquals(ConnectionState.Connected, controller.connectionState.value)
    }

    @Test
    fun sendValidPayloadDelegatesToProtocolClientAndPersistsOutgoingLog() = runTest {
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
        controller.send("ping {counter}", PayloadEncoding.TEXT)
        advanceUntilIdle()

        assertEquals("ping 1", client.sentPayloads.single().text)
        assertTrue(gateway.logs.any { it.payloadText == "ping 1" })
    }

    @Test
    fun failedConnectionMarksSessionAsFailed() = runTest {
        val client = FakeProtocolClient(mutableListOf(ConnectionState.Failed("offline")))
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

        assertEquals(SessionStatus.FAILED, gateway.finishes.single().status)
        assertTrue(gateway.logs.any { it.payloadText.contains("Connection failed after") })
    }

    @Test
    fun invalidRepeatPayloadDoesNotStartRepeating() = runTest {
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
        controller.startRepeat("ABC", PayloadEncoding.HEX, delayText = "100")
        advanceUntilIdle()

        assertEquals(false, controller.isRepeating.value)
        assertEquals(0, client.sentPayloads.size)
        assertTrue(gateway.logs.any { it.payloadText.contains("Invalid HEX payload") })
    }

    @Test
    fun disconnectStopsSessionWithFinishedStatus() = runTest {
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

        assertEquals(SessionStatus.FINISHED, gateway.finishes.single().status)
        assertEquals(1, client.disconnectCalls)
    }

}
