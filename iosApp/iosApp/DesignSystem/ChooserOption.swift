import SwiftUI
import TummyShared

// Recipe values — the chooser's bespoke 44pt glyph tile sized down to
// HeightControlMd. Not a generic dimension worth a token of its own, but
// shared with anything that hosts a `Glyphs.*` mark in this card.
private let chooserGlyphTileSize: CGFloat = CGFloat(AppDimens.shared.HeightControlMd)

/// Rectangular tappable option card — leading glyph tile, title + hint
/// stack with an optional `BadgePill`, and a trailing arrow. Used in
/// add-flow choosers, settings groups, and any "pick one of a few"
/// affordance.
///
/// The glyph is provided by the caller (typically one of the project
/// `Glyphs.*` marks) so the card stays domain-agnostic.
struct ChooserOption<Glyph: View>: View {
    let title: String
    let hint: String
    var badge: String? = nil
    let onTap: () -> Void
    @ViewBuilder let glyph: () -> Glyph

    @Environment(\.theme) private var theme

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: CGFloat(AppDimens.shared.SpaceM)) {
                glyphTile
                VStack(alignment: .leading, spacing: CGFloat(AppDimens.shared.SpaceXS) / 2) {
                    HStack(spacing: CGFloat(AppDimens.shared.SpaceS)) {
                        Text(title)
                            .appTextStyle(AppTypography.shared.BodyL)
                            .fontWeight(.semibold)
                            .foregroundStyle(Color(theme.ink))
                        if let badge {
                            BadgePill(text: badge)
                        }
                    }
                    Text(hint)
                        .appTextStyle(AppTypography.shared.BodyM)
                        .foregroundStyle(Color(theme.inkMuted))
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                Text("→")
                    .appTextStyle(AppTypography.shared.Mono)
                    .foregroundStyle(Color(theme.inkFaint))
            }
            .padding(CGFloat(AppDimens.shared.SpaceM))
            .background(Color(theme.background),
                        in: RoundedRectangle(cornerRadius: CGFloat(AppDimens.shared.RadiusM),
                                             style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: CGFloat(AppDimens.shared.RadiusM),
                                 style: .continuous)
                    .strokeBorder(Color(theme.stroke),
                                  lineWidth: CGFloat(AppDimens.shared.StrokeThin))
            )
        }
        .buttonStyle(PressedScaleButtonStyle())
    }

    private var glyphTile: some View {
        glyph()
            .frame(width: chooserGlyphTileSize, height: chooserGlyphTileSize)
            .background(Color(theme.surface),
                        in: RoundedRectangle(cornerRadius: CGFloat(AppDimens.shared.RadiusS),
                                             style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: CGFloat(AppDimens.shared.RadiusS),
                                 style: .continuous)
                    .strokeBorder(Color(theme.stroke),
                                  lineWidth: CGFloat(AppDimens.shared.StrokeThin))
            )
    }
}
