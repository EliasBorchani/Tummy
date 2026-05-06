package com.tummy.features.home

import androidx.lifecycle.viewModelScope
import com.tummy.domain.ingredients.model.Ingredient
import com.tummy.domain.ingredients.model.IngredientLogEntry
import com.tummy.domain.ingredients.repository.IngredientRepository
import com.tummy.domain.ingredients.usecase.StandardIngredientNameProvider
import com.tummy.domain.symptoms.model.Symptom
import com.tummy.domain.symptoms.model.SymptomLogEntry
import com.tummy.domain.symptoms.repository.SymptomRepository
import com.tummy.utilities.presentation.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

class HomeViewModel(
    private val ingredientRepository: IngredientRepository,
    private val symptomRepository: SymptomRepository,
    private val standardIngredientNameProvider: StandardIngredientNameProvider,
) : BaseViewModel<HomeState, HomeIntent, HomeEvent>() {
    private val initialDate: LocalDate = today()
    private val selectedDate = MutableStateFlow(initialDate)

    override val state: StateFlow<HomeState> = combine(
        selectedDate,
        ingredientRepository.get(),
        symptomRepository.get(),
    ) { date, ingredientLogs, symptomLogs ->
        buildState(date, ingredientLogs, symptomLogs)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = HomeState(selectedDate = initialDate),
    )

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.PreviousDay -> selectedDate.update { it.minus(1, DateTimeUnit.DAY) }
            HomeIntent.NextDay -> selectedDate.update {
                val next = it.plus(1, DateTimeUnit.DAY)
                if (next > today()) it else next
            }
            is HomeIntent.SelectDate -> selectedDate.value = intent.date
            HomeIntent.AddIngredient ->
                emitEvent(HomeEvent.NavigateToLogIngredient(selectedDate.value))
            HomeIntent.AddSymptom ->
                emitEvent(HomeEvent.NavigateToLogSymptom(selectedDate.value))
            is HomeIntent.DeleteIngredient -> handleDeleteIngredient(intent.ingredient)
            is HomeIntent.DeleteSymptom -> handleDeleteSymptom(intent.symptom)
        }
    }

    private fun handleDeleteIngredient(ingredient: Ingredient) {
        scope.launch {
            runCatching {
                ingredientRepository.delete(
                    IngredientLogEntry(date = selectedDate.value, ingredient = ingredient),
                )
            }.onFailure {
                emitEvent(HomeEvent.ShowError(it.message ?: "Failed to delete"))
            }
        }
    }

    private fun handleDeleteSymptom(symptom: Symptom) {
        scope.launch {
            runCatching {
                symptomRepository.delete(
                    SymptomLogEntry(date = selectedDate.value, symptom = symptom),
                )
            }.onFailure {
                emitEvent(HomeEvent.ShowError(it.message ?: "Failed to delete"))
            }
        }
    }

    private fun buildState(
        date: LocalDate,
        ingredientLogs: List<IngredientLogEntry>,
        symptomLogs: List<SymptomLogEntry>,
    ): HomeState {
        val ingredientsToday = ingredientLogs.filter { it.date == date }
        val symptomsToday = symptomLogs.filter { it.date == date }.map { it.symptom }.toSet()
        val daysLoggedTotal = ingredientLogs.map { it.date }.toSet().size

        // An ingredient's dot is global (independent of which day we're
        // viewing), so compute once per unique ingredient and reuse for
        // both today's list and the ribbon.
        val dotByIngredient: Map<Ingredient, DotColor> = ingredientLogs
            .map { it.ingredient }
            .toSet()
            .associateWith { ingredient ->
                dotFor(
                    SuspectScoreComputer.compute(
                        ingredient = ingredient,
                        ingredientLogs = ingredientLogs,
                        symptomLogs = symptomLogs,
                    ),
                )
            }

        val ingredientsWithDots = ingredientsToday.map { entry ->
            val result = SuspectScoreComputer.compute(
                ingredient = entry.ingredient,
                ingredientLogs = ingredientLogs,
                symptomLogs = symptomLogs,
            )
            IngredientWithDot(
                ingredient = entry.ingredient,
                displayName = displayNameFor(entry.ingredient),
                dot = dotFor(result),
                perSymptomScore = (result as? SuspectScoreResult.Computed)?.scoresPerSymptom,
            )
        }

        val ribbon = buildRibbon(today(), ingredientLogs, symptomLogs, dotByIngredient)

        return HomeState(
            selectedDate = date,
            ingredients = ingredientsWithDots,
            symptoms = symptomsToday,
            daysLoggedTotal = daysLoggedTotal,
            ribbon = ribbon,
        )
    }

    private fun buildRibbon(
        today: LocalDate,
        ingredientLogs: List<IngredientLogEntry>,
        symptomLogs: List<SymptomLogEntry>,
        dotByIngredient: Map<Ingredient, DotColor>,
    ): List<RibbonDay> {
        val ingredientsByDate = ingredientLogs.groupBy { it.date }
        val symptomsByDate = symptomLogs.groupBy { it.date }
        val window = (RIBBON_WINDOW_DAYS - 1) downTo 0
        return window.map { offset ->
            val day = today.minus(offset, DateTimeUnit.DAY)
            val bands = ingredientsByDate[day]
                ?.mapNotNull { dotByIngredient[it.ingredient] }
                ?.toSet()
                .orEmpty()
            RibbonDay(
                date = day,
                presentBands = bands,
                symptomCount = symptomsByDate[day]?.size ?: 0,
            )
        }
    }

    private fun displayNameFor(ingredient: Ingredient): String = when (ingredient) {
        is Ingredient.Standard -> standardIngredientNameProvider.nameOf(ingredient.ref)
        is Ingredient.Custom -> ingredient.name
    }

    private fun dotFor(result: SuspectScoreResult): DotColor = when (result) {
        is SuspectScoreResult.InsufficientGlobalData -> DotColor.Grey
        is SuspectScoreResult.InsufficientIngredientData -> DotColor.Grey
        is SuspectScoreResult.Computed -> {
            val maxScore = result.scoresPerSymptom.values.maxOrNull() ?: 0.0
            when {
                maxScore > 0.3 -> DotColor.Red
                maxScore > 0.0 -> DotColor.Yellow
                else -> DotColor.Green
            }
        }
    }

    private fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

    companion object {
        // Calendar ribbon width (days). Today included, so the leftmost
        // column is `today - (RIBBON_WINDOW_DAYS - 1)`.
        const val RIBBON_WINDOW_DAYS = 14
    }
}
