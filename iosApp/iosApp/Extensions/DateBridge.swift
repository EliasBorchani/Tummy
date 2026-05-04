import Foundation
import TummyShared

extension Date {
    func toKotlinLocalDate() -> LocalDate {
        let comps = Calendar.current.dateComponents([.year, .month, .day], from: self)
        return LocalDate(
            year: Int32(comps.year ?? 1970),
            monthNumber: Int32(comps.month ?? 1),
            dayOfMonth: Int32(comps.day ?? 1)
        )
    }
}

extension LocalDate {
    func toSwiftDate() -> Date {
        var comps = DateComponents()
        comps.year = Int(self.year)
        comps.month = Int(self.monthNumber)
        comps.day = Int(self.dayOfMonth)
        return Calendar.current.date(from: comps) ?? Date()
    }
}
