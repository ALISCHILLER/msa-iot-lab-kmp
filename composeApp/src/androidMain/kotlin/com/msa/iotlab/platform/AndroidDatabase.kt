package com.msa.iotlab.platform

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.msa.iotlab.database.AppDatabase
import com.msa.iotlab.database.buildRoomDatabase

/**
 * Builds the Android Room database using the app-private database directory.
 */
fun getAndroidDatabase(context: Context): AppDatabase {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("msa_iot_lab.db")
    val builder: RoomDatabase.Builder<AppDatabase> = Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
    return buildRoomDatabase(builder)
}
