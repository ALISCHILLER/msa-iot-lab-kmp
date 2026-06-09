package com.msa.iotlab.settings

import com.msa.iotlab.core.TimeProvider
import com.msa.iotlab.database.AppSettingDao
import com.msa.iotlab.database.AppSettingEntity
import com.msa.iotlab.i18n.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed implementation that persists operator preferences such as the selected UI language.
 */
class RoomAppSettingsRepository(
    private val dao: AppSettingDao,
    private val timeProvider: TimeProvider
) : AppSettingsRepository {
    override fun observeLanguage(): Flow<AppLanguage> {
        return dao.observe(AppSettingsKeys.Language)
            .map { row -> AppLanguage.fromCode(row?.value) }
    }

    override suspend fun setLanguage(language: AppLanguage) {
        dao.upsert(
            AppSettingEntity(
                key = AppSettingsKeys.Language,
                value = language.code,
                updatedAt = timeProvider.nowMillis()
            )
        )
    }
}
