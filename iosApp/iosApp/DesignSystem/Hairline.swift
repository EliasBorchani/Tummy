import SwiftUI
import TummyShared

/// 1pt horizontal rule using the theme's `hairline` color. Use under the
/// CalendarRibbon, between sections, or anywhere a quiet divider belongs.
struct Hairline: View {
    @Environment(\.theme) private var theme

    var body: some View {
        Rectangle()
            .fill(Color(theme.hairline))
            .frame(height: CGFloat(AppDimens.shared.StrokeThin))
    }
}
