package com.msa.iotlab

import com.msa.iotlab.core.AppResult
import com.msa.iotlab.protocol.PayloadEncoding
import com.msa.iotlab.protocol.ProtocolType
import com.msa.iotlab.template.PayloadTemplateDraft
import com.msa.iotlab.template.SavePayloadTemplateUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Unit tests for payload template creation, validation and repository persistence.
 */
class SavePayloadTemplateUseCaseTest {
    @Test
    fun validTemplateIsPersistedWithTrimmedName() = runTest {
        val repository = InMemoryPayloadTemplateRepository()
        val useCase = SavePayloadTemplateUseCase(repository)

        val result = useCase.save(
            PayloadTemplateDraft(
                name = "  Status Request  ",
                protocol = ProtocolType.MQTT,
                encoding = PayloadEncoding.JSON,
                payload = """{"cmd":"status"}"""
            )
        )

        assertIs<AppResult.Success<*>>(result)
        val saved = repository.observeAll().first().single()
        assertEquals("Status Request", saved.name)
        assertEquals(ProtocolType.MQTT, saved.protocol)
    }

    @Test
    fun invalidTemplateReturnsErrorAndDoesNotPersist() = runTest {
        val repository = InMemoryPayloadTemplateRepository()
        val useCase = SavePayloadTemplateUseCase(repository)

        val result = useCase.save(
            PayloadTemplateDraft(
                name = "",
                protocol = null,
                encoding = PayloadEncoding.HEX,
                payload = "ABC"
            )
        )

        assertIs<AppResult.Error>(result)
        assertTrue(repository.observeAll().first().isEmpty())
    }

    @Test
    fun validTemplateUsesInjectedTimeAndIdProviders() = runTest {
        val repository = InMemoryPayloadTemplateRepository()
        val useCase = SavePayloadTemplateUseCase(
            repository = repository,
            timeProvider = FixedTimeProvider(321L),
            idProvider = SequentialIdProvider(mutableListOf("template-id"))
        )

        val result = useCase.save(
            PayloadTemplateDraft(
                name = "Ping",
                protocol = ProtocolType.UDP,
                encoding = PayloadEncoding.TEXT,
                payload = "PING"
            )
        )

        assertIs<AppResult.Success<*>>(result)
        val saved = repository.observeAll().first().single()
        assertEquals("template-id", saved.id)
        assertEquals(321L, saved.createdAt)
        assertEquals(321L, saved.updatedAt)
    }

}
