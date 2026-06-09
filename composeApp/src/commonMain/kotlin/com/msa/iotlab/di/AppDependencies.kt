package com.msa.iotlab.di

import com.msa.iotlab.console.ConsoleControllerFactory
import com.msa.iotlab.console.ConsoleSessionManager
import com.msa.iotlab.console.RoomConsoleHistoryGateway
import com.msa.iotlab.core.AppClock
import com.msa.iotlab.core.IdGenerator
import com.msa.iotlab.core.IdProvider
import com.msa.iotlab.core.TimeProvider
import com.msa.iotlab.database.AppDatabase
import com.msa.iotlab.export.ExportRepository
import com.msa.iotlab.export.ExportWorkspaceUseCase
import com.msa.iotlab.export.ImportWorkspaceUseCase
import com.msa.iotlab.history.HistoryRepository
import com.msa.iotlab.profile.ProfileRepository
import com.msa.iotlab.profile.RoomProfileRepository
import com.msa.iotlab.profile.SaveProfileUseCase
import com.msa.iotlab.protocol.ProtocolClientFactory
import com.msa.iotlab.settings.AppSettingsRepository
import com.msa.iotlab.settings.RoomAppSettingsRepository
import com.msa.iotlab.template.PayloadTemplateRepository
import com.msa.iotlab.template.RoomPayloadTemplateRepository
import com.msa.iotlab.template.SavePayloadTemplateUseCase

/**
 * Manual composition root for shared application services.
 * It keeps object construction outside UI screens and exposes stable dependencies for each feature.
 */
class AppDependencies(
    database: AppDatabase,
    val protocolClientFactory: ProtocolClientFactory,
    private val timeProvider: TimeProvider = AppClock,
    private val idProvider: IdProvider = IdGenerator
) {
    val profileRepository: ProfileRepository = RoomProfileRepository(database.profileDao())
    val historyRepository: HistoryRepository = HistoryRepository(database.sessionDao(), database.messageLogDao(), timeProvider = timeProvider, idProvider = idProvider)
    val templateRepository: PayloadTemplateRepository = RoomPayloadTemplateRepository(database.payloadTemplateDao())
    val settingsRepository: AppSettingsRepository = RoomAppSettingsRepository(database.appSettingDao(), timeProvider)
    val saveProfileUseCase: SaveProfileUseCase = SaveProfileUseCase(profileRepository, timeProvider, idProvider)
    val savePayloadTemplateUseCase: SavePayloadTemplateUseCase = SavePayloadTemplateUseCase(templateRepository, timeProvider, idProvider)
    val exportRepository: ExportRepository = ExportRepository(profileRepository, templateRepository, timeProvider = timeProvider)
    val exportWorkspaceUseCase: ExportWorkspaceUseCase = ExportWorkspaceUseCase(exportRepository)
    val importWorkspaceUseCase: ImportWorkspaceUseCase = ImportWorkspaceUseCase(exportRepository)
    val consoleHistoryGateway = RoomConsoleHistoryGateway(historyRepository)
    val consoleSessionManager: ConsoleSessionManager = ConsoleSessionManager(consoleHistoryGateway, timeProvider, idProvider)
    val consoleControllerFactory: ConsoleControllerFactory = ConsoleControllerFactory(protocolClientFactory, consoleSessionManager, timeProvider, idProvider)
}
