package com.tummy.domain.ingredients.usecase

import com.tummy.domain.ingredients.model.Ingredient
import com.tummy.domain.ingredients.model.StandardIngredient
import com.tummy.domain.ingredients.repository.IngredientRepository
import kotlinx.coroutines.flow.first

data class IngredientSuggestion(
    val ref: Ingredient,
    val displayName: String,
    val score: Double,
)

class SearchIngredientsUseCase(
    private val ingredientRepository: IngredientRepository,
    private val standardIngredientNameProvider: StandardIngredientNameProvider,
) {
    suspend fun invoke(query: String): List<IngredientSuggestion> {
        val normalizedQuery = query.normalize()
        if (normalizedQuery.isEmpty()) return emptyList()

        val standardHits = StandardIngredient.entries.map { ingredient ->
            val name = standardIngredientNameProvider.nameOf(ingredient)
            IngredientSuggestion(
                ref = Ingredient.Standard(ingredient),
                displayName = name,
                score = fuzzyScore(normalizedQuery, name.normalize()),
            )
        }
        val normalizedStandardNames = standardHits.map { it.displayName.normalize() }.toSet()

        val customHits = ingredientRepository.get().first()
            .asSequence()
            .map { it.ingredient }
            .filterIsInstance<Ingredient.Custom>()
            .map { it.name }
            .distinct()
            .filter { it.normalize() !in normalizedStandardNames }
            .map { name ->
                IngredientSuggestion(
                    ref = Ingredient.Custom(name),
                    displayName = name,
                    score = fuzzyScore(normalizedQuery, name.normalize()),
                )
            }
            .toList()

        return (standardHits + customHits)
            .filter { it.score >= MIN_SCORE }
            .sortedByDescending { it.score }
            .take(MAX_SUGGESTIONS)
    }

    companion object {
        const val MIN_SCORE: Double = 0.3
        const val MAX_SUGGESTIONS: Int = 10
    }
}

internal fun String.normalize(): String = lowercase().trim().replace(Regex("\\s+"), " ")

internal fun fuzzyScore(
    query: String,
    candidate: String,
): Double {
    if (query.isEmpty() || candidate.isEmpty()) return 0.0
    if (candidate == query) return 1.0
    if (candidate.startsWith(query)) return 0.95
    if (candidate.contains(query)) return 0.85
    return jaccard(query.bigrams(), candidate.bigrams())
}

internal fun String.bigrams(): Set<String> {
    if (length < 2) return setOf(this)
    return (0 until length - 1).mapTo(mutableSetOf()) { substring(it, it + 2) }
}

internal fun jaccard(
    a: Set<String>,
    b: Set<String>,
): Double {
    if (a.isEmpty() && b.isEmpty()) return 0.0
    val intersect = a.intersect(b).size
    val union = a.union(b).size
    return intersect.toDouble() / union
}
