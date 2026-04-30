import Combine
import SwiftUI
import TummyShared

@MainActor
final class SettingsObservable: ObservableObject {
    @Published private(set) var state: SettingsState = SettingsState(user: nil)
    let vm: SettingsViewModel

    init(vm: SettingsViewModel) {
        self.vm = vm
        Task { [weak self] in
            guard let self else { return }
            for await s in vm.state { self.state = s }
        }
    }
}

struct SettingsView: View {
    @StateObject private var obs: SettingsObservable

    init() {
        self._obs = StateObject(wrappedValue: SettingsObservable(vm: Resolver.settingsViewModel))
    }

    var body: some View {
        VStack(spacing: 16) {
            Text("Hello \(obs.state.user?.displayName ?? "—")")
            Text("Daily goal: \(obs.state.user?.dailyCalorieGoal ?? 0) kcal")
        }
        .padding()
        .navigationTitle("Settings")
    }
}
