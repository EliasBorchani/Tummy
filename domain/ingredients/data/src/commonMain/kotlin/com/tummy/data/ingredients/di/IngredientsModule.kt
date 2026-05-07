package com.tummy.data.ingredients.di

import com.tummy.data.ingredients.IngredientRepositoryImpl
import com.tummy.data.ingredients.db.IngredientsDatabase
import com.tummy.data.ingredients.db.buildIngredientsDatabase
import com.tummy.domain.ingredients.repository.IngredientRepository
import com.tummy.domain.ingredients.usecase.SearchIngredientsUseCase
import com.tummy.domain.ingredients.usecase.UpsertIngredientLogUseCase
import org.koin.dsl.module

val ingredientsModule = module {
    single { buildIngredientsDatabase() }
    single { get<IngredientsDatabase>().ingredientLogDao() }
    single<IngredientRepository> { IngredientRepositoryImpl(get()) }

    factory { SearchIngredientsUseCase(get(), get()) }
    factory { UpsertIngredientLogUseCase(get()) }
}
