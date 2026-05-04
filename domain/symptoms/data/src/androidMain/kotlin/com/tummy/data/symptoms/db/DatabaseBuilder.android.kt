package com.tummy.data.symptoms.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

object SymptomsAndroidDatabaseContext {
    lateinit var applicationContext: Context
}

actual fun symptomsDatabaseBuilder(): RoomDatabase.Builder<SymptomsDatabase> {
    val ctx = SymptomsAndroidDatabaseContext.applicationContext
    val dbFile = ctx.getDatabasePath("symptoms.db")
    return Room.databaseBuilder<SymptomsDatabase>(
        context = ctx,
        name = dbFile.absolutePath,
    )
}
