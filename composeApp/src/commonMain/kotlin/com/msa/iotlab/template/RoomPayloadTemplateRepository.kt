package com.msa.iotlab.template

import com.msa.iotlab.core.AppClock
import com.msa.iotlab.core.IdGenerator
import com.msa.iotlab.core.IdProvider
import com.msa.iotlab.core.TimeProvider
import com.msa.iotlab.database.PayloadTemplateDao
import com.msa.iotlab.protocol.PayloadEncoding
import com.msa.iotlab.protocol.PayloadTemplate
import com.msa.iotlab.protocol.ProtocolType
import com.msa.iotlab.validation.PayloadTemplateValidator
import com.msa.iotlab.validation.requireValid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed payload template repository used by the production application.
 * It validates domain objects before persistence and maps Room entities at the boundary.
 */
class RoomPayloadTemplateRepository(
    private val dao: PayloadTemplateDao,
    private val timeProvider: TimeProvider = AppClock,
    private val idProvider: IdProvider = IdGenerator
) : PayloadTemplateRepository {
    override fun observeAll(): Flow<List<PayloadTemplate>> = dao.observeAll().map { it.map { entity -> entity.toDomain() } }

    override fun observeForProtocol(protocol: ProtocolType): Flow<List<PayloadTemplate>> =
        dao.observeForProtocol(protocol.name).map { it.map { entity -> entity.toDomain() } }

    override suspend fun save(template: PayloadTemplate) {
        validate(template)
        dao.upsert(template.toEntity())
    }

    override suspend fun saveAll(templates: List<PayloadTemplate>) {
        templates.forEach(::validate)
        dao.upsertAll(templates.map { it.toEntity() })
    }

    override suspend fun delete(id: String) = dao.deleteById(id)

    override suspend fun seedDefaults() {
        val now = timeProvider.nowMillis()
        saveAll(
            listOf(
                PayloadTemplate(idProvider.newId(), "Heartbeat JSON", null, PayloadEncoding.JSON, """{"type":"heartbeat","ts":{timestamp}}""", now, now),
                PayloadTemplate(idProvider.newId(), "Device Ping", null, PayloadEncoding.JSON, """{"cmd":"ping","id":"{uuid}"}""", now, now),
                PayloadTemplate(idProvider.newId(), "UDP Discovery", ProtocolType.UDP, PayloadEncoding.TEXT, "DISCOVER:{counter}", now, now),
                PayloadTemplate(idProvider.newId(), "MQTT Command", ProtocolType.MQTT, PayloadEncoding.JSON, """{"cmd":"status","counter":{counter}}""", now, now)
            )
        )
    }

    private fun validate(template: PayloadTemplate) {
        PayloadTemplateValidator.validate(template).requireValid()
    }
}
