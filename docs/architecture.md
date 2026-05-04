# Architecture

How Tummy is structured and the rules that keep the codebase coherent.

For *style of the code* (naming, MVI shape), see [code-style.md](./code-style.md).
For *platform quirks* (Room/MOKO/SKIE/AGP), see [kmp-gotchas.md](./kmp-gotchas.md).

## Guiding principles

1. **Native UI, shared logic.** SwiftUI on iOS, Jetpack Compose on Android.
   Only design tokens and non-UI code (state, use cases, repositories, data
   sources) are shared through KMP.
2. **Bounded contexts drive the module graph.** Each business area is an
   independent `:domain:<context>` subtree with its own `api` and `data`
   modules. Features and the app compose these contexts — they never reach
   into a context's internals.
3. **No god modules.** No single `shared`, `common`, or `core` module that
   aggregates everything. Growth happens by adding new contexts or features,
   not by enlarging existing ones.
4. **Contracts in `:api`, implementations in `:data`.** The public surface of
   a bounded context (models, repository interfaces, use cases) lives in
   `:api`. Everything else — Room entities, Ktor DTOs, repository
   implementations, Koin modules — lives in `:data`.
5. **Domain modules are independent of each other.** No `domain:<ctx>:api` or
   `domain:<ctx>:data` may depend on another `domain:<ctx>:*`. Cross-context
   logic is composed at the feature/VM layer; if the orchestration is itself
   a stable, named domain concept, lift it into a new bounded context rather
   than wiring two existing ones together.

## Module graph

```
utilities/
  kotlin-ext/          stdlib + coroutines helpers + AsyncData<T>
  network/             Ktor client factory, platform-specific engine
  presentation/        BaseViewModel + MVI primitives

core/
  ui-tokens/           AppColor / AppDimens / AppTypography + MOKO strings

domain/
  ingredients/
    api/               Ingredient model, repository iface, search/upsert use cases
    data/              Room entities, dao, db, impl, Koin module
  symptoms/
    api/ data/

features/
  home/                HomeViewModel + suspect-score computation (cross-context)
  log/                 LogIngredientViewModel, LogSymptomViewModel

umbrella/              Single iOS framework "TummyShared" + SKIE + Koin init
composeApp/            Android app: Compose UI + NavHost + ViewModelStoreOwner DI
iosApp/                Xcode project (XcodeGen): SwiftUI + NavigationStack + SKIE bridges
```

## Dependency rules

| Module                  | May depend on                                     | Must not depend on              |
|-------------------------|---------------------------------------------------|---------------------------------|
| `utilities:*`           | stdlib, kotlinx, one targeted lib per module      | `core:*`, `domain:*`, `features:*` |
| `core:ui-tokens`        | `utilities:kotlin-ext`                            | `domain:*`, `features:*`        |
| `domain:<ctx>:api`      | `utilities:*`                                     | other `domain:<ctx>:*`, any `:data`, `core:*`, `features:*` |
| `domain:<ctx>:data`     | own `:api`, `utilities:*`                         | other `domain:<ctx>:*`, `features:*`, `core:*` |
| `features:<name>`       | `domain:*:api`, `utilities:presentation`, `utilities:kotlin-ext` | any `:data`, `core:ui-tokens`, other features |
| `umbrella`              | everything (aggregates for iOS)                   | —                               |
| `composeApp`            | everything (aggregates for Android)               | —                               |

Key implication: **features never import `:data` modules**. They know about
repository interfaces from `:api`. `:data` is wired to `:api` only at the DI
boundary (in `umbrella` for iOS, in `composeApp` for Android).

## Bounded contexts

A context is an independent slice of the product. Each context owns:

- Its **models** (`domain/<ctx>/api/.../model/`)
- Its **repository contracts** (`domain/<ctx>/api/.../repository/`)
- Its **use cases** (`domain/<ctx>/api/.../usecase/`)
- Its **storage schema** (`domain/<ctx>/data/.../db/`) — Room entities, DAOs,
  one `@Database` per context
- Its **repository implementations** (`domain/<ctx>/data/.../`)
- Its **Koin module** (`domain/<ctx>/data/.../di/<ctx>Module.kt`)

### Adding a new bounded context

1. `include(":domain:<ctx>:api", ":domain:<ctx>:data")` in `settings.gradle.kts`.
2. Create `api/build.gradle.kts` applying `tummy.kmp.library`.
3. Create `data/build.gradle.kts` applying `tummy.kmp.library` + `tummy.kmp.room`
   (if persistence is needed).
4. Write the model/repository contract in `:api` first. Do not leak entities
   or DTOs through it.
