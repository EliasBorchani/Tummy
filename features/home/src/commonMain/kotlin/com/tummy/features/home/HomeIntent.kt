package com.tummy.features.home

import com.tummy.domain.ingredients.model.Ingredient
import com.tummy.domain.symptoms.model.Symptom
import kotlinx.datetime.LocalDate

sealed interface HomeIntent {
    data object PreviousDay : HomeIntent

    data object NextDay : HomeIntent

    data class SelectDate(
        val date: LocalDate,
    ) : HomeIntent

    data object AddIngredient : HomeIntent

    data object AddSymptom : HomeIntent

    data class DeleteIngredient(
        val ingredient: Ingredient,
    ) : HomeIntent

    data class DeleteSymptom(
        val symptom: Symptom,
    ) : HomeIntent
}
