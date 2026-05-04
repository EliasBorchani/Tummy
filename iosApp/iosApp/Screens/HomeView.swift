import Combine
import SwiftUI
import TummyShared

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
    @State private var showActionSheet: Bool = false

    init(onNavigate: @escaping (Route) -> Void) {
        self.onNavigate = onNavigate
        self._obs = StateObject(wrappedValue: HomeObservable(vm: Resolver.homeViewModel))
    }

    var body: some View {
        VStack(spacing: 0) {
            DateHeader(
                date: obs.state.selectedDate,
                onPrevious: { obs.vm.onIntent(intent: HomeIntentPreviousDay()) },
                onNext: { obs.vm.onIntent(intent: HomeIntentNextDay()) }
            )

            DayContent(
                state: obs.state,
                onDeleteIngredient: { entry in
                    obs.vm.onIntent(intent: HomeIntentDeleteIngredient(ingredient: entry.ingredient))
                },
                onDeleteSymptom: { symptom in
                    obs.vm.onIntent(intent: HomeIntentDeleteSymptom(symptom: symptom))
                }
            )
        }
        .navigationTitle("Tummy")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button(action: { showActionSheet = true }) {
                    Image(systemName: "plus")
                }
            }
        }
        .confirmationDialog("Add", isPresented: $showActionSheet, titleVisibility: .hidden) {
            Button("Add ingredient") {
                obs.vm.onIntent(intent: HomeIntentAddIngredient())
            }
            Button("Add symptom") {
                obs.vm.onIntent(intent: HomeIntentAddSymptom())
            }
            Button("Cancel", role: .cancel) {}
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

private struct DateHeader: View {
    let date: LocalDate
    let onPrevious: () -> Void
    let onNext: () -> Void

    var body: some View {
        HStack {
            Button(action: onPrevious) { Image(systemName: "chevron.left") }
            Spacer()
            Text("\(date)")
                .font(.headline)
            Spacer()
            Button(action: onNext) { Image(systemName: "chevron.right") }
        }
        .padding()
    }
}

private struct DayContent: View {
    let state: HomeState
    let onDeleteIngredient: (IngredientWithDot) -> Void
    let onDeleteSymptom: (Symptom) -> Void

    private var isEmpty: Bool {
        state.ingredients.isEmpty && state.symptoms.isEmpty
    }

    private var daysRemaining: Int32 {
        max(0, SuspectScoreComputer.shared.MIN_DAYS_FOR_SCORING - state.daysLoggedTotal)
    }

    var body: some View {
        if isEmpty {
            VStack {
                Spacer()
                Text("Nothing logged for this day. Tap + to start.")
                    .multilineTextAlignment(.center)
                    .foregroundStyle(.secondary)
                    .padding()
                Spacer()
            }
        } else {
            List {
                if state.daysLoggedTotal > 0 && state.daysLoggedTotal < SuspectScoreComputer.shared.MIN_DAYS_FOR_SCORING {
                    Section {
                        Text("\(daysRemaining) more days of logging to start seeing patterns.")
                            .font(.footnote)
                            .foregroundStyle(.secondary)
                    }
                }

                if !state.ingredients.isEmpty {
                    Section("Ingredients") {
                        ForEach(state.ingredients, id: \.displayName) { entry in
                            IngredientRow(entry: entry, onDelete: { onDeleteIngredient(entry) })
                        }
                    }
                }

                if !state.symptoms.isEmpty {
                    Section("Symptoms") {
                        ForEach(Array(state.symptoms), id: \.self) { symptom in
                            SymptomRow(symptom: symptom, onDelete: { onDeleteSymptom(symptom) })
                        }
                    }
                }
            }
        }
    }
}

private struct IngredientRow: View {
    let entry: IngredientWithDot
    let onDelete: () -> Void

    var body: some View {
        HStack {
            Text(entry.displayName)
            Spacer()
            DotIndicator(color: entry.dot)
        }
        .swipeActions {
            Button(role: .destructive, action: onDelete) { Image(systemName: "trash") }
        }
    }
}

private struct SymptomRow: View {
    let symptom: Symptom
    let onDelete: () -> Void

    var body: some View {
        HStack { Text("\(symptom.name)") }
            .swipeActions {
                Button(role: .destructive, action: onDelete) { Image(systemName: "trash") }
            }
    }
}

private struct DotIndicator: View {
    let color: DotColor

    var body: some View {
        Circle()
            .fill(swiftColor)
            .frame(width: 12, height: 12)
    }

    private var swiftColor: Color {
        switch color {
        case .grey: return .gray
        case .green: return .green
        case .yellow: return .yellow
        case .red: return .red
        @unknown default: return .gray
        }
    }
}
