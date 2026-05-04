package com.tummy.domain.ingredients.usecase

import com.tummy.domain.ingredients.model.StandardIngredient

fun interface StandardIngredientNameProvider {
    fun nameOf(ingredient: StandardIngredient): String
}
