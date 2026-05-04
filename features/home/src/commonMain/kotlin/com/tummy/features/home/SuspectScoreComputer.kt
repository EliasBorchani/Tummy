package com.tummy.features.home

import com.tummy.domain.ingredients.model.Ingredient
import com.tummy.domain.ingredients.model.IngredientLogEntry
import com.tummy.domain.symptoms.model.Symptom
import com.tummy.domain.symptoms.model.SymptomLogEntry
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

sealed interface SuspectScoreResult {
    data class InsufficientGlobalData(
        val daysLogged: Int,
        val daysNeeded: Int,
    ) : SuspectScoreResult

    data class InsufficientIngredientData(
        val daysAte: Int,
        val daysDidNotEat: Int,
        val daysNeeded: Int,
    ) : SuspectScoreResult

    data class Computed(
        val scoresPerSymptom: Map<Symptom, Double>,
    ) : SuspectScoreResult
}

object SuspectScoreComputer {
    const val MIN_DAYS_FOR_SCORING: Int = 7
    const val MIN_DAYS_PER_INGREDIENT: Int = 3
    private const val SAME_DAY_WEIGHT: Double = 1.0
    private const val NEXT_DAY_WEIGHT: Double = 0.5

    fun compute(
        ingredient: Ingredient,
        ingredientLogs: List<IngredientLogEntry>,
        symptomLogs: List<SymptomLogEntry>,
    ): SuspectScoreResult {
        val loggedDates = ingredientLogs.map { it.date }.toSet()
        if (loggedDates.size < MIN_DAYS_FOR_SCORING) {
            return SuspectScoreResult.InsufficientGlobalData(
                daysLogged = loggedDates.size,
                daysNeeded = MIN_DAYS_FOR_SCORING,
            )
        }

        val daysAte =
            ingredientLogs
                .filter { it.ingredient == ingredient }
                .map { it.date }
                .toSet()
        val daysDidntEat = loggedDates - daysAte

        if (daysAte.size < MIN_DAYS_PER_INGREDIENT ||
            daysDidntEat.size < MIN_DAYS_PER_INGREDIENT
        ) {
            return SuspectScoreResult.InsufficientIngredientData(
                daysAte = daysAte.size,
                daysDidNotEat = daysDidntEat.size,
                daysNeeded = MIN_DAYS_PER_INGREDIENT,
            )
        }

        val scores = Symptom.entries.associateWith { symptom ->
            val symptomDays = symptomLogs
                .filter { it.symptom == symptom }
                .map { it.date }
                .toSet()
            val pAte = weightedSymptomRatio(daysAte, symptomDays)
            val pDidntEat = weightedSymptomRatio(daysDidntEat, symptomDays)
            pAte - pDidntEat
        }

        return SuspectScoreResult.Computed(scoresPerSymptom = scores)
    }

    private fun weightedSymptomRatio(
        days: Set<LocalDate>,
        symptomDays: Set<LocalDate>,
    ): Double {
        if (days.isEmpty()) return 0.0
        var symptomWeight = 0.0
        for (day in days) {
            if (day in symptomDays) symptomWeight += SAME_DAY_WEIGHT
            val tomorrow = day.plus(1, DateTimeUnit.DAY)
            if (tomorrow in symptomDays) symptomWeight += NEXT_DAY_WEIGHT
        }
        val totalWeight = days.size * (SAME_DAY_WEIGHT + NEXT_DAY_WEIGHT)
        return symptomWeight / totalWeight
    }
}
