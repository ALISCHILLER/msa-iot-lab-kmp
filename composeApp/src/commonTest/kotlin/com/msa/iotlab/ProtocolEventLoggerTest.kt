package com.msa.iotlab

import com.msa.iotlab.protocol.MessageDirection
import com.msa.iotlab.protocol.ProtocolEvent
import com.msa.iotlab.protocol.ProtocolEventLogger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit tests for mapping protocol events into persisted history messages.
 */
class ProtocolEventLoggerTest {
    @Test
    fun connectedEventMapsToSystemMessage() {
        val message = ProtocolEventLogger.toLogMessage(
            event = ProtocolEvent.Connected(timestampMillis = 1),
            profile = TestDomainFactory.profile(),
            sessionId = "session-1"
        )

        assertNotNull(message)
        assertEquals(MessageDirection.SYSTEM, message.direction)
        assertEquals("Connected", message.payloadText)
        assertEquals("session-1", message.sessionId)
    }

    @Test
    fun errorEventMapsToErrorMessage() {
        val message = ProtocolEventLogger.toLogMessage(
            event = ProtocolEvent.Error("boom", timestampMillis = 1),
            profile = TestDomainFactory.profile(),
            sessionId = "session-1"
        )

        assertNotNull(message)
        assertEquals(MessageDirection.ERROR, message.direction)
        assertEquals("boom", message.payloadText)
    }
    @Test
    fun systemEventUsesInjectedRuntimeProviders() {
        val message = ProtocolEventLogger.toLogMessage(
            event = ProtocolEvent.System("diagnostic", timestampMillis = 111),
            profile = TestDomainFactory.profile(),
            sessionId = "session-1",
            timeProvider = FixedTimeProvider(4242L),
            idProvider = SequentialIdProvider(mutableListOf("log-id"))
        )

        assertNotNull(message)
        assertEquals("log-id", message.id)
        assertEquals(4242L, message.timestampMillis)
        assertEquals("diagnostic", message.payloadText)
    }

}
