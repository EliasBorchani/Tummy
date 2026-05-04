package com.tummy.features.log.ingredient

sealed interface LogIngredientEvent {
    data object Saved : LogIngredientEvent
    data class ShowError(val message: String) : LogIngredientEvent
}
