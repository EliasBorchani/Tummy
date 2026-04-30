import SwiftUI
import TummyShared

@main
struct TummyApp: App {
    init() {
        // Démarre Koin (modules domaines + network + features).
        _ = TummyKoinKt.startTummyKoin { _ in }
    }

    var body: some Scene {
        WindowGroup {
            RootView()
        }
    }
}
