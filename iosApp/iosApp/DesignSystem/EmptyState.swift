import SwiftUI
import TummyShared

/// Editorial empty state — `HeadingM` headline, optional `BodyM` message,
/// and up to two actions (primary + secondary). Renders directly on the
/// canvas; do not wrap in a `Card` (the design intentionally lets it
/// breathe on paper).
struct EmptyState: View {
    struct Action {
        let label: String
        let onTap: () -> Void
    }

    let headline: String
    var message: String? = nil
    var primary: Action? = nil
    var secondary: Action? = nil

    @Environment(\.theme) private var theme

    var body: some View {
        VStack(alignment: .leading, spacing: CGFloat(AppDimens.shared.SpaceS)) {
            Text(headline)
                .appTextStyle(AppTypography.shared.HeadingM)
                .foregroundStyle(Color(theme.ink))
                .fixedSize(horizontal: false, vertical: true)

            if let message {
                Text(message)
                    .appTextStyle(AppTypography.shared.BodyM)
                    .foregroundStyle(Color(theme.inkMuted))
                    .fixedSize(horizontal: false, vertical: true)
            }

            if primary != nil || secondary != nil {
                HStack(spacing: CGFloat(AppDimens.shared.SpaceS)) {
                    if let primary {
                        AppButton(
                            title: primary.label,
                            kind: .primary,
                            size: .md,
                            action: primary.onTap
                        )
                    }
                    if let secondary {
                        AppButton(
                            title: secondary.label,
                            kind: .secondary,
                            size: .md,
                            action: secondary.onTap
                        )
                    }
                }
                .padding(.top, CGFloat(AppDimens.shared.SpaceS))
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.top, CGFloat(AppDimens.shared.SpaceXL))
        .padding(.bottom, CGFloat(AppDimens.shared.SpaceL))
        .padding(.horizontal, CGFloat(AppDimens.shared.SpaceM))
    }
}
