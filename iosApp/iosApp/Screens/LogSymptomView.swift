import Combine
import SwiftUI
import TummyShared

@MainActor
final class LogSymptomObservable: ObservableObject {
    @Published private(set) var state: LogSymptomState
    let events = PassthroughSubject<LogSymptomEvent, Never>()
    let vm: LogSymptomViewModel

    init(vm: LogSymptomViewModel) {
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

struct LogSymptomView: View {
    let date: Date
    let onClose: () -> Void
    @StateObject private var obs: LogSymptomObservable

    init(date: Date, onClose: @escaping () -> Void) {
        self.date = date
        self.onClose = onClose
        self._obs = StateObject(wrappedValue: LogSymptomObservable(
            vm: Resolver.logSymptomViewModel(date: date.toKotlinLocalDate())
        ))
    }

    var body: some View {
        List {
            ForEach(allSymptoms, id: \.self) { symptom in
                Toggle(
                    "\(symptom.name)",
                    isOn: Binding(
                        get: { obs.state.activeSymptoms.contains(symptom) },
                        set: { _ in obs.vm.onIntent(intent: LogSymptomIntentToggle(symptom: symptom)) }
                    )
                )
            }
        }
        .navigationTitle("Symptoms")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button("Done") { obs.vm.onIntent(intent: LogSymptomIntentDone()) }
            }
        }
        .onReceive(obs.events) { event in
            switch onEnum(of: event) {
            case .closed:
                onClose()
            case .showError:
                break
            }
        }
    }

    private var allSymptoms: [Symptom] {
        [.bloating, .abdominalPain, .gas, .diarrhea, .constipation, .nausea]
    }
}
