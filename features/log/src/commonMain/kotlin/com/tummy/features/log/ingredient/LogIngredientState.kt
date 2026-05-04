package com.tummy.features.log.ingredient

import com.tummy.domain.ingredients.usecase.IngredientSuggestion
import kotlinx.datetime.LocalDate

data class LogIngredientState(
    val date: LocalDate,
    val query: String = "",
    val suggestions: List<IngredientSuggestion> = emptyList(),
    val isSearching: Boolean = false,
)
