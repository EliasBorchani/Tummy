package com.tummy.domain.symptoms.model

import kotlinx.datetime.LocalDate

data class SymptomLogEntry(
    val date: LocalDate,
    val symptom: Symptom,
)
