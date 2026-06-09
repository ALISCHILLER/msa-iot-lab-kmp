package com.msa.iotlab

import com.msa.iotlab.export.MsaExportBundle
import com.msa.iotlab.export.WorkspaceImportValidator
import com.msa.iotlab.protocol.PayloadEncoding
import com.msa.iotlab.protocol.PayloadTemplate
import com.msa.iotlab.protocol.ProtocolType
import com.msa.iotlab.validation.ValidationResult
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Unit tests for workspace import validation before profile and template data is persisted.
 */
class WorkspaceImportValidatorTest {
    @Test
    fun validBundlePassesValidation() {
        val bundle = MsaExportBundle(
            exportedAt = 1L,
            profiles = listOf(TestDomainFactory.profile()),
            templates = listOf(validTemplate())
        )

        val result = WorkspaceImportValidator.validate(bundle)

        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun duplicateProfileIdsAreRejected() {
        val profile = TestDomainFactory.profile()
        val bundle = MsaExportBundle(exportedAt = 1L, profiles = listOf(profile, profile.copy(name = "Duplicate")))

        val result = WorkspaceImportValidator.validate(bundle)

        val invalid = assertIs<ValidationResult.Invalid>(result)
        assertTrue(invalid.message().contains("Duplicate profile id"))
    }

    @Test
    fun invalidTemplatePayloadIsRejected() {
        val bundle = MsaExportBundle(
            exportedAt = 1L,
            profiles = listOf(TestDomainFactory.profile()),
            templates = listOf(validTemplate().copy(encoding = PayloadEncoding.HEX, payload = "ABC"))
        )

        val result = WorkspaceImportValidator.validate(bundle)

        val invalid = assertIs<ValidationResult.Invalid>(result)
        assertTrue(invalid.message().contains("Template #1"))
    }

    @Test
    fun unsupportedSchemaVersionIsRejected() {
        val bundle = MsaExportBundle(exportedAt = 1L, schemaVersion = 99)

        val result = WorkspaceImportValidator.validate(bundle)

        val invalid = assertIs<ValidationResult.Invalid>(result)
        assertTrue(invalid.message().contains("Unsupported export schema version"))
    }

    private fun validTemplate(): PayloadTemplate = PayloadTemplate(
        id = "template-1",
        name = "Status Request",
        protocol = ProtocolType.TCP,
        encoding = PayloadEncoding.TEXT,
        payload = "status",
        createdAt = 1L,
        updatedAt = 1L
    )
}
