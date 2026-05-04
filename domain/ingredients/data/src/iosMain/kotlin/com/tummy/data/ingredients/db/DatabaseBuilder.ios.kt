package com.tummy.data.ingredients.db

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual fun ingredientsDatabaseBuilder(): RoomDatabase.Builder<IngredientsDatabase> {
    val documentDir = NSFileManager.defaultManager
        .URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )?.path
        .orEmpty()
    val dbPath = "$documentDir/ingredients.db"
    return Room.databaseBuilder<IngredientsDatabase>(
        name = dbPath,
        factory = { IngredientsDatabaseConstructor.initialize() },
    )
}
