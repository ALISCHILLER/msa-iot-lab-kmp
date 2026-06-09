package com.msa.iotlab.platform

import androidx.room.Room
import androidx.room.RoomDatabase
import com.msa.iotlab.database.AppDatabase
import com.msa.iotlab.database.buildRoomDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * Builds the iOS Room database under the app documents directory.
 */
fun getIosDatabase(): AppDatabase {
    val dbFilePath = documentDirectory() + "/msa_iot_lab.db"
    val builder: RoomDatabase.Builder<AppDatabase> = Room.databaseBuilder<AppDatabase>(name = dbFilePath)
    return buildRoomDatabase(builder)
}

/**
 * Resolves the iOS documents directory for database storage.
 */
private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null
    )
    return requireNotNull(documentDirectory?.path)
}
