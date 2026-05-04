package com.tummy.data.symptoms

import com.tummy.data.symptoms.db.SymptomLogDao
import com.tummy.data.symptoms.db.SymptomLogEntity
import com.tummy.domain.symptoms.model.Symptom
import com.tummy.domain.symptoms.model.SymptomLogEntry
import com.tummy.domain.symptoms.repository.SymptomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class SymptomRepositoryImpl(
    private val dao: SymptomLogDao,
) : SymptomRepository {

    override fun get(
        start: LocalDate?,
        endInclusive: LocalDate?,
    ): Flow<List<SymptomLogEntry>> {
        return dao.get(start?.toString(), endInclusive?.toString())
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getAtDate(date: LocalDate): Flow<List<SymptomLogEntry>> = get(
        start = date,
        endInclusive = date,
    )

    override suspend fun upsert(logEntry: SymptomLogEntry) {
        dao.insert(
            SymptomLogEntity(
                date = logEntry.date.toString(),
                symptom = logEntry.symptom.name,
            ),
        )
    }

    override suspend fun delete(logEntry: SymptomLogEntry) {
        dao.delete(
            date = logEntry.date.toString(),
            symptom = logEntry.symptom.name,
        )
    }
}

private fun SymptomLogEntity.toDomain(): SymptomLogEntry = SymptomLogEntry(
    date = LocalDate.parse(date),
    symptom = Symptom.valueOf(symptom),
)
