package com.tummy.domain.meal.model

import kotlin.time.Instant

data class Meal(
    val id: MealId,
    val name: String,
    val calories: Int,
    val loggedAt: Instant,
)

// data class (pas value class) pour que Swift voie un vrai type — sinon erasé en String.
data class MealId(val raw: String)
