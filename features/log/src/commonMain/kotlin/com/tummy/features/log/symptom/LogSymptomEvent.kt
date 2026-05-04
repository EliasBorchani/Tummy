package com.tummy.features.log.symptom

sealed interface LogSymptomEvent {
    data object Closed : LogSymptomEvent
    data class ShowError(val message: String) : LogSymptomEvent
}
