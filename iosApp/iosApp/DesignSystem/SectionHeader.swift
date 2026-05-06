import SwiftUI
import TummyShared

/// Eyebrow label, optional 2-digit zero-padded count, and a filler hairline
/// that extends to the right edge. Sits above a content section.
struct SectionHeader: View {
    let label: String
    var count: Int? = nil

    @Environment(\.theme) private var theme

    var body: some View {
        HStack(alignment: .center, spacing: CGFloat(AppDimens.shared.SpaceS)) {
            Eyebrow(text: label)
            if let count {
                Text(String(format: "%02d", count))
                    .monospacedDigit()
                    .appTextStyle(AppTypography.shared.Mono)
                    .foregroundStyle(Color(theme.inkFaint))
            }
            Hairline()
        }
    }
}
