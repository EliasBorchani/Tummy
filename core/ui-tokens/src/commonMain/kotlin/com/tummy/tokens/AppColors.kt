package com.tummy.tokens

/**
 * Role-based palette ("Primary"/"Surface", not "Vermillion"/"Bone") so that
 * re-skins don't cascade through every call site.
 *
 * `data class` so Swift sees it as a struct — see [AppColor]. Each platform
 * picks [AppColors.Light] or [AppColors.Dark] based on the system color scheme.
 */
data class AppColorScheme(
    // Surfaces — paper, page, sunken page.
    val background: AppColor,
    val surface: AppColor,
    val surfaceAlt: AppColor,
    // Ink — body, secondary label, tertiary/hint.
    val ink: AppColor,
    val inkMuted: AppColor,
    val inkFaint: AppColor,
    // Lines — hairline divider (~10%) and drafted stroke (~20%).
    val hairline: AppColor,
    val stroke: AppColor,
    // Brand. In the Lab system ink IS the primary; signal colors are reserved
    // for data so chrome never competes with the suspect dot.
    val primary: AppColor,
    val onPrimary: AppColor,
    // Suspect spectrum — the only chromatic moment in the UI.
    // none = insufficient data, low = cleared, mid = weak suspect, high = strong suspect.
    val signalNone: AppColor,
    val signalLow: AppColor,
    val signalMid: AppColor,
    val signalHigh: AppColor,
    // Error states.
    val error: AppColor,
    val onError: AppColor,
)

object AppColors {
    val Light = AppColorScheme(
        background = AppColor(0xFFF2F1EC),
        surface = AppColor(0xFFFAFAF6),
        surfaceAlt = AppColor(0xFFE8E7E1),
        ink = AppColor(0xFF0E0F0C),
        inkMuted = AppColor(0xFF5A5C56),
        inkFaint = AppColor(0xFF9A9C94),
        hairline = AppColor(0x1A0E0F0C),
        stroke = AppColor(0x380E0F0C),
        primary = AppColor(0xFF0E0F0C),
        onPrimary = AppColor(0xFFFAFAF6),
        signalNone = AppColor(0xFF9A9C94),
        signalLow = AppColor(0xFF3F8C70),
        signalMid = AppColor(0xFFD69E2E),
        signalHigh = AppColor(0xFFC73B26),
        error = AppColor(0xFFC73B26),
        onError = AppColor(0xFFFAFAF6),
    )

    val Dark = AppColorScheme(
        background = AppColor(0xFF0C0D0A),
        surface = AppColor(0xFF141512),
        surfaceAlt = AppColor(0xFF1D1E1A),
        ink = AppColor(0xFFEFEFE8),
        inkMuted = AppColor(0xFF9FA197),
        inkFaint = AppColor(0xFF5E6058),
        hairline = AppColor(0x14EFEFE8),
        stroke = AppColor(0x2EEFEFE8),
        primary = AppColor(0xFFEFEFE8),
        onPrimary = AppColor(0xFF0C0D0A),
        signalNone = AppColor(0xFF4F5249),
        signalLow = AppColor(0xFF6FB695),
        signalMid = AppColor(0xFFE5B656),
        signalHigh = AppColor(0xFFE55A45),
        error = AppColor(0xFFE55A45),
        onError = AppColor(0xFF0C0D0A),
    )
}
