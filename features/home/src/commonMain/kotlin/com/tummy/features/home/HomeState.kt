package com.tummy.features.home

import com.tummy.domain.ingredients.model.Ingredient
import com.tummy.domain.symptoms.model.Symptom
import kotlinx.datetime.LocalDate

data class HomeState(
    val selectedDate: LocalDate,
    val ingredients: List<IngredientWithDot> = emptyList(),
    val symptoms: Set<Symptom> = emptySet(),
    val daysLoggedTotal: Int = 0,
)

data class IngredientWithDot(
    val ingredient: Ingredient,
    val displayName: String,
    val dot: DotColor,
    val perSymptomScore: Map<Symptom, Double>?,
)

enum class DotColor { Grey, Green, Yellow, Red }
