import SwiftUI
import TummyShared

/// Single text field with optional eyebrow label, optional helper / error
/// text, focus ring and disabled mode. HeightControlMd × RadiusM, hairline
/// stroke. Pass `label: nil` when the surrounding screen already provides
/// the field's context.
struct AppTextField: View {
    var label: String? = nil
    @Binding var text: String
    var placeholder: String = ""
    var error: String? = nil

    @FocusState private var focused: Bool
    @Environment(\.theme) private var theme
    @Environment(\.isEnabled) private var isEnabled

    var body: some View {
        VStack(alignment: .leading, spacing: CGFloat(AppDimens.shared.SpaceXS)) {
            if let label {
                Eyebrow(text: label)
            }

            TextField(placeholder, text: $text)
                .focused($focused)
                .appTextStyle(AppTypography.shared.BodyL)
                .foregroundStyle(Color(theme.ink))
                .frame(height: CGFloat(AppDimens.shared.HeightControlMd))
                .padding(.horizontal, CGFloat(AppDimens.shared.SpaceM))
                .background(Color(theme.surface))
                .overlay(borderOverlay)
                .clipShape(RoundedRectangle(cornerRadius: CGFloat(AppDimens.shared.RadiusM),
                                            style: .continuous))

            if let error {
                Text(error)
                    .appTextStyle(AppTypography.shared.BodyM)
                    .foregroundStyle(Color(theme.error))
            }
        }
        .opacity(isEnabled ? 1 : Double(AppOpacity.shared.Disabled))
    }

    private var borderOverlay: some View {
        let width: CGFloat = (focused || error != nil)
            ? CGFloat(AppDimens.shared.StrokeThick)
            : CGFloat(AppDimens.shared.StrokeThin)
        let color: Color = {
            if error != nil { return Color(theme.signalHigh) }
            if focused { return Color(theme.ink) }
            return Color(theme.stroke)
        }()
        return RoundedRectangle(cornerRadius: CGFloat(AppDimens.shared.RadiusM),
                                style: .continuous)
            .strokeBorder(color, lineWidth: width)
    }
}

/// Search field with leading magnifier and an optional trailing results
/// count. Eyebrow label is optional — pass `nil` when context is set by
/// the surrounding screen (e.g. a nav-bar title that names the search).
struct AppSearchField: View {
    var label: String? = nil
    @Binding var text: String
    var placeholder: String = ""
    /// Pre-formatted trailing readout, e.g. "3 results". The DS doesn't
    /// localize on the caller's behalf — pass the result of a plural lookup.
    var resultsLabel: String? = nil

    @FocusState private var focused: Bool
    @Environment(\.theme) private var theme

    var body: some View {
        VStack(alignment: .leading, spacing: CGFloat(AppDimens.shared.SpaceXS)) {
            if let label {
                Eyebrow(text: label)
            }

            HStack(spacing: CGFloat(AppDimens.shared.SpaceS)) {
                Image(systemName: "magnifyingglass")
                    .font(.system(size: CGFloat(AppTypography.shared.BodyM.sizeSp), weight: .medium))
                    .foregroundStyle(Color(focused ? theme.ink : theme.inkMuted))

                TextField(placeholder, text: $text)
                    .focused($focused)
                    .appTextStyle(AppTypography.shared.BodyL)
                    .foregroundStyle(Color(theme.ink))

                if let resultsLabel {
                    Text(resultsLabel)
                        .monospacedDigit()
                        .appTextStyle(AppTypography.shared.Mono)
                        .foregroundStyle(Color(theme.inkFaint))
                }
            }
            .frame(height: CGFloat(AppDimens.shared.HeightControlMd))
            .padding(.horizontal, CGFloat(AppDimens.shared.SpaceM))
            .background(Color(theme.surface))
            .overlay(
                RoundedRectangle(cornerRadius: CGFloat(AppDimens.shared.RadiusM),
                                 style: .continuous)
                    .strokeBorder(focused ? Color(theme.ink) : Color(theme.stroke),
                                  lineWidth: focused
                                    ? CGFloat(AppDimens.shared.StrokeThick)
                                    : CGFloat(AppDimens.shared.StrokeThin))
            )
            .clipShape(RoundedRectangle(cornerRadius: CGFloat(AppDimens.shared.RadiusM),
                                        style: .continuous))
        }
    }
}

/// −/+ stepper with a centered tabular value. Caller formats the displayed
/// string (e.g. "1.5 cups"), so the stepper is type-agnostic.
struct AppStepper: View {
    let label: String
    let value: String
    let onIncrement: () -> Void
    let onDecrement: () -> Void
    var canDecrement: Bool = true
    var canIncrement: Bool = true

    @Environment(\.theme) private var theme

    var body: some View {
        VStack(alignment: .leading, spacing: CGFloat(AppDimens.shared.SpaceXS)) {
            Eyebrow(text: label)

            HStack {
                StepperButton(systemName: "minus", action: onDecrement)
                    .disabled(!canDecrement)
                    .opacity(canDecrement ? 1 : Double(AppOpacity.shared.DisabledStrong))
                Spacer()
                Text(value)
                    .monospacedDigit()
                    .appTextStyle(AppTypography.shared.BodyL)
                    .foregroundStyle(Color(theme.ink))
                Spacer()
                StepperButton(systemName: "plus", action: onIncrement)
                    .disabled(!canIncrement)
                    .opacity(canIncrement ? 1 : Double(AppOpacity.shared.DisabledStrong))
            }
            .padding(.horizontal, CGFloat(AppDimens.shared.SpaceS))
            .frame(height: CGFloat(AppDimens.shared.HeightControlMd))
            .background(Color(theme.surface))
            .overlay(
                RoundedRectangle(cornerRadius: CGFloat(AppDimens.shared.RadiusM),
                                 style: .continuous)
                    .strokeBorder(Color(theme.stroke),
                                  lineWidth: CGFloat(AppDimens.shared.StrokeThin))
            )
            .clipShape(RoundedRectangle(cornerRadius: CGFloat(AppDimens.shared.RadiusM),
                                        style: .continuous))
        }
    }
}

private struct StepperButton: View {
    let systemName: String
    let action: () -> Void

    @Environment(\.theme) private var theme

    var body: some View {
        Button(action: action) {
            Image(systemName: systemName)
                .font(.system(size: CGFloat(AppTypography.shared.BodyM.sizeSp), weight: .semibold))
                .foregroundStyle(Color(theme.ink))
                .frame(width: CGFloat(AppDimens.shared.HeightControlSm),
                       height: CGFloat(AppDimens.shared.HeightControlSm))
                .overlay(
                    RoundedRectangle(cornerRadius: CGFloat(AppDimens.shared.RadiusS),
                                     style: .continuous)
                        .strokeBorder(Color(theme.stroke),
                                      lineWidth: CGFloat(AppDimens.shared.StrokeThin))
                )
        }
        .buttonStyle(PressedScaleButtonStyle())
    }
}
