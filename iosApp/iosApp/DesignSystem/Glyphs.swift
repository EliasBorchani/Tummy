import SwiftUI
import TummyShared

// All product glyphs are designed to render inside a 22×22 viewBox; they
// scale to whatever frame they're placed in. Stroke widths and dot radii
// are recipe values for the marks themselves — analogous to SuspectDot's
// stroke proportions.
private let glyphMarkSize: CGFloat = 22

/// 3 stacked hairlines (rows of food log) + a `signalHigh` dot. The mark
/// the app reaches for whenever it needs to say "ingredient" without using
/// food iconography.
struct IngredientGlyph: View {
    @Environment(\.theme) private var theme

    var body: some View {
        Canvas { context, size in
            let scale = min(size.width, size.height) / glyphMarkSize
            let lines: [(y: CGFloat, x2: CGFloat)] = [(6, 19), (11, 14), (16, 16)]
            for line in lines {
                var path = Path()
                path.move(to: CGPoint(x: 3 * scale, y: line.y * scale))
                path.addLine(to: CGPoint(x: line.x2 * scale, y: line.y * scale))
                context.stroke(path, with: .color(Color(theme.ink)), lineWidth: 1.25)
            }
            let r: CGFloat = 2 * scale
            let dot = Path(ellipseIn: CGRect(
                x: 19 * scale - r, y: 11 * scale - r,
                width: 2 * r, height: 2 * r
            ))
            context.fill(dot, with: .color(Color(theme.signalHigh)))
        }
        .frame(width: glyphMarkSize, height: glyphMarkSize)
    }
}

/// Wave (rhythm) over a horizontal axis. The mark for "symptom" — same
/// vocabulary as the in-app symptom row's waveform indicator.
struct SymptomGlyph: View {
    @Environment(\.theme) private var theme

    var body: some View {
        Canvas { context, size in
            let scale = min(size.width, size.height) / glyphMarkSize

            // Wave: M3 11 q3 -5 6 0 t6 0 t6 0 (in 22pt viewBox)
            var wave = Path()
            wave.move(to: CGPoint(x: 3 * scale, y: 11 * scale))
            wave.addQuadCurve(
                to: CGPoint(x: 9 * scale, y: 11 * scale),
                control: CGPoint(x: 6 * scale, y: 6 * scale)
            )
            wave.addQuadCurve(
                to: CGPoint(x: 15 * scale, y: 11 * scale),
                control: CGPoint(x: 12 * scale, y: 16 * scale)
            )
            wave.addQuadCurve(
                to: CGPoint(x: 21 * scale, y: 11 * scale),
                control: CGPoint(x: 18 * scale, y: 6 * scale)
            )
            context.stroke(
                wave,
                with: .color(Color(theme.ink)),
                style: StrokeStyle(lineWidth: 1.4, lineCap: .round)
            )

            // Axis baseline
            var axis = Path()
            axis.move(to: CGPoint(x: 3 * scale, y: 17 * scale))
            axis.addLine(to: CGPoint(x: 19 * scale, y: 17 * scale))
            context.stroke(axis, with: .color(Color(theme.stroke)), lineWidth: 1)
        }
        .frame(width: glyphMarkSize, height: glyphMarkSize)
    }
}
