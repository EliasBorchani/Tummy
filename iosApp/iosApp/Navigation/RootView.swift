import SwiftUI
import TummyShared

struct RootView: View {
    @State private var path = NavigationPath()

    var body: some View {
        NavigationStack(path: $path) {
            HomeView(onNavigate: { route in path.append(route) })
                .navigationDestination(for: Route.self) { route in
                    switch route {
                    case .logIngredient(let date):
                        LogIngredientView(date: date, onClose: { path.removeLast() })
                    case .logSymptom(let date):
                        LogSymptomView(date: date, onClose: { path.removeLast() })
                    }
                }
        }
    }
}
