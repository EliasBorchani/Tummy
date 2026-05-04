import Foundation
import TummyShared

enum Route: Hashable {
    case logIngredient(date: Date)
    case logSymptom(date: Date)
}
