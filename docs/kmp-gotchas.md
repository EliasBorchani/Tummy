# KMP gotchas

Platform-specific quirks of the Kotlin / KMP / Room / MOKO / SKIE stack we use.
Each entry has a one-line summary; consult this file when you hit any of these
symptoms.

## commonMain

### `Dispatchers.IO` is iOS-internal — don't use it in commonMain

`kotlinx.coroutines.Dispatchers.IO` is `internal` on Kotlin/Native iOS. Compiles
on Android, fails on iOS link. Don't add `flowOn(Dispatchers.IO)` in
commonMain code. Room runs queries on its own dispatcher off the main thread,
and `combine` doesn't block, so explicit dispatching isn't needed.

### `kotlinx-datetime` 0.7+ moved `Instant` and `Clock` to stdlib

```kotlin
// good
import kotlin.time.Instant
import kotlin.time.Clock

// breaks since 0.7
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock
```

`LocalDate`, `LocalDateTime`, `TimeZone` etc. *stay* in `kotlinx.datetime`.

### `@JvmInline` needs an explicit import in commonMain

```kotlin
import kotlin.jvm.JvmInline  // required, even though it compiles on JVM without it
```

## Room KMP

### Each `@Database` requires an `expect object Constructor`

Room generates a different concrete subclass per target. The `expect object` is
the bridge — KSP fills in the `actual` per-target.

```kotlin
@Database(entities = [...], version = 1, exportSchema = true)
@ConstructedBy(XxxDatabaseConstructor::class)
abstract class XxxDatabase : RoomDatabase() {
    abstract fun xxxDao(): XxxDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object XxxDatabaseConstructor : RoomDatabaseConstructor<XxxDatabase> {
    override fun initialize(): XxxDatabase
}
```

The `@Suppress` silences false-positive "no actual for expect" warnings — KSP
generates the `actual` after the standard expect/actual matching pass, so the
analyzer doesn't see them in time. Don't remove the annotation.

### Beta-feature warning is silenced project-wide

`expect/actual` for non-class types is still in Beta. The
`-Xexpect-actual-classes` compiler flag opts in and silences the warning. It's
applied in `build-logic/convention/.../KmpLibraryConventionPlugin.kt`, so every
KMP module inherits it via `tummy.kmp.library`. The flag and the per-declaration
`@Suppress` cover **two different** warnings — keep both.

### Date stored as ISO string

`LocalDate` round-trips to TEXT columns via `.toString()` / `LocalDate.parse()`.
Sorts lexicographically the same as chronologically. Easier debugging than
epoch-day longs.

### Composite PKs over auto-increment when there's a natural key

For `(date, ingredient)` style data, prefer composite primary keys with
`OnConflictStrategy.IGNORE` — dedup is automatic, deletes are by domain key,
no extra column.

## MOKO Resources

### Source files live in `moko-resources/`, not `resources/MR/`

MOKO 0.26 expects `src/commonMain/moko-resources/<locale>/strings.xml`.
The pre-0.24 `resources/MR/<locale>/strings.xml` layout is silently ignored —
no Kotlin generated, no error. If `MR.strings.<get-$instance>` doesn't resolve
or you see no `MR` class generated, verify the file location.

### iOS static framework: bundle must be copied manually into the .app

With `isStatic = true`, MOKO does **not** automatically copy its `.bundle` into
`TummyShared.framework` or the .app. The bundle is generated at the klib level
under `core/ui-tokens/build/classes/kotlin/<target>/main/klib/.../resources/`.
At runtime, `MR.strings.<get-$instance>` crashes via
`CallInitGlobalPossiblyLock` because `NSBundle(identifier:)` finds nothing.

