import Combine
import SwiftUI
import TummyShared

// Sheet content height isn't a dimension on the spacing/sizing scale —
// it's screen-specific composition. Single recipe value, kept here so it's
// reviewable.
private let addSheetDetentHeight: CGFloat = 300

@MainActor
final class HomeObservable: ObservableObject {
    @Published private(set) var state: HomeState
    let events = PassthroughSubject<HomeEvent, Never>()
    let vm: HomeViewModel

    init(vm: HomeViewModel) {
        self.vm = vm
        self.state = vm.state.value
        Task { [weak self] in
            guard let self else { return }
            for await s in vm.state { self.state = s }
        }
        Task { [weak self] in
            guard let self else { return }
            for await e in vm.events { self.events.send(e) }
        }
    }
}

struct HomeView: View {
    @StateObject private var obs: HomeObservable
    let onNavigate: (Route) -> Void
    @State private var showAddSheet: Bool = false

    @Environment(\.theme) private var theme

    init(onNavigate: @escaping (Route) -> Void) {
        self.onNavigate = onNavigate
        self._obs = StateObject(wrappedValue: HomeObservable(vm: Resolver.homeViewModel))
    }

    private var isEmpty: Bool {
        obs.state.ingredients.isEmpty && obs.state.symptoms.isEmpty
    }

    private var daysRemaining: Int32 {
        max(0, SuspectScoreComputer.shared.MIN_DAYS_FOR_SCORING - obs.state.daysLoggedTotal)
    }

    private var showsListening: Bool {
        obs.state.daysLoggedTotal > 0
            && obs.state.daysLoggedTotal < SuspectScoreComputer.shared.MIN_DAYS_FOR_SCORING
    }

    var body: some View {
        ZStack(alignment: .top) {
            Color(theme.background).ignoresSafeArea()

            VStack(spacing: CGFloat(AppDimens.shared.SpaceM)) {
                Color.clear.frame(height: CGFloat(AppDimens.shared.HeightControlLg))

                DateCard(
                    date: obs.state.selectedDate,
                    onPrevious: { obs.vm.onIntent(intent: HomeIntentPreviousDay()) },
                    onNext: { obs.vm.onIntent(intent: HomeIntentNextDay()) }
                )
                .padding(.horizontal, CGFloat(AppDimens.shared.SpaceM))

                if showsListening {
                    ListeningBanner(
                        message: MR.strings.shared.home_more_days_hint.localized(daysRemaining)
                    )
                    .padding(.horizontal, CGFloat(AppDimens.shared.SpaceM))
                }

                if isEmpty {
                    EmptyDayCard()
                        .padding(.horizontal, CGFloat(AppDimens.shared.SpaceM))
                    Spacer()
                } else {
                    DayList(
                        state: obs.state,
                        onDeleteIngredient: { entry in
                            obs.vm.onIntent(intent: HomeIntentDeleteIngredient(ingredient: entry.ingredient))
                        },
                        onDeleteSymptom: { symptom in
                            obs.vm.onIntent(intent: HomeIntentDeleteSymptom(symptom: symptom))
                        }
                    )
                }
            }

            VStack {
                GlassTopBar(eyebrow: MR.strings.shared.app_name.localized()) {
                    NavIconButton(
                        systemName: "plus",
                        accessibilityLabel: MR.strings.shared.a11y_add.localized()
                    ) { showAddSheet = true }
                }
                .padding(.horizontal, CGFloat(AppDimens.shared.SpaceM))
                .padding(.top, CGFloat(AppDimens.shared.SpaceS))
                Spacer()
            }
        }
        .toolbar(.hidden, for: .navigationBar)
        .sheet(isPresented: $showAddSheet) {
            AddChooserSheet(
                onAddIngredient: {
                    showAddSheet = false
                    obs.vm.onIntent(intent: HomeIntentAddIngredient())
                },
                onAddSymptom: {
                    showAddSheet = false
                    obs.vm.onIntent(intent: HomeIntentAddSymptom())
                },
                onCancel: { showAddSheet = false }
            )
            .presentationDetents([.height(addSheetDetentHeight)])
            .presentationDragIndicator(.visible)
            .presentationBackground(Color(theme.surface))
        }
        .onReceive(obs.events) { event in
            handleEvent(event)
        }
    }

