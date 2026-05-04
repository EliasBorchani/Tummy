package com.tummy.features.meal

import androidx.lifecycle.viewModelScope
import com.tummy.domain.meal.model.Meal
import com.tummy.domain.meal.model.MealId
import com.tummy.domain.meal.usecase.LogMealUseCase
import com.tummy.domain.meal.usecase.ObserveMealsUseCase
import com.tummy.utilities.presentation.BaseViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Clock

class MealViewModel(
    private val observeMeals: ObserveMealsUseCase,
    private val logMeal: LogMealUseCase,
) : BaseViewModel<MealState, MealIntent, MealEvent>() {

    override val state: StateFlow<MealState> = observeMeals()
        .map { meals -> MealState(isLoading = false, meals = meals) }
        .catch { e -> emit(MealState(isLoading = false, error = e.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = MealState(isLoading = true),
        )

    override fun onIntent(intent: MealIntent) {
        when (intent) {
            MealIntent.Load, MealIntent.Refresh -> Unit
            is MealIntent.Select -> emitEvent(MealEvent.NavigateToDetail(intent.id))
            is MealIntent.LogQuick -> quickLog(intent.name, intent.calories)
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
