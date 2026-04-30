package com.tummy.data.meal.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Côté Android il faut le [Context]. On l'expose via un holder initialisé au
 * démarrage de l'app (composeApp) — évite de polluer commonMain avec Context.
 */
object AndroidDatabaseContext {
    lateinit var applicationContext: Context
}

actual fun mealDatabaseBuilder(): RoomDatabase.Builder<MealDatabase> {
    val ctx = AndroidDatabaseContext.applicationContext
    val dbFile = ctx.getDatabasePath("meal.db")
    return Room.databaseBuilder<MealDatabase>(
        context = ctx,
        name = dbFile.absolutePath,
    )
}
