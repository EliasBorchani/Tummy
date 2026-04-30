import Combine
import SwiftUI
import TummyShared

@MainActor
final class MealObservable: ObservableObject {
    @Published private(set) var state: MealState = MealState(isLoading: false, meals: [], error: nil)
    let vm: MealViewModel

    init(vm: MealViewModel) {
        self.vm = vm
        // SKIE expose StateFlow<MealState> comme AsyncSequence.
        Task { [weak self] in
            guard let self else { return }
            for await s in vm.state {
                self.state = s
            }
        }
    }
}

struct MealView: View {
    @StateObject private var obs: MealObservable
    let onOpenDetail: (String) -> Void

    init(onOpenDetail: @escaping (String) -> Void) {
        self.onOpenDetail = onOpenDetail
        self._obs = StateObject(wrappedValue: MealObservable(vm: Resolver.mealViewModel))
    }

    var body: some View {
        List(obs.state.meals, id: \.id.raw) { meal in
            Button {
                obs.vm.onIntent(intent: MealIntentSelect(id: meal.id))
            } label: {
                HStack {
                    Text(meal.name)
                    Spacer()
                    Text("\(meal.calories) kcal").foregroundStyle(.secondary)
                }
            }
        }
        .overlay {
            if obs.state.isLoading { ProgressView() }
        }
        .navigationTitle("Meals")
        .task {
            // Consomme les events one-shot (navigation)
            for await event in obs.vm.events {
                if let nav = event as? MealEventNavigateToDetail {
                    onOpenDetail(nav.id.raw)
                }
            }
        }
    }
}
