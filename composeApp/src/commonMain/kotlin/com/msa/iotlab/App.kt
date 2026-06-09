package com.msa.iotlab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.msa.iotlab.database.AppDatabase
import com.msa.iotlab.di.AppDependencies
import com.msa.iotlab.i18n.AppLanguage
import com.msa.iotlab.i18n.LocalAppLanguage
import com.msa.iotlab.i18n.localizedTitle
import com.msa.iotlab.i18n.t
import com.msa.iotlab.protocol.ProtocolClientFactory
import com.msa.iotlab.ui.AppRoute
import com.msa.iotlab.ui.ConsoleScreen
import com.msa.iotlab.ui.DashboardScreen
import com.msa.iotlab.ui.GuideScreen
import com.msa.iotlab.ui.HistoryScreen
import com.msa.iotlab.ui.MsaTheme
import com.msa.iotlab.ui.NavigationSection
import com.msa.iotlab.ui.ProfileEditorScreen
import com.msa.iotlab.ui.ProfileListScreen
import com.msa.iotlab.ui.SettingsScreen
import com.msa.iotlab.ui.TemplateScreen
import com.msa.iotlab.ui.WorkbenchMetric
import com.msa.iotlab.ui.WorkbenchShell
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Shared Compose application root that wires platform services into the desktop-first workbench UI.
 */
@Composable
fun MsaIoTLabApp(database: AppDatabase, protocolClientFactory: ProtocolClientFactory) {
    val dependencies = remember(database, protocolClientFactory) { AppDependencies(database, protocolClientFactory) }
    var route by remember { mutableStateOf<AppRoute>(AppRoute.Dashboard) }
    val language by dependencies.settingsRepository.observeLanguage().collectAsState(AppLanguage.English)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (dependencies.templateRepository.observeAll().first().isEmpty()) dependencies.templateRepository.seedDefaults()
    }

    MsaTheme {
        CompositionLocalProvider(
            LocalAppLanguage provides language,
            LocalLayoutDirection provides if (language == AppLanguage.Persian) LayoutDirection.Rtl else LayoutDirection.Ltr
        ) {
            WorkbenchShell(
                selected = route.toNavigationSection(),
                title = route.titleForShell(language),
                subtitle = route.subtitleForShell(language),
                metrics = route.metricsForShell(language),
                language = language,
                onLanguageChange = { next -> scope.launch { dependencies.settingsRepository.setLanguage(next) } },
                onNavigate = { route = it.toRoute() }
            ) {
            when (val current = route) {
                AppRoute.Dashboard -> DashboardScreen(
                    profileRepository = dependencies.profileRepository,
                    historyRepository = dependencies.historyRepository,
                    onOpenProfiles = { route = AppRoute.Profiles },
                    onOpenHistory = { route = AppRoute.History },
                    onOpenSettings = { route = AppRoute.Settings },
                    onOpenTemplates = { route = AppRoute.Templates },
                    onOpenGuide = { route = AppRoute.Guide },
                    onCreateProtocol = { route = AppRoute.EditProfile(protocol = it) }
                )
                AppRoute.Profiles -> ProfileListScreen(
                    repository = dependencies.profileRepository,
                    onCreate = { route = AppRoute.EditProfile() },
                    onEdit = { route = AppRoute.EditProfile(profile = it) },
                    onOpenConsole = { route = AppRoute.Console(it) }
                )
                is AppRoute.EditProfile -> ProfileEditorScreen(
                    saveProfileUseCase = dependencies.saveProfileUseCase,
                    initialProfile = current.profile,
                    initialProtocol = current.protocol,
                    onCancel = { route = AppRoute.Profiles },
                    onSaved = { saved -> route = AppRoute.Console(saved) }
                )
                is AppRoute.Console -> ConsoleScreen(
                    profile = current.profile,
                    controllerFactory = dependencies.consoleControllerFactory,
                    templateRepository = dependencies.templateRepository,
                    onBack = { route = AppRoute.Profiles }
                )
                AppRoute.History -> HistoryScreen(historyRepository = dependencies.historyRepository)
                AppRoute.Guide -> GuideScreen()
                AppRoute.Templates -> TemplateScreen(
                    repository = dependencies.templateRepository,
                    savePayloadTemplateUseCase = dependencies.savePayloadTemplateUseCase
                )
                AppRoute.Settings -> SettingsScreen(
                    exportWorkspaceUseCase = dependencies.exportWorkspaceUseCase,
                    importWorkspaceUseCase = dependencies.importWorkspaceUseCase
                )
            }
            }
        }
    }
}

