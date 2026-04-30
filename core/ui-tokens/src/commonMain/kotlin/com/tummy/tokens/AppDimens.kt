package com.tummy.tokens

/**
 * Dimension en dp/pt (conceptuellement identique sur les 2 plateformes).
 * `data class` pour exposition Swift (cf. AppColor).
 */
data class AppDp(val value: Float)

object AppDimens {
    val PaddingXS = AppDp(4f)
    val PaddingS  = AppDp(8f)
    val PaddingM  = AppDp(16f)
    val PaddingL  = AppDp(24f)
    val PaddingXL = AppDp(32f)

    val RadiusS = AppDp(4f)
    val RadiusM = AppDp(12f)
    val RadiusL = AppDp(24f)

    val StrokeThin  = AppDp(1f)
    val StrokeThick = AppDp(2f)
}
