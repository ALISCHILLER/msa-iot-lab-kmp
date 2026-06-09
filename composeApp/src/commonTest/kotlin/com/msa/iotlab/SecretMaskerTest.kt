package com.msa.iotlab

import com.msa.iotlab.protocol.ConnectionProfile
import com.msa.iotlab.protocol.MqttOptions
import com.msa.iotlab.protocol.PayloadEncoding
import com.msa.iotlab.protocol.ProtocolType
import com.msa.iotlab.security.SecretMasker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Verifies that sensitive profile fields are redacted before safe export or diagnostics.
 */
class SecretMaskerTest {
    @Test
    fun sanitizeMasksMqttPassword() {
        val profile = ConnectionProfile(
            id = "1",
            name = "MQTT",
            protocol = ProtocolType.MQTT,
            host = "broker.local",
            port = 1883,
            payloadEncoding = PayloadEncoding.TEXT,
            createdAt = 1,
            updatedAt = 1,
            mqtt = MqttOptions(password = "secret")
        )

        val sanitized = SecretMasker.sanitize(profile)

        assertEquals(SecretMasker.MASK, sanitized.mqtt.password)
    }

    @Test
    fun maskKeepsBlankValuesEmpty() {
        assertNull(SecretMasker.mask(null))
        assertNull(SecretMasker.mask(""))
    }
}
