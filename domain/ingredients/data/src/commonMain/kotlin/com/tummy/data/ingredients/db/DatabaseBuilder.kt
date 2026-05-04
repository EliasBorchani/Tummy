package com.tummy.data.ingredients.db

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

expect fun ingredientsDatabaseBuilder(): RoomDatabase.Builder<IngredientsDatabase>

fun buildIngredientsDatabase(): IngredientsDatabase =
    ingredientsDatabaseBuilder()
        .setDriver(BundledSQLiteDriver())
        .build()
