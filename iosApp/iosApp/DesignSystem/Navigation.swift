import SwiftUI
import TummyShared

/// Glass top bar — eyebrow on the left, optional trailing content.
/// HeightControlMd tall, glass surface at RadiusL. Designed to sit at the
/// top of a screen as floating chrome over scrolling content.
struct GlassTopBar<Trailing: View>: View {
    let eyebrow: String
    @ViewBuilder let trailing: () -> Trailing

    var body: some View {
        GlassSurface(cornerRadius: CGFloat(AppDimens.shared.RadiusL)) {
            HStack {
                Eyebrow(text: eyebrow)
                Spacer()
                trailing()
            }
            .padding(.horizontal, CGFloat(AppDimens.shared.SpaceM))
            .frame(height: CGFloat(AppDimens.shared.HeightControlMd))
        }
    }
}

extension GlassTopBar where Trailing == EmptyView {
    init(eyebrow: String) {
        self.init(eyebrow: eyebrow, trailing: { EmptyView() })
    }
}

/// HeightControlSm hairline-bordered icon button, expanded to a
/// HeightControlMd tap target. Used for the trailing `+` on iOS top bars
/// and other single-icon affordances.
struct NavIconButton: View {
    let systemName: String
    var accessibilityLabel: String? = nil
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
                .contentShape(Rectangle())
                .frame(width: CGFloat(AppDimens.shared.HeightControlMd),
                       height: CGFloat(AppDimens.shared.HeightControlMd))
        }
        .buttonStyle(PressedScaleButtonStyle())
        .accessibilityLabel(accessibilityLabel ?? systemName)
    }
}

/// Cancel · eyebrow title · Done bar. Used as the header inside modally
/// presented sheets. `doneEnabled` gates the primary action without
/// disabling Cancel. Defaults pull from the shared `common_*` MOKO strings;
/// callers pass overrides when a screen needs feature-specific verbs.
struct SheetNavBar: View {
    let title: String
    var cancelLabel: String = MR.strings.shared.common_cancel.localized()
    var doneLabel: String = MR.strings.shared.common_done.localized()
    var doneEnabled: Bool = true
    let onCancel: () -> Void
    let onDone: () -> Void

    @Environment(\.theme) private var theme

    var body: some View {
        HStack {
            Button(cancelLabel, action: onCancel)
                .appTextStyle(AppTypography.shared.BodyM)
                .foregroundStyle(Color(theme.inkMuted))

            Spacer()
            Eyebrow(text: title, color: Color(theme.ink))
            Spacer()

            AppButton(title: doneLabel, kind: .primary, size: .sm, action: onDone)
                .disabled(!doneEnabled)
        }
        .padding(.horizontal, CGFloat(AppDimens.shared.SpaceM))
        .frame(height: CGFloat(AppDimens.shared.HeightControlLg))
    }
}
