package com.msa.iotlab

import com.msa.iotlab.protocol.MqttTopicValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Unit tests for MQTT topic validation rules independent from a concrete broker library.
 */
class MqttTopicValidatorTest {
    @Test
    fun publishTopicRejectsWildcards() {
        assertNotNull(MqttTopicValidator.validatePublishTopic("devices/+/cmd"))
        assertNotNull(MqttTopicValidator.validatePublishTopic("devices/#"))
    }

    @Test
    fun subscribeTopicAllowsTerminalHashWildcard() {
        assertNull(MqttTopicValidator.validateSubscribeTopic("devices/#"))
        assertNull(MqttTopicValidator.validateSubscribeTopic("#"))
    }

    @Test
    fun subscribeTopicRejectsMiddleHashWildcard() {
        assertNotNull(MqttTopicValidator.validateSubscribeTopic("devices/#/state"))
    }

    @Test
    fun subscribeTopicRejectsPlusWildcardInsidePartialLevel() {
        val error = MqttTopicValidator.validateSubscribeTopic("devices/+sensor/status")

        assertEquals("MQTT + wildcard must occupy an entire topic level.", error)
    }

    @Test
    fun subscribeTopicRejectsHashInsidePartialLevel() {
        val error = MqttTopicValidator.validateSubscribeTopic("devices/status#")

        assertEquals("MQTT # wildcard must occupy an entire topic level.", error)
    }

    @Test
    fun subscribeTopicAcceptsValidMultiLevelWildcardAtEnd() {
        val error = MqttTopicValidator.validateSubscribeTopic("devices/+/telemetry/#")

        assertEquals(null, error)
    }

}
