package com.tummy.domain.meal.usecase

import com.tummy.domain.meal.model.Meal
import com.tummy.domain.meal.repository.MealRepository

class LogMealUseCase(
    private val repo: MealRepository,
) {
    suspend operator fun invoke(meal: Meal) = repo.log(meal)
}
