# Infrastructure

Build system, toolchain, and libraries that power the project. For the *why*
behind the module shape, see [architecture.md](./architecture.md). For
platform quirks, see [kmp-gotchas.md](./kmp-gotchas.md).

## Toolchain

| Tool        | Version  | How it is provided                                       |
|-------------|----------|----------------------------------------------------------|
| Gradle      | 9.4.1    | `./gradlew` (wrapper, bootstraps on first run)           |
| JDK         | 17       | Any JDK 17 available to Gradle (Homebrew, sdkman, Xcode) |
| Android SDK | API 37   | `~/Library/Android/sdk`, declared in `local.properties`  |
| Xcode       | Full     | Required for iOS builds (Command Line Tools alone lack the `iphonesimulator` SDK) |
| XcodeGen    | latest   | `brew bundle install` at repo root                       |

`local.properties` (gitignored) holds the local SDK path: `sdk.dir=/Users/<you>/Library/Android/sdk`.

## Stack

| Area             | Library                                                   | Version    | Notes |
|------------------|-----------------------------------------------------------|------------|-------|
| Language         | Kotlin Multiplatform                                      | 2.3.20     | |
| Build plugin     | KSP                                                       | 2.3.7      | Used by Room KMP only |
| UI (Android)     | Jetpack Compose (via BOM)                                 | 2025.04.00 | Pure AndroidX — no CMP on `composeApp` |
| UI (iOS)         | SwiftUI (native)                                          | —          | No CMP runtime on iOS |
| ViewModel        | `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel`    | 2.10.0     | JB multiplatform port, not AndroidX |
| Navigation (Android) | `androidx.navigation:navigation-compose`              | 2.9.7      | |
| Navigation (iOS) | `NavigationStack` (native)                                | —          | |
| DI               | Koin                                                      | 4.2.0      | DSL only, no `koin-annotations` (see [kmp-gotchas.md#koin](./kmp-gotchas.md#koin)) |
| Persistence      | Room KMP                                                  | 2.8.3      | Stable. One `@Database` per bounded context |
| SQLite driver    | `androidx.sqlite:sqlite-bundled`                          | 2.6.0      | Same driver on Android and iOS |
| Networking       | Ktor client (OkHttp Android / Darwin iOS)                 | 3.4.3      | |
| Serialization    | kotlinx.serialization                                     | 1.8.0      | JSON only |
| Resources        | MOKO Resources                                            | 0.26.2     | Strings only; no Compose UI dependency |
| iOS interop      | SKIE                                                      | 0.10.11    | Defaults — Flow/Suspend/Sealed/Default-args interop |
| Logging          | Napier                                                    | 2.7.1      | |

All versions live in `gradle/libs.versions.toml`. See
[dependencies.md](./dependencies.md) for upgrade flows and risk tiers.

## Convention plugins

Live in `build-logic/convention/src/main/kotlin/`. Applied via plugin alias
in module build scripts.

| Plugin ID            | Applied to             | Effect |
|----------------------|------------------------|--------|
| `tummy.kmp.library`  | All KMP library modules | `kotlin.multiplatform` + `com.android.kotlin.multiplatform.library` (AGP 9 unified plugin), iOS triple, namespace, compileSdk/minSdk, coroutines in commonMain, **`-Xexpect-actual-classes`** flag |
| `tummy.kmp.feature`  | `features/*`           | Wraps `tummy.kmp.library` + lifecycle-viewmodel + koin-core |
| `tummy.kmp.room`     | `domain/*/data` (with DB) | KSP + Room plugin, sqlite-bundled, schemaDirectory, Room compiler on all iOS targets |
| `tummy.kmp.moko`     | `core/ui-tokens`       | MOKO Resources plugin, resourcesPackage/className set |
| `tummy.android.app`  | `composeApp`           | `com.android.application` + Compose compiler, `buildFeatures.compose = true`, Android-only |

Adding a new convention: write `*ConventionPlugin.kt`, register it in
`build-logic/convention/build.gradle.kts`.

## Version catalog

`gradle/libs.versions.toml` is the single source of truth.

- `[versions]`: bump numbers here, propagates everywhere.
- `[libraries]`: referenced as `libs.xxx.yyy`.
- `[plugins]`: referenced as `alias(libs.plugins.xxx)`. Convention plugins are
  declared with empty version (resolved via included build).

Type-safe project accessors are enabled, so references look like
`projects.domain.ingredients.api`, not `project(":domain:ingredients:api")`.

## DI approach

Koin is configured **entirely through the DSL**. No `@Single` / `@Factory` /
`@Module` annotations (see [kmp-gotchas.md#koin](./kmp-gotchas.md#koin) for why
we reverted).

Each `:data` module exposes `val <ctx>Module: Module` aggregating its
repositories + use cases. Modules are gathered in `umbrella/TummyKoin.kt`'s
`startTummyKoin`, called from both Android (`TummyApplication.onCreate`) and
iOS (`TummyApp.init`).

ViewModels are registered twice on purpose:
- In `umbrella`'s `viewModelsModule` as plain `factory { ... }` — used by iOS.
- In `composeApp`'s `androidAppModule` as `viewModelOf(::...)` — used by
  Android so `koinViewModel()` binds to `NavBackStackEntry`.

Platform-specific bindings (e.g. `StandardIngredientNameProvider`) are
provided per platform: `composeApp` for Android, `umbrella/iosMain`'s
`platformModules` (a per-platform `actual val`) for iOS.

## Umbrella framework

`umbrella/` is the single KMP module whose iOS framework output `TummyShared`
is consumed by Xcode.

- Declares `binaries.framework { baseName = "TummyShared"; isStatic = true; export(...) }`
  for each iOS target (x64, arm64, simulatorArm64).
- Applies SKIE: defaults cover Flow / Suspend / Sealed / Default-args interop.
  Analytics upload disabled.
- Every module visible in Swift must be `export(...)`'d *and* `api(...)`'d
  (exports need the api dependency to type-check).

### Adding a new exported module to the framework

1. Edit `umbrella/build.gradle.kts`:
   ```kotlin
   target.binaries.framework {
       export(projects.domain.<new>.api)
   }
   sourceSets.commonMain.dependencies {
       api(projects.domain.<new>.api)
   }
   ```
2. Register its Koin module in `TummyKoin.kt`.
3. Re-link: `./gradlew :umbrella:linkDebugFrameworkIosSimulatorArm64`.

## iOS Xcode project (generated)

The Xcode project is **not committed** — generated from `iosApp/project.yml`
by XcodeGen. After cloning, or whenever `project.yml` changes:

```
brew bundle install      # one-time, installs xcodegen
iosApp/regen.sh          # regenerates iosApp.xcodeproj
```

Adding a new Swift file is just dropping it under `iosApp/iosApp/` —
auto-discovered via `sources: - path: iosApp` in the spec.

Build settings, Run Script Phases, bundle ID, deployment target etc. all live
in `project.yml`. **Never** edit them in the Xcode UI — changes are wiped on
next regen.

## Build commands

### Android

```
./gradlew :composeApp:assembleDebug                      # APK
./gradlew :composeApp:installDebug                       # build + install
```

### iOS (Kotlin side)

```
./gradlew :umbrella:linkDebugFrameworkIosSimulatorArm64  # produce TummyShared.framework
./gradlew :umbrella:embedAndSignAppleFrameworkForXcode   # invoked automatically by Xcode
./gradlew :umbrella:compileKotlinIosSimulatorArm64       # iOS code only — fast, no SDK needed
```

### iOS (full build via Xcode CLI)

```
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug \
           -destination 'generic/platform=iOS Simulator' -sdk iphonesimulator build
```

### Module sanity check

```
./gradlew :<module>:compileDebugKotlinAndroid            # Android variant
./gradlew :<module>:compileKotlinIosSimulatorArm64       # iOS simulator variant
```

### Housekeeping

```
./gradlew --stop                                         # stop daemons (after toolchain changes)
./gradlew clean                                          # nuke build outputs
```

## Operational notes

- **`--no-configuration-cache`** is currently required because Room/MOKO don't
  fully support it. Use the flag in CI; local builds may emit warnings without.
- **SKIE caps Kotlin version.** Check <https://skie.touchlab.co/intro> before
  bumping `kotlin` in the catalog.
- **Gradle plugin classpath.** `build-logic/convention` only depends on
  plugins whose *API types* it references (Kotlin, AGP, KSP, Room, MOKO).
  Plugins applied via `alias()` in module scripts (SKIE, Compose,
  Serialization) don't need to be on the convention classpath — they resolve
  through `pluginManagement` at the root.
