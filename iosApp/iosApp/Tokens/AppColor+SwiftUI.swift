import SwiftUI
import TummyShared

extension Color {
    init(_ token: AppColor) {
        let argb = UInt64(bitPattern: Int64(token.argb))
        let a = Double((argb >> 24) & 0xFF) / 255.0
        let r = Double((argb >> 16) & 0xFF) / 255.0
        let g = Double((argb >> 8) & 0xFF) / 255.0
        let b = Double(argb & 0xFF) / 255.0
        self.init(.sRGB, red: r, green: g, blue: b, opacity: a)
    }
}

extension CGFloat {
    init(_ dp: AppDp) { self.init(dp.value) }
}
