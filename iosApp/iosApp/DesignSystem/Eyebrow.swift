import SwiftUI
import TummyShared

/// Mono, uppercased, tracked label that sits above headings, surfaces, and
/// signature components. InkMuted by default — pass an explicit color if used
/// over a different ground.
struct Eyebrow: View {
    let text: String
    var color: Color? = nil

    @Environment(\.theme) private var theme

    var body: some View {
        Text(text.uppercased())
            .appTextStyle(AppTypography.shared.Eyebrow)
            .foregroundStyle(color ?? Color(theme.inkMuted))
    }
}
