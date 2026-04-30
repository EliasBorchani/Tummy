package com.tummy.features.meal

import com.tummy.domain.meal.model.MealId

sealed interface MealIntent {
    data object Load : MealIntent
    data object Refresh : MealIntent
    data class Select(val id: MealId) : MealIntent
    data class LogQuick(val name: String, val calories: Int) : MealIntent
}
