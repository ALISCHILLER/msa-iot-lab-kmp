package com.msa.iotlab.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.msa.iotlab.i18n.localizedTitle
import com.msa.iotlab.i18n.t
import com.msa.iotlab.protocol.ProtocolType

/**
 * Documentation row rendered inside the in-app operator guide.
 */
data class GuideStep(val title: String, val body: String, val badge: String? = null)

/**
 * In-app operator manual that teaches users how to configure protocols, send payloads and read traffic.
 */
@Composable
fun GuideScreen() {
    AdaptiveTwoPane(
        primaryWeight = 1.15f,
        secondaryWeight = 0.95f,
        primary = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                QuickStartGuide()
                ProtocolPlaybookGuide()
            }
        },
        secondary = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SetupAndRequirementsGuide()
                SetupRunbookGuide()
                PlatformRequirementsGuide()
                DesktopWorkflowGuide()
                ProfessionalUxGuide()
                QualityGateGuide()
                TroubleshootingGuide()
                DataSafetyGuide()
            }
        }
    )
}

@Composable
private fun QuickStartGuide() {
    GuideSection(
        title = t("Quick start workflow", "مسیر شروع سریع"),
        subtitle = t("A clean operational path from profile creation to traffic inspection.", "یک مسیر عملیاتی تمیز از ساخت پروفایل تا بررسی ترافیک."),
        steps = listOf(
            GuideStep(t("Create a profile", "ساخت پروفایل"), t("Pick MQTT, WebSocket, TCP or UDP, then define host, port, payload encoding and protocol-specific options.", "MQTT، وب‌سوکت، TCP یا UDP را انتخاب کنید و host، port، encoding و گزینه‌های مخصوص پروتکل را تنظیم کنید."), "1"),
            GuideStep(t("Open Live Console", "باز کردن کنسول زنده"), t("Use the saved profile to enter Command Center, check diagnostics and connect to the endpoint.", "با پروفایل ذخیره‌شده وارد مرکز فرمان شوید، diagnosticها را ببینید و به endpoint وصل شوید."), "2"),
            GuideStep(t("Send or repeat payloads", "ارسال یا تکرار Payload"), t("Compose text, JSON, HEX or Base64 payloads, load templates and run controlled auto-repeat tests.", "Payloadهای Text، JSON، HEX یا Base64 بسازید، template بارگذاری کنید و تست تکرارشونده کنترل‌شده اجرا کنید."), "3"),
            GuideStep(t("Review traffic", "بررسی ترافیک"), t("Monitor incoming/outgoing bytes, system events, errors and persisted session history.", "Byteهای دریافتی/ارسالی، رویدادهای سیستمی، خطاها و تاریخچه نشست‌ها را مانیتور کنید."), "4")
        )
    )
}

