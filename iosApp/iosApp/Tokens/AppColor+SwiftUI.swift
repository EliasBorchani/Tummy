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

extension AppColors {
    static func scheme(for colorScheme: ColorScheme) -> AppColorScheme {
        colorScheme == .dark ? AppColors.shared.Dark : AppColors.shared.Light
    }
}

extension Font {
    /// Resolves an [AppTextStyle] to a SwiftUI Font. Family maps to
    /// `Font.Design` (`.default` for Sans, `.monospaced` for Mono); SF Pro
    /// Text/Display optical-size cutover is handled by the system.
    init(_ style: AppTextStyle) {
        let weight: Font.Weight = {
            switch style.weight {
            case .regular: return .regular
            case .medium: return .medium
            case .semiBold: return .semibold
            case .bold: return .bold
            }
        }()
        let design: Font.Design = style.family == .mono ? .monospaced : .default
        self = Font.system(size: CGFloat(style.sizeSp), weight: weight, design: design)
    }
}

extension Animation {
    init(_ motion: AppMotionStyle) {
        let e = motion.easing
        self = .timingCurve(
            Double(e.x1), Double(e.y1), Double(e.x2), Double(e.y2),
            duration: Double(motion.durationMillis) / 1000.0
        )
    }
}

// MARK: - Ergonomics

extension View {
    /// Applies an [AppTextStyle] to any text-bearing view: font, kerning, and
    /// the extra line-spacing needed to land on the token's lineHeight.
    /// SwiftUI's `lineSpacing` is the gap added between lines, so we subtract
    /// the font size from the target line-height.
    func appTextStyle(_ style: AppTextStyle) -> some View {
        self
            .font(Font(style))
            .kerning(CGFloat(style.trackingSp))
            .lineSpacing(max(0, CGFloat(style.lineHeightSp - style.sizeSp)))
    }
}

// MARK: - Theme environment

private struct ThemeKey: EnvironmentKey {
    static let defaultValue: AppColorScheme = AppColors.shared.Light
}

extension EnvironmentValues {
    /// Resolved color scheme for the current light/dark mode. Inject once at
    /// the root of a screen with `.tummyTheme()`; descendants read it via
    /// `@Environment(\.theme)` instead of re-resolving.
    var theme: AppColorScheme {
        get { self[ThemeKey.self] }
        set { self[ThemeKey.self] = newValue }
    }
}

extension View {
    /// Resolves the system color scheme to an [AppColorScheme] and injects it
    /// into the environment. Apply once at the root of each screen (or the
    /// app), then read with `@Environment(\.theme)`.
    func tummyTheme() -> some View {
        modifier(TummyThemeModifier())
    }
}

private struct TummyThemeModifier: ViewModifier {
    @Environment(\.colorScheme) private var colorScheme
    func body(content: Content) -> some View {
        content.environment(\.theme, AppColors.scheme(for: colorScheme))
    }
}
