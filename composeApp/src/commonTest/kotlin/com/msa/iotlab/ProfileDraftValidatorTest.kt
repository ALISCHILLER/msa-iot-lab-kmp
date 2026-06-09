package com.msa.iotlab

import com.msa.iotlab.profile.ProfileDraftDefaults
import com.msa.iotlab.protocol.ProtocolType
import com.msa.iotlab.validation.ProfileDraftValidator
import com.msa.iotlab.validation.ValidationResult
import kotlin.test.Test
import kotlin.test.assertIs

/**
 * Unit tests for raw editor draft validation before ProfileFactory applies safe defaults.
 */
class ProfileDraftValidatorTest {
    @Test
    fun blankHostAndInvalidPortAreRejectedBeforeDefaults() {
        val draft = ProfileDraftDefaults.from(initialProfile = null, initialProtocol = ProtocolType.TCP)
            .copy(host = "", port = "not-a-port")

        assertIs<ValidationResult.Invalid>(ProfileDraftValidator.validate(draft))
    }

    @Test
    fun invalidWebSocketHeadersAreRejected() {
        val draft = ProfileDraftDefaults.from(initialProfile = null, initialProtocol = ProtocolType.WEBSOCKET)
            .copy(wsHeadersJson = "[1,2,3]")

        assertIs<ValidationResult.Invalid>(ProfileDraftValidator.validate(draft))
    }
}
