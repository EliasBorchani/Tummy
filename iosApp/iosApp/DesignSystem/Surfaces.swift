import SwiftUI
import TummyShared

/// Liquid glass — backdrop blur tinted by surface color, with a hairline
/// edge and a subtle inner shine. iOS only; do not use on data surfaces.
///
/// Glass alphas (0.55 tint, 0.85/0.08 shine highs, 0.06/0.40 shine lows) are
/// part of the recipe — they're not dimensions, can't be expressed in the
/// scale, and aren't reusable elsewhere. Kept inline so the visual logic
/// reads in one place.
struct GlassSurface<Content: View>: View {
    var cornerRadius: CGFloat = CGFloat(AppDimens.shared.RadiusL)
    @ViewBuilder let content: () -> Content

    @Environment(\.theme) private var theme
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let shape = RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
        let tint = Color(theme.surface).opacity(0.55)
        let shineHi = colorScheme == .dark
            ? Color.white.opacity(0.08)
            : Color.white.opacity(0.85)
        let shineLo = colorScheme == .dark
            ? Color.black.opacity(0.40)
            : Color(theme.ink).opacity(0.06)

        content()
            .background(.thinMaterial, in: shape)
            .background(tint, in: shape)
            .overlay(
                shape.strokeBorder(Color(theme.hairline),
                                   lineWidth: CGFloat(AppDimens.shared.StrokeHairline))
            )
            .overlay(
                shape
                    .stroke(LinearGradient(
                        colors: [shineHi, .clear, shineLo],
                        startPoint: .top, endPoint: .bottom
                    ), lineWidth: CGFloat(AppDimens.shared.StrokeThin))
                    .blendMode(.plusLighter)
                    .allowsHitTesting(false)
            )
            .clipShape(shape)
    }
}

/// Solid card on paper. Hairline border, surface fill, RadiusM.
struct Card<Content: View>: View {
    var cornerRadius: CGFloat = CGFloat(AppDimens.shared.RadiusM)
    var padding: CGFloat = CGFloat(AppDimens.shared.SpaceM)
    @ViewBuilder let content: () -> Content

    @Environment(\.theme) private var theme

    var body: some View {
        let shape = RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
        content()
            .padding(padding)
            .background(Color(theme.surface), in: shape)
            .overlay(shape.strokeBorder(Color(theme.hairline),
                                        lineWidth: CGFloat(AppDimens.shared.StrokeThin)))
    }
}

/// Modal sheet content surface — surface fill, optional grabber. Wrap inside
/// `.sheet(...)`; iOS handles drag-to-dismiss. Use
/// `.presentationDragIndicator(.visible)` for the system grabber instead of
/// `showsGrabber: true` — the local grabber exists for non-system contexts.
struct SheetSurface<Content: View>: View {
    var showsGrabber: Bool = false
    @ViewBuilder let content: () -> Content

    @Environment(\.theme) private var theme

    var body: some View {
        VStack(spacing: 0) {
            if showsGrabber {
                RoundedRectangle(cornerRadius: CGFloat(AppDimens.shared.RadiusXS),
                                 style: .continuous)
                    .fill(Color(theme.stroke))
                    .frame(width: CGFloat(AppDimens.shared.SpaceXL),
                           height: CGFloat(AppDimens.shared.SpaceXS))
                    .padding(.top, CGFloat(AppDimens.shared.SpaceS))
                    .padding(.bottom, CGFloat(AppDimens.shared.SpaceS))
            }
            content()
        }
        .frame(maxWidth: .infinity)
        .background(Color(theme.surface))
    }
}
