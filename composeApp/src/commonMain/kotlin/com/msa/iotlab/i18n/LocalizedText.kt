package com.msa.iotlab.i18n

import androidx.compose.runtime.Composable
import com.msa.iotlab.protocol.ConnectionState
import com.msa.iotlab.protocol.MessageDirection
import com.msa.iotlab.protocol.PayloadEncoding
import com.msa.iotlab.protocol.ProtocolType

/**
 * Lightweight localization helper for bilingual Compose Multiplatform screens.
 */
object LocalizedText {
    /** Picks English or Persian text using the current composition language. */
    @Composable
    fun text(en: String, fa: String): String = text(LocalAppLanguage.current, en, fa)

    /** Picks English or Persian text using an explicit language value. */
    fun text(language: AppLanguage, en: String, fa: String): String = when (language) {
        AppLanguage.English -> en
        AppLanguage.Persian -> fa
    }
}

/** Shorthand for current-language text selection inside composable UI code. */
@Composable
fun t(en: String, fa: String): String = LocalizedText.text(en, fa)

/** Shorthand for explicit-language text selection outside composable lambdas. */
fun t(language: AppLanguage, en: String, fa: String): String = LocalizedText.text(language, en, fa)

/** Localized protocol display names for UI labels. */
@Composable
fun ProtocolType.localizedTitle(): String = when (this) {
    ProtocolType.MQTT -> t("MQTT", "MQTT")
    ProtocolType.WEBSOCKET -> t("WebSocket", "وب‌سوکت")
    ProtocolType.TCP -> t("TCP", "TCP")
    ProtocolType.UDP -> t("UDP", "UDP")
}

/** Localized protocol display names outside composable code. */
fun ProtocolType.localizedTitle(language: AppLanguage): String = when (this) {
    ProtocolType.MQTT -> t(language, "MQTT", "MQTT")
    ProtocolType.WEBSOCKET -> t(language, "WebSocket", "وب‌سوکت")
    ProtocolType.TCP -> t(language, "TCP", "TCP")
    ProtocolType.UDP -> t(language, "UDP", "UDP")
}

/** Localized protocol summaries for dashboard and guide content. */
@Composable
fun ProtocolType.localizedSummary(): String = when (this) {
    ProtocolType.MQTT -> t("Publish, subscribe and monitor broker topics for IoT devices.", "انتشار، اشتراک و مانیتور topicهای بروکر برای دستگاه‌های IoT.")
    ProtocolType.WEBSOCKET -> t("Validate realtime backend gateways using ws/wss endpoints.", "تست gatewayهای بلادرنگ بک‌اند با endpointهای ws/wss.")
    ProtocolType.TCP -> t("Send raw text, hex or JSON to TCP-based devices and services.", "ارسال متن خام، HEX یا JSON به دستگاه‌ها و سرویس‌های مبتنی بر TCP.")
    ProtocolType.UDP -> t("Broadcast discovery packets or listen for datagrams on local networks.", "ارسال packetهای discovery یا شنیدن datagramها روی شبکه محلی.")
}

/** Localized message direction names. */
@Composable
fun MessageDirection.localizedTitle(): String = when (this) {
    MessageDirection.IN -> t("Incoming", "دریافتی")
    MessageDirection.OUT -> t("Outgoing", "ارسالی")
    MessageDirection.SYSTEM -> t("System", "سیستمی")
    MessageDirection.ERROR -> t("Error", "خطا")
}

/** Localized connection state names. */
@Composable
fun ConnectionState.localizedLabel(): String = when (this) {
    ConnectionState.Idle -> t("Idle", "آماده")
    ConnectionState.Connecting -> t("Connecting", "در حال اتصال")
    ConnectionState.Connected -> t("Connected", "متصل")
    is ConnectionState.Disconnecting -> t("Disconnecting", "در حال قطع اتصال")
    is ConnectionState.Disconnected -> t("Disconnected", "قطع شده")
    is ConnectionState.Failed -> t("Failed", "ناموفق")
}

/** Localized payload encoding names for forms and cards. */
@Composable
fun PayloadEncoding.localizedTitle(): String = when (this) {
    PayloadEncoding.TEXT -> t("Text", "متن")
    PayloadEncoding.JSON -> t("JSON", "JSON")
    PayloadEncoding.HEX -> t("HEX", "HEX")
    PayloadEncoding.BASE64 -> t("Base64", "Base64")
}
