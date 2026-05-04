package com.tummy.data.ingredients.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

object IngredientsAndroidDatabaseContext {
    lateinit var applicationContext: Context
}

actual fun ingredientsDatabaseBuilder(): RoomDatabase.Builder<IngredientsDatabase> {
    val ctx = IngredientsAndroidDatabaseContext.applicationContext
    val dbFile = ctx.getDatabasePath("ingredients.db")
    return Room.databaseBuilder<IngredientsDatabase>(
        context = ctx,
        name = dbFile.absolutePath,
    )
}
