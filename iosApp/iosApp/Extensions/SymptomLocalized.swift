import Foundation
import TummyShared

extension Symptom {
    var localizedName: String {
        switch self {
        case .bloating: return MR.strings.shared.symptom_bloating.localized()
        case .abdominalPain: return MR.strings.shared.symptom_abdominal_pain.localized()
        case .gas: return MR.strings.shared.symptom_gas.localized()
        case .diarrhea: return MR.strings.shared.symptom_diarrhea.localized()
        case .constipation: return MR.strings.shared.symptom_constipation.localized()
        case .nausea: return MR.strings.shared.symptom_nausea.localized()
        @unknown default: return "\(self)"
        }
    }
}
