package com.tummy.domain.meal.repository

import com.tummy.domain.meal.model.Meal
import com.tummy.domain.meal.model.MealId
import kotlinx.coroutines.flow.Flow

interface MealRepository {
    fun observeAll(): Flow<List<Meal>>
    suspend fun get(id: MealId): Meal?
    suspend fun log(meal: Meal)
    suspend fun delete(id: MealId)
    suspend fun refresh()
}
