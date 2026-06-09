package com.msa.iotlab.i18n

import androidx.compose.runtime.compositionLocalOf

/**
 * Supported runtime languages for the shared Compose UI.
 */
enum class AppLanguage(val code: String, val englishName: String, val nativeName: String) {
    English(code = "en", englishName = "English", nativeName = "English"),
    Persian(code = "fa", englishName = "Persian", nativeName = "فارسی");

    /** Returns the opposite language for the compact language toggle. */
    fun toggled(): AppLanguage = if (this == English) Persian else English

    companion object {
        /** Maps persisted language codes to a supported language while falling back safely to English. */
        fun fromCode(code: String?): AppLanguage = entries.firstOrNull { it.code == code } ?: English
    }
}

/**
 * Composition local that exposes the currently selected app language to all commonMain UI code.
 */
val LocalAppLanguage = compositionLocalOf { AppLanguage.English }
