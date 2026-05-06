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

                VStack(alignment: .leading, spacing: CGFloat(AppDimens.shared.SpaceS)) {
                    EditorialDate(date: obs.state.selectedDate)
                        .padding(.horizontal, CGFloat(AppDimens.shared.SpaceM))

                    CalendarRibbon(
                        days: obs.state.ribbon,
                        selectedDate: obs.state.selectedDate,
                        onSelect: { date in
                            obs.vm.onIntent(intent: HomeIntentSelectDate(date: date))
                        }
                    )

                    Hairline()
                }

                if showsListening {
                    Banner(
                        eyebrow: MR.strings.shared.home_listening_eyebrow.localized(),
                        message: MR.strings.shared.home_more_days_hint.localized(daysRemaining)
                    )
                    .padding(.horizontal, CGFloat(AppDimens.shared.SpaceM))
                }

                Group {
                    if isEmpty {
                        VStack(spacing: 0) {
                            EmptyState(
                                headline: MR.strings.shared.home_empty_day.localized(),
                                message: MR.strings.shared.home_empty_day_body.localized(),
                                primary: .init(
                                    label: MR.strings.shared.home_add_ingredient.localized(),
                                    onTap: { obs.vm.onIntent(intent: HomeIntentAddIngredient()) }
                                ),
                                secondary: .init(
                                    label: MR.strings.shared.home_add_symptom.localized(),
                                    onTap: { obs.vm.onIntent(intent: HomeIntentAddSymptom()) }
                                )
                            )
                            Spacer()
                        }
                    } else {
                        DayList(
                            state: obs.state,
                            onDeleteIngredient: { entry in
                                obs.vm.onIntent(intent: HomeIntentDeleteIngredient(ingredient: entry.ingredient))
                            },
                            onDeleteSymptom: { symptom in
                                obs.vm.onIntent(intent: HomeIntentDeleteSymptom(symptom: symptom))
                            },
                            onAddSymptom: {
                                obs.vm.onIntent(intent: HomeIntentAddSymptom())
                            }
                        )
                    }
                }
                .paginatedSwipe(
                    onPrevious: { obs.vm.onIntent(intent: HomeIntentPreviousDay()) },
                    onNext: { obs.vm.onIntent(intent: HomeIntentNextDay()) }
                )
                .animation(Animation(AppMotion.shared.Swipe), value: obs.state.selectedDate)
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

// MARK: - Editorial date

private struct EditorialDate: View {
    let date: LocalDate

    @Environment(\.theme) private var theme

    var body: some View {
        VStack(alignment: .leading, spacing: CGFloat(AppDimens.shared.SpaceXS)) {
            Eyebrow(text: eyebrowLine)
            Text(headlineLine)
                .appTextStyle(AppTypography.shared.DisplayL)
                .foregroundStyle(Color(theme.ink))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private var eyebrowLine: String {
        // "Today · 05/05" when looking at today; "Tuesday · 05/05" otherwise.
        let weekday: String
        if Calendar.current.isDateInToday(date.toSwiftDate()) {
            weekday = MR.strings.shared.home_today_eyebrow.localized()
        } else {
            let f = DateFormatter()
            f.dateFormat = "EEEE"
            weekday = f.string(from: date.toSwiftDate())
        }
        let f = DateFormatter()
        f.dateFormat = "MM/dd"
        return "\(weekday) · \(f.string(from: date.toSwiftDate()))"
    }

    private var headlineLine: String {
        let f = DateFormatter()
        f.dateFormat = "MMMM d"
        return f.string(from: date.toSwiftDate())
    }
}

// MARK: - Day list (rows + sections)

private struct DayList: View {
    let state: HomeState
    let onDeleteIngredient: (IngredientWithDot) -> Void
    let onDeleteSymptom: (Symptom) -> Void
    let onAddSymptom: () -> Void

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
                    SectionHeader(
                        label: MR.strings.shared.home_section_ingredients.localized(),
                        count: state.ingredients.count
                    )
                }
            }

            // Symptoms always render — the cluster includes the "+" add chip
            // so users can log a symptom even on a day with none yet.
            Section {
                WrapLayout(
                    hSpacing: CGFloat(AppDimens.shared.SpaceS),
                    vSpacing: CGFloat(AppDimens.shared.SpaceS)
                ) {
                    ForEach(Array(state.symptoms), id: \.self) { symptom in
                        Chip(
                            label: symptom.localizedName,
                            indicator: Color(theme.signalHigh),
                            onTap: { onDeleteSymptom(symptom) }
                        )
                    }
                    Chip(
                        label: MR.strings.shared.home_add_symptom_chip.localized(),
                        style: .dashed,
                        leadingSystemImage: "plus",
                        onTap: onAddSymptom
                    )
                }
                .padding(.vertical, CGFloat(AppDimens.shared.SpaceS))
                .listRowInsets(EdgeInsets(
                    top: 0,
                    leading: CGFloat(AppDimens.shared.SpaceM),
                    bottom: 0,
                    trailing: CGFloat(AppDimens.shared.SpaceM)
                ))
                .listRowBackground(Color.clear)
                .listRowSeparator(.hidden)
            } header: {
                SectionHeader(
                    label: MR.strings.shared.home_section_symptoms.localized(),
                    count: state.symptoms.count
                )
            }
        }
        .listStyle(.insetGrouped)
        .scrollContentBackground(.hidden)
    }
}

private struct IngredientRow: View {
    let entry: IngredientWithDot

    var body: some View {
        LedgerRow(label: entry.displayName, meta: occurrenceLabel) {
            SuspectDot(color: entry.dot, size: CGFloat(AppDimens.shared.SpaceL))
        }
    }

    private var occurrenceLabel: String {
        MR.plurals.shared.ingredient_occurrences.localized(Int(entry.occurrenceCount))
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
