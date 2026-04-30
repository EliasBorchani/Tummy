package com.tummy.data.meal.di

import com.tummy.data.meal.MealRepositoryImpl
import com.tummy.data.meal.db.buildMealDatabase
import com.tummy.data.meal.network.MealApi
import com.tummy.domain.meal.repository.MealRepository
import com.tummy.domain.meal.usecase.LogMealUseCase
import com.tummy.domain.meal.usecase.ObserveMealsUseCase
import org.koin.dsl.module

val mealModule = module {
    single { buildMealDatabase() }
    single { get<com.tummy.data.meal.db.MealDatabase>().mealDao() }
    single { MealApi(get()) }
    single<MealRepository> { MealRepositoryImpl(get(), get()) }

    factory { ObserveMealsUseCase(get()) }
    factory { LogMealUseCase(get()) }
}
