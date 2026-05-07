package com.tummy.domain.ingredients.usecase

import com.tummy.domain.ingredients.model.Ingredient
import com.tummy.domain.ingredients.model.IngredientLogEntry
import com.tummy.domain.ingredients.repository.IngredientRepository
import kotlinx.datetime.LocalDate

/**
 * Persists an ingredient for a given date. Custom names are normalized
 * (lowercase / trim / collapse whitespace) before storage.
 *
 * This use case does **not** silently promote a custom name to a curated
 * `Standard` ingredient even when a fuzzy match is strong. Per the PRD, a
 * curated upgrade requires explicit user acceptance, which is mediated by
 * the "Did you mean?" banner in the search UI — by the time we reach this
 * use case, the caller's choice (`Custom` vs `Standard`) is final.
 */
class UpsertIngredientLogUseCase(
    private val ingredientRepository: IngredientRepository,
) {
    suspend fun invoke(
        date: LocalDate,
        ingredient: Ingredient,
    ) {
        val toStore: Ingredient = when (ingredient) {
            is Ingredient.Standard -> ingredient
            is Ingredient.Custom -> {
                val normalized = ingredient.name.normalize()
                if (normalized.isEmpty()) return
                Ingredient.Custom(normalized)
            }
        }
        ingredientRepository.upsert(IngredientLogEntry(date, toStore))
    }
}
