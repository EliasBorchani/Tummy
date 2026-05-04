package com.tummy.tokens

/**
 * Dimension in dp/pt (conceptually identical on both platforms).
 * `data class` for Swift interop — see AppColor.
 */
data class AppDp(
    val value: Float,
)

object AppDimens {
    val PaddingXS = AppDp(4f)
    val PaddingS = AppDp(8f)
    val PaddingM = AppDp(16f)
    val PaddingL = AppDp(24f)
    val PaddingXL = AppDp(32f)

    val RadiusS = AppDp(4f)
    val RadiusM = AppDp(12f)
    val RadiusL = AppDp(24f)

    val StrokeThin = AppDp(1f)
    val StrokeThick = AppDp(2f)
}