5. Implement `:data` — Room entities, DAOs, a `<Ctx>Database`-style `@Database`,
   repository impl, and a `val <ctx>Module = module { ... }`.
6. Register `<ctx>Module` in `umbrella/TummyKoin.kt`.
7. For iOS access: `export(projects.domain.<ctx>.api)` + `api(...)` it in
   `umbrella/build.gradle.kts`. Re-link the framework after.
8. For Android: add `implementation(projects.domain.<ctx>.{api,data})` to
   `composeApp/build.gradle.kts`, add the data module to `androidDataModules`,
   and set the Android Context holder in `TummyApplication.onCreate`
   (each `:data` module has its own holder — see existing examples in
   `domain/ingredients/data/.../db/DatabaseBuilder.android.kt`).

For Room specifics (the `expect object Constructor` requirement, the
`-Xexpect-actual-classes` flag), see [kmp-gotchas.md](./kmp-gotchas.md#room-kmp).

### Cross-context logic

Domain modules never depend on each other (principle #5). There is also no
`_app` / `orchestration` / `shared` module that aggregates them. Two strategies
instead, in order of preference:

- **VM-level orchestration (default).** If a single ViewModel needs to combine
  `IngredientRepository` + `SymptomRepository`, inject both `:api`s into the
  feature module and do the combine in the VM. The cross-context wiring lives
  in the feature, not in any domain module.
- **New bounded context (only when justified).** If the orchestration is
  stable, non-trivial, reused across multiple features, *and* names a coherent
  domain concept, give it its own context. The new context owns the concept
  end-to-end; it does not link the others as dependencies.

If you find yourself wanting a `domain:<ctx>:data` module to depend on another
context's `:api`, stop — that's the rule this section exists to prevent.

## Presentation layer

VMs use MVI: `BaseViewModel<State, Intent, Event>` with reactive state
(`combine(...).stateIn(...)`). Full pattern in
[code-style.md#mvi--presentation](./code-style.md#mvi--presentation).

## Platform UI

### Android

- `composeApp` is an Android-only module built by the `tummy.android.app`
  convention plugin. Hosts Compose UI, `NavHost`, MaterialTheme.
- Screens live in `composeApp/.../feature/<name>/`.
- VMs accessed via `org.koin.compose.viewmodel.koinViewModel()`, scoped to the
  current `NavBackStackEntry`.
- Navigation via `androidx.navigation.compose`.

### iOS

- `umbrella` produces `TummyShared.framework` with SKIE applied. All VMs,
  tokens, models, and use cases are exported through it.
- `iosApp` is a native Xcode project (SwiftUI). Imports `TummyShared`,
  consumes VMs via lightweight `@MainActor ObservableObject` wrappers that
  read `StateFlow<State>` as `AsyncSequence` (SKIE Flow interop).
- Navigation via `NavigationStack` + `navigationDestination(for:)`. Each
  screen subscribes to the VM's `events` and pushes routes locally.
- Tokens: Kotlin `AppColor` / `AppDp` bridged to `SwiftUI.Color` / `CGFloat`
  via Swift extensions.

## Resources (MOKO)

Strings live in `core/ui-tokens/src/commonMain/moko-resources/<locale>/strings.xml`
(`base` is required, `fr` etc. override subsets). Code accesses them as
`MR.strings.<key>`. Non-string tokens (colors, dimens, typography) are *not*
MOKO — they are Kotlin objects in `core/ui-tokens` to keep them available to
Swift without a Compose runtime dependency on iOS.

iOS bundling has a known sharp edge for static frameworks — see
[kmp-gotchas.md#moko-resources](./kmp-gotchas.md#moko-resources).

## Data storage (Room KMP)

Each bounded context that needs persistence has its **own** Room `@Database`
in its `:data` module. Reasons:

- Migrations are localized per context.
- Schema changes in one context cannot break compilation of another.
- The `@Database` annotation requires all entities up-front — splitting
  avoids mega-databases.

A single `expect fun <ctx>DatabaseBuilder(): RoomDatabase.Builder<XyzDatabase>`
per context provides the platform-specific construction (Android needs a
`Context`, iOS needs a file path under `NSDocumentDirectory`). The SQLite
driver is `BundledSQLiteDriver` on both platforms for behavior parity.

For the `expect object Constructor` requirement and the suppression
annotations, see [kmp-gotchas.md#room-kmp](./kmp-gotchas.md#room-kmp).

## Summary

- Module choice is driven by the business shape, not technical layers.
- `:api` is pure, `:data` is concrete; features only see `:api`.
- Each context has its own DB, its own Koin module, its own migration path.
- The VM layer is pure MVI; platforms adapt it to their UI idioms.
- Shared code stops at the rendering boundary.