Fix: the iOS Run Script Phase in `iosApp/project.yml` runs after
`embedAndSignAppleFrameworkForXcode` and `cp -R`s the bundle into
`$BUILT_PRODUCTS_DIR/$CONTENTS_FOLDER_PATH/`, renaming `:` → `_` in the file
name. NSBundle then finds it via `CFBundleIdentifier`
(`com.tummy.tokens.resources.main`, set in the bundle's Info.plist).

The colon-in-filename comes from the Gradle path `:core:ui-tokens` — cosmetic,
not the lookup key.

### Accessing MR

| Where | How |
|---|---|
| Compose (Android) | `stringResource(MR.strings.X.resourceId)` — Compose's helper accepts the @StringRes Int |
| Kotlin commonMain / iosMain | `MR.strings.X.desc().localized()` (iOS) — needs `import dev.icerock.moko.resources.desc.desc` |
| Swift via SKIE | `MR.strings.shared.X.localized()` — `.shared` for the singleton, plus the custom `ResourcesStringResource` extension in `iosApp/iosApp/Extensions/LocalizedStringResource.swift` (SKIE doesn't bridge MOKO's `desc()` extension) |

## SKIE / Swift interop

### Sealed-class subclasses use concatenated names in Swift

For Kotlin:
```kotlin
sealed interface HomeIntent {
    data class PreviousDay : HomeIntent
    data class DeleteIngredient(val ingredient: Ingredient) : HomeIntent
}
```

Swift sees:
- **Construction**: `HomeIntentPreviousDay()`, `HomeIntentDeleteIngredient(ingredient: ...)` — no dot.
- **Pattern matching**: `switch onEnum(of: event) { case .previousDay: ...; case .deleteIngredient(let e): ... }` — lowerCamelCase cases inside SKIE's `__Sealed` enum.

When in doubt, look at the SKIE-generated Swift in
`umbrella/build/skie/binaries/debugFramework/DEBUG/iosSimulatorArm64/swift/generated/...`.

### `MR.strings` access from Swift

Kotlin nested objects bridge as Swift classes with `.shared` for the singleton.
`MR.strings` instance member access requires the singleton:
```swift
MR.strings.shared.app_name  // ResourcesStringResource
```

### `value class` is erased to its underlying type in Swift

`@JvmInline value class FooId(val raw: String)` becomes plain `String` to
Swift — `\.id.raw` keypaths break and SKIE can't generate structs. Any type
consumed by SwiftUI (models, IDs, tokens) must be `data class`.

### Function names starting with `init` collide with Swift initializers

Rename in Kotlin (e.g., `initTummyKoin` → `startTummyKoin`) or SKIE drops them
from the framework.

### Expose Koin VMs through a typed `object TummyDI` façade

Direct typed calls (`TummyDI.shared.mealViewModel()`) are cleaner than a
reflection-based resolver in Swift.

### SwiftUI `@StateObject` + `@MainActor` VMs

Use explicit `init` that builds the wrapper:

```swift
init(...) {
    self._obs = StateObject(wrappedValue: HomeObservable(vm: Resolver.homeViewModel))
}
```

Default-arg form triggers Swift 6 actor-isolation errors.

## Android / AGP 9

### `com.android.library` + `kotlin.multiplatform` is rejected

For KMP library modules, use `com.android.kotlin.multiplatform.library`
(unified plugin, aliased as `libs.plugins.androidKmpLibrary` in our catalog).

### Don't apply `org.jetbrains.kotlin.android` alongside `com.android.application`

AGP 9 ships Kotlin built-in. Adding the standalone Kotlin plugin to
`composeApp` fails the build.

### Compose on `composeApp` uses AndroidX BOM, not Compose Multiplatform

Pure Android. Use `libs.androidx.compose.*` (via BOM). The
`compose.runtime` String accessor in JB Compose MP 1.10 is deprecated and
unnecessary — we don't ship CMP runtime on iOS.

### `composeApp` source layout

`composeApp` is a regular Android app — uses the standard AGP layout
(`src/main/kotlin`, `src/main/AndroidManifest.xml`). KMP library modules
keep the multiplatform layout (`src/commonMain`, `src/androidMain`,
`src/iosMain`).

## Koin

### Koin 4.2 dropped `GlobalContext`

Use `org.koin.mp.KoinPlatform.getKoin()` instead.

### No `koin-annotations`

We tried `@Single` / `@Factory` / `@Module` + KSP and reverted: generated
`.module` extensions don't cross module boundaries cleanly in our multi-module
KMP setup. All DI is plain DSL: `val xxxModule = module { ... }`.

## iOS / Xcode

### Deployment target is 18.0

`NavigationPath` (iOS 16+) and newer concurrency ergonomics. No reason to
target lower.

### `iosApp.xcodeproj` is generated by XcodeGen — never edit in Xcode

All build settings (bundle ID, deployment target, Run Script phases, xcconfig
binding, sandboxing) live in `iosApp/project.yml`. After editing, run
`iosApp/regen.sh`. The `.xcodeproj` is gitignored.

Adding a new Swift file requires no project edit — XcodeGen auto-discovers
files via `sources: - path: iosApp` in the spec.

## Gradle

### `--no-configuration-cache` is currently required

Room and MOKO don't fully support it. Use the flag in CI; local builds without
it may emit warnings.

### SKIE caps Kotlin version

SKIE follows Kotlin with a 1–4 week lag. Before bumping `kotlin` in the
catalog, check <https://skie.touchlab.co/intro> for compatibility.
