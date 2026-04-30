package com.tummy.data.meal.db

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual fun mealDatabaseBuilder(): RoomDatabase.Builder<MealDatabase> {
    val documentDir = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )?.path.orEmpty()
    val dbPath = "$documentDir/meal.db"
    return Room.databaseBuilder<MealDatabase>(name = dbPath, factory = { MealDatabaseConstructor.initialize() })
}
