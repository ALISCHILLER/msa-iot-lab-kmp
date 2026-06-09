package com.msa.iotlab.settings

import com.msa.iotlab.i18n.AppLanguage
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for durable application preferences shared by all Compose targets.
 */
interface AppSettingsRepository {
    fun observeLanguage(): Flow<AppLanguage>
    suspend fun setLanguage(language: AppLanguage)
}
