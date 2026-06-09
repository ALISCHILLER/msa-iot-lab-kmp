package com.msa.iotlab

import com.msa.iotlab.protocol.ProtocolFindingSeverity
import com.msa.iotlab.protocol.ProtocolProfileInspector
import com.msa.iotlab.protocol.ProtocolType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Verifies profile diagnostics that power the professional console inspector UI.
 */
class ProtocolProfileInspectorTest {
    @Test
    fun mqttProfileWithoutTopicsProducesActionableWarning() {
        val findings = ProtocolProfileInspector.inspect(TestDomainFactory.profile(ProtocolType.MQTT))
        assertTrue(findings.any { it.title == "No MQTT topic" && it.severity == ProtocolFindingSeverity.WARNING })
    }

    @Test
    fun invalidPortProducesError() {
        val profile = TestDomainFactory.profile(ProtocolType.TCP).copy(port = 70_000)
        val findings = ProtocolProfileInspector.inspect(profile)
        assertTrue(findings.any { it.title == "Invalid port" && it.severity == ProtocolFindingSeverity.ERROR })
    }

    @Test
    fun cleanTcpProfileHasNoErrors() {
        val findings = ProtocolProfileInspector.inspect(TestDomainFactory.profile(ProtocolType.TCP))
        assertEquals(0, findings.count { it.severity == ProtocolFindingSeverity.ERROR })
    }
}
