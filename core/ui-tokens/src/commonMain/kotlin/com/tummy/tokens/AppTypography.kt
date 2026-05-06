package com.tummy.tokens

enum class FontWeight { Regular, Medium, SemiBold, Bold }

/**
 * Semantic font family. Resolved per-platform — `Sans` is SF Pro on iOS,
 * Roboto Flex on Android; `Mono` is SF Mono / Roboto Mono.
 */
enum class FontFamily { Sans, Mono }

data class AppTextStyle(
    val sizeSp: Float,
    val lineHeightSp: Float,
    val weight: FontWeight,
    // Letter-spacing in sp. Negative tightens (display sizes), positive opens up (eyebrows).
    val trackingSp: Float,
    val family: FontFamily = FontFamily.Sans,
)

/**
 * Type scale. Fonts (family, resources) are resolved per-platform — only roles
 * and metrics are shared. Numerals should be rendered with `tabular-nums` in
 * any data context (scores, counts, dates).
 *
 * - iOS: SF Pro Display (>=20sp) and SF Pro Text (<20sp), system stack.
 * - Android: Roboto Flex, system stack.
 */
object AppTypography {
    val DisplayL = AppTextStyle(34f, 40f, FontWeight.Bold, -0.6f)
    val DisplayM = AppTextStyle(28f, 34f, FontWeight.Bold, -0.4f)
    val HeadingM = AppTextStyle(22f, 28f, FontWeight.SemiBold, -0.2f)
    val BodyL = AppTextStyle(17f, 24f, FontWeight.Regular, -0.2f)
    val BodyM = AppTextStyle(15f, 20f, FontWeight.Regular, -0.1f)
    val Label = AppTextStyle(12f, 16f, FontWeight.SemiBold, 0.6f)
    val Mono = AppTextStyle(12f, 16f, FontWeight.Medium, 0f, FontFamily.Mono)

    // Section eyebrow. Render uppercased; family is mono. Used everywhere a
    // small, deliberate label sits above a heading or signature surface.
    val Eyebrow = AppTextStyle(11f, 14f, FontWeight.Bold, 1.4f, FontFamily.Mono)
}
