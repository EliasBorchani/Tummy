package com.tummy.data.ingredients.db

import androidx.room.Entity

@Entity(
    tableName = "custom_ingredient_log",
    primaryKeys = ["date", "name"],
)
data class CustomIngredientLogEntity(
    val date: String,
    val name: String,
)
