package com.tummy.data.nutrition.di

import com.tummy.domain.nutrition.model.MacroBreakdown
import com.tummy.domain.nutrition.repository.NutritionRepository
import org.koin.dsl.module

val nutritionModule = module {
    single<NutritionRepository> { StubNutritionRepository() }
}

private class StubNutritionRepository : NutritionRepository {
    override suspend fun estimate(query: String): MacroBreakdown =
        MacroBreakdown(proteinGrams = 20, carbGrams = 30, fatGrams = 10)
}
