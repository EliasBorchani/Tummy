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

    @Environment(\.theme) private var theme

    var body: some View {
        ZStack {
            Color(theme.background).ignoresSafeArea()

            VStack(alignment: .leading, spacing: 0) {
                navBar
                    .padding(.horizontal, CGFloat(AppDimens.shared.SpaceM))

                editorialHeader
                    .padding(.horizontal, CGFloat(AppDimens.shared.SpaceM))
                    .padding(.top, CGFloat(AppDimens.shared.SpaceM))

                tilesGrid
                    .padding(.horizontal, CGFloat(AppDimens.shared.SpaceM))
                    .padding(.top, CGFloat(AppDimens.shared.SpaceL))

                Spacer(minLength: CGFloat(AppDimens.shared.SpaceL))

                footer
                    .padding(.horizontal, CGFloat(AppDimens.shared.SpaceM))
                    .padding(.bottom, CGFloat(AppDimens.shared.SpaceM))
            }
        }
        .toolbar(.hidden, for: .navigationBar)
        .onReceive(obs.events) { event in
            switch onEnum(of: event) {
            case .closed:
                onClose()
            case .showError:
                break
            }
        }
    }

    // MARK: - Sub-views

    private var navBar: some View {
        HStack(spacing: CGFloat(AppDimens.shared.SpaceS)) {
            NavIconButton(
                systemName: "chevron.left",
                accessibilityLabel: MR.strings.shared.a11y_back.localized(),
                action: onClose
            )
            Spacer()
            Eyebrow(text: navEyebrow)
            Spacer()
            AppButton(
                title: MR.strings.shared.log_symptom_done.localized(),
                kind: .primary,
                size: .sm,
                action: { obs.vm.onIntent(intent: LogSymptomIntentDone()) }
            )
        }
        .padding(.top, CGFloat(AppDimens.shared.SpaceS))
    }

    private var editorialHeader: some View {
        VStack(alignment: .leading, spacing: CGFloat(AppDimens.shared.SpaceS)) {
            Text(MR.strings.shared.log_symptom_question.localized())
                .appTextStyle(AppTypography.shared.DisplayM)
                .foregroundStyle(Color(theme.ink))
                .fixedSize(horizontal: false, vertical: true)
            Text(MR.strings.shared.log_symptom_subtitle.localized())
                .appTextStyle(AppTypography.shared.BodyM)
                .foregroundStyle(Color(theme.inkMuted))
                .fixedSize(horizontal: false, vertical: true)
        }
    }

    private var tilesGrid: some View {
        LazyVGrid(
            columns: [
                GridItem(.flexible(), spacing: CGFloat(AppDimens.shared.SpaceS)),
                GridItem(.flexible(), spacing: CGFloat(AppDimens.shared.SpaceS)),
            ],
            spacing: CGFloat(AppDimens.shared.SpaceS)
        ) {
            ForEach(Symptom.allCases, id: \.self) { symptom in
                ToggleTile(
                    label: symptom.localizedName,
                    hint: symptom.localizedHint,
                    onEyebrow: MR.strings.shared.log_symptom_eyebrow_logged.localized(),
                    offEyebrow: MR.strings.shared.log_symptom_eyebrow_off.localized(),
                    isOn: obs.state.activeSymptoms.contains(symptom),
                    onToggle: { obs.vm.onIntent(intent: LogSymptomIntentToggle(symptom: symptom)) }
                )
            }
        }
    }

    private var footer: some View {
        VStack(spacing: 0) {
            Hairline()
            HStack {
                Eyebrow(text: countLine)
                Spacer()
                AppButton(
                    title: MR.strings.shared.log_symptom_clear_all.localized(),
                    kind: .tertiary,
                    size: .sm,
                    action: { obs.vm.onIntent(intent: LogSymptomIntentClearAll()) }
                )
                .disabled(obs.state.activeSymptoms.isEmpty)
            }
            .padding(.top, CGFloat(AppDimens.shared.SpaceM))
        }
    }

    // MARK: - Helpers

    private var navEyebrow: String {
        let title = MR.strings.shared.log_symptom_title.localized()
        let f = DateFormatter()
        f.dateFormat = "MMM d"
        return "\(title) · \(f.string(from: date))"
    }

    private var countLine: String {
        MR.strings.shared.log_symptom_logged_count
            .localized(Int32(obs.state.activeSymptoms.count), Int32(Symptom.allCases.count))
    }
}
