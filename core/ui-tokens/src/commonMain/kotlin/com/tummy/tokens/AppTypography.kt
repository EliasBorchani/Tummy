package com.tummy.tokens

enum class FontWeight { Regular, Medium, SemiBold, Bold }

data class AppTextStyle(
    val sizeSp: Float,
    val lineHeightSp: Float,
    val weight: FontWeight,
)

/**
 * Échelle typographique. Les `Font` (famille, ressources) sont résolus côté
 * plateforme — on ne partage que les rôles et les métriques.
 */
object AppTypography {
    val DisplayL = AppTextStyle(32f, 40f, FontWeight.Bold)
    val HeadingM = AppTextStyle(22f, 28f, FontWeight.SemiBold)
    val BodyL    = AppTextStyle(16f, 24f, FontWeight.Regular)
    val BodyM    = AppTextStyle(14f, 20f, FontWeight.Regular)
    val Label    = AppTextStyle(12f, 16f, FontWeight.Medium)
}
