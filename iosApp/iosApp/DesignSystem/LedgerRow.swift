import SwiftUI
import TummyShared

/// The atomic unit of a list-of-things screen. A primary label sits on top
/// of an optional mono-styled meta line; an optional trailing view (status
/// dot, severity ticks, etc.) sits to the right.
///
/// Designed to live inside a `List` row, so dividers come from the List
/// itself; if you place LedgerRow on a custom layout, add hairline
/// separators yourself.
struct LedgerRow<Trailing: View>: View {
    let label: String
    var meta: String? = nil
    @ViewBuilder let trailing: () -> Trailing

    @Environment(\.theme) private var theme

    var body: some View {
        HStack(alignment: .center, spacing: CGFloat(AppDimens.shared.SpaceM)) {
            VStack(alignment: .leading, spacing: CGFloat(AppDimens.shared.SpaceXS)) {
                Text(label)
                    .appTextStyle(AppTypography.shared.BodyL)
                    .foregroundStyle(Color(theme.ink))

                if let meta {
                    Text(meta)
                        .appTextStyle(AppTypography.shared.Mono)
                        .foregroundStyle(Color(theme.inkFaint))
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            trailing()
        }
    }
}

extension LedgerRow where Trailing == EmptyView {
    init(label: String, meta: String? = nil) {
        self.init(label: label, meta: meta, trailing: { EmptyView() })
    }
}
