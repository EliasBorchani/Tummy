package com.tummy.features.diary

import androidx.lifecycle.viewModelScope
import com.tummy.domain.meal.model.Meal
import com.tummy.domain.meal.usecase.ObserveMealsUseCase
import com.tummy.domain.user.model.UserProfile
import com.tummy.domain.user.usecase.ObserveCurrentUserUseCase
import com.tummy.utilities.presentation.BaseViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DiaryState(
    val user: UserProfile? = null,
    val meals: List<Meal> = emptyList(),
    val caloriesConsumed: Int = 0,
    val caloriesRemaining: Int = 0,
)

sealed interface DiaryIntent { data object Load : DiaryIntent }
sealed interface DiaryEvent

class DiaryViewModel(
    private val observeMeals: ObserveMealsUseCase,
    private val observeUser: ObserveCurrentUserUseCase,
) : BaseViewModel<DiaryState, DiaryIntent, DiaryEvent>() {

    override val state: StateFlow<DiaryState> = combine(
        observeMeals(),
        observeUser(),
    ) { meals, user ->
        val consumed = meals.sumOf { it.calories }
        DiaryState(
            user = user,
            meals = meals,
            caloriesConsumed = consumed,
            caloriesRemaining = (user?.dailyCalorieGoal ?: 0) - consumed,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = DiaryState(),
    )

    override fun onIntent(intent: DiaryIntent) {
        when (intent) {
            DiaryIntent.Load -> Unit
        }
    }
}
