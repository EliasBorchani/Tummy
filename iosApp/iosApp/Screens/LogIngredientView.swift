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

    @Environment(\.theme) private var theme

    var body: some View {
        ZStack {
            Color(theme.background).ignoresSafeArea()

            VStack(alignment: .leading, spacing: CGFloat(AppDimens.shared.SpaceM)) {
                navBar
                    .padding(.horizontal, CGFloat(AppDimens.shared.SpaceM))

                searchField
                    .padding(.horizontal, CGFloat(AppDimens.shared.SpaceM))

                if let dym = didYouMean {
                    Banner(
                        eyebrow: MR.strings.shared.log_ingredient_did_you_mean.localized(),
                        message: dym.displayName,
                        action: .init(
                            label: MR.strings.shared.log_ingredient_use.localized(),
                            onTap: {
                                obs.vm.onIntent(intent: LogIngredientIntentSelectSuggestion(suggestion: dym))
                            }
                        )
                    )
                    .padding(.horizontal, CGFloat(AppDimens.shared.SpaceM))
                }

                resultsList

                if showsCustomAdd {
                    AddCustomButton(query: trimmedQuery) {
                        obs.vm.onIntent(intent: LogIngredientIntentSaveAsCustom())
                    }
                    .padding(.horizontal, CGFloat(AppDimens.shared.SpaceM))
                    .padding(.bottom, CGFloat(AppDimens.shared.SpaceM))
                }
            }
        }
        .toolbar(.hidden, for: .navigationBar)
        .onReceive(obs.events) { event in
            switch onEnum(of: event) {
            case .saved:
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
            Eyebrow(text: MR.strings.shared.log_ingredient_title.localized())
            Spacer()
            AppButton(
                title: MR.strings.shared.common_cancel.localized(),
                kind: .tertiary,
                size: .sm,
                action: onClose
            )
        }
        .padding(.top, CGFloat(AppDimens.shared.SpaceS))
    }

    private var searchField: some View {
        AppSearchField(
            text: Binding(
                get: { obs.state.query },
                set: { obs.vm.onIntent(intent: LogIngredientIntentQueryChanged(text: $0)) }
            ),
            placeholder: MR.strings.shared.log_ingredient_search_hint.localized(),
            resultsLabel: resultsLabel
        )
    }

    private var resultsLabel: String? {
        guard !obs.state.query.isEmpty else { return nil }
        return MR.plurals.shared.ingredient_search_results
            .localized(Int(obs.state.suggestions.count))
    }

    @ViewBuilder
    private var resultsList: some View {
        let curated = obs.state.suggestions.filter { $0.isCurated }
        let custom = obs.state.suggestions.filter { !$0.isCurated }

        List {
            if !curated.isEmpty {
                Section {
                    ForEach(curated, id: \.displayName) { suggestion in
                        SearchRow(suggestion: suggestion, query: obs.state.query)
                            .listRowBackground(Color(theme.surface))
                            .onTapGesture { select(suggestion) }
                    }
                } header: {
                    SectionHeader(
                        label: MR.strings.shared.log_ingredient_curated_section.localized(),
                        count: curated.count
                    )
                }
            }

            if !custom.isEmpty {
                Section {
                    ForEach(custom, id: \.displayName) { suggestion in
                        SearchRow(suggestion: suggestion, query: obs.state.query)
                            .listRowBackground(Color(theme.surface))
                            .onTapGesture { select(suggestion) }
                    }
                } header: {
                    SectionHeader(
                        label: MR.strings.shared.log_ingredient_custom_section.localized(),
                        count: custom.count
                    )
                }
            }
        }
        .listStyle(.plain)
        .scrollContentBackground(.hidden)
    }

    // MARK: - Helpers

    private var trimmedQuery: String {
        obs.state.query.trimmingCharacters(in: .whitespaces)
    }

    private var hasExactMatch: Bool {
        let q = trimmedQuery.lowercased()
        return obs.state.suggestions.contains { $0.displayName.lowercased() == q }
    }

    private var showsCustomAdd: Bool {
        !trimmedQuery.isEmpty && !obs.state.isSearching && !hasExactMatch
    }

    /// Best curated match for the typed query when the user hasn't already
    /// hit it exactly — the hook for the "Did you mean Eggs?" banner.
    private var didYouMean: IngredientSuggestion? {
        let q = trimmedQuery.lowercased()
        guard !q.isEmpty, !hasExactMatch else { return nil }
        return obs.state.suggestions.first {
            $0.isCurated && $0.displayName.lowercased() != q
        }
    }

    private func select(_ suggestion: IngredientSuggestion) {
        obs.vm.onIntent(intent: LogIngredientIntentSelectSuggestion(suggestion: suggestion))
    }
}

// MARK: - Search row

private struct SearchRow: View {
    let suggestion: IngredientSuggestion
    let query: String

