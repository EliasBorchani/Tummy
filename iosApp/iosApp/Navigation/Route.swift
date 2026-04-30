import Foundation
import TummyShared

enum Route: Hashable {
    case meal
    case mealDetail(id: String)
    case settings
}
