package com.tummy.features.meal

import com.tummy.domain.meal.model.MealId

sealed interface MealEvent {
    data class NavigateToDetail(val id: MealId) : MealEvent
    data class ShowError(val message: String) : MealEvent
}
