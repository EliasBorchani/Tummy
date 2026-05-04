package com.tummy.domain.ingredients.repository

import com.tummy.domain.ingredients.model.IngredientLogEntry
import com.tummy.domain.ingredients.model.Ingredient
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface IngredientRepository {
    fun get(start: LocalDate? = null, endInclusive: LocalDate? = null): Flow<List<IngredientLogEntry>>
    fun getAtDate(date: LocalDate): Flow<List<IngredientLogEntry>>
    suspend fun upsert(logEntry: IngredientLogEntry)
    suspend fun delete(logEntry: IngredientLogEntry)
}
