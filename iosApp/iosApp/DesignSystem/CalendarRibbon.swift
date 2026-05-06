import SwiftUI
import TummyShared

/// 14-day date navigator with per-day signal density. Replaces the chevron
/// row at the top of the day view.
///
/// Each column shows the set of suspect-score bands present among that
/// day's ingredients (up to four dots — Red / Yellow / Green / Grey, only
/// shown if at least one ingredient that day matches the band) plus a
/// vermillion underline whose opacity scales with the symptom count.
///
/// The ribbon is horizontally scrollable so it adapts to any device width
/// without truncation.
struct CalendarRibbon: View {
    let days: [RibbonDay]
    let selectedDate: LocalDate
    let onSelect: (LocalDate) -> Void

    var body: some View {
        ScrollViewReader { proxy in
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 0) {
                    ForEach(days, id: \.date) { day in
                        RibbonColumn(
                            day: day,
                            isSelected: day.date == selectedDate,
                            onTap: { onSelect(day.date) }
                        )
                        .id(day.date)
                    }
                }
                .padding(.horizontal, CGFloat(AppDimens.shared.SpaceM))
            }
            // Window slides with selection (VM-side), so the rightmost column
            // changes when the user navigates beyond the current window. Pin
            // the new rightmost edge to trailing so the ribbon stays in sync
            // with the data.
            .onAppear { scrollToWindowEdge(proxy: proxy, animated: false) }
            .onChange(of: days.last?.date) { _, _ in
                scrollToWindowEdge(proxy: proxy, animated: true)
            }
            // Baseline + day-number transitions glide between columns when the
            // selection moves within the visible window.
            .animation(Animation(AppMotion.shared.Standard), value: selectedDate)
        }
    }

    private func scrollToWindowEdge(proxy: ScrollViewProxy, animated: Bool) {
        guard let last = days.last?.date else { return }
        if animated {
            withAnimation(Animation(AppMotion.shared.Standard)) {
                proxy.scrollTo(last, anchor: .trailing)
            }
        } else {
            proxy.scrollTo(last, anchor: .trailing)
        }
    }
}

// MARK: - Column

// Per-column micro-grid. These define the ribbon's visual identity at the
// small scale where global tokens don't have the resolution; treated as
// recipe values, like SuspectDot's stroke proportions.
private let columnWidth: CGFloat = 28
private let bandDotDiameter: CGFloat = 6
private let bandDotGap: CGFloat = 2
private let bandStackMaxHeight: CGFloat = 56
private let symptomBarWidth: CGFloat = 14
private let symptomBarHeight: CGFloat = 2
// Symptom-bar opacity recipe: min(1, base + count × step). Hand-tuned so the
// bar reads as faint at 1 symptom and saturated by 4+.
private let symptomOpacityBase: Double = 0.4
private let symptomOpacityStep: Double = 0.15

private struct RibbonColumn: View {
    let day: RibbonDay
    let isSelected: Bool
    let onTap: () -> Void

    @Environment(\.theme) private var theme

    var body: some View {
        Button(action: onTap) {
            VStack(spacing: CGFloat(AppDimens.shared.SpaceXS)) {
                bandStack
                baseline
                dayLabel
                symptomUnderline
            }
            .frame(width: columnWidth)
            .padding(.vertical, CGFloat(AppDimens.shared.SpaceS))
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(accessibilityLabel)
    }

    // MARK: Pieces

    private var bandStack: some View {
        // Stacked bottom-up so present bands fill from the baseline. Order
        // is severity-descending: red on top, then yellow, green, grey.
        let bandsInOrder: [DotColor] = [.red, .yellow, .green, .grey]
        let visible = bandsInOrder.filter { day.presentBands.contains($0) }
        return VStack(spacing: bandDotGap) {
            if visible.isEmpty {
                // Empty day → faint placeholder so the column doesn't collapse.
                Circle()
                    .fill(Color(theme.hairline))
                    .frame(width: bandDotDiameter, height: bandDotDiameter)
            } else {
                ForEach(visible, id: \.self) { band in
                    Circle()
                        .fill(color(for: band))
                        .frame(width: bandDotDiameter, height: bandDotDiameter)
                }
            }
        }
        .frame(height: bandStackMaxHeight, alignment: .bottom)
    }

    private var baseline: some View {
        Rectangle()
            .fill(isSelected ? Color(theme.ink) : Color(theme.hairline))
            .frame(height: isSelected
                   ? CGFloat(AppDimens.shared.StrokeThick)
                   : CGFloat(AppDimens.shared.StrokeThin))
    }

    private var dayLabel: some View {
        VStack(spacing: 0) {
            Text("\(dayNumber)")
                .monospacedDigit()
                .font(.system(size: CGFloat(AppTypography.shared.BodyM.sizeSp),
                              weight: isSelected ? .bold : .medium))
                .foregroundStyle(Color(isSelected ? theme.ink : theme.inkMuted))
            Text(weekdayLetter.uppercased())
                .appTextStyle(AppTypography.shared.Eyebrow)
                .foregroundStyle(Color(theme.inkFaint))
        }
    }

    @ViewBuilder
    private var symptomUnderline: some View {
        if day.symptomCount > 0 {
            RoundedRectangle(cornerRadius: symptomBarHeight / 2, style: .continuous)
                .fill(Color(theme.signalHigh))
                .frame(width: symptomBarWidth, height: symptomBarHeight)
                .opacity(min(1.0, symptomOpacityBase
                             + Double(day.symptomCount) * symptomOpacityStep))
        } else {
            Color.clear.frame(width: symptomBarWidth, height: symptomBarHeight)
        }
    }

    // MARK: Helpers

    private func color(for band: DotColor) -> Color {
        switch band {
        case .grey: return Color(theme.signalNone)
        case .green: return Color(theme.signalLow)
        case .yellow: return Color(theme.signalMid)
        case .red: return Color(theme.signalHigh)
        @unknown default: return Color(theme.signalNone)
        }
    }

    private var dayNumber: Int {
        Calendar.current.component(.day, from: day.date.toSwiftDate())
    }

    private var weekdayLetter: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "EEEEE" // narrow weekday (e.g. "M", "T")
        return formatter.string(from: day.date.toSwiftDate())
    }

    private var accessibilityLabel: String {
        let formatter = DateFormatter()
        formatter.dateStyle = .full
        let dateLabel = formatter.string(from: day.date.toSwiftDate())
        let bandCount = day.presentBands.count
        let symptomLabel = day.symptomCount > 0
            ? ", \(day.symptomCount) symptom\(day.symptomCount == 1 ? "" : "s")"
            : ""
        let bandLabel = bandCount > 0
            ? ", \(bandCount) ingredient band\(bandCount == 1 ? "" : "s")"
            : ""
        return "\(dateLabel)\(bandLabel)\(symptomLabel)"
    }
}