    private func handleEvent(_ event: HomeEvent) {
        switch onEnum(of: event) {
        case .navigateToLogIngredient(let e):
            onNavigate(.logIngredient(date: e.date.toSwiftDate()))
        case .navigateToLogSymptom(let e):
            onNavigate(.logSymptom(date: e.date.toSwiftDate()))
        case .showError:
            break
        }
    }
}

// MARK: - Date card

private struct DateCard: View {
    let date: LocalDate
    let onPrevious: () -> Void
    let onNext: () -> Void

    @Environment(\.theme) private var theme

    var body: some View {
        Card {
            HStack(spacing: CGFloat(AppDimens.shared.SpaceS)) {
                NavIconButton(
                    systemName: "chevron.left",
                    accessibilityLabel: MR.strings.shared.a11y_previous_day.localized(),
                    action: onPrevious
                )

                VStack(alignment: .leading, spacing: CGFloat(AppDimens.shared.SpaceXS)) {
                    Eyebrow(text: weekdayLine)
                    Text(headlineLine)
                        .appTextStyle(AppTypography.shared.DisplayM)
                        .foregroundStyle(Color(theme.ink))
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.leading, CGFloat(AppDimens.shared.SpaceS))

                NavIconButton(
                    systemName: "chevron.right",
                    accessibilityLabel: MR.strings.shared.a11y_next_day.localized(),
                    action: onNext
                )
            }
        }
    }

    private var weekdayLine: String {
        let f = DateFormatter()
        f.dateFormat = "EEEE"
        return f.string(from: date.toSwiftDate())
    }

    private var headlineLine: String {
        let f = DateFormatter()
        f.dateFormat = "MMM d"
        return f.string(from: date.toSwiftDate())
    }
}

// MARK: - Empty state + listening banner

private struct EmptyDayCard: View {
    @Environment(\.theme) private var theme

