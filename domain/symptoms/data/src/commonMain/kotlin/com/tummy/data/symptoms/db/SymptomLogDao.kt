package com.tummy.data.symptoms.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomLogDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: SymptomLogEntity)

    @Query("DELETE FROM symptom_log WHERE date = :date AND symptom = :symptom")
    suspend fun delete(
        date: String,
        symptom: String,
    )

    @Query(
        """
        SELECT * FROM symptom_log
        WHERE (:start IS NULL OR date >= :start)
          AND (:endInclusive IS NULL OR date <= :endInclusive)
        ORDER BY date
        """,
    )
    fun get(
        start: String? = null,
        endInclusive: String? = null,
    ): Flow<List<SymptomLogEntity>>
}
