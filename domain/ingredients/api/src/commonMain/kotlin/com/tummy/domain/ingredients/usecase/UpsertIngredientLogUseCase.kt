package com.tummy.domain.ingredients.usecase

import com.tummy.domain.ingredients.model.Ingredient
import com.tummy.domain.ingredients.model.IngredientLogEntry
import com.tummy.domain.ingredients.model.StandardIngredient
import com.tummy.domain.ingredients.repository.IngredientRepository
import kotlinx.datetime.LocalDate

class UpsertIngredientLogUseCase(
    private val ingredientRepository: IngredientRepository,
    private val standardIngredientNameProvider: StandardIngredientNameProvider,
) {
    suspend fun invoke(date: LocalDate, ingredient: Ingredient) {
        val resolved = when (ingredient) {
            is Ingredient.Standard -> ingredient
            is Ingredient.Custom -> resolve(ingredient.name)
        }
        ingredientRepository.upsert(IngredientLogEntry(date, resolved))
    }

    private fun resolve(rawName: String): Ingredient {
        val normalized = rawName.normalize()
        if (normalized.isEmpty()) return Ingredient.Custom(normalized)
        val matchedStandard = StandardIngredient.entries.firstOrNull { entry ->
            fuzzyScore(normalized, standardIngredientNameProvider.nameOf(entry).normalize()) >= MIN_RESOLVE_SCORE
        }
        return matchedStandard?.let(Ingredient::Standard) ?: Ingredient.Custom(normalized)
    }

    companion object {
        const val MIN_RESOLVE_SCORE: Double = 0.85
    }
}
