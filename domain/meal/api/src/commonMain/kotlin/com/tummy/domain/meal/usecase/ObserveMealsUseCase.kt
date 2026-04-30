package com.tummy.domain.meal.usecase

import com.tummy.domain.meal.model.Meal
import com.tummy.domain.meal.repository.MealRepository
import kotlinx.coroutines.flow.Flow

class ObserveMealsUseCase(
    private val repo: MealRepository,
) {
    operator fun invoke(): Flow<List<Meal>> = repo.observeAll()
}
