package com.tummy.tokens

/**
 * Dimension in dp/pt (conceptually identical on both platforms).
 * `data class` for Swift interop — see AppColor.
 */
data class AppDp(
    val value: Float,
)

/**
 * Discipline rule: every dimension consumed by any Tummy component or screen
 * must come from this object. If a value can't be expressed in the scale
 * below, snap it to the nearest token rather than introducing a one-off
 * literal — small visual deltas are acceptable; the system's authority is
 * not.
 */
object AppDimens {
    // Spacing.
    val SpaceXS = AppDp(4f)
    val SpaceS = AppDp(8f)
    val SpaceM = AppDp(16f)
    val SpaceL = AppDp(24f)
    val SpaceXL = AppDp(32f)
    val SpaceXXL = AppDp(48f)

    // Radii. 4/8/14/22/28 reads "drafted"; off-the-shelf 4/12/24 reads
    // "stock card".
    val RadiusXS = AppDp(4f)
    val RadiusS = AppDp(8f)
    val RadiusM = AppDp(14f)
    val RadiusL = AppDp(22f)
    val RadiusXL = AppDp(28f)

    // Strokes. 1.5 reads more "drafted" than 2 at typical hairline density;
    // 0.5 is the glass edge.
    val StrokeHairline = AppDp(0.5f)
    val StrokeThin = AppDp(1f)
    val StrokeThick = AppDp(1.5f)

    // Control heights — iOS HIG-aligned. Used by buttons, inputs, top bars,
    // sheet headers; the only height tokens components are allowed to use.
    val HeightControlSm = AppDp(32f)
    val HeightControlMd = AppDp(44f)
    val HeightControlLg = AppDp(56f)
}
