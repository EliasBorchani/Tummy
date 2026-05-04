package com.tummy.tokens

enum class FontWeight { Regular, Medium, SemiBold, Bold }

data class AppTextStyle(
    val sizeSp: Float,
    val lineHeightSp: Float,
    val weight: FontWeight,
)

/**
 * Type scale. Fonts (family, resources) are resolved per-platform — only roles
 * and metrics are shared.
 */
object AppTypography {
    val DisplayL = AppTextStyle(32f, 40f, FontWeight.Bold)
    val HeadingM = AppTextStyle(22f, 28f, FontWeight.SemiBold)
    val BodyL = AppTextStyle(16f, 24f, FontWeight.Regular)
    val BodyM = AppTextStyle(14f, 20f, FontWeight.Regular)
    val Label = AppTextStyle(12f, 16f, FontWeight.Medium)
}
