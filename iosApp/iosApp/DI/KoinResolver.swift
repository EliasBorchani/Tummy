import Foundation
import TummyShared

/// Façade Swift sur les getters Koin typés exposés par `TummyDI`.
/// Chaque VM est résolu via un appel direct, typé, sans reflection.
enum Resolver {
    static var mealViewModel: MealViewModel { TummyDI.shared.mealViewModel() }
    static var diaryViewModel: DiaryViewModel { TummyDI.shared.diaryViewModel() }
    static var settingsViewModel: SettingsViewModel { TummyDI.shared.settingsViewModel() }
}
