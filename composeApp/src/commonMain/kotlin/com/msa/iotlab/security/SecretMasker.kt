package com.msa.iotlab.security

import com.msa.iotlab.protocol.ConnectionProfile

/**
 * Centralizes secret redaction for exports, logs and future diagnostic reports.
 * Keeping redaction in one place prevents accidental leakage of credentials from feature code.
 */
object SecretMasker {
    const val MASK: String = "********"

    fun mask(value: String?): String? = value?.takeIf { it.isNotBlank() }?.let { MASK }

    fun sanitize(profile: ConnectionProfile): ConnectionProfile = profile.copy(
        mqtt = profile.mqtt.copy(password = mask(profile.mqtt.password))
    )
}
