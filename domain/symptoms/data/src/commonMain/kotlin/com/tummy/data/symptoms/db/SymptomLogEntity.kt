package com.tummy.data.symptoms.db

import androidx.room.Entity

@Entity(
    tableName = "symptom_log",
    primaryKeys = ["date", "symptom"],
)
data class SymptomLogEntity(
    val date: String,
    val symptom: String,
)
