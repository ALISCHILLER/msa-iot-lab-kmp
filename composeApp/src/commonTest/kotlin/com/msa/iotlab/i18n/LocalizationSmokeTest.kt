package com.msa.iotlab.i18n

import com.msa.iotlab.protocol.ProtocolType
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Verifies language-independent localization helpers used by shell titles and metrics.
 */
class LocalizationSmokeTest {
    /** Ensures explicit English/Persian text selection is deterministic outside Compose runtime. */
    @Test
    fun explicitTextSelectionRespectsLanguage() {
        assertEquals("Profiles", t(AppLanguage.English, "Profiles", "پروفایل‌ها"))
        assertEquals("پروفایل‌ها", t(AppLanguage.Persian, "Profiles", "پروفایل‌ها"))
    }

    /** Ensures protocol titles can be rendered outside composable UI for shell metadata. */
    @Test
    fun protocolTitleSupportsExplicitLanguage() {
        assertEquals("WebSocket", ProtocolType.WEBSOCKET.localizedTitle(AppLanguage.English))
        assertEquals("وب‌سوکت", ProtocolType.WEBSOCKET.localizedTitle(AppLanguage.Persian))
    }
}
