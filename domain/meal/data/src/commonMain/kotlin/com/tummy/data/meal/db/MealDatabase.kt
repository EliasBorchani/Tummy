package com.tummy.data.meal.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(entities = [MealEntity::class], version = 1, exportSchema = true)
@ConstructedBy(MealDatabaseConstructor::class)
abstract class MealDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
}

/**
 * Room KMP requires an explicit [RoomDatabaseConstructor]. KSP generates the impl
 * on each target — this declaration is a plain `expect` object.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object MealDatabaseConstructor : RoomDatabaseConstructor<MealDatabase> {
    override fun initialize(): MealDatabase
}
