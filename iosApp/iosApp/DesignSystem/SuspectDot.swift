import SwiftUI
import TummyShared

/// The product's signature data mark. Same DNA as the app icon: an outer
/// track ring + an inner signal disc. Used everywhere an ingredient's
/// suspect score is surfaced (ledger rows, popovers, hero treatments).
///
/// Two visual channels:
///   - hue (signalNone/Low/Mid/High) encodes score band
///   - inner-disc size encodes severity within the band, so colorblind
///     users still get the signal
///
/// Below threshold (`color == .grey`) the dot has no inner disc and the
/// outer ring breathes to convey "still listening for patterns".
///
/// Stroke and inner-disc proportions derive from `size` so the mark holds
/// at any scale; they're recipe values, not generic dimension tokens.
struct SuspectDot: View {
    let color: DotColor
    var size: CGFloat = CGFloat(AppDimens.shared.SpaceM)

    @Environment(\.theme) private var theme
    @State private var breath: CGFloat = 1.0

    private var isReady: Bool { color != .grey }

    private var strokeWidth: CGFloat { max(1.25, size * 0.09) }
    private var ringDiameter: CGFloat { size - strokeWidth }
    // 0.4 (low) → 0.7 (mid) → 1.0 (high) of inner radius. Insufficient = no disc.
    private var discDiameter: CGFloat {
        let innerRadius = (ringDiameter / 2) - strokeWidth * 1.2
        let scale: CGFloat
        switch color {
        case .grey: scale = 0
        case .green: scale = 0.4
        case .yellow: scale = 0.7
        case .red: scale = 1.0
        @unknown default: scale = 0
        }
        return max(0, innerRadius * scale * 2)
    }

    private var signal: Color {
        switch color {
        case .grey: return Color(theme.signalNone)
        case .green: return Color(theme.signalLow)
        case .yellow: return Color(theme.signalMid)
        case .red: return Color(theme.signalHigh)
        @unknown default: return Color(theme.signalNone)
        }
    }

    var body: some View {
        ZStack {
            // Track — hairline ring at full circumference.
            Circle()
                .stroke(Color(theme.hairline), lineWidth: strokeWidth)
                .frame(width: ringDiameter, height: ringDiameter)

            // Progress / signal ring. Full circle when ready; half-trimmed
            // and breathing when listening.
            Circle()
                .trim(from: 0, to: isReady ? 1.0 : 0.5)
                .stroke(signal,
                        style: StrokeStyle(lineWidth: strokeWidth, lineCap: .butt))
                .frame(width: ringDiameter, height: ringDiameter)
                .rotationEffect(.degrees(-90))
                .scaleEffect(isReady ? 1.0 : breath)

            // Inner severity disc — sized by score band.
            if isReady {
                Circle()
                    .fill(signal)
                    .frame(width: discDiameter, height: discDiameter)
            }
        }
        .frame(width: size, height: size)
        .onAppear { startBreathingIfNeeded() }
        .onChange(of: isReady) { _, newReady in
            if newReady {
                breath = 1.0
            } else {
                startBreathingIfNeeded()
            }
        }
    }

    private func startBreathingIfNeeded() {
        guard !isReady else { return }
        withAnimation(.easeInOut(duration: 2.2).repeatForever(autoreverses: true)) {
            breath = 0.92
        }
    }
}
