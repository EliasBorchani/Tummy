package com.tummy.features.log.ingredient

import androidx.lifecycle.viewModelScope
import com.tummy.domain.ingredients.model.Ingredient
import com.tummy.domain.ingredients.usecase.SearchIngredientsUseCase
import com.tummy.domain.ingredients.usecase.UpsertIngredientLogUseCase
import com.tummy.utilities.presentation.BaseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class LogIngredientViewModel(
    private val date: LocalDate,
    private val searchIngredients: SearchIngredientsUseCase,
    private val upsertIngredientLog: UpsertIngredientLogUseCase,
) : BaseViewModel<LogIngredientState, LogIngredientIntent, LogIngredientEvent>() {
    private val query = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<LogIngredientState> = query
        .flatMapLatest { q ->
            if (q.isBlank()) {
                flowOf(LogIngredientState(date = date, query = q))
            } else {
                flow {
                    emit(LogIngredientState(date = date, query = q, isSearching = true))
                    val results = searchIngredients.invoke(q)
                    emit(
                        LogIngredientState(
                            date = date,
                            query = q,
                            suggestions = results,
                            isSearching = false,
                        ),
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = LogIngredientState(date = date),
        )

    override fun onIntent(intent: LogIngredientIntent) {
        when (intent) {
            is LogIngredientIntent.QueryChanged -> query.value = intent.text
            is LogIngredientIntent.SelectSuggestion -> handleSelect(intent.suggestion.ref)
            LogIngredientIntent.SaveAsCustom -> handleSaveAsCustom()
        }
    }

    private fun handleSelect(ingredient: Ingredient) {
        scope.launch {
            runCatching { upsertIngredientLog.invoke(date, ingredient) }
                .onSuccess { emitEvent(LogIngredientEvent.Saved) }
                .onFailure {
                    emitEvent(LogIngredientEvent.ShowError(it.message ?: "Failed to save"))
                }
        }
    }

    private fun handleSaveAsCustom() {
        val raw = query.value
        if (raw.isBlank()) return
        handleSelect(Ingredient.Custom(raw))
    }
}
