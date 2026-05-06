package com.tummy.features.home

import com.tummy.domain.ingredients.model.Ingredient
import com.tummy.domain.symptoms.model.Symptom
import kotlinx.datetime.LocalDate

data class HomeState(
    val selectedDate: LocalDate,
    val ingredients: List<IngredientWithDot> = emptyList(),
    val symptoms: Set<Symptom> = emptySet(),
    val daysLoggedTotal: Int = 0,
    val ribbon: List<RibbonDay> = emptyList(),
)

data class IngredientWithDot(
    val ingredient: Ingredient,
    val displayName: String,
    val dot: DotColor,
    val perSymptomScore: Map<Symptom, Double>?,
    val occurrenceCount: Int,
)

/**
 * One column of the calendar ribbon. `presentBands` is the set of distinct
 * suspect-score bands that appear among ingredients logged on this day —
 * up to four entries (Grey/Green/Yellow/Red), each shown if at least one
 * ingredient that day matches that band.
 */
data class RibbonDay(
    val date: LocalDate,
    val presentBands: Set<DotColor>,
    val symptomCount: Int,
)

enum class DotColor { Grey, Green, Yellow, Red }
