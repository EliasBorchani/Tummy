# Architecture

This document describes how Tummy is structured, why, and the rules that keep the
codebase coherent as it grows.

## Guiding principles

1. **Native UI, shared logic.** SwiftUI on iOS, Jetpack Compose on Android. Only
   design tokens and all non-UI code (state, use cases, repositories, data
   sources) are shared through KMP.
2. **Bounded contexts drive the module graph.** Each business area is an
   independent `:domain:<context>` subtree with its own `api` and `data`
   modules. Features and the app compose these contexts — they never reach into
   a context's internals.
3. **No god modules.** No single `shared`, `common`, or `core` module that
   aggregates everything. Growth happens by adding new contexts or features,
   not by enlarging existing ones.
4. **Contracts in `:api`, implementations in `:data`.** The public surface of a
   bounded context (models, repository interfaces, use cases) lives in `:api`.
   Everything else — Room entities, Ktor DTOs, repository implementations,
   Koin modules — lives in `:data`.

## Module graph

```
utilities/
  kotlin-ext/          stdlib + coroutines helpers, no external deps
  network/             Ktor client factory, platform-specific engine
  presentation/        BaseViewModel + MVI primitives

core/
  ui-tokens/           AppColor / AppDimens / AppTypography + MOKO strings

domain/
  meal/
    api/               Meal model, MealRepository, meal use cases
    data/              MealEntity, MealDao, MealDatabase, MealApi, impls + Koin module
  user/
    api/ data/
  nutrition/
    api/ data/

features/
  meal/                MealViewModel, MealState, MealIntent, MealEvent (commonMain only)
  diary/               DiaryViewModel...
  settings/            SettingsViewModel...

umbrella/              Single iOS framework "TummyShared" + SKIE + Koin init
composeApp/            Android app: Compose UI + NavHost + ViewModelStoreOwner DI
iosApp/                Xcode project: SwiftUI Views + NavigationStack + SKIE bridges
```

## Dependency rules

The following rules keep the architecture honest. Violating them usually means
a type is leaking from where it shouldn't be.

| Module                  | May depend on                                     | Must not depend on              |
|-------------------------|---------------------------------------------------|---------------------------------|
| `utilities:*`           | stdlib, kotlinx, one targeted lib per module      | `core:*`, `domain:*`, `features:*` |
| `core:ui-tokens`        | `utilities:kotlin-ext`                            | `domain:*`, `features:*`        |
| `domain:<ctx>:api`      | `utilities:*`                                     | any other `:api`, any `:data`, `core:*` |
| `domain:<ctx>:data`     | own `:api`, `utilities:*`, other `:api`s         | `features:*`, other `:data`s    |
| `features:<name>`       | `domain:*:api`, `utilities:presentation`, `utilities:kotlin-ext` | any `:data`, `core:ui-tokens`, other features |
| `umbrella`              | everything (aggregates for iOS)                   | —                               |
| `composeApp`            | everything (aggregates for Android)               | —                               |

Key implication: **features never import `:data` modules**. They know about
repository interfaces from `:api`. `:data` is wired to `:api` only at the DI
boundary (in `umbrella` for iOS, in `composeApp` for Android).

## Bounded contexts

A context is an independent slice of the product: `meal`, `user`, `nutrition`,
etc. Each context owns:

- Its **models** (`domain/<ctx>/api/src/commonMain/kotlin/.../model/`)
- Its **repository contracts** (`domain/<ctx>/api/.../repository/`)
- Its **use cases** (`domain/<ctx>/api/.../usecase/`)
- Its **storage schema** (`domain/<ctx>/data/.../db/`) — Room entities, DAOs,
  one `@Database` per context
- Its **network DTOs and API client** (`domain/<ctx>/data/.../network/`)
- Its **repository implementations** (`domain/<ctx>/data/.../`)
- Its **Koin module** (`domain/<ctx>/data/.../di/<ctx>Module.kt`)

### Adding a new bounded context

1. `include(":domain:<ctx>:api", ":domain:<ctx>:data")` in `settings.gradle.kts`.
2. Create `api/build.gradle.kts` applying `tummy.kmp.library`.
3. Create `data/build.gradle.kts` applying `tummy.kmp.library` + `tummy.kmp.room`
   (if persistence is needed) + `kotlinSerialization` (if network).
4. Write the model/repository contract in `:api` first. Do not leak entities or
   DTOs through it.
5. Implement `:data` — Room entities, DAOs, a `MealDatabase`-style `@Database`,
   Ktor DTOs + API, repository impl, and a `val <ctx>Module = module { ... }`.
6. Register `<ctx>Module` in `umbrella/TummyKoin.kt`.

### Cross-context logic

There is no `_app` / `orchestration` / `shared` module for cross-context use
cases. Two strategies instead:

