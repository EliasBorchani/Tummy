package com.tummy.features.meal

import com.tummy.domain.meal.model.Meal
import com.tummy.domain.meal.model.MealId
import com.tummy.domain.meal.usecase.LogMealUseCase
import com.tummy.domain.meal.usecase.ObserveMealsUseCase
import com.tummy.utilities.presentation.BaseViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.random.Random

class MealViewModel(
    private val observeMeals: ObserveMealsUseCase,
    private val logMeal: LogMealUseCase,
) : BaseViewModel<MealState, MealIntent, MealEvent>(MealState()) {

    init { onIntent(MealIntent.Load) }

    override fun onIntent(intent: MealIntent) {
        when (intent) {
            MealIntent.Load -> load()
            MealIntent.Refresh -> load()
            is MealIntent.Select -> emitEvent(MealEvent.NavigateToDetail(intent.id))
            is MealIntent.LogQuick -> quickLog(intent.name, intent.calories)
        }
    }

    private fun load() {
        updateState { it.copy(isLoading = true, error = null) }
        scope.launch {
            observeMeals()
                .catch { e ->
                    updateState { it.copy(isLoading = false, error = e.message) }
                    emitEvent(MealEvent.ShowError(e.message ?: "Unknown error"))
                }
                .onEach { meals ->
                    updateState { it.copy(isLoading = false, meals = meals) }
                }
                .collect()
        }
    }

    private fun quickLog(name: String, calories: Int) {
        scope.launch {
            val meal = Meal(
                id = MealId(Random.nextLong().toString()),
                name = name,
                calories = calories,
                loggedAt = Clock.System.now(),
            )
            runCatching { logMeal(meal) }
                .onFailure { emitEvent(MealEvent.ShowError(it.message ?: "Failed to log")) }
        }
    }

}
