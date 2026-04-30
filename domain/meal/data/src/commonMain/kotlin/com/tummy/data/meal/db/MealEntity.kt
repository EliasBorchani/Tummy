package com.tummy.data.meal.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal")
data class MealEntity(
    @PrimaryKey val id: String,
    val name: String,
    val calories: Int,
    val loggedAtEpochMillis: Long,
)
