package com.tummy.domain.ingredients.model

sealed interface Ingredient {
    data class Standard(
        val ref: StandardIngredient,
    ) : Ingredient

    data class Custom(
        val name: String,
    ) : Ingredient
}
