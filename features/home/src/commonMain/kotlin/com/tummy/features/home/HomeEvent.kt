package com.tummy.features.home

import kotlinx.datetime.LocalDate

sealed interface HomeEvent {
    data class NavigateToLogIngredient(
        val date: LocalDate,
    ) : HomeEvent

    data class NavigateToLogSymptom(
        val date: LocalDate,
    ) : HomeEvent

    data class ShowError(
        val message: String,
    ) : HomeEvent
}
