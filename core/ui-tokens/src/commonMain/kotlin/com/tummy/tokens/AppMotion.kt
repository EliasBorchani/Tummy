package com.tummy.tokens

/**
 * Cubic-bezier easing curve, control points (x1, y1, x2, y2). Both endpoints
 * are implicit at (0,0) and (1,1). `data class` so Swift sees a struct.
 */
data class CubicBezier(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
)

data class AppMotionStyle(
    val durationMillis: Int,
    val easing: CubicBezier,
)

/**
 * Motion roles. Platforms map these to native APIs:
 * - iOS: `Animation.timingCurve(x1, y1, x2, y2, duration:)`.
 * - Android / Compose: `tween(durationMillis, easing = CubicBezierEasing(...))`.
 */
object AppMotion {
    val Micro = AppMotionStyle(120, CubicBezier(0.2f, 0f, 0f, 1f))
    val Standard = AppMotionStyle(240, CubicBezier(0.2f, 0f, 0f, 1f))
    val Expressive = AppMotionStyle(360, CubicBezier(0.32f, 0.72f, 0f, 1f))
    val Swipe = AppMotionStyle(280, CubicBezier(0.4f, 0f, 0.2f, 1f))

    // Press feedback scale paired with [Micro]. Below 0.97 reads as
    // "depressed too far"; above 0.99 isn't perceptible.
    val PressScale: Float = 0.98f
}
