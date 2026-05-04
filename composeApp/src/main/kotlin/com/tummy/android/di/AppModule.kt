package com.tummy.android.di

import com.tummy.android.resources.AndroidStandardIngredientNameProvider
import com.tummy.data.ingredients.di.ingredientsModule
import com.tummy.data.symptoms.di.symptomsModule
import com.tummy.domain.ingredients.usecase.StandardIngredientNameProvider
import com.tummy.features.home.HomeViewModel
import com.tummy.features.log.ingredient.LogIngredientViewModel
import com.tummy.features.log.symptom.LogSymptomViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val androidAppModule = module {
    single<StandardIngredientNameProvider> {
        AndroidStandardIngredientNameProvider(context = androidContext())
    }

    viewModelOf(::HomeViewModel)
    viewModelOf(::LogIngredientViewModel)
    viewModelOf(::LogSymptomViewModel)
}

val androidDataModules = listOf(ingredientsModule, symptomsModule)
