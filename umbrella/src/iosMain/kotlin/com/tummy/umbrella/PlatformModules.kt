package com.tummy.umbrella

import com.tummy.domain.ingredients.model.StandardIngredient
import com.tummy.domain.ingredients.usecase.StandardIngredientNameProvider
import com.tummy.tokens.resources.MR
import dev.icerock.moko.resources.desc.desc
import org.koin.core.module.Module
import org.koin.dsl.module

private class IosStandardIngredientNameProvider : StandardIngredientNameProvider {
    override fun nameOf(ingredient: StandardIngredient): String {
        val res = when (ingredient) {
            StandardIngredient.Dairy -> MR.strings.ingredient_dairy
            StandardIngredient.Wheat -> MR.strings.ingredient_wheat
            StandardIngredient.Gluten -> MR.strings.ingredient_gluten
            StandardIngredient.Soy -> MR.strings.ingredient_soy
            StandardIngredient.Eggs -> MR.strings.ingredient_eggs
            StandardIngredient.Peanuts -> MR.strings.ingredient_peanuts
            StandardIngredient.TreeNuts -> MR.strings.ingredient_tree_nuts
            StandardIngredient.Shellfish -> MR.strings.ingredient_shellfish
        }
        return res.desc().localized()
    }
}

private val iosNameProviderModule = module {
    single<StandardIngredientNameProvider> { IosStandardIngredientNameProvider() }
}

internal actual val platformModules: List<Module> = listOf(iosNameProviderModule)
