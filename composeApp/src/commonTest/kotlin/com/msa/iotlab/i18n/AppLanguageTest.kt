package com.msa.iotlab.i18n

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Verifies persisted language-code mapping used by Room-backed settings.
 */
class AppLanguageTest {
    /** Ensures supported language codes map to stable enum values. */
    @Test
    fun fromCodeReadsPersistedLanguageCodes() {
        assertEquals(AppLanguage.English, AppLanguage.fromCode("en"))
        assertEquals(AppLanguage.Persian, AppLanguage.fromCode("fa"))
    }

    /** Ensures unknown or absent language values do not crash startup. */
    @Test
    fun fromCodeFallsBackToEnglishForInvalidValues() {
        assertEquals(AppLanguage.English, AppLanguage.fromCode(null))
        assertEquals(AppLanguage.English, AppLanguage.fromCode("unknown"))
    }
}
