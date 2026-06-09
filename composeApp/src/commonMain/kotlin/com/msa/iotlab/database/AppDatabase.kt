package com.msa.iotlab.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

/**
 * Shared Room KMP database definition.
 * Entities and DAOs live in commonMain while builders are platform-specific.
 */
@Database(
    entities = [
        ProfileEntity::class,
        SessionEntity::class,
        MessageLogEntity::class,
        PayloadTemplateEntity::class,
        AppSettingEntity::class
    ],
    version = 1,
    exportSchema = true
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun sessionDao(): SessionDao
    abstract fun messageLogDao(): MessageLogDao
    abstract fun payloadTemplateDao(): PayloadTemplateDao
    abstract fun appSettingDao(): AppSettingDao
}

/**
 * Room KMP generated database constructor declaration.
 */
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

/**
 * Applies shared Room configuration to every platform-specific database builder.
 */
fun buildRoomDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
