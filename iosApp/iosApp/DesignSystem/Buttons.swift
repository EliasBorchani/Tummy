import SwiftUI
import TummyShared

/// Restrained, hierarchical button. Primary is solid ink; secondary is a
/// hairline-bordered ghost; tertiary is text + underline.
/// Three sizes: sm 32 / md 44 / lg 56 (HeightControlSm/Md/Lg).
struct AppButton: View {
    enum Kind { case primary, secondary, tertiary, destructive }
    enum Size {
        case sm, md, lg
        var height: CGFloat {
            switch self {
            case .sm: return CGFloat(AppDimens.shared.HeightControlSm)
            case .md: return CGFloat(AppDimens.shared.HeightControlMd)
            case .lg: return CGFloat(AppDimens.shared.HeightControlLg)
            }
        }
        var padX: CGFloat {
            switch self {
            case .sm: return CGFloat(AppDimens.shared.SpaceS)
            case .md: return CGFloat(AppDimens.shared.SpaceM)
            case .lg: return CGFloat(AppDimens.shared.SpaceL)
            }
        }
        var radius: CGFloat {
            switch self {
            case .sm, .md: return CGFloat(AppDimens.shared.RadiusS)
            case .lg: return CGFloat(AppDimens.shared.RadiusM)
            }
        }
        // Body type tokens drive button text size; weight is overridden to
        // SemiBold at render so all buttons share the bold body voice.
        var textStyle: AppTextStyle {
            switch self {
            case .sm, .md: return AppTypography.shared.BodyM
            case .lg: return AppTypography.shared.BodyL
            }
        }
    }

    let title: String
    var kind: Kind = .primary
    var size: Size = .md
    var leadingSystemImage: String? = nil
    var isLoading: Bool = false
    let action: () -> Void

    @Environment(\.theme) private var theme
    @Environment(\.isEnabled) private var isEnabled

    var body: some View {
        Button(action: action) { label }
            .buttonStyle(PressedScaleButtonStyle())
            .disabled(isLoading || !isEnabled)
            .opacity(isEnabled ? 1 : Double(AppOpacity.shared.DisabledStrong))
    }

    @ViewBuilder
    private var label: some View {
        if kind == .tertiary {
            text
                .underline(true, pattern: .solid, color: Color(theme.ink))
        } else {
            HStack(spacing: CGFloat(AppDimens.shared.SpaceS)) {
                if isLoading {
                    ProgressView()
                        .controlSize(.small)
                        .tint(textColor)
                } else if let icon = leadingSystemImage {
                    Image(systemName: icon)
                        .font(.system(size: CGFloat(size.textStyle.sizeSp), weight: .semibold))
                }
                text
            }
            .frame(height: size.height)
            .padding(.horizontal, size.padX)
            .background(backgroundColor)
            .overlay(borderOverlay)
            .clipShape(RoundedRectangle(cornerRadius: size.radius, style: .continuous))
        }
    }

    private var text: some View {
        Text(title)
            .font(.system(size: CGFloat(size.textStyle.sizeSp), weight: .semibold))
            .kerning(CGFloat(size.textStyle.trackingSp))
            .foregroundStyle(textColor)
    }

    private var textColor: Color {
        switch kind {
        case .primary, .destructive: return Color(theme.onPrimary)
        case .secondary, .tertiary: return Color(theme.ink)
        }
    }

    private var backgroundColor: Color {
        switch kind {
        case .primary: return Color(theme.primary)
        case .destructive: return Color(theme.signalHigh)
        case .secondary, .tertiary: return .clear
        }
    }

    @ViewBuilder
    private var borderOverlay: some View {
        if kind == .secondary {
            RoundedRectangle(cornerRadius: size.radius, style: .continuous)
                .strokeBorder(Color(theme.stroke),
                              lineWidth: CGFloat(AppDimens.shared.StrokeThin))
        }
    }
}

/// Press-scale on the `Micro` motion curve. Used by [AppButton] and any
/// custom tappable that wants the same feel.
struct PressedScaleButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? CGFloat(AppMotion.shared.PressScale) : 1)
            .animation(Animation(AppMotion.shared.Micro), value: configuration.isPressed)
    }
}
