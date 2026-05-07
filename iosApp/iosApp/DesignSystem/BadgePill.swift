import SwiftUI
import TummyShared

/// Small uppercased mono pill with a hairline border. Used as a side label
/// for category tags, status hints, or "Most days" affordances next to a
/// primary title.
///
/// Sized below the `Eyebrow` token so it sits visually subordinate when
/// adjacent to body or heading text.
struct BadgePill: View {
    let text: String

    @Environment(\.theme) private var theme

    var body: some View {
        Text(text.uppercased())
            .font(.system(size: 10, weight: .semibold, design: .monospaced))
            .kerning(0.6)
            .foregroundStyle(Color(theme.inkFaint))
            .padding(.horizontal, CGFloat(AppDimens.shared.SpaceXS) + 2)
            .padding(.vertical, 2)
            .overlay(
                RoundedRectangle(cornerRadius: CGFloat(AppDimens.shared.RadiusXS),
                                 style: .continuous)
                    .strokeBorder(Color(theme.hairline),
                                  lineWidth: CGFloat(AppDimens.shared.StrokeThin))
            )
    }
}
