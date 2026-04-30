package com.tummy.tokens

/**
 * Palette sémantique Tummy. Les noms sont volontairement sémantiques (role-based)
 * et non descriptifs — pas de "Orange500", mais "Primary", "Surface", etc.
 * Ça évite les reskins douloureux.
 */
object AppColors {
    val Primary       = AppColor(0xFFFF7043)
    val OnPrimary     = AppColor(0xFFFFFFFF)
    val Secondary     = AppColor(0xFF26A69A)
    val OnSecondary   = AppColor(0xFFFFFFFF)

    val Background    = AppColor(0xFFFFFBF7)
    val Surface       = AppColor(0xFFFFFFFF)
    val OnBackground  = AppColor(0xFF1C1B1F)
    val OnSurface     = AppColor(0xFF1C1B1F)

    val Error         = AppColor(0xFFB3261E)
    val OnError       = AppColor(0xFFFFFFFF)

    val Outline       = AppColor(0xFF79747E)
}
