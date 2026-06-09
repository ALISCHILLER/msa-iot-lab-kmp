package com.msa.iotlab

import com.msa.iotlab.protocol.ProtocolCapability
import com.msa.iotlab.protocol.ProtocolCapabilityRegistry
import com.msa.iotlab.protocol.ProtocolType
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for protocol capability metadata used by dashboard and platform limitation messaging.
 */
class ProtocolCapabilityRegistryTest {
    @Test
    fun mqttExposesSubscribeAndAuthenticationCapabilities() {
        val capabilities = ProtocolCapabilityRegistry.capabilitiesFor(ProtocolType.MQTT)

        assertTrue(ProtocolCapability.SUBSCRIBE in capabilities)
        assertTrue(ProtocolCapability.AUTHENTICATION in capabilities)
    }

    @Test
    fun udpExposesBroadcastButNotAuthentication() {
        val capabilities = ProtocolCapabilityRegistry.capabilitiesFor(ProtocolType.UDP)

        assertTrue(ProtocolCapability.BROADCAST in capabilities)
        assertFalse(ProtocolCapability.AUTHENTICATION in capabilities)
    }
}
