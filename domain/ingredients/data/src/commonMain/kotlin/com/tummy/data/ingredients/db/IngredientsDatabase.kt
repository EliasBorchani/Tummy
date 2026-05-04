package com.tummy.data.ingredients.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [
        StandardIngredientLogEntity::class,
        CustomIngredientLogEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(IngredientsDatabaseConstructor::class)
abstract class IngredientsDatabase : RoomDatabase() {
    abstract fun ingredientLogDao(): IngredientLogDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object IngredientsDatabaseConstructor : RoomDatabaseConstructor<IngredientsDatabase> {
    override fun initialize(): IngredientsDatabase
}
