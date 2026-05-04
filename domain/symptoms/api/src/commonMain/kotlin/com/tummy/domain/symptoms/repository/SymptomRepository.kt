package com.tummy.domain.symptoms.repository

import com.tummy.domain.symptoms.model.SymptomLogEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface SymptomRepository {
    fun get(
        start: LocalDate? = null,
        endInclusive: LocalDate? = null,
    ): Flow<List<SymptomLogEntry>>

    fun getAtDate(date: LocalDate): Flow<List<SymptomLogEntry>>

    suspend fun upsert(logEntry: SymptomLogEntry)

    suspend fun delete(logEntry: SymptomLogEntry)
}
