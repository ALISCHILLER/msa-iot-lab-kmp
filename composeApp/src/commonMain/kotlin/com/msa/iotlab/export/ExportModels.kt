package com.msa.iotlab.export

import com.msa.iotlab.protocol.ConnectionProfile
import com.msa.iotlab.protocol.PayloadTemplate
import com.msa.iotlab.protocol.ProtocolMessage
import kotlinx.serialization.Serializable

/**
 * Versioned portable export envelope for profiles, templates and optional protocol messages.
 */
@Serializable
data class MsaExportBundle(
    val app: String = "MSA IoT Lab",
    val schemaVersion: Int = 1,
    val exportedAt: Long,
    val profiles: List<ConnectionProfile> = emptyList(),
    val templates: List<PayloadTemplate> = emptyList(),
    val messages: List<ProtocolMessage> = emptyList()
)
