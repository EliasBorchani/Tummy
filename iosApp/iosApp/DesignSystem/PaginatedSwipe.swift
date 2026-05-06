import SwiftUI
import TummyShared

// Gesture recipe — the translation/velocity thresholds that distinguish a
// "commit the page change" flick from any other horizontal gesture in the
// hierarchy (most notably List's row swipe-to-delete, which acts at ~60pt).
// These are tuned together with the gesture's minimum-distance below; they
// live alongside the modifier so the recipe reads in one place.
private let commitMinTranslation: CGFloat = 80
private let commitMinVelocity: CGFloat = 250

/// Horizontal page-flick gesture. Swipe left → `onNext`; swipe right →
/// `onPrevious`. Attached as `simultaneousGesture` so child views (Lists with
/// row swipe actions, horizontal `ScrollView`s, etc.) keep their own
/// gestures — short drags reach those first; only a committed flick past the
/// thresholds triggers page navigation.
///
/// Pair with `.animation(Animation(AppMotion.shared.Swipe), value: …)` on
/// the same content to cross-fade between pages.
struct PaginatedSwipe: ViewModifier {
    let onPrevious: () -> Void
    let onNext: () -> Void

    private var gesture: some Gesture {
        DragGesture(minimumDistance: CGFloat(AppDimens.shared.SpaceL))
            .onEnded { value in
                let translation = value.translation.width
                let velocity = value.predictedEndTranslation.width
                let committed = abs(translation) >= commitMinTranslation
                    || abs(velocity) >= commitMinVelocity
                guard committed else { return }
                if translation < 0 {
                    onNext()
                } else {
                    onPrevious()
                }
            }
    }

    func body(content: Content) -> some View {
        content.simultaneousGesture(gesture)
    }
}

extension View {
    func paginatedSwipe(
        onPrevious: @escaping () -> Void,
        onNext: @escaping () -> Void
    ) -> some View {
        modifier(PaginatedSwipe(onPrevious: onPrevious, onNext: onNext))
    }
}
