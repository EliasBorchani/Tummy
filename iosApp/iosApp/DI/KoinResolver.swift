import Foundation
import TummyShared

/// Façade Swift sur les getters Koin typés exposés par `TummyDI`.
/// Chaque VM est résolu via un appel direct, typé, sans reflection.
enum Resolver {
    static var mealViewModel: MealViewModel { TummyDI.shared.mealViewModel() }
    static var diaryViewModel: DiaryViewModel { TummyDI.shared.diaryViewModel() }
    static var settingsViewModel: SettingsViewModel { TummyDI.shared.settingsViewModel() }

    static var homeViewModel: HomeViewModel { TummyDI.shared.homeViewModel() }

    static func logIngredientViewModel(date: LocalDate) -> LogIngredientViewModel {
        TummyDI.shared.logIngredientViewModel(date: date)
    }

    static func logSymptomViewModel(date: LocalDate) -> LogSymptomViewModel {
        TummyDI.shared.logSymptomViewModel(date: date)
    }
}
