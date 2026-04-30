package com.tummy.data.meal.db

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

/**
 * Chaque plateforme fournit un [RoomDatabase.Builder] préconfiguré (path, context).
 * Le driver SQLite bundled est appliqué ici pour rester cohérent.
 */
expect fun mealDatabaseBuilder(): RoomDatabase.Builder<MealDatabase>

fun buildMealDatabase(): MealDatabase =
    mealDatabaseBuilder()
        .setDriver(BundledSQLiteDriver())
        .build()
