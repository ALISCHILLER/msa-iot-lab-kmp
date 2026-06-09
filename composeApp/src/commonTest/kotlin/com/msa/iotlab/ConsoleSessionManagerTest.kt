package com.msa.iotlab

import com.msa.iotlab.console.ConsoleSessionManager
import com.msa.iotlab.protocol.ProtocolEvent
import com.msa.iotlab.protocol.SessionStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for the console session persistence coordinator.
 */
class ConsoleSessionManagerTest {
    @Test
    fun startIfNeededCreatesOnlyOneSession() = runTest {
        val gateway = RecordingConsoleHistoryGateway()
        val manager = ConsoleSessionManager(gateway)
        val profile = TestDomainFactory.profile()

        val first = manager.startIfNeeded(null, profile)
        val second = manager.startIfNeeded(first, profile)

        assertEquals(first, second)
        assertEquals(1, gateway.starts.size)
    }

    @Test
    fun lifecycleEventsArePersistedAsSystemLogs() = runTest {
        val gateway = RecordingConsoleHistoryGateway()
        val manager = ConsoleSessionManager(gateway)
        val profile = TestDomainFactory.profile()

        manager.persistEvent(ProtocolEvent.Connected(timestampMillis = 1), profile, "session-1")
        manager.persistEvent(ProtocolEvent.Disconnected("closed", timestampMillis = 2), profile, "session-1")

        assertEquals(listOf("Connected", "Disconnected: closed"), gateway.logs.map { it.payloadText })
    }

    @Test
    fun finishIgnoresMissingSessionId() = runTest {
        val gateway = RecordingConsoleHistoryGateway()
        val manager = ConsoleSessionManager(gateway)

        manager.finish(null, SessionStatus.CANCELLED)

        assertEquals(0, gateway.finishes.size)
    }
    @Test
    fun persistedSystemLogsUseInjectedRuntimeProviders() = runTest {
        val gateway = RecordingConsoleHistoryGateway()
        val manager = ConsoleSessionManager(
            historyGateway = gateway,
            timeProvider = FixedTimeProvider(777L),
            idProvider = SequentialIdProvider(mutableListOf("log-1"))
        )
        val profile = TestDomainFactory.profile()

        manager.persistEvent(ProtocolEvent.System("ready", timestampMillis = 1), profile, "session-1")

        assertEquals("log-1", gateway.logs.single().id)
        assertEquals(777L, gateway.logs.single().timestampMillis)
    }

}
