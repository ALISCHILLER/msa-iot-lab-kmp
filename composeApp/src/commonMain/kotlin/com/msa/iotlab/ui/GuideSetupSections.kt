package com.msa.iotlab.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.msa.iotlab.i18n.t

/**
 * Additional setup and release-readiness guide sections displayed in the in-app manual.
 */
@Composable
fun SetupRunbookGuide() {
    SectionCard(
        title = t("Setup runbook", "راهنمای راه‌اندازی"),
        subtitle = t("A practical checklist for opening, syncing, testing and running the workspace.", "چک‌لیست عملی برای باز کردن، sync، تست و اجرای پروژه.")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SetupCommandRow("1", t("Open project root", "باز کردن ریشه پروژه"), "settings.gradle.kts", t("Open the folder that contains settings.gradle.kts, not only composeApp.", "پوشه‌ای را باز کنید که settings.gradle.kts داخل آن است، نه فقط composeApp."))
            SetupCommandRow("2", t("Sync Gradle", "Sync کردن Gradle"), "./gradlew --version", t("Verify the wrapper downloads and the JDK version is compatible before running targets.", "قبل از اجرای targetها مطمئن شوید wrapper دانلود می‌شود و JDK سازگار است."))
            SetupCommandRow("3", t("Run tests", "اجرای تست‌ها"), "./gradlew :composeApp:allTests", t("Run shared unit tests before manual protocol testing.", "قبل از تست دستی پروتکل‌ها، تست‌های مشترک را اجرا کنید."))
            SetupCommandRow("4", t("Run desktop", "اجرای دسکتاپ"), "./gradlew :composeApp:run", t("Desktop is the best target for wide traffic inspection and protocol debugging.", "دسکتاپ بهترین target برای بررسی عریض ترافیک و دیباگ پروتکل‌هاست."))
        }
    }
}

/**
 * Platform requirement panel for operators who run the app on Android, Desktop and iOS.
 */
@Composable
fun PlatformRequirementsGuide() {
    SectionCard(
        title = t("Platform requirements", "نیازمندی‌های پلتفرم"),
        subtitle = t("What must be available before each target can run reliably.", "مواردی که قبل از اجرای پایدار هر target باید آماده باشد.")
    ) {
        AdaptiveCardGrid(itemCount = 3) { index, modifier ->
            val card = platformRequirementCards()[index]
            OutlinedCard(modifier = modifier) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(card.title, fontWeight = FontWeight.SemiBold)
                    Text(card.body)
                }
            }
        }
    }
}

/**
 * Quality gate guide that makes release verification visible inside the application.
 */
@Composable
fun QualityGateGuide() {
    SectionCard(
        title = t("Quality gate", "دروازه کیفیت"),
        subtitle = t("Commands and expectations before handing the project to a tester or operator.", "دستورها و انتظارهای لازم قبل از تحویل پروژه به تستر یا اپراتور.")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            QualityGateRow(t("Static audit", "ممیزی استاتیک"), "python3 tools/static_audit.py", t("Checks KDoc coverage, package paths, architecture boundaries and source hygiene.", "KDoc، مسیر package، مرزبندی معماری و سلامت source را بررسی می‌کند."))
            QualityGateRow(t("Unit tests", "تست‌های واحد"), "./gradlew :composeApp:allTests", t("Covers profile validation, payloads, protocol diagnostics, import/export and console orchestration.", "اعتبارسنجی پروفایل، payload، diagnostic پروتکل، import/export و orchestrator کنسول را پوشش می‌دهد."))
            QualityGateRow(t("Desktop smoke test", "تست سریع دسکتاپ"), "./gradlew :composeApp:run", t("Create one profile for each protocol and verify incoming/outgoing traffic cards.", "برای هر پروتکل یک پروفایل بسازید و کارت‌های ترافیک ورودی/خروجی را چک کنید."))
            QualityGateRow(t("Android smoke test", "تست سریع اندروید"), "./gradlew :composeApp:assembleDebug", t("Install on a device in the same local network as the IoT target.", "روی دستگاهی نصب کنید که در همان شبکه محلی target IoT است."))
        }
    }
}

/**
 * UX guide that explains how the adaptive workbench should be operated on different screen sizes.
 */
@Composable
fun ProfessionalUxGuide() {
    SectionCard(
        title = t("Professional UI workflow", "گردش‌کار UI حرفه‌ای"),
        subtitle = t("How the UI is optimized for phones, tablets and desktop workstations.", "چطور UI برای موبایل، تبلت و دسکتاپ بهینه شده است.")
    ) {
        AdaptiveCardGrid(itemCount = 3) { index, modifier ->
            val card = uxWorkflowCards()[index]
            OutlinedCard(modifier = modifier) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(card.title, fontWeight = FontWeight.SemiBold)
                    Text(card.body)
                }
            }
        }
    }
}

@Composable
private fun SetupCommandRow(number: String, title: String, command: String, body: String) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatusBadge(number)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(command, fontWeight = FontWeight.Medium)
                Text(body)
            }
        }
    }
}

@Composable
private fun QualityGateRow(title: String, command: String, body: String) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(command, fontWeight = FontWeight.Medium)
            Text(body)
        }
    }
}

@Composable
private fun platformRequirementCards(): List<GuideInfoCard> = listOf(
    GuideInfoCard(t("Desktop", "دسکتاپ"), t("JDK 21, Gradle wrapper access and network access to the backend/device. Best target for traffic-heavy debugging.", "JDK 21، دسترسی Gradle wrapper و دسترسی شبکه به بک‌اند/دستگاه. بهترین target برای دیباگ پرترافیک.")),
    GuideInfoCard(t("Android", "اندروید"), t("Android SDK, emulator or physical device, INTERNET permission and same-LAN access for IoT devices.", "Android SDK، emulator یا دستگاه واقعی، مجوز INTERNET و دسترسی هم‌شبکه برای دستگاه‌های IoT.")),
    GuideInfoCard(t("iOS", "iOS"), t("macOS and Xcode are required. Shared UI, Room and WebSocket are available; raw TCP/UDP/MQTT need native engines.", "macOS و Xcode لازم است. UI مشترک، Room و WebSocket فعال‌اند؛ TCP/UDP/MQTT خام نیازمند engine native هستند."))
)

@Composable
private fun uxWorkflowCards(): List<GuideInfoCard> = listOf(
    GuideInfoCard(t("Phone", "موبایل"), t("Compact navigation, stacked forms and full-width actions keep protocol testing usable on small screens.", "Navigation فشرده، فرم‌های ستونی و دکمه‌های تمام‌عرض تست پروتکل را روی صفحه کوچک قابل استفاده نگه می‌دارند.")),
    GuideInfoCard(t("Tablet", "تبلت"), t("Medium layouts use rail navigation and balanced panels for profile editing and traffic inspection.", "Layout متوسط از rail navigation و پنل‌های متعادل برای ویرایش پروفایل و بررسی ترافیک استفاده می‌کند.")),
    GuideInfoCard(t("Desktop", "دسکتاپ"), t("Persistent sidebar, command center and traffic intelligence panels support long engineering sessions.", "Sidebar ثابت، مرکز فرمان و پنل تحلیل ترافیک برای sessionهای طولانی مهندسی مناسب‌اند."))
)

/**
 * Small immutable content model used by setup guide cards.
 */
private data class GuideInfoCard(val title: String, val body: String)
