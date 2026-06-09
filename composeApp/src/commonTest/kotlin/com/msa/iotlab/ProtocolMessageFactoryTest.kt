package com.msa.iotlab

import com.msa.iotlab.protocol.MessageDirection
import com.msa.iotlab.protocol.ProtocolMessageFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for normalized protocol message creation used by every transport client.
 */
class ProtocolMessageFactoryTest {
    @Test
    fun fromBytesCreatesPreviewHexAndMetadata() {
        val profile = TestDomainFactory.profile()

        val message = ProtocolMessageFactory.fromBytes(
            profile = profile,
            direction = MessageDirection.IN,
            bytes = "OK".encodeToByteArray(),
            metadata = mapOf("source" to "tcp")
        )

        assertEquals(profile.id, message.profileId)
        assertEquals(profile.protocol, message.protocol)
        assertEquals(MessageDirection.IN, message.direction)
        assertEquals("OK", message.payloadText)
        assertEquals("4F 4B", message.payloadHex)
        assertEquals("tcp", message.metadata["source"])
        assertTrue(message.id.isNotBlank())
    }

    @Test
    fun systemMessageUsesSystemDirection() {
        val profile = TestDomainFactory.profile()

        val message = ProtocolMessageFactory.system(profile, "Connected")

        assertEquals(MessageDirection.SYSTEM, message.direction)
        assertEquals("Connected", message.payloadText)
    }

    @Test
    fun fromBytesUsesInjectedRuntimeProviders() {
        val profile = TestDomainFactory.profile()

        val message = ProtocolMessageFactory.fromBytes(
            profile = profile,
            direction = MessageDirection.OUT,
            bytes = "PING".encodeToByteArray(),
            timeProvider = FixedTimeProvider(999L),
            idProvider = SequentialIdProvider(mutableListOf("message-id"))
        )

        assertEquals("message-id", message.id)
        assertEquals(999L, message.timestampMillis)
        assertEquals("50 49 4E 47", message.payloadHex)
    }

}
