package com.msa.iotlab

import com.msa.iotlab.profile.ProfileDraftDefaults
import com.msa.iotlab.profile.ProfileFactory
import com.msa.iotlab.protocol.ProtocolType
import com.msa.iotlab.validation.ProfileValidator
import com.msa.iotlab.validation.ValidationResult
import kotlin.test.Test
import kotlin.test.assertIs

/**
 * Unit tests for profile validation rules that protect protocol clients from invalid settings.
 */
class ProfileValidatorTest {
    @Test
    fun mqttPublishTopicRejectsWildcards() {
        val draft = ProfileDraftDefaults.from(initialProfile = null, initialProtocol = ProtocolType.MQTT)
            .copy(
                name = "MQTT",
                host = "broker.local",
                port = "1883",
                mqttSubscribeTopic = "devices/#",
                mqttPublishTopic = "devices/+/cmd",
                mqttQos = "1"
            )
        val profile = ProfileFactory.create(draft)

        assertIs<ValidationResult.Invalid>(ProfileValidator.validate(profile))
    }
}
