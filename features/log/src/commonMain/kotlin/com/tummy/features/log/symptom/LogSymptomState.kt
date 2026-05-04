package com.tummy.features.log.symptom

import com.tummy.domain.symptoms.model.Symptom
import kotlinx.datetime.LocalDate

data class LogSymptomState(
    val date: LocalDate,
    val activeSymptoms: Set<Symptom> = emptySet(),
)
