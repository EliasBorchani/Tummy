import SwiftUI
import TummyShared

@main
struct TummyApp: App {
    init() {
        // Start Koin
        _ = TummyKoinKt.startTummyKoin { _ in }
    }

    var body: some Scene {
        WindowGroup {
            RootView()
        }
    }
}
