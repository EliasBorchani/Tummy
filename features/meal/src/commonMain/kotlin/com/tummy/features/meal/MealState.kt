package com.tummy.features.meal

import com.tummy.domain.meal.model.Meal

data class MealState(
    val isLoading: Boolean = false,
    val meals: List<Meal> = emptyList(),
    val error: String? = null,
)
