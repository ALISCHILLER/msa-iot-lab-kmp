package com.msa.iotlab.template

import com.msa.iotlab.protocol.PayloadEncoding
import com.msa.iotlab.protocol.ProtocolType

/**
 * Editable template form state before it is validated and converted to a persisted PayloadTemplate.
 */
data class PayloadTemplateDraft(
    val name: String,
    val protocol: ProtocolType? = null,
    val encoding: PayloadEncoding,
    val payload: String
)
