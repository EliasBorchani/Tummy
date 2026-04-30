package com.tummy.domain.nutrition.repository

import com.tummy.domain.nutrition.model.MacroBreakdown

interface NutritionRepository {
    suspend fun estimate(query: String): MacroBreakdown
}
