package com.msa.iotlab

import com.msa.iotlab.core.AppResult
import com.msa.iotlab.export.ExportRepository
import com.msa.iotlab.export.ExportSecurityPolicy
import com.msa.iotlab.export.ExportWorkspaceUseCase
import com.msa.iotlab.export.ImportWorkspaceUseCase
import com.msa.iotlab.protocol.ConnectionProfile
import com.msa.iotlab.protocol.MqttOptions
import com.msa.iotlab.protocol.PayloadEncoding
import com.msa.iotlab.protocol.PayloadTemplate
import com.msa.iotlab.protocol.ProtocolType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * End-to-end use case tests for workspace import/export without touching Room or files.
 */
class ExportImportUseCaseTest {
    @Test
    fun exportMasksMqttPasswordsByDefault() = runTest {
        val profiles = InMemoryProfileRepository()
        val templates = InMemoryPayloadTemplateRepository()
        profiles.save(secretMqttProfile())
        val useCase = ExportWorkspaceUseCase(ExportRepository(profiles, templates))

        val result = useCase.exportJson()

        val json = assertIs<AppResult.Success<*>>(result).data as String
        assertFalse(json.contains("top-secret"))
        assertTrue(json.contains("***"))
    }

    @Test
    fun exportCanIncludeSecretsWhenExplicitlyRequested() = runTest {
        val profiles = InMemoryProfileRepository()
        val templates = InMemoryPayloadTemplateRepository()
        profiles.save(secretMqttProfile())
        val useCase = ExportWorkspaceUseCase(ExportRepository(profiles, templates))

        val result = useCase.exportJson(ExportSecurityPolicy(includeSecrets = true))

        val json = assertIs<AppResult.Success<*>>(result).data as String
        assertTrue(json.contains("top-secret"))
    }

    @Test
    fun importRestoresProfilesAndTemplates() = runTest {
        val sourceProfiles = InMemoryProfileRepository()
        val sourceTemplates = InMemoryPayloadTemplateRepository()
        sourceProfiles.save(secretMqttProfile())
        sourceTemplates.save(sampleTemplate())
        val exported = assertIs<AppResult.Success<*>>(
            ExportWorkspaceUseCase(ExportRepository(sourceProfiles, sourceTemplates)).exportJson(ExportSecurityPolicy(includeSecrets = true))
        ).data as String

        val targetProfiles = InMemoryProfileRepository()
        val targetTemplates = InMemoryPayloadTemplateRepository()
        val importResult = ImportWorkspaceUseCase(ExportRepository(targetProfiles, targetTemplates)).importJson(exported)

        assertIs<AppResult.Success<*>>(importResult)
        assertEquals("MQTT Secure", targetProfiles.observeProfiles().first().single().name)
        assertEquals("Heartbeat", targetTemplates.observeAll().first().single().name)
    }

    @Test
    fun blankImportJsonReturnsUserReadableError() = runTest {
        val result = ImportWorkspaceUseCase(
            ExportRepository(InMemoryProfileRepository(), InMemoryPayloadTemplateRepository())
        ).importJson("   ")

        assertIs<AppResult.Error>(result)
    }
    @Test
    fun importRejectsInvalidProfileBeforePersistence() = runTest {
        val sourceProfiles = InMemoryProfileRepository()
        val sourceTemplates = InMemoryPayloadTemplateRepository()
        sourceProfiles.save(secretMqttProfile().copy(port = 0))
        val exported = assertIs<AppResult.Success<*>>(
            ExportWorkspaceUseCase(ExportRepository(sourceProfiles, sourceTemplates)).exportJson(ExportSecurityPolicy(includeSecrets = true))
        ).data as String

        val targetProfiles = InMemoryProfileRepository()
        val result = ImportWorkspaceUseCase(ExportRepository(targetProfiles, InMemoryPayloadTemplateRepository())).importJson(exported)

        val error = assertIs<AppResult.Error>(result)
        assertTrue(error.message.contains("Profile #1"))
        assertTrue(targetProfiles.observeProfiles().first().isEmpty())
    }


    @Test
    fun exportUsesInjectedClockForBundleTimestamp() = runTest {
        val profiles = InMemoryProfileRepository()
        val templates = InMemoryPayloadTemplateRepository()
        val useCase = ExportWorkspaceUseCase(
            ExportRepository(
                profileRepository = profiles,
                templateRepository = templates,
                timeProvider = FixedTimeProvider(777L)
            )
        )

        val json = assertIs<AppResult.Success<*>>(useCase.exportJson()).data as String

        assertTrue(json.contains("\"exportedAt\": 777"))
    }

    private fun secretMqttProfile(): ConnectionProfile = TestDomainFactory.profile(protocol = ProtocolType.MQTT).copy(
        name = "MQTT Secure",
        mqtt = MqttOptions(
            clientId = "client-1",
            username = "operator",
            password = "top-secret",
            publishTopic = "device/cmd"
        )
    )

    private fun sampleTemplate(): PayloadTemplate = PayloadTemplate(
        id = "template-1",
        name = "Heartbeat",
        protocol = ProtocolType.MQTT,
        encoding = PayloadEncoding.JSON,
        payload = """{"type":"heartbeat"}""",
        createdAt = 1L,
        updatedAt = 1L
    )
}
