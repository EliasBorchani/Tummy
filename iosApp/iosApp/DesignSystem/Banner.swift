import SwiftUI
import TummyShared

/// Soft suggestion or warning. Always rendered with a dashed border so it's
/// visually distinct from solid action surfaces. Combines an optional
/// mono eyebrow, a body string, and an optional trailing action button.
///
/// Use `tone == .warning` only when the message is genuinely actionable
/// urgency (a strong-signal pattern, a destructive confirmation). Default
/// `tone == .neutral` covers "listening", "did you mean", and empty states.
struct Banner: View {
    enum Tone { case neutral, warning }
    enum Alignment { case leading, center }

    struct Action {
        let label: String
        let onTap: () -> Void
    }

    var eyebrow: String? = nil
    let message: String
    var tone: Tone = .neutral
    var alignment: Alignment = .leading
    var action: Action? = nil

    @Environment(\.theme) private var theme

    var body: some View {
        let shape = RoundedRectangle(
            cornerRadius: CGFloat(AppDimens.shared.RadiusS),
            style: .continuous
        )
        content
            .padding(.vertical, CGFloat(AppDimens.shared.SpaceS))
            .padding(.horizontal, CGFloat(AppDimens.shared.SpaceM))
            .frame(maxWidth: .infinity, alignment: bodyFrameAlignment)
            .overlay(
                shape.strokeBorder(
                    borderColor,
                    // Dash pattern is a banner-recipe value (rhythm of the
                    // hashes, not a reusable dimension).
                    style: StrokeStyle(
                        lineWidth: CGFloat(AppDimens.shared.StrokeThin),
                        dash: [4, 3]
                    )
                )
            )
    }

    @ViewBuilder
    private var content: some View {
        if alignment == .center, eyebrow == nil, action == nil {
            // Single-line centered variant (empty / no-results state).
            Text(message)
                .appTextStyle(AppTypography.shared.BodyM)
                .foregroundStyle(Color(theme.inkMuted))
                .multilineTextAlignment(.center)
        } else {
            HStack(alignment: .firstTextBaseline, spacing: CGFloat(AppDimens.shared.SpaceM)) {
                if let eyebrow {
                    Eyebrow(text: eyebrow, color: eyebrowColor)
                        .fixedSize(horizontal: true, vertical: false)
                }
                Text(message)
                    .appTextStyle(AppTypography.shared.BodyM)
                    .foregroundStyle(messageColor)
                    .frame(maxWidth: .infinity, alignment: .leading)
                if let action {
                    AppButton(
                        title: action.label,
                        kind: .primary,
                        size: .sm,
                        action: action.onTap
                    )
                    .fixedSize()
                }
            }
        }
    }

    private var borderColor: Color {
        switch tone {
        case .neutral: return Color(theme.stroke)
        case .warning: return Color(theme.signalHigh)
        }
    }

    private var eyebrowColor: Color {
        switch tone {
        case .neutral: return Color(theme.inkMuted)
        case .warning: return Color(theme.signalHigh)
        }
    }

    private var messageColor: Color {
        // Neutral banners read as soft / advisory; warnings put the message
        // on primary ink so it reads as the headline.
        switch tone {
        case .neutral: return Color(theme.inkMuted)
        case .warning: return Color(theme.ink)
        }
    }

    private var bodyFrameAlignment: SwiftUI.Alignment {
        alignment == .center ? .center : .leading
    }
}
