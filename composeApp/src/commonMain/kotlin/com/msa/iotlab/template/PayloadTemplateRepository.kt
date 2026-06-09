package com.msa.iotlab.template

import com.msa.iotlab.protocol.PayloadTemplate
import com.msa.iotlab.protocol.ProtocolType
import kotlinx.coroutines.flow.Flow

/**
 * Domain-facing repository contract for reusable payload templates.
 * Use cases depend on this abstraction so tests can use in-memory fakes and production can use Room.
 */
interface PayloadTemplateRepository {
    fun observeAll(): Flow<List<PayloadTemplate>>
    fun observeForProtocol(protocol: ProtocolType): Flow<List<PayloadTemplate>>
    suspend fun save(template: PayloadTemplate)
    suspend fun saveAll(templates: List<PayloadTemplate>)
    suspend fun delete(id: String)
    suspend fun seedDefaults()
}
