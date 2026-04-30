package com.tummy.domain.nutrition.model

data class MacroBreakdown(
    val proteinGrams: Int,
    val carbGrams: Int,
    val fatGrams: Int,
) {
    val kcal: Int get() = proteinGrams * 4 + carbGrams * 4 + fatGrams * 9
}
