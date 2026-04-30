# Dependencies

This document describes who maintains each dependency, how tightly it is
coupled to the rest of the stack, and how upgrades typically play out. For
pinned versions, see [`gradle/libs.versions.toml`](../gradle/libs.versions.toml).

## Tier 1 — Kotlin-coupled (lock-step)

These libraries track the exact Kotlin compiler version. A Kotlin upgrade
cannot happen until **all** of them support the target version.

| Library | Maintainer | Cadence | Notes |
|---|---|---|---|
| **Kotlin** (KMP, stdlib, compiler plugin) | JetBrains | ~4 majors/year + monthly patches | Major releases (2.0, 2.1, 2.2…) bring compiler-flag and K2 changes. Minor releases are generally safe. |
| **KSP** | Google | Matches each Kotlin release | Version format `<kotlin>-<ksp-patch>`. Must match Kotlin to the byte. No upgrade work beyond the version bump. |
| **Compose Compiler plugin** (`org.jetbrains.kotlin.plugin.compose`) | JetBrains | Version = Kotlin version | Always bump together. |
| **SKIE** | **Touchlab** (~15-person company, US) | ~1 release/month | Follows Kotlin with 1–4 weeks lag. **Effectively gates every Kotlin upgrade.** Check <https://skie.touchlab.co/intro> before bumping Kotlin. |

**Implication.** Your Kotlin upgrade calendar is dictated by SKIE. No SKIE
support ⇒ wait.

## Tier 2 — Large ecosystems, independent cadences

These evolve on their own schedule. Major releases introduce breaking changes;
minors and patches are usually smooth.

| Library | Maintainer | Cadence | Notes |
|---|---|---|---|
| **Gradle** | Gradle Inc. | Minor every ~3 months, major every 1–2 years | Major bumps (8→9) invalidate old plugin APIs and deprecate build-script patterns. Wrapper upgrade is one line; fallout is in chasing deprecation warnings. |
| **AGP** (Android Gradle Plugin) | Google | Minor every 2–3 months, major yearly | **The riskiest yearly upgrade after Kotlin.** Touches the plugin graph (8→9 forced `com.android.kotlin.multiplatform.library`), DSL, sometimes `compileSdk`. Google publishes a public roadmap 6 months ahead and an "Upgrade Assistant". |
| **Room** (KMP) | Google / AndroidX | Minor every ~2 months | Stable KMP support since 2.8. Once in a stable line, upgrades are routine. Broad Kotlin compatibility. |
| **AndroidX Compose** (via BOM) | Google | Monthly BOM release | The BOM pins a coherent set; bump conservatively. |
| **Jetpack Lifecycle** (JB multiplatform port, `org.jetbrains.androidx.lifecycle`) | JetBrains | Tracks AndroidX with a lag | Version numbers match AndroidX but not all versions are published on the JB side (e.g. `2.8.7` exists in AndroidX, not in JB). Always verify the artifact exists before bumping. |
| **Ktor** | JetBrains | Minor every ~2 months | Stable 3.x line. Slightly looser Kotlin coupling than compiler plugins. |
| **kotlinx** (coroutines, serialization, datetime) | JetBrains | Variable | `kotlinx-datetime` 0.7 moved `Instant` / `Clock` into Kotlin stdlib (`kotlin.time.*`) — watch for this kind of boundary shift in release notes. |

**Implication.** These are usually safe to bump regularly. Budget a few
minutes for minors, a few hours for majors (mostly AGP).

## Tier 3 — Smaller community libraries

High utility, structurally fragile. If a maintainer disappears, the choice is
to fork or to migrate.

