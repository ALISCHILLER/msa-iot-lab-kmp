package com.msa.iotlab.template

import com.msa.iotlab.database.PayloadTemplateEntity
import com.msa.iotlab.protocol.PayloadEncoding
import com.msa.iotlab.protocol.PayloadTemplate
import com.msa.iotlab.protocol.ProtocolType

/**
 * Converts a domain payload template into a Room entity.
 */
fun PayloadTemplate.toEntity(): PayloadTemplateEntity = PayloadTemplateEntity(
    id = id,
    name = name,
    protocol = protocol?.name,
    encoding = encoding.name,
    payload = payload,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * Converts a Room payload template entity back into a domain model.
 */
fun PayloadTemplateEntity.toDomain(): PayloadTemplate = PayloadTemplate(
    id = id,
    name = name,
    protocol = protocol?.let { ProtocolType.valueOf(it) },
    encoding = PayloadEncoding.valueOf(encoding),
    payload = payload,
    createdAt = createdAt,
    updatedAt = updatedAt
)