private fun AppRoute.toNavigationSection(): NavigationSection = when (this) {
    AppRoute.Dashboard -> NavigationSection.Dashboard
    AppRoute.Profiles, is AppRoute.EditProfile, is AppRoute.Console -> NavigationSection.Profiles
    AppRoute.Templates -> NavigationSection.Templates
    AppRoute.History -> NavigationSection.History
    AppRoute.Guide -> NavigationSection.Guide
    AppRoute.Settings -> NavigationSection.Settings
}

private fun NavigationSection.toRoute(): AppRoute = when (this) {
    NavigationSection.Dashboard -> AppRoute.Dashboard
    NavigationSection.Profiles -> AppRoute.Profiles
    NavigationSection.Templates -> AppRoute.Templates
    NavigationSection.History -> AppRoute.History
    NavigationSection.Guide -> AppRoute.Guide
    NavigationSection.Settings -> AppRoute.Settings
}

private fun AppRoute.titleForShell(language: AppLanguage): String = when (this) {
    AppRoute.Dashboard -> t(language, "IoT Protocol Workbench", "میزکار پروتکل‌های IoT")
    AppRoute.Profiles -> t(language, "Connection Profiles", "پروفایل‌های اتصال")
    is AppRoute.EditProfile -> if (profile == null) t(language, "Create Connection Profile", "ساخت پروفایل اتصال") else t(language, "Edit ${profile.name}", "ویرایش ${profile.name}")
    is AppRoute.Console -> t(language, "Live Console", "کنسول زنده")
    AppRoute.History -> t(language, "Session History", "تاریخچه نشست‌ها")
    AppRoute.Guide -> t(language, "Operator Guide", "راهنمای کاربری")
    AppRoute.Templates -> t(language, "Payload Templates", "قالب‌های Payload")
    AppRoute.Settings -> t(language, "Workspace Settings", "تنظیمات فضای کاری")
}

private fun AppRoute.subtitleForShell(language: AppLanguage): String = when (this) {
    AppRoute.Dashboard -> t(language, "Desktop-first testing station for MQTT, WebSocket, TCP and UDP devices.", "ایستگاه تست حرفه‌ای برای MQTT، وب‌سوکت، TCP و UDP در پروژه‌های IoT.")
    AppRoute.Profiles -> t(language, "Save reusable device and backend connection settings.", "تنظیمات اتصال بک‌اند و دستگاه‌ها را ذخیره و دوباره استفاده کنید.")
    is AppRoute.EditProfile -> t(language, "Configure protocol, transport, payload encoding and IoT options.", "پروتکل، transport، نوع payload و گزینه‌های IoT را تنظیم کنید.")
    is AppRoute.Console -> "${profile.protocol.localizedTitle(language)} • ${profile.host}:${profile.port} • ${profile.payloadEncoding}"
    AppRoute.History -> t(language, "Review persisted sessions, traffic and diagnostic events.", "نشست‌ها، ترافیک و رویدادهای diagnostic ذخیره‌شده را بررسی کنید.")
    AppRoute.Guide -> t(language, "Learn the full workflow for profiles, live console, payloads, history and troubleshooting.", "مسیر کامل کار با پروفایل‌ها، کنسول زنده، payloadها، history و رفع خطا را ببینید.")
    AppRoute.Templates -> t(language, "Manage reusable commands, heartbeats and diagnostic payloads.", "دستورها، heartbeatها و payloadهای diagnostic قابل استفاده مجدد را مدیریت کنید.")
    AppRoute.Settings -> t(language, "Export, import and audit portable workspace bundles.", "پکیج‌های قابل انتقال workspace را export، import و audit کنید.")
}

private fun AppRoute.metricsForShell(language: AppLanguage): List<WorkbenchMetric> = when (this) {
    is AppRoute.Console -> listOf(
        WorkbenchMetric(t(language, "Protocol", "پروتکل"), profile.protocol.localizedTitle(language), t(language, "active profile", "پروفایل فعال")),
        WorkbenchMetric(t(language, "Endpoint", "Endpoint"), "${profile.host}:${profile.port}", if (profile.tlsEnabled) t(language, "TLS enabled", "TLS فعال") else t(language, "plain transport", "ارتباط ساده"))
    )
    else -> emptyList()
}
