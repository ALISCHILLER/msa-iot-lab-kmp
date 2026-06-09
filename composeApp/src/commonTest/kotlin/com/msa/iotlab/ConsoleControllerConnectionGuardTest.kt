package com.msa.iotlab

import com.msa.iotlab.console.ConnectionRetryPolicy
import com.msa.iotlab.console.ConsoleController
import com.msa.iotlab.console.ConsoleSessionManager
import com.msa.iotlab.protocol.PayloadEncoding
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for console send guards that prevent transport writes before a connection is ready.
 */
class ConsoleControllerConnectionGuardTest {
    @Test
    fun sendBeforeConnectDoesNotReachTransportAndPersistsError() = runTest {
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

        controller.send("ping", PayloadEncoding.TEXT)
        advanceUntilIdle()

        assertEquals(0, client.sentPayloads.size)
        assertTrue(gateway.logs.any { it.payloadText.contains("Cannot send payload while connection state is Idle") })
    }

    @Test
    fun repeatBeforeConnectDoesNotStartLoop() = runTest {
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

        controller.startRepeat("ping", PayloadEncoding.TEXT, delayText = "100")
        advanceUntilIdle()

        assertEquals(false, controller.isRepeating.value)
        assertEquals(0, client.sentPayloads.size)
        assertTrue(gateway.logs.any { it.payloadText.contains("Connect first") })
    }
}