| Library | Maintainer | Fragility | Exit plan |
|---|---|---|---|
| **Koin** | InsertKoinIO (Arnaud Giuliani + ~5 active maintainers) | Low. Mature, broadly used. OSS, no corporate guarantee. | Migrate to Kodein or hand-rolled DI. |
| **MOKO Resources** | **IceRock** (~10-person company, Russia) | Moderate. Bus factor on a few engineers; Gradle/Kotlin compat sometimes lags. | Migrate to Compose Multiplatform Resources (requires dropping the "no CMP runtime on iOS" stance) or a custom resources generator. |
| **Napier** | aAkira (solo maintainer) | Very low code surface. | Replace with a thin custom wrapper over platform loggers in an afternoon. |

**Implication.** Keep a mental migration plan for the moderate-risk ones. SKIE
has the largest blast radius of the "small-company" dependencies because there
is no mature equivalent for Swift interop today.

## Tier 4 — Apple toolchain

| Tool | Role | Upgrade behavior |
|---|---|---|
| **Xcode / Swift / SwiftUI / iOS SDK** | IDE and iOS platform | Yearly bump (Sept/Oct). Swift preserves source compat well; Xcode periodically raises the minimum `IPHONEOS_DEPLOYMENT_TARGET`. **SKIE must catch up to new Xcode versions** — new Xcode has broken SKIE in the past (weeks-long gap). |

## Canonical Kotlin upgrade flow

Every Kotlin upgrade follows the same sequence:

1. **Check SKIE compatibility** at <https://skie.touchlab.co/intro>. No support ⇒ stop.
2. Bump `kotlin` in `libs.versions.toml`.
3. Bump `ksp` to the matching `<kotlin>-<ksp-patch>`.
4. Bump `composeCompiler` to equal Kotlin.
5. Bump `skie` to a version that supports the new Kotlin.
6. Run:
   ```
   ./gradlew :composeApp:assembleDebug :umbrella:linkDebugFrameworkIosSimulatorArm64
   ```
   Fix regressions (typical fallout: stdlib deprecations, one plugin needing its own bump).
7. Optionally bump Ktor / Room / Koin / MOKO in a separate commit to isolate regressions.

Budget: **15 min for a minor** (2.3.20 → 2.3.21), **1 h to 1 day for a major**
(2.3.x → 2.4.0) depending on breaking changes.

## Gradle / AGP upgrade flow

Independent of Kotlin. Majors land roughly yearly.

- **Gradle**: bump `gradle-wrapper.properties`, run `./gradlew --stop` to clear
  daemons, run a build, chase deprecation warnings. A major usually removes
  APIs a few plugins rely on — those plugins need their own bump first.
- **AGP**: typically the hardest yearly upgrade. Read release notes end-to-end.
  For a KMP project, expect DSL shifts around the android/kmp boundary (8→9
  introduced the unified `com.android.kotlin.multiplatform.library` plugin).
  Plan half a day.

## Single source of truth

All versions live in [`gradle/libs.versions.toml`](../gradle/libs.versions.toml).
When in doubt, start there and follow the catalog references out.

## Monitoring

- **Kotlin**: <https://blog.jetbrains.com/kotlin/>
- **SKIE**: <https://skie.touchlab.co/changelog/>
- **AGP**: <https://developer.android.com/build/releases/gradle-plugin>
- **Gradle**: <https://gradle.org/releases/>
- **Compose Multiplatform**: <https://github.com/JetBrains/compose-multiplatform/releases>
- **Room**: <https://developer.android.com/jetpack/androidx/releases/room>
- **Ktor**: <https://ktor.io/docs/releases.html>
- **Koin**: <https://github.com/InsertKoinIO/koin/releases>
- **MOKO Resources**: <https://github.com/icerockdev/moko-resources/releases>

## TL;DR operational

- Active watch: **SKIE** (monthly), **Kotlin** (quarterly), **AGP** (semi-annually).
  Everything else: bump when convenient.
- Fragility order: **SKIE > MOKO > Koin / Room**. SKIE is the hardest to
  replace; plan accordingly if it shows signs of slowing down.
- Single lever: everything funnels through `gradle/libs.versions.toml`.
