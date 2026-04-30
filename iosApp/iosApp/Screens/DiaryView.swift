import Combine
import SwiftUI
import TummyShared

@MainActor
final class DiaryObservable: ObservableObject {
    @Published private(set) var state: DiaryState = DiaryState(user: nil, meals: [], caloriesConsumed: 0, caloriesRemaining: 0)
    let vm: DiaryViewModel

    init(vm: DiaryViewModel) {
        self.vm = vm
        Task { [weak self] in
            guard let self else { return }
            for await s in vm.state { self.state = s }
        }
    }
}

struct DiaryView: View {
    @StateObject private var obs: DiaryObservable
    let onOpenMeals: () -> Void

    init(onOpenMeals: @escaping () -> Void) {
        self.onOpenMeals = onOpenMeals
        self._obs = StateObject(wrappedValue: DiaryObservable(vm: Resolver.diaryViewModel))
    }

    var body: some View {
        VStack(spacing: 16) {
            Text("Consumed: \(obs.state.caloriesConsumed) kcal")
            Text("Remaining: \(obs.state.caloriesRemaining) kcal")
            Button("Open meals", action: onOpenMeals)
        }
        .padding()
        .navigationTitle("Diary")
    }
}
