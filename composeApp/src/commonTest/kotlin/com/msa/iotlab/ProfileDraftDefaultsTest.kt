package com.msa.iotlab

import com.msa.iotlab.profile.ProfileDraftDefaults
import com.msa.iotlab.protocol.ProtocolType
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for editor draft defaults that keep UI initialization deterministic.
 */
class ProfileDraftDefaultsTest {
    @Test
    fun newDraftUsesRequestedProtocolDefaults() {
        val draft = ProfileDraftDefaults.from(initialProfile = null, initialProtocol = ProtocolType.UDP)

        assertEquals(ProtocolType.UDP, draft.protocol)
        assertEquals(ProtocolType.UDP.defaultPort.toString(), draft.port)
        assertEquals("New IoT Profile", draft.name)
    }

    @Test
    fun existingProfileOverridesRequestedProtocol() {
        val existing = TestDomainFactory.profile(protocol = ProtocolType.TCP).copy(name = "Device TCP")

        val draft = ProfileDraftDefaults.from(initialProfile = existing, initialProtocol = ProtocolType.MQTT)

        assertEquals(ProtocolType.TCP, draft.protocol)
        assertEquals("Device TCP", draft.name)
        assertEquals(existing.port.toString(), draft.port)
    }
}
