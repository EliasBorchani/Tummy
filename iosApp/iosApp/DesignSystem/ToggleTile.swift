import SwiftUI
import TummyShared

// Recipe values — proportions of the tile that don't generalize.
private let tileMinHeight: CGFloat = 96
private let tileCheckSize: CGFloat = 18
private let tileCheckGlyphSize: CGFloat = 10

/// Selectable tile for binary on/off lists (symptoms, filters, multi-pick
/// flows). Inverts ink/paper on selection — selected tile becomes solid ink
/// with paper text; unselected is surface with hairline border.
///
/// Layout: eyebrow ("Logged" / "Off") top-left, label below, hint at the
/// bottom; circular check indicator top-right.
struct ToggleTile: View {
    let label: String
    var hint: String? = nil
    var onEyebrow: String
    var offEyebrow: String
    let isOn: Bool
    let onToggle: () -> Void

    @Environment(\.theme) private var theme

    var body: some View {
        Button(action: onToggle) {
            content
        }
        .buttonStyle(PressedScaleButtonStyle())
        .accessibilityLabel(label)
        .accessibilityValue(isOn ? onEyebrow : offEyebrow)
        .accessibilityAddTraits(isOn ? [.isSelected] : [])
    }

    @ViewBuilder
    private var content: some View {
        let shape = RoundedRectangle(
            cornerRadius: CGFloat(AppDimens.shared.RadiusM),
            style: .continuous
        )
        ZStack(alignment: .topTrailing) {
            VStack(alignment: .leading, spacing: 0) {
                Eyebrow(text: isOn ? onEyebrow : offEyebrow, color: eyebrowColor)
                Text(label)
                    .appTextStyle(AppTypography.shared.BodyL)
                    .fontWeight(.semibold)
                    .foregroundStyle(foregroundColor)
                    .padding(.top, CGFloat(AppDimens.shared.SpaceXS))
                Spacer(minLength: CGFloat(AppDimens.shared.SpaceS))
                if let hint {
                    Text(hint)
                        .appTextStyle(AppTypography.shared.BodyM)
                        .foregroundStyle(hintColor)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            checkIndicator
        }
        .padding(CGFloat(AppDimens.shared.SpaceM))
        .frame(minHeight: tileMinHeight, alignment: .topLeading)
        .background(backgroundColor, in: shape)
        .overlay(shape.strokeBorder(borderColor,
                                    lineWidth: CGFloat(AppDimens.shared.StrokeThin)))
        .animation(Animation(AppMotion.shared.Micro), value: isOn)
    }

    private var checkIndicator: some View {
        ZStack {
            Circle()
                .strokeBorder(
                    isOn ? Color(theme.onPrimary) : Color(theme.stroke),
                    lineWidth: CGFloat(AppDimens.shared.StrokeThick)
                )
                .background(Circle().fill(isOn ? Color(theme.onPrimary) : .clear))
                .frame(width: tileCheckSize, height: tileCheckSize)
            if isOn {
                Image(systemName: "checkmark")
                    .font(.system(size: tileCheckGlyphSize, weight: .bold))
                    .foregroundStyle(Color(theme.ink))
            }
        }
    }

    private var foregroundColor: Color {
        isOn ? Color(theme.onPrimary) : Color(theme.ink)
    }

    private var eyebrowColor: Color {
        isOn
            ? Color(theme.onPrimary).opacity(0.6)
            : Color(theme.inkFaint)
    }

    private var hintColor: Color {
        isOn
            ? Color(theme.onPrimary).opacity(0.7)
            : Color(theme.inkMuted)
    }

    private var backgroundColor: Color {
        isOn ? Color(theme.ink) : Color(theme.surface)
    }

    private var borderColor: Color {
        isOn ? Color(theme.ink) : Color(theme.stroke)
    }
}
