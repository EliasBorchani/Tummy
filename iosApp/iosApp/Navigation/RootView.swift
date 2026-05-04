import SwiftUI
import TummyShared

struct RootView: View {
    @State private var path = NavigationPath()

    var body: some View {
        NavigationStack(path: $path) {
            HomeView(onNavigate: { route in path.append(route) })
                .navigationDestination(for: Route.self) { route in
                    switch route {
                    case .meal:
                        MealView(onOpenDetail: { id in path.append(Route.mealDetail(id: id)) })
                    case .mealDetail(let id):
                        Text("Meal detail: \(id)")
                    case .settings:
                        SettingsView()
                    case .logIngredient(let date):
                        LogIngredientView(date: date, onClose: { path.removeLast() })
                    case .logSymptom(let date):
                        LogSymptomView(date: date, onClose: { path.removeLast() })
                    }
                }
        }
    }
}
