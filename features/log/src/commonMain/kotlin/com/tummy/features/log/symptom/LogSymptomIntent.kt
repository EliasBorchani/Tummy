package com.tummy.features.log.symptom

import com.tummy.domain.symptoms.model.Symptom

sealed interface LogSymptomIntent {
    data class Toggle(val symptom: Symptom) : LogSymptomIntent
    data object Done : LogSymptomIntent
}
