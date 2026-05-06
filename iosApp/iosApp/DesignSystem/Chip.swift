import SwiftUI
import TummyShared

// Indicator dot size — recipe value tied to the chip's HeightControlSm
// height; not a generic dimension.
private let chipIndicatorSize: CGFloat = 6

/// Pill-shaped tappable. Two styles:
/// - `.filled` — sits on `surfaceAlt`, ink text, optional leading indicator
///   dot. Used for currently-active items (logged symptoms, active filters).
/// - `.dashed` — transparent bg, inkMuted text, dashed stroke border.
///   Used for "+ add" affordances and other low-stakes additions.
struct Chip: View {
    enum Style { case filled, dashed }

    let label: String
    var style: Style = .filled
    var indicator: Color? = nil
    var leadingSystemImage: String? = nil
    var accessibilityLabel: String? = nil
    let onTap: () -> Void

    @Environment(\.theme) private var theme

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: CGFloat(AppDimens.shared.SpaceS)) {
                if let indicator {
                    Circle()
                        .fill(indicator)
                        .frame(width: chipIndicatorSize, height: chipIndicatorSize)
                }
                if let icon = leadingSystemImage {
                    Image(systemName: icon)
                        .font(.system(size: CGFloat(AppTypography.shared.BodyM.sizeSp),
                                      weight: .medium))
                }
                Text(label)
                    .font(.system(size: CGFloat(AppTypography.shared.BodyM.sizeSp),
                                  weight: .medium))
            }
            .foregroundStyle(foregroundColor)
            .padding(.horizontal, CGFloat(AppDimens.shared.SpaceM))
            .frame(height: CGFloat(AppDimens.shared.HeightControlSm))
            .background(backgroundColor, in: Capsule())
            .overlay(borderOverlay)
        }
        .buttonStyle(PressedScaleButtonStyle())
        .accessibilityLabel(accessibilityLabel ?? label)
    }

    private var foregroundColor: Color {
        switch style {
        case .filled: return Color(theme.ink)
        case .dashed: return Color(theme.inkMuted)
        }
    }

    private var backgroundColor: Color {
        switch style {
        case .filled: return Color(theme.surfaceAlt)
        case .dashed: return .clear
        }
    }

    @ViewBuilder
    private var borderOverlay: some View {
        if style == .dashed {
            Capsule().strokeBorder(
                Color(theme.stroke),
                // Same dashed rhythm as Banner's border — recipe value.
                style: StrokeStyle(
                    lineWidth: CGFloat(AppDimens.shared.StrokeThin),
                    dash: [4, 3]
                )
            )
        }
    }
}

/// Horizontal flow layout that wraps to a new line when subviews would
/// overflow the proposed width. Used by chip clusters; reusable for any
/// pill / tag wrapping.
struct WrapLayout: Layout {
    var hSpacing: CGFloat
    var vSpacing: CGFloat

    func sizeThatFits(
        proposal: ProposedViewSize,
        subviews: Subviews,
        cache _: inout ()
    ) -> CGSize {
        let maxWidth = proposal.width ?? .infinity
        var x: CGFloat = 0
        var y: CGFloat = 0
        var lineHeight: CGFloat = 0
        var maxLineWidth: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x + size.width > maxWidth, x > 0 {
                maxLineWidth = max(maxLineWidth, x - hSpacing)
                x = 0
                y += lineHeight + vSpacing
                lineHeight = 0
            }
            x += size.width + hSpacing
            lineHeight = max(lineHeight, size.height)
        }
        maxLineWidth = max(maxLineWidth, x - hSpacing)
        return CGSize(width: maxLineWidth, height: y + lineHeight)
    }

    func placeSubviews(
        in bounds: CGRect,
        proposal _: ProposedViewSize,
        subviews: Subviews,
        cache _: inout ()
    ) {
        var x: CGFloat = bounds.minX
        var y: CGFloat = bounds.minY
        var lineHeight: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x + size.width > bounds.maxX, x > bounds.minX {
                x = bounds.minX
                y += lineHeight + vSpacing
                lineHeight = 0
            }
            subview.place(at: CGPoint(x: x, y: y), proposal: ProposedViewSize(size))
            x += size.width + hSpacing
            lineHeight = max(lineHeight, size.height)
        }
    }
}
