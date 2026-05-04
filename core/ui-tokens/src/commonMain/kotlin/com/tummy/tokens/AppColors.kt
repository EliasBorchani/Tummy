package com.tummy.tokens

/**
 * Role-based palette ("Primary"/"Surface", not "Orange500") so that re-skins
 * don't cascade through every call site.
 */
object AppColors {
    val Primary = AppColor(0xFFFF7043)
    val OnPrimary = AppColor(0xFFFFFFFF)
    val Secondary = AppColor(0xFF26A69A)
    val OnSecondary = AppColor(0xFFFFFFFF)

    val Background = AppColor(0xFFFFFBF7)
    val Surface = AppColor(0xFFFFFFFF)
    val OnBackground = AppColor(0xFF1C1B1F)
    val OnSurface = AppColor(0xFF1C1B1F)

    val Error = AppColor(0xFFB3261E)
    val OnError = AppColor(0xFFFFFFFF)

    val Outline = AppColor(0xFF79747E)
}
