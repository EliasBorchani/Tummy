import SwiftUI
import TummyShared

struct RootView: View {
    @State private var path = NavigationPath()

    var body: some View {
        NavigationStack(path: $path) {
            DiaryView(onOpenMeals: { path.append(Route.meal) })
                .navigationDestination(for: Route.self) { route in
                    switch route {
                    case .meal:
                        MealView(onOpenDetail: { id in path.append(Route.mealDetail(id: id)) })
                    case .mealDetail(let id):
                        Text("Meal detail: \(id)")
                    case .settings:
                        SettingsView()
                    }
                }
                .toolbar {
                    ToolbarItem(placement: .topBarTrailing) {
                        Button("Settings") { path.append(Route.settings) }
                    }
                }
        }
    }
}
