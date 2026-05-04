import Foundation
import TummyShared

// MOKO's `.desc()` extension n'est pas bridée vers Swift par SKIE.
// On résout directement via NSLocalizedString sur le bundle du resource.
extension ResourcesStringResource {
    func localized() -> String {
        return NSLocalizedString(self.resourceId, bundle: self.bundle, comment: "")
    }
}
