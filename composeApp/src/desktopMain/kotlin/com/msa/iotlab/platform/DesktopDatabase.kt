package com.msa.iotlab.platform

import androidx.room.Room
import androidx.room.RoomDatabase
import com.msa.iotlab.database.AppDatabase
import com.msa.iotlab.database.buildRoomDatabase
import java.io.File

/**
 * Builds the Desktop Room database under the user's home directory.
 */
fun getDesktopDatabase(): AppDatabase {
    val appDir = File(System.getProperty("user.home"), ".msa-iot-lab")
    if (!appDir.exists()) appDir.mkdirs()
    val dbFile = File(appDir, "msa_iot_lab.db")
    val builder: RoomDatabase.Builder<AppDatabase> = Room.databaseBuilder<AppDatabase>(name = dbFile.absolutePath)
    return buildRoomDatabase(builder)
}
