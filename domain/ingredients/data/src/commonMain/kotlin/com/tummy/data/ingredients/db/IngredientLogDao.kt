package com.tummy.data.ingredients.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientLogDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStandard(entity: StandardIngredientLogEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCustom(entity: CustomIngredientLogEntity)

    @Query("DELETE FROM standard_ingredient_log WHERE date = :date AND ingredient = :ingredient")
    suspend fun deleteStandard(date: String, ingredient: String)

    @Query("DELETE FROM custom_ingredient_log WHERE date = :date AND name = :name")
    suspend fun deleteCustom(date: String, name: String)

    @Query(
        """
        SELECT * FROM standard_ingredient_log
        WHERE (:start IS NULL OR date >= :start)
          AND (:endInclusive IS NULL OR date <= :endInclusive)
        ORDER BY date
        """,
    )
    fun getStandard(
        start: String? = null,
        endInclusive: String? = null,
    ): Flow<List<StandardIngredientLogEntity>>

    @Query(
        """
        SELECT * FROM custom_ingredient_log
        WHERE (:start IS NULL OR date >= :start)
          AND (:endInclusive IS NULL OR date <= :endInclusive)
        ORDER BY date
        """,
    )
    fun getCustom(
        start: String? = null,
        endInclusive: String? = null,
    ): Flow<List<CustomIngredientLogEntity>>
}
