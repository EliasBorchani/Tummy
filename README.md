# Tummy

A Kotlin Multiplatform food-tracking app with 100% native UI.

- **Android**: Jetpack Compose
- **iOS**: SwiftUI
- **Shared (Kotlin)**: ViewModels, domain logic, data layer, design tokens

## Tech stack

Kotlin 2.3.20 · KMP · KSP · Gradle 9.4.1 · AGP 9.1.1 · Jetpack Compose ·
Room KMP 2.8.3 · Ktor 3.4 · Koin 4.2 · MOKO Resources · SKIE.

Pinned versions live in [`gradle/libs.versions.toml`](./gradle/libs.versions.toml).

## Project layout

```
utilities/          cross-cutting helpers
core/ui-tokens      shared design tokens + MOKO strings
domain/<context>    bounded contexts, split into api/ and data/
features/<name>     commonMain ViewModels (native UI consumes them)
umbrella/           single iOS framework (TummyShared) + Koin init
composeApp/         Android app
iosApp/             Xcode / SwiftUI app
```

See [`docs/architecture.md`](./docs/architecture.md) for the rationale.

## Prerequisites

- JDK 17
- Android SDK (API 35) — path goes in `local.properties`:
  ```
  sdk.dir=/Users/<you>/Library/Android/sdk
  ```
- Xcode (full install) for iOS builds

Gradle is bootstrapped by the wrapper — nothing to install globally.

## Quick start

```bash
# Android
./gradlew :composeApp:assembleDebug

# iOS framework (needed before opening iosApp.xcodeproj)
./gradlew :umbrella:linkDebugFrameworkIosSimulatorArm64

# Then open iosApp/iosApp.xcodeproj in Xcode and run.
```

## Documentation

- [Architecture](./docs/architecture.md) — module graph, bounded contexts, MVI,
  dependency rules.
- [Infrastructure](./docs/infrastructure.md) — toolchain, stack, build
  conventions, operational notes.
- [Dependencies](./docs/dependencies.md) — who owns each library, fragility
  tiers, upgrade flows.
