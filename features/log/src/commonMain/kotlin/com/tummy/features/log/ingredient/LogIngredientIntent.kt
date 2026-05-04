package com.tummy.features.log.ingredient

import com.tummy.domain.ingredients.usecase.IngredientSuggestion

sealed interface LogIngredientIntent {
    data class QueryChanged(val text: String) : LogIngredientIntent
    data class SelectSuggestion(val suggestion: IngredientSuggestion) : LogIngredientIntent
    data object SaveAsCustom : LogIngredientIntent
}
