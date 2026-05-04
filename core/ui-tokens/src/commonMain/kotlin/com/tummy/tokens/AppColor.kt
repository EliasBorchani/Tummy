package com.tummy.tokens

/**
 * Couleur ARGB partagée. Chaque plateforme la convertit dans son type natif :
 * - Android / Compose : Color(argb.toInt())
 * - iOS / SwiftUI     : UIColor(red:, green:, blue:, alpha:) via extension Swift
 *
 * Stockée en Long pour éviter la perte de signe. `data class` plutôt que `value
 * class` pour que Swift la voie comme un struct (les value classes Kotlin sont
 * effacées vers leur type sous-jacent dans l'interop ObjC/Swift).
 */
data class AppColor(
    val argb: Long,
) {
    val alpha: Int get() = ((argb shr 24) and 0xFF).toInt()
    val red: Int get() = ((argb shr 16) and 0xFF).toInt()
    val green: Int get() = ((argb shr 8) and 0xFF).toInt()
    val blue: Int get() = (argb and 0xFF).toInt()
}