@Composable
private fun ProtocolPlaybookGuide() {
    SectionCard(t("Protocol playbooks", "راهنمای پروتکل‌ها"), t("Recommended checks before testing each transport", "چک‌های پیشنهادی قبل از تست هر transport")) {
        AdaptiveCardGrid(itemCount = ProtocolType.entries.size) { index, modifier ->
            val protocol = ProtocolType.entries[index]
            OutlinedCard(modifier = modifier) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(protocol.localizedTitle(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        StatusBadge(protocol.defaultPort.toString())
                    }
                    protocol.guideSteps().forEach { item ->
                        Text("• $item", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupAndRequirementsGuide() {
    GuideSection(
        title = t("Setup and requirements", "راه‌اندازی و نیازمندی‌ها"),
        subtitle = t("Minimum checklist before running Android, Desktop or iOS builds.", "حداقل چک‌لیست قبل از اجرای buildهای Android، Desktop یا iOS."),
        steps = listOf(
            GuideStep(t("JDK and IDE", "JDK و IDE"), t("Use JDK 21 with Android Studio or IntelliJ IDEA that supports Kotlin Multiplatform.", "از JDK 21 همراه Android Studio یا IntelliJ IDEA دارای پشتیبانی Kotlin Multiplatform استفاده کنید.")),
            GuideStep(t("Desktop run", "اجرای Desktop"), t("Run ./gradlew :composeApp:run from the project root after Gradle sync is complete.", "بعد از sync کامل Gradle، از ریشه پروژه دستور ./gradlew :composeApp:run را اجرا کنید.")),
            GuideStep(t("Android run", "اجرای Android"), t("Install Android SDK, choose an emulator or real device, then run the composeApp target.", "Android SDK را نصب کنید، emulator یا دستگاه واقعی انتخاب کنید و target composeApp را اجرا کنید.")),
            GuideStep(t("iOS run", "اجرای iOS"), t("Use macOS with Xcode; iOS has shared UI, Room and WebSocket while raw MQTT/TCP/UDP are safe unsupported clients.", "برای iOS از macOS و Xcode استفاده کنید؛ UI، Room و WebSocket مشترک هستند و MQTT/TCP/UDP خام فعلاً با clientهای unsupported امن مدیریت شده‌اند.")),
            GuideStep(t("Quality gate", "Quality gate"), t("Before delivery run python3 tools/static_audit.py and ./gradlew :composeApp:allTests.", "قبل از تحویل، python3 tools/static_audit.py و ./gradlew :composeApp:allTests را اجرا کنید."))
        )
    )
}

@Composable
private fun DesktopWorkflowGuide() {
    GuideSection(
        title = t("Desktop workbench layout", "چیدمان میزکار Desktop"),
        subtitle = t("How to use the wide-screen UI like a real engineering panel.", "نحوه استفاده از UI عریض مثل یک پنل مهندسی واقعی."),
        steps = listOf(
            GuideStep(t("Left sidebar", "Sidebar کناری"), t("Navigate between dashboard, profiles, templates, history, guide and settings without losing context.", "بین داشبورد، پروفایل‌ها، قالب‌ها، تاریخچه، راهنما و تنظیمات بدون از دست دادن context جابه‌جا شوید.")),
            GuideStep(t("Command Center", "مرکز فرمان"), t("Connect, disconnect, compose payloads, format JSON and start/stop repeat sending.", "اتصال/قطع اتصال، ساخت payload، فرمت JSON و شروع/توقف ارسال تکرارشونده را انجام دهید.")),
            GuideStep(t("Traffic Intelligence", "تحلیل ترافیک"), t("Inspect live protocol events, byte counters, errors and the latest runtime summary.", "رویدادهای زنده، شمارنده‌های byte، خطاها و آخرین خلاصه runtime را بررسی کنید.")),
            GuideStep(t("History", "تاریخچه"), t("Use persisted sessions to compare backend/device behavior over time.", "از نشست‌های ذخیره‌شده برای مقایسه رفتار بک‌اند/دستگاه در طول زمان استفاده کنید."))
        )
    )
}

@Composable
private fun TroubleshootingGuide() {
    GuideSection(
        title = t("Troubleshooting checklist", "چک‌لیست رفع خطا"),
        subtitle = t("Fast checks when a broker, backend or device is not responding.", "چک‌های سریع وقتی بروکر، بک‌اند یا دستگاه پاسخ نمی‌دهد."),
        steps = listOf(
            GuideStep(t("No connection", "اتصال برقرار نمی‌شود"), t("Verify IP/host, port, local network, firewall, VPN and TLS/plain mode.", "IP/host، port، شبکه محلی، firewall، VPN و حالت TLS/plain را بررسی کنید.")),
            GuideStep(t("Send disabled", "ارسال غیرفعال است"), t("Payload sending is allowed only after the connection state becomes Connected.", "ارسال payload فقط بعد از Connected شدن مجاز است.")),
            GuideStep(t("MQTT publish fails", "ارسال MQTT ناموفق است"), t("Check topic, QoS, credentials, TLS and broker-side ACL rules.", "Topic، QoS، credentialها، TLS و قوانین ACL سمت بروکر را بررسی کنید.")),
            GuideStep(t("UDP discovery fails", "UDP discovery ناموفق است"), t("Confirm broadcast permission, subnet, local bind port and device firewall rules.", "مجوز broadcast، subnet، پورت bind محلی و firewall دستگاه را بررسی کنید."))
        )
    )
}

@Composable
private fun DataSafetyGuide() {
    GuideSection(
        title = t("Data and security notes", "نکات داده و امنیت"),
        subtitle = t("Project defaults are designed for local IoT testing and safer exports.", "پیش‌فرض‌های پروژه برای تست محلی IoT و export امن‌تر طراحی شده‌اند."),
        steps = listOf(
            GuideStep(t("Profile secrets", "اطلاعات محرمانه پروفایل"), t("MQTT passwords are masked during export by default.", "رمزهای MQTT هنگام export به‌صورت پیش‌فرض mask می‌شوند.")),
            GuideStep(t("Local database", "دیتابیس محلی"), t("Room stores profiles, templates, sessions and messages locally.", "Room پروفایل‌ها، قالب‌ها، نشست‌ها و پیام‌ها را به‌صورت محلی ذخیره می‌کند.")),
            GuideStep(t("Cleartext traffic", "ترافیک بدون رمزنگاری"), t("Plain TCP/UDP/MQTT/WebSocket is useful for labs, but TLS should be used when testing production systems.", "TCP/UDP/MQTT/WebSocket ساده برای lab مفید است، اما برای سیستم production باید از TLS استفاده شود.")),
            GuideStep(t("Share exports carefully", "Exportها را با احتیاط به اشتراک بگذارید"), t("Even masked exports can reveal hostnames, ports, topics and operational structure.", "حتی exportهای mask شده هم می‌توانند hostname، port، topic و ساختار عملیاتی را نشان دهند."))
        )
    )
}

@Composable
private fun GuideSection(title: String, subtitle: String, steps: List<GuideStep>) {
    SectionCard(title, subtitle) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.heightIn(max = 420.dp)) {
            items(steps) { step -> GuideStepRow(step) }
        }
    }
}

@Composable
private fun GuideStepRow(step: GuideStep) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (step.badge != null) StatusBadge(step.badge)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                Text(step.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(step.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ProtocolType.guideSteps(): List<String> = when (this) {
    ProtocolType.MQTT -> listOf(t("Set broker host, port and client ID", "Host، port و client ID بروکر را تنظیم کنید"), t("Validate publish/subscribe topics", "Topicهای publish/subscribe را اعتبارسنجی کنید"), t("Use TLS and credentials for non-lab brokers", "برای بروکرهای غیر lab از TLS و credential استفاده کنید"))
    ProtocolType.WEBSOCKET -> listOf(t("Use ws:// for lab and wss:// for secure endpoints", "برای lab از ws:// و برای endpoint امن از wss:// استفاده کنید"), t("Validate headers JSON", "JSON مربوط به headerها را اعتبارسنجی کنید"), t("Watch close/error events in Traffic Intelligence", "رویدادهای close/error را در تحلیل ترافیک ببینید"))
    ProtocolType.TCP -> listOf(t("Confirm persistent socket behavior", "رفتار socket پایدار را تأیید کنید"), t("Choose text/hex line endings carefully", "Line ending متن/HEX را با دقت انتخاب کنید"), t("Use history to compare binary responses", "از history برای مقایسه پاسخ‌های binary استفاده کنید"))
    ProtocolType.UDP -> listOf(t("Set target and optional local bind port", "Target و پورت bind محلی اختیاری را تنظیم کنید"), t("Enable broadcast only for discovery flows", "Broadcast را فقط برای flowهای discovery فعال کنید"), t("Remember UDP is connectionless and lossy", "به یاد داشته باشید UDP connectionless و lossy است"))
}
