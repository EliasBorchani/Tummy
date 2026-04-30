# CLAUDE.md

Context for Claude Code working on Tummy, a Kotlin Multiplatform food-tracking
app with 100% native UI (SwiftUI on iOS, Jetpack Compose on Android).

Read these before making non-trivial changes:
- [docs/architecture.md](./docs/architecture.md) — module graph, bounded
  contexts, MVI, dependency rules.
- [docs/infrastructure.md](./docs/infrastructure.md) — build system, toolchain,
  stack versions, commands, operational notes.
- [docs/dependencies.md](./docs/dependencies.md) — who maintains each library,
  fragility tiers, canonical upgrade flows.

## Load-bearing decisions

- **100% native UI.** Only design tokens (`core/ui-tokens`) and non-UI code are
  shared via KMP. No Compose Multiplatform runtime on iOS. Don't propose adding
  CMP UI unless the user asks.
- **Koin DSL only, no annotations.** We tried `koin-annotations` + KSP and
  reverted: generated `.module` extensions don't cross module boundaries
  cleanly. All DI is declared as `val xxxModule = module { ... }`. Don't
  reintroduce `@Single`, `@Factory`, `@Module`.
- **Each bounded context has its own Room `@Database`.** No shared mega-DB.
  Adding persistence to a context means a new `@Database` in that context's
  `:data`.
- **Features never depend on `:data`.** They see `:api` only. `:data` is wired
  at the DI boundary in `umbrella` (iOS) and `composeApp` (Android).
- **No `_app` / `shared` / `core` god-module for cross-context use cases.**
  Either orchestrate in the VM, or identify the missing bounded context and
  create one.
- **Versions are pinned in `gradle/libs.versions.toml`.** Don't hardcode
  versions in `build.gradle.kts` files.

## Module quick-reference

```
utilities/          stdlib-level helpers (kotlin-ext, network, presentation)
core/ui-tokens      Kotlin tokens + MOKO strings
domain/<ctx>/api    models + repository interfaces + use cases (pure)
domain/<ctx>/data   Room + Ktor + impls + Koin module
features/<name>     commonMain-only: VM + State + Intent + Event
umbrella/           iOS framework "TummyShared" + SKIE + Koin init
composeApp/         Android app
iosApp/             Xcode/SwiftUI
```

## Environment

- **Repo root**: `/Users/eborchani/Dev/Tummy`
- **Android SDK**: `/Users/eborchani/Library/Android/sdk` (declared in
  `local.properties`, gitignored).
- **Gradle**: wrapper only (`./gradlew`). Do not install Gradle globally.
- **Xcode**: the full app is required for iOS builds; Command Line Tools alone
  lack the `iphonesimulator` SDK.

## Conventions

- Prefer editing existing files. Don't introduce new `:core:*` or `:shared:*`
  modules; use the existing structure unless adding a new bounded context.
- When adding a `:domain:<ctx>`, follow the full checklist in
  [architecture.md#adding-a-new-bounded-context](./docs/architecture.md#adding-a-new-bounded-context).
- When adding a feature, register the VM in **both** `umbrella/TummyKoin.kt`
  (iOS) and `composeApp/.../di/AppModule.kt` (Android) — see
  [architecture.md#feature-module-shape](./docs/architecture.md#feature-module-shape).
- Keep `:api` pure: no Koin imports, no Room, no Ktor. Just Kotlin and
  `kotlinx.*`.

## Commands that are known to work

```
./gradlew :composeApp:assembleDebug                       # Android APK
./gradlew :umbrella:linkDebugFrameworkIosSimulatorArm64    # iOS framework
./gradlew :umbrella:compileKotlinIosSimulatorArm64         # iOS code only (no SDK needed)
```

The `--no-configuration-cache` flag is currently needed because Room/MOKO
don't fully support it.

## Pitfalls already hit (don't re-hit)

- `@JvmInline` requires an explicit `import kotlin.jvm.JvmInline` in
  `commonMain` even though it compiles on JVM targets without it.
- `Dispatchers.IO` is internal on iOS native; don't use it in `commonMain`. Let
  Room use its default query dispatcher.
- SKIE caps the Kotlin version it supports — check
  <https://skie.touchlab.co/intro> before bumping `kotlin` in the catalog.
- `skie { features { group { FlowInterop.Enabled(true) } } }` DSL symbols don't
  resolve in 0.10.x build scripts; stick to defaults.
- **AGP 9**: `com.android.library` + `kotlin.multiplatform` is rejected. All
  KMP library modules use `com.android.kotlin.multiplatform.library` (aliased
  as `libs.plugins.androidKmpLibrary`). AGP 9 also ships Kotlin built-in, so do
  not apply `org.jetbrains.kotlin.android` alongside `com.android.application`.
- **kotlinx-datetime 0.7+**: `Instant` and `Clock` moved to `kotlin.time` in
  the stdlib. Import from `kotlin.time`, not `kotlinx.datetime`.
- **Compose MP 1.10 `compose.runtime` String accessor is deprecated.** For
  `composeApp` (Android-only), use the Compose BOM + AndroidX artifacts
  (`libs.androidx.compose.*`), not JB Compose MP.
- **Value classes in commonMain become erased String/Long in Swift.** Any
  type consumed by SwiftUI (models, IDs, tokens) must be `data class`, not
  `value class`. Otherwise `\.id.raw` keypaths break and SKIE can't generate
  structs.
- **Koin 4.2 dropped `GlobalContext`.** Use `org.koin.mp.KoinPlatform.getKoin()`
  instead.
- **Kotlin function names starting with `init` conflict with Swift initializers.**
  Prefix-rename (e.g., `initTummyKoin` → `startTummyKoin`) or SKIE fails to
  expose the function.
- **Expose Koin-resolved VMs through a typed façade `object TummyDI`** rather
  than a raw reflection-based Resolver in Swift. Direct typed calls
  (`TummyDI.shared.mealViewModel()`) are cleaner and type-safe.
- **SwiftUI `@StateObject` + `@MainActor`-isolated VMs**: use explicit `init`
  with `_obs = StateObject(wrappedValue: …)` rather than default args, to
  avoid Swift 6 actor-isolation errors.
- **iOS deployment target 18.0** — `NavigationPath` (iOS 16+) and newer
  concurrency ergonomics. No reason to target lower.
- **`iosApp.xcodeproj` is generated by XcodeGen, never edit in Xcode UI.**
  All build settings (bundle ID, deployment target, Run Script phases,
  xcconfig binding, sandboxing) live in `iosApp/project.yml`. Run
  `iosApp/regen.sh` after editing. The `.xcodeproj` is gitignored.
