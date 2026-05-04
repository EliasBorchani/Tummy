package com.tummy.data.symptoms.db

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual fun symptomsDatabaseBuilder(): RoomDatabase.Builder<SymptomsDatabase> {
    val documentDir = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )?.path.orEmpty()
    val dbPath = "$documentDir/symptoms.db"
    return Room.databaseBuilder<SymptomsDatabase>(
        name = dbPath,
        factory = { SymptomsDatabaseConstructor.initialize() },
    )
}
