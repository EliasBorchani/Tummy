# Infrastructure

This document describes the build system, toolchain, and libraries that power
the project. For the "why" behind the module shape, see
[architecture.md](./architecture.md).

## Toolchain

All toolchains are scoped to the project. Nothing is required to be on the
user's global `PATH`.

| Tool       | Version  | How it is provided                                        |
|------------|----------|-----------------------------------------------------------|
| Gradle     | 9.4.1    | `./gradlew` (wrapper, bootstraps on first run)            |
| JDK        | 17       | Any JDK 17 available to Gradle (Homebrew, sdkman, Xcode)  |
| Android SDK| API 37   | `~/Library/Android/sdk`, declared in `local.properties`   |
| Xcode      | Full     | Required for iOS builds (Command Line Tools alone are not enough — no `iphonesimulator` SDK) |

`local.properties` (gitignored) holds the local SDK path:
```
sdk.dir=/Users/<you>/Library/Android/sdk
```

## Stack

| Area             | Library                                                   | Version   | Notes |
|------------------|-----------------------------------------------------------|-----------|-------|
| Language         | Kotlin Multiplatform                                      | 2.3.20    | |
| Build plugin     | KSP                                                       | 2.3.7     | Used by Room KMP only |
| UI (Android)     | Jetpack Compose (via BOM)                                 | 2025.04.00 | Pure AndroidX — no Compose Multiplatform on `composeApp` |
| UI (iOS)         | SwiftUI (native)                                          | —         | No CMP runtime on iOS |
| ViewModel        | `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel`    | 2.10.0    | JB multiplatform port, not AndroidX |
| Navigation (Android) | `androidx.navigation:navigation-compose`              | 2.9.7     | |
| Navigation (iOS) | `NavigationStack` (native)                                | —         | |
| DI               | Koin                                                      | 4.2.0     | DSL only, no `koin-annotations` (see [DI](#di-approach)) |
| Persistence      | Room KMP                                                  | 2.8.3     | Stable. One `@Database` per bounded context |
| SQLite driver    | `androidx.sqlite:sqlite-bundled`                          | 2.6.0     | Same driver on Android and iOS |
| Networking       | Ktor client (OkHttp Android / Darwin iOS)                 | 3.4.3     | |
| Serialization    | kotlinx.serialization                                     | 1.8.0     | JSON only |
| Resources        | MOKO Resources                                            | 0.26.2    | Strings only; no Compose UI dependency |
| iOS interop      | SKIE                                                      | 0.10.11   | Defaults only — Flow/Suspend/Sealed/Default-args interop |
| Logging          | Napier                                                    | 2.7.1     | |

All versions are pinned in `gradle/libs.versions.toml`.

## Convention plugins

Lives in `build-logic/convention/src/main/kotlin/`. Applied as plugin IDs
(e.g., `alias(libs.plugins.tummy.kmp.library)`).

| Plugin ID                     | Applied to                    | Effect |
|-------------------------------|-------------------------------|--------|
| `tummy.kmp.library`           | All KMP library modules       | `kotlin.multiplatform` + `com.android.kotlin.multiplatform.library` (AGP 9 unified plugin), iOS triple, namespace, compileSdk/minSdk from catalog, coroutines in commonMain |
| `tummy.kmp.feature`           | `features/*`                  | Wraps `tummy.kmp.library` + lifecycle-viewmodel + koin-core |
| `tummy.kmp.room`              | `domain/*/data` (when DB)     | KSP + Room plugin, sqlite-bundled in commonMain, schemaDirectory, Room compiler on all iOS targets |
| `tummy.kmp.moko`              | `core/ui-tokens`              | MOKO Resources plugin, resourcesPackage/className set |
| `tummy.android.app`           | `composeApp`                  | `com.android.application` (AGP 9 ships Kotlin built-in) + Compose compiler, `buildFeatures.compose = true`, Android-only |

Adding a new convention: create a `*ConventionPlugin.kt` and register it in
`build-logic/convention/build.gradle.kts`.

## Version catalog

`gradle/libs.versions.toml` is the single source of truth for dependency
versions.

- **Versions** (`[versions]`): upgrade numbers here, propagates everywhere.
- **Libraries** (`[libraries]`): referenced as `libs.xxx.yyy` in build scripts.
- **Plugins** (`[plugins]`): referenced as `alias(libs.plugins.xxx)` in build
  scripts. Convention plugins are declared with empty version (resolved via
  included build).

Type-safe project accessors are enabled in `settings.gradle.kts`, so references
look like `projects.domain.meal.api` instead of `project(":domain:meal:api")`.

## DI approach

Koin is configured **entirely through the DSL**. No `@Single`, `@Factory`, or
`@Module` annotations.

### Why no annotations

Koin Annotations with KSP work within a module but do not cross module
boundaries cleanly in a multi-module KMP setup. The generated `.module`
extension lives in the module that declared the `@Module` class, and importing
it from another module is fragile (cross-module KSP-generated symbols). We
tried and reverted.

### How modules are declared

Each bounded context's `:data` module exposes `val <ctx>Module: Module`:

```kotlin
val mealModule = module {
    single { buildMealDatabase() }
    single { get<MealDatabase>().mealDao() }
    single { MealApi(get()) }
    single<MealRepository> { MealRepositoryImpl(get(), get()) }
    factory { ObserveMealsUseCase(get()) }
    factory { LogMealUseCase(get()) }
}
```

All modules are aggregated in `umbrella/TummyKoin.kt`'s `initTummyKoin`, which
is called from both Android (`TummyApplication.onCreate`) and iOS
(`TummyApp.init`).

ViewModels are registered twice on purpose:

- In `umbrella`'s `viewModelsModule` as plain `factory { ... }` — used by iOS.
- In `composeApp`'s `androidAppModule` as `viewModelOf(::...)` — used by
  Android so `koinViewModel()` binds to `NavBackStackEntry`'s
  `ViewModelStoreOwner`.

## Umbrella framework

`umbrella/` is the single KMP module whose iOS framework output `TummyShared`
is consumed by Xcode.

- Declares `binaries.framework { baseName = "TummyShared"; isStatic = true; export(...) }`
  for each iOS target (x64, arm64, simulatorArm64).
- Applies SKIE: `plugins { alias(libs.plugins.skie) }`. Defaults cover Flow,
  Suspend, Sealed, and default-argument interop. Analytics upload is disabled.
- Every module that should be visible in Swift must be listed in `export(...)`
  *and* pulled in as `api(...)` (needed for exports to type-check).

### Adding a new exported module to the framework

1. Add it to `umbrella/build.gradle.kts`:
   ```kotlin
   target.binaries.framework {
       export(projects.domain.<new>.api)
   }
   sourceSets.commonMain.dependencies {
       api(projects.domain.<new>.api)
   }
   ```
2. Register its Koin module in `TummyKoin.kt`.
3. Re-link the framework:
   `./gradlew :umbrella:linkDebugFrameworkIosSimulatorArm64`.

## Resource pipeline (MOKO)

- Strings live in `core/ui-tokens/src/commonMain/resources/MR/<locale>/strings.xml`.
  `base` is required; other locales (e.g., `fr`) override subsets.
- `tummy.kmp.moko` convention sets `resourcesPackage = com.tummy.tokens.resources`
  and `resourcesClassName = MR`, so code accesses strings as `MR.strings.xxx`.
- On Android, MOKO generates Android resources — available through the regular
  Android resource system as well.
- On iOS, MOKO generates NSBundle entries. Swift can read them through
  `TummyShared`'s `MR` class.

The `moko.resources.disableStaticFrameworkWarning=true` flag in
`gradle.properties` suppresses the warning that reminds iOS framework consumers
to run `copyFrameworkResourcesToApp` — which `embedAndSignAppleFrameworkForXcode`
already handles.

## Persistence pipeline (Room KMP)

Room 2.7-alpha ships an expect/actual-based constructor mechanism:

```kotlin
@Database(entities = [MealEntity::class], version = 1, exportSchema = true)
@ConstructedBy(MealDatabaseConstructor::class)
abstract class MealDatabase : RoomDatabase() { abstract fun mealDao(): MealDao }

expect object MealDatabaseConstructor : RoomDatabaseConstructor<MealDatabase>
```

KSP generates the `actual object` on each target. The `tummy.kmp.room`
convention wires the Room compiler on `androidMain`, `iosX64`, `iosArm64`, and
`iosSimulatorArm64`.

Platform-specific database location:

- Android: `AndroidDatabaseContext.applicationContext.getDatabasePath("<name>.db")`.
  The `applicationContext` must be set in `TummyApplication.onCreate()` before
  `initTummyKoin`.
- iOS: `NSFileManager`'s `NSDocumentDirectory` + a filename.

## Commands

### Android
```
./gradlew :composeApp:assembleDebug           # build APK
./gradlew :composeApp:installDebug            # build + install on connected device/emulator
```

### iOS (Kotlin side)
```
./gradlew :umbrella:linkDebugFrameworkIosSimulatorArm64     # produce TummyShared.framework for simulator
./gradlew :umbrella:embedAndSignAppleFrameworkForXcode      # invoked automatically by Xcode Run Script
```

### Module-level sanity
```
./gradlew :<module>:compileDebugKotlinAndroid               # Android variant
./gradlew :<module>:compileKotlinIosSimulatorArm64          # iOS simulator variant
```

### Housekeeping
```
./gradlew --stop                              # stop gradle daemons (after toolchain changes)
./gradlew clean                               # nuke build outputs
```

## Known operational notes

- **SKIE vs Kotlin versions.** SKIE lags Kotlin releases by a few weeks. When a
  new Kotlin comes out, check <https://skie.touchlab.co/> for supported versions
  before bumping `kotlin` in the catalog.
- **Room `expect`/`actual` warnings are expected.** KSP-generated code uses
  `expect object Constructor` which Kotlin still flags as Beta. Silence with
  `-Xexpect-actual-classes` if it becomes noisy.
- **No `configuration-cache`.** Some plugins (Room, MOKO) don't fully support
  it yet. Use `--no-configuration-cache` in CI pipelines. Local builds without
  the flag may emit warnings.
- **AGP 9 ships Kotlin built-in.** Do not apply `org.jetbrains.kotlin.android`
  alongside `com.android.application` — AGP 9 refuses it. For KMP libraries,
  do not combine `com.android.library` + `kotlin.multiplatform` either — use
  the unified `com.android.kotlin.multiplatform.library` (`libs.plugins.androidKmpLibrary`).
- **Android source layout.** `composeApp` uses the standard AGP layout
  (`src/main/kotlin`, `src/main/AndroidManifest.xml`). KMP library modules keep
  the multiplatform layout (`src/commonMain`, `src/androidMain`, `src/iosMain`).
- **Gradle plugin classpath for convention plugins.** `build-logic/convention`
  only depends on plugins whose *API types* it references (Kotlin, AGP, KSP,
  Room, MOKO). Plugins applied via `alias()` in module scripts (SKIE, Compose,
  Serialization) don't need to be on the convention classpath — they are
  resolved through `pluginManagement` at the root level.