- **VM-level orchestration**: if only one ViewModel needs to combine
  `MealRepository` + `UserRepository`, inject both and do the combine in the
  VM.
- **New bounded context**: if the orchestration is stable, non-trivial, and
  reused across multiple features, it deserves its own context. Example: a
  "budget" context that combines nutrition + meals.

If you find yourself creating a module whose only purpose is to combine two
existing contexts, the underlying domain concept probably deserves a name.

## Presentation layer (MVI)

`utilities:presentation` provides `BaseViewModel<State, Intent, Event>`:

- **State** — immutable, rendered by the UI. A `StateFlow` exposed via `state`.
- **Intent** — user input or lifecycle event. Dispatched through `onIntent(...)`.
- **Event** — one-shot effect (navigation, toast, haptic). Consumed via a
  `SharedFlow<Event>` exposed as `events`.

The VM knows nothing about platform or navigation. It emits a navigation
`Event` (e.g., `MealEvent.NavigateToDetail(id)`); the platform-side UI observes
`events` and actually navigates.

### Feature module shape

A feature module is commonMain-only. It contains exactly four file kinds:

- `XyzState.kt` — one `data class`.
- `XyzIntent.kt` — one `sealed interface`.
- `XyzEvent.kt` — one `sealed interface`.
- `XyzViewModel.kt` — extends `BaseViewModel<XyzState, XyzIntent, XyzEvent>`,
  declares dependencies via constructor, wires use cases.

Register the VM in two places:

- `umbrella/TummyKoin.kt` — `factory { XyzViewModel(get(), ...) }` in
  `viewModelsModule`, for iOS.
- `composeApp/.../di/AppModule.kt` — `viewModelOf(::XyzViewModel)`, for Android
  (needed for `koinViewModel()` and ViewModelStoreOwner binding).

## Platform-specific UI

### Android

- `composeApp` is an Android-only module built by the `tummy.android.app`
  convention plugin. It hosts Compose UI, `NavHost`, and MaterialTheme.
- Each screen (`MealScreen`, `DiaryScreen`, ...) lives in
  `composeApp/.../feature/<name>/`.
- Screens access VMs with `org.koin.compose.viewmodel.koinViewModel()`, which
  scopes to the current `NavBackStackEntry`.
- Navigation is `androidx.navigation.compose` (`NavHost`, `composable(...)`).
  Routes are declared in `composeApp/.../navigation/TummyNavHost.kt`.

### iOS

- `umbrella` produces `TummyShared.framework` with SKIE applied. All VMs,
  tokens, models, and use cases are exported through it.
- `iosApp` is a native Xcode project (SwiftUI). It imports `TummyShared` and
  consumes VMs via lightweight `@MainActor` `ObservableObject` wrappers that
  read `StateFlow<State>` as an `AsyncSequence` (provided by SKIE's Flow
  interop).
- Navigation is `NavigationStack` + `navigationDestination(for:)`. Each screen
  subscribes to the VM's `events` stream and triggers path pushes locally.
- Tokens: Kotlin `AppColor` / `AppDp` are bridged to `SwiftUI.Color` / `CGFloat`
  via Swift extensions (`iosApp/iosApp/Tokens/AppColor+SwiftUI.swift`).

## Resources (MOKO)

Strings are defined once in `core/ui-tokens/src/commonMain/resources/MR/`
and compiled into the native resource systems on each platform:

- Android: strings become part of the standard Android resource system
  (`R.string.*` via MOKO's generated `MR` class).
- iOS: strings are bundled into `NSBundle`, accessible through the same `MR`
  class.

Non-string tokens (colors, dimens, typography) are **not** MOKO — they are
Kotlin objects in `core/ui-tokens`. This keeps them available to Swift without
any Compose runtime dependency on iOS.

## Data storage (Room KMP)

Each bounded context that needs persistence has its **own** Room `@Database`
class in its `:data` module. Reasons:

- Migrations are localized per context.
- Schema changes in one context cannot break compilation of another.
- The `@Database` annotation requires all entities up-front; splitting avoids
  mega-databases.

A single `expect fun <ctx>DatabaseBuilder(): RoomDatabase.Builder<XyzDatabase>`
per context provides the platform-specific construction (Android needs a
`Context`, iOS needs a file path under `NSDocumentDirectory`).

The SQLite driver is `BundledSQLiteDriver` (`androidx.sqlite:sqlite-bundled`) on
both platforms for behavior parity.

## Summary

- Module choice is driven by the business shape, not technical layers.
- `:api` is pure, `:data` is concrete; features only see `:api`.
- Each context has its own DB, its own Koin module, its own migration path.
- The VM layer is pure MVI; platforms adapt it to their UI idioms.
- Shared code stops at the rendering boundary.
