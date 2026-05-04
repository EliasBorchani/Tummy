package com.tummy.android.resources

import android.content.Context
import com.tummy.domain.ingredients.model.StandardIngredient
import com.tummy.domain.ingredients.usecase.StandardIngredientNameProvider
import com.tummy.tokens.resources.MR

class AndroidStandardIngredientNameProvider(
    private val context: Context,
) : StandardIngredientNameProvider {
    override fun nameOf(ingredient: StandardIngredient): String {
        val resourceId = when (ingredient) {
            StandardIngredient.Dairy -> MR.strings.ingredient_dairy.resourceId
            StandardIngredient.Wheat -> MR.strings.ingredient_wheat.resourceId
            StandardIngredient.Gluten -> MR.strings.ingredient_gluten.resourceId
            StandardIngredient.Soy -> MR.strings.ingredient_soy.resourceId
            StandardIngredient.Eggs -> MR.strings.ingredient_eggs.resourceId
            StandardIngredient.Peanuts -> MR.strings.ingredient_peanuts.resourceId
            StandardIngredient.TreeNuts -> MR.strings.ingredient_tree_nuts.resourceId
            StandardIngredient.Shellfish -> MR.strings.ingredient_shellfish.resourceId
        }
        return context.getString(resourceId)
    }
}
