package com.tummy.domain.ingredients.model

import kotlinx.datetime.LocalDate

data class IngredientLogEntry(
    val date: LocalDate,
    val ingredient: Ingredient,
)