    @Environment(\.theme) private var theme

    var body: some View {
        HStack(spacing: CGFloat(AppDimens.shared.SpaceM)) {
            kindGlyph
            highlightedLabel
                .frame(maxWidth: .infinity, alignment: .leading)
            kindBadge
        }
        .padding(.vertical, CGFloat(AppDimens.shared.SpaceS))
        .contentShape(Rectangle())
    }

    private var kindGlyph: some View {
        let glyph = suggestion.isCurated ? "·" : "✦"
        return Text(glyph)
            .appTextStyle(AppTypography.shared.Mono)
            .foregroundStyle(Color(theme.inkFaint))
            .frame(
                width: CGFloat(AppDimens.shared.SpaceL),
                height: CGFloat(AppDimens.shared.SpaceL)
            )
            .overlay(
                RoundedRectangle(cornerRadius: CGFloat(AppDimens.shared.RadiusXS),
                                 style: .continuous)
                    .strokeBorder(Color(theme.hairline),
                                  lineWidth: CGFloat(AppDimens.shared.StrokeThin))
            )
    }

    private var highlightedLabel: some View {
        Text(attributedLabel)
            .appTextStyle(AppTypography.shared.BodyL)
            .foregroundStyle(Color(theme.ink))
    }

    /// Highlights the matched substring in the suggestion's displayName by
    /// raising it to SemiBold; falls back to plain text when no match.
    private var attributedLabel: AttributedString {
        var result = AttributedString(suggestion.displayName)
        let q = query.trimmingCharacters(in: .whitespaces)
        guard !q.isEmpty else { return result }
        if let range = result.range(of: q, options: [.caseInsensitive]) {
            result[range].font = .system(
                size: CGFloat(AppTypography.shared.BodyL.sizeSp),
                weight: .semibold
            )
            result[range].foregroundColor = Color(theme.ink)
        }
        return result
    }

    private var kindBadge: some View {
        let key = suggestion.isCurated
            ? MR.strings.shared.log_ingredient_curated_kind
            : MR.strings.shared.log_ingredient_custom_kind
        return Text(key.localized().uppercased())
            .appTextStyle(AppTypography.shared.Eyebrow)
            .foregroundStyle(Color(theme.inkFaint))
    }
}

// MARK: - Add-custom button

private struct AddCustomButton: View {
    let query: String
    let onTap: () -> Void

    @Environment(\.theme) private var theme

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: CGFloat(AppDimens.shared.SpaceS)) {
                plusGlyph
                VStack(alignment: .leading, spacing: 0) {
                    Text(MR.strings.shared.log_ingredient_save_as_new.localized(query))
                        .appTextStyle(AppTypography.shared.BodyL)
                        .fontWeight(.semibold)
                        .foregroundStyle(Color(theme.ink))
                    Text(MR.strings.shared.log_ingredient_save_as_custom_hint.localized())
                        .appTextStyle(AppTypography.shared.Mono)
                        .foregroundStyle(Color(theme.inkMuted))
                }
                Spacer()
                Text("↵")
                    .appTextStyle(AppTypography.shared.Mono)
                    .foregroundStyle(Color(theme.inkFaint))
            }
            .padding(CGFloat(AppDimens.shared.SpaceM))
            .frame(maxWidth: .infinity, alignment: .leading)
            .overlay(
                RoundedRectangle(cornerRadius: CGFloat(AppDimens.shared.RadiusM),
                                 style: .continuous)
                    .strokeBorder(
                        Color(theme.stroke),
                        // Same dashed rhythm as the Banner / +Symptom chip.
                        style: StrokeStyle(
                            lineWidth: CGFloat(AppDimens.shared.StrokeThick),
                            dash: [4, 3]
                        )
                    )
            )
        }
        .buttonStyle(PressedScaleButtonStyle())
    }

    private var plusGlyph: some View {
        Image(systemName: "plus")
            .font(.system(size: CGFloat(AppTypography.shared.BodyM.sizeSp), weight: .semibold))
            .foregroundStyle(Color(theme.ink))
            .frame(
                width: CGFloat(AppDimens.shared.SpaceL),
                height: CGFloat(AppDimens.shared.SpaceL)
            )
            .overlay(
                RoundedRectangle(cornerRadius: CGFloat(AppDimens.shared.RadiusXS),
                                 style: .continuous)
                    .strokeBorder(Color(theme.ink),
                                  lineWidth: CGFloat(AppDimens.shared.StrokeThick))
            )
    }
}

// MARK: - Suggestion kind helper

private extension IngredientSuggestion {
    /// `true` when the suggestion is from the curated standard list, `false`
    /// when it's a previously-saved custom ingredient. Branches off the
    /// SKIE-bridged sealed `Ingredient` enum.
    var isCurated: Bool {
        switch onEnum(of: ref) {
        case .standard: return true
        case .custom: return false
        }
    }
}
