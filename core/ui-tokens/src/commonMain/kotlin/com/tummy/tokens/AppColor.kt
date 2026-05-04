package com.tummy.tokens

/**
 * Shared ARGB color. Each platform converts to its native type:
 * - Android / Compose: Color(argb.toInt())
 * - iOS / SwiftUI:     UIColor(red:, green:, blue:, alpha:) via Swift extension
 *
 * Stored as Long to avoid sign truncation. `data class` (not `value class`) so
 * Swift sees it as a struct — Kotlin value classes are erased to their
 * underlying type in ObjC/Swift interop.
 */
data class AppColor(
    val argb: Long,
) {
    val alpha: Int get() = ((argb shr 24) and 0xFF).toInt()
    val red: Int get() = ((argb shr 16) and 0xFF).toInt()
    val green: Int get() = ((argb shr 8) and 0xFF).toInt()
    val blue: Int get() = (argb and 0xFF).toInt()
}