    var body: some View {
        Card {
            VStack(alignment: .leading, spacing: CGFloat(AppDimens.shared.SpaceS)) {
                Eyebrow(text: MR.strings.shared.home_today_eyebrow.localized())
                Text(MR.strings.shared.home_empty_day.localized())
                    .appTextStyle(AppTypography.shared.BodyL)
                    .foregroundStyle(Color(theme.inkMuted))
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

private struct ListeningBanner: View {
    let message: String

    @Environment(\.theme) private var theme

    var body: some View {
        HStack(alignment: .top, spacing: CGFloat(AppDimens.shared.SpaceM)) {
            Eyebrow(text: MR.strings.shared.home_listening_eyebrow.localized())
            Text(message)
                .appTextStyle(AppTypography.shared.BodyM)
                .foregroundStyle(Color(theme.inkMuted))
                .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(.horizontal, CGFloat(AppDimens.shared.SpaceM))
        .padding(.vertical, CGFloat(AppDimens.shared.SpaceS) + 2)
        .overlay(
            RoundedRectangle(cornerRadius: CGFloat(AppDimens.shared.RadiusS), style: .continuous)
                .strokeBorder(
                    Color(theme.stroke),
                    // Dash pattern is a drawing recipe (on/off lengths), not a
                    // dimension expressible in the spacing scale. Kept inline.
                    style: StrokeStyle(lineWidth: CGFloat(AppDimens.shared.StrokeThin),
                                       dash: [4, 3])
                )
        )
    }
}

// MARK: - Day list (rows + sections)

private struct DayList: View {
    let state: HomeState
    let onDeleteIngredient: (IngredientWithDot) -> Void
    let onDeleteSymptom: (Symptom) -> Void

    @Environment(\.theme) private var theme

    var body: some View {
        List {
            if !state.ingredients.isEmpty {
                Section {
                    ForEach(state.ingredients, id: \.displayName) { entry in
                        IngredientRow(entry: entry)
                            .listRowBackground(Color(theme.surface))
                            .swipeActions {
                                Button(role: .destructive) { onDeleteIngredient(entry) } label: {
                                    Label("Delete", systemImage: "trash")
                                }
                            }
                    }
                } header: {
                    Eyebrow(text: MR.strings.shared.home_section_ingredients.localized())
                }
            }

            if !state.symptoms.isEmpty {
                Section {
                    ForEach(Array(state.symptoms), id: \.self) { symptom in
                        SymptomRow(symptom: symptom)
                            .listRowBackground(Color(theme.surface))
                            .swipeActions {
                                Button(role: .destructive) { onDeleteSymptom(symptom) } label: {
                                    Label("Delete", systemImage: "trash")
                                }
                            }
                    }
                } header: {
                    Eyebrow(text: MR.strings.shared.home_section_symptoms.localized())
                }
            }
        }
        .listStyle(.insetGrouped)
        .scrollContentBackground(.hidden)
    }
}

private struct IngredientRow: View {
    let entry: IngredientWithDot

    @Environment(\.theme) private var theme

    var body: some View {
        HStack(spacing: CGFloat(AppDimens.shared.SpaceM)) {
            SuspectDot(color: entry.dot, size: CGFloat(AppDimens.shared.SpaceL))
            Text(entry.displayName)
                .appTextStyle(AppTypography.shared.BodyL)
                .foregroundStyle(Color(theme.ink))
            Spacer()
        }
    }
}

private struct SymptomRow: View {
    let symptom: Symptom

    @Environment(\.theme) private var theme

    var body: some View {
        Text(symptom.localizedName)
            .appTextStyle(AppTypography.shared.BodyL)
            .foregroundStyle(Color(theme.ink))
    }
}

// MARK: - Add chooser sheet

private struct AddChooserSheet: View {
    let onAddIngredient: () -> Void
    let onAddSymptom: () -> Void
    let onCancel: () -> Void

    @Environment(\.theme) private var theme

    var body: some View {
        SheetSurface {
            VStack(alignment: .leading, spacing: CGFloat(AppDimens.shared.SpaceL)) {
                VStack(alignment: .leading, spacing: CGFloat(AppDimens.shared.SpaceXS)) {
                    Eyebrow(text: MR.strings.shared.home_add_eyebrow.localized())
                    Text(MR.strings.shared.home_add_question.localized())
                        .appTextStyle(AppTypography.shared.HeadingM)
                        .foregroundStyle(Color(theme.ink))
                }

                VStack(spacing: CGFloat(AppDimens.shared.SpaceS)) {
                    AppButton(
                        title: MR.strings.shared.home_add_ingredient.localized(),
                        kind: .primary,
                        size: .lg,
                        leadingSystemImage: "leaf",
                        action: onAddIngredient
                    )
                    .frame(maxWidth: .infinity)

                    AppButton(
                        title: MR.strings.shared.home_add_symptom.localized(),
                        kind: .secondary,
                        size: .lg,
                        leadingSystemImage: "waveform.path",
                        action: onAddSymptom
                    )
                    .frame(maxWidth: .infinity)
                }

                AppButton(
                    title: MR.strings.shared.common_cancel.localized(),
                    kind: .tertiary,
                    size: .md,
                    action: onCancel
                )
                .frame(maxWidth: .infinity)
            }
            .padding(.horizontal, CGFloat(AppDimens.shared.SpaceL))
            .padding(.top, CGFloat(AppDimens.shared.SpaceM))
            .padding(.bottom, CGFloat(AppDimens.shared.SpaceXL))
        }
    }
}
