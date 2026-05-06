import Combine
import SwiftUI
import TummyShared

@MainActor
final class LogIngredientObservable: ObservableObject {
    @Published private(set) var state: LogIngredientState
    let events = PassthroughSubject<LogIngredientEvent, Never>()
    let vm: LogIngredientViewModel

    init(vm: LogIngredientViewModel) {
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

struct LogIngredientView: View {
    let date: Date
    let onClose: () -> Void
    @StateObject private var obs: LogIngredientObservable

    init(date: Date, onClose: @escaping () -> Void) {
        self.date = date
        self.onClose = onClose
        self._obs = StateObject(wrappedValue: LogIngredientObservable(
            vm: Resolver.logIngredientViewModel(date: date.toKotlinLocalDate())
        ))
    }

    var body: some View {
        VStack(spacing: 0) {
            TextField(
                MR.strings.shared.log_ingredient_search_hint.localized(),
                text: Binding(
                    get: { obs.state.query },
                    set: { obs.vm.onIntent(intent: LogIngredientIntentQueryChanged(text: $0)) }
                )
            )
            .textFieldStyle(.roundedBorder)
            .padding()

            List {
                ForEach(obs.state.suggestions, id: \.displayName) { sug in
                    Button(action: {
                        obs.vm.onIntent(intent: LogIngredientIntentSelectSuggestion(suggestion: sug))
                    }) {
                        Text(sug.displayName).foregroundStyle(.primary)
                    }
                }

                if !obs.state.query.isEmpty && !obs.state.isSearching && !hasExactMatch {
                    Button(action: { obs.vm.onIntent(intent: LogIngredientIntentSaveAsCustom()) }) {
                        Text(MR.strings.shared.log_ingredient_save_as_new.localized(obs.state.query.trimmingCharacters(in: .whitespaces)))
                            .foregroundStyle(.primary)
                    }
                }
            }
        }
        .navigationTitle(MR.strings.shared.log_ingredient_title.localized())
        .navigationBarTitleDisplayMode(.inline)
        .onReceive(obs.events) { event in
            switch onEnum(of: event) {
            case .saved:
                onClose()
            case .showError:
                break
            }
        }
    }

    private var hasExactMatch: Bool {
        let trimmed = obs.state.query.trimmingCharacters(in: .whitespaces).lowercased()
        return obs.state.suggestions.contains { $0.displayName.lowercased() == trimmed }
    }
}
