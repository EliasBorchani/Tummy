package com.tummy.data.ingredients.db

import androidx.room.Entity

@Entity(
    tableName = "standard_ingredient_log",
    primaryKeys = ["date", "ingredient"],
)
data class StandardIngredientLogEntity(
    val date: String,
    val ingredient: String,
)
