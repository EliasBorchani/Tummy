package com.tummy.data.meal.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meal ORDER BY loggedAtEpochMillis DESC")
    fun observeAll(): Flow<List<MealEntity>>

    @Query("SELECT * FROM meal WHERE id = :id")
    suspend fun get(id: String): MealEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(meal: MealEntity)

    @Query("DELETE FROM meal WHERE id = :id")
    suspend fun delete(id: String)
}
