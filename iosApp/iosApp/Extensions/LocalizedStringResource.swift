import Foundation
import TummyShared

// MOKO's `.desc()` extension is not bridged to Swift by SKIE, so we resolve
// directly via NSLocalizedString on the resource bundle.
extension ResourcesStringResource {
    func localized() -> String {
        return NSLocalizedString(self.resourceId, bundle: self.bundle, comment: "")
    }

    /// Localizes and substitutes positional args. MOKO emits `%s` (Java/Android
    /// printf specifier) for string args, but Foundation's `String(format:)`
    /// requires `%@` and crashes on `%s` under iOS 17+ strict format checking.
    /// We translate `%s` → `%@` here so the bundle can stay
    /// platform-agnostic.
    func localized(_ args: CVarArg...) -> String {
        let raw = NSLocalizedString(self.resourceId, bundle: self.bundle, comment: "")
        let normalized = raw.replacingOccurrences(of: "%s", with: "%@")
        return String(format: normalized, arguments: args)
    }
}
