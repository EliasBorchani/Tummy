import Foundation
import TummyShared

/// Swift Bridge on Koin getters from `TummyDI`.
enum Resolver {
    static var homeViewModel: HomeViewModel { TummyDI.shared.homeViewModel() }

    static func logIngredientViewModel(date: LocalDate) -> LogIngredientViewModel {
        TummyDI.shared.logIngredientViewModel(date: date)
    }

    static func logSymptomViewModel(date: LocalDate) -> LogSymptomViewModel {
        TummyDI.shared.logSymptomViewModel(date: date)
    }
}
