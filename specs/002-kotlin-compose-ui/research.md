# Research: Kotlin and Compose Migration

**Selection date**: 2026-09-15
**Baseline**: `f62519b4bff420ab3f78b1d544732c25683f4772`, plus feature-002 specification work.
**Scope**: Source inspection, official documentation, published POM/module/AAR inspection.
No application implementation, Gradle dependency resolution, compilation, IDE execution, device
validation, SDK installation/license acceptance, or live provider audit occurred during planning.

## R1. Toolchain and Exact Dependency Selection

**Decision:** Retain AGP 9.4.0, Gradle 9.6.0, JDK 17, Java/JVM target 8, Build Tools 36.0.0,
compile/target SDK 37, and Android Studio Quail 4 (2026.1.4). Enable AGP built-in Kotlin
**2.2.10** and apply `org.jetbrains.kotlin.plugin.compose` **2.2.10**. Raise minSdk to the
owner-approved **23** during implementation, not as part of these documentation changes.

**Rationale:** The [AGP 9.4 compatibility table](https://developer.android.com/build/releases/agp-9-4-0-release-notes)
supports the retained toolchain. The [Studio stable release page](https://developer.android.com/studio/releases)
identifies Quail 4. [Android 17 SDK setup](https://developer.android.com/about/versions/17/setup-sdk)
uses compile/target 37. Select Android 17/API 37 as the dated current runtime alongside API 23.
The [Compose compiler guide](https://developer.android.com/develop/ui/compose/compiler) requires
a compiler plugin matching Kotlin, independently of the Compose library BOM.

### Compiler research blocker resolved by primary artifacts

The research child could not extract the AGP POM with its web fetch tool and correctly left
Kotlin's exact version unknown. Parent follow-up fetched and parsed published artifacts with
Python urllib, without executing downloaded code or changing Gradle configuration:

- [AGP 9.4.0 POM](https://dl.google.com/dl/android/maven2/com/android/tools/build/gradle/9.4.0/gradle-9.4.0.pom):
  runtime dependency `org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.10`.
- [AGP 9.4.0 module metadata](https://dl.google.com/dl/android/maven2/com/android/tools/build/gradle/9.4.0/gradle-9.4.0.module):
  `runtimeElements` requires `kotlin-gradle-plugin` version `2.2.10`.
- [Compose compiler Gradle plugin 2.2.10 POM](https://repo.maven.apache.org/maven2/org/jetbrains/kotlin/compose-compiler-gradle-plugin/2.2.10/compose-compiler-gradle-plugin-2.2.10.pom):
  references KGP/API/model 2.2.10; the matching artifact exists.

This resolves the planning pin, not a claim that a mixed-language build has run. During
implementation, inspect buildscript resolution and reject unexpected compiler/plugin divergence.
Do not infer the compiler version from a newer Kotlin stdlib transitive dependency.

### Direct dependency baseline

| Coordinate / plugin | Pin | Configuration / role |
|---|---|---|
| `org.jetbrains.kotlin.plugin.compose` | 2.2.10 | Root plugin declaration, applied in app |
| `androidx.compose:compose-bom` | 2026.09.00 | Same platform in implementation and androidTestImplementation |
| `androidx.compose.ui:ui` | BOM: 1.12.1 | UI/semantics |
| `androidx.compose.foundation:foundation` | BOM: 1.12.1 | Lists, scrolling, layout, pointer input |
| `androidx.compose.runtime:runtime` | BOM: 1.12.1 | State and effects |
| `androidx.compose.runtime:runtime-saveable` | BOM: 1.12.1 | Saved UI state |
| `androidx.compose.material3:material3` | BOM: 1.4.0 | Standard components and light/dark theme |
| `androidx.activity:activity-compose` | 1.13.0 | ComponentActivity/setContent/edge-to-edge |
| `androidx.lifecycle:lifecycle-runtime-compose` | 2.11.0 | Lifecycle-aware state collection |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | 2.11.0 | Screen-scoped ViewModel access |
| `androidx.lifecycle:lifecycle-viewmodel-savedstate` | 2.11.0 | Small reconstruction state when needed |
| `org.jetbrains.kotlinx:kotlinx-coroutines-android` | 1.11.0 | Main-safe loading, scoped jobs, preference Flow |
| `junit:junit` | 4.13.2 | Retained JVM tests |
| `org.jetbrains.kotlinx:kotlinx-coroutines-test` | 1.11.0 | Dispatcher-controlled JVM tests |
| `androidx.test:runner` | 1.7.0 | AndroidJUnitRunner |
| `androidx.test.ext:junit` | 1.3.0 | Instrumented JUnit4 |
| `androidx.compose.ui:ui-test-junit4` | BOM: 1.12.1 | Real Activity Compose tests |
| `libs/pinyin4j-2.5.0.jar` | Existing bytes | Retained local dependency |

Declare BOM-managed libraries without independent version literals. Do not add Navigation,
Fragments, DI, database, kapt/KSP, benchmark, Material2, or extended icon libraries for this work.
If previews are introduced, `ui-tooling-preview`/debug-only `ui-tooling` use the same BOM.
Use real Activity tests initially; `ui-test-manifest` is unnecessary for
`createAndroidComposeRule<DeclaredActivity>()`. If isolated-host tests become necessary, add
that debug-only artifact via the same BOM and justify the host separately.

[Official BOM mapping](https://developer.android.com/develop/ui/compose/bom/bom-mapping) and the
[published BOM POM](https://dl.google.com/dl/android/maven2/androidx/compose/compose-bom/2026.09.00/compose-bom-2026.09.00.pom)
confirm core/UI/test 1.12.1 and Material3 1.4.0. Generic setup prose still mentions the August
BOM; the explicit September mapping/artifact is the selected snapshot. Do not mix these versions
with alpha channels. A BOM is a set of constraints, not a lock of the complete resolved graph.

### Inspected Android artifact constraints

Parent downloaded each listed AAR into memory and read `AndroidManifest.xml` and
`META-INF/com/android/build/gradle/aar-metadata.properties` on the selection date.
Sources use Google Maven's canonical layout:
`https://dl.google.com/dl/android/maven2/<group path>/<artifact>/<version>/<artifact>-<version>.aar`.

| Android artifact | Version | minSdk | minCompileSdk | min AGP |
|---|---|---:|---:|---|
| `androidx.compose.ui:ui-android` | 1.12.1 | 23 | 37 | 9.1.0 |
| `androidx.compose.foundation:foundation-android` | 1.12.1 | 23 | 37 | 9.1.0 |
| `androidx.compose.runtime:runtime-android` | 1.12.1 | 23 | 34 | 8.1.1 |
| `androidx.compose.runtime:runtime-saveable-android` | 1.12.1 | 23 | 37 | 9.1.0 |
| `androidx.compose.material3:material3-android` | 1.4.0 | 21 | 35 | 8.6.0 |
| `androidx.activity:activity-compose` | 1.13.0 | 23 | 36 | 8.9.1 |
| `androidx.lifecycle:lifecycle-runtime-compose-android` | 2.11.0 | 23 | 37 | 9.1.0 |
| `androidx.lifecycle:lifecycle-viewmodel-compose-android` | 2.11.0 | 23 | 37 | 9.1.0 |
| `androidx.lifecycle:lifecycle-viewmodel-savedstate-android` | 2.11.0 | 23 | 34 | 8.1.1 |
| `androidx.compose.ui:ui-test-junit4-android` | 1.12.1 | 23 | 37 | 9.1.0 |
| `androidx.test:runner` | 1.7.0 | 21 | Not declared in AAR metadata | Not declared |
| `androidx.test.ext:junit` | 1.3.0 | 21 | Not declared in AAR metadata | Not declared |

Published Compose prose cites differing AGP minima (9.1.2/9.2.0); selected AGP 9.4 exceeds
those and the inspected metadata. All inspected AARs fit API 23/compile37/AGP9.4. This is not
inspection of every transitive artifact. Coroutines is a JAR, not an AAR with a minSdk manifest;
its [1.11.0 release](https://github.com/Kotlin/kotlinx.coroutines/releases/tag/1.11.0) and
[published Android POM](https://repo.maven.apache.org/maven2/org/jetbrains/kotlinx/kotlinx-coroutines-android/1.11.0/kotlinx-coroutines-android-1.11.0.pom)
reference Kotlin 2.2.20 stdlib. That does not change the selected KGP compiler pin. Full graph,
Kotlin metadata/bytecode compatibility, merged manifests, lint and API23 execution remain
mandatory implementation verification, not unresolved product decisions or claimed passes.

**Alternatives considered:** Separate Kotlin Android plugin conflicts with built-in Kotlin;
blindly using the docs' compiler example 2.3.21 would not match AGP's default. No AGP downgrade,
KGP override, higher minimum, manifest override, or obsolete library workaround is justified.
Generic KGP support tables do not certify this AGP built-in integration; use the AGP-published
runtime dependency and enforce a real enablement build gate before UI work.

### Manifest policy follow-up from cross-artifact analysis

Read-only inspection of published artifacts during analysis found
[Activity Compose1.13.0](https://dl.google.com/dl/android/maven2/androidx/activity/activity-compose/1.13.0/activity-compose-1.13.0.pom)
→ [Core KTX1.18.0](https://dl.google.com/dl/android/maven2/androidx/core/core-ktx/1.18.0/core-ktx-1.18.0.pom)
→ [Core1.18.0](https://dl.google.com/dl/android/maven2/androidx/core/core/1.18.0/core-1.18.0.aar).
The Core AAR manifest declares `${applicationId}.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`
with `signature` protection and a matching uses-permission. The original two-permission-only
acceptance wording therefore conflicted with the planned dependency chain.

**Remediation decision (2026-09-15):** Under the owner's authorization to resolve the findings,
allow only the exact applicationId-expanded protective signature permission alongside the two
existing network permissions. Build contract B2 owns this explicit allowlist. It grants no new
dangerous/runtime capability and is not a blanket exception for dependency manifests or exported
components. Inspect actual merged-manifest attribution/protection level in T009; reject any other
addition for review instead of stripping the protection or silently widening policy. Published
artifact inspection is not a claim that the application's resolved manifest has been tested.

## R2. Source Sets and Incremental Activity Hosts

**Decision:** Keep existing Java under root `src/`; put new Kotlin under
`app/src/main/kotlin/com/goodtrendltd/HolySongs/`. Preserve manifest/res/assets remapping and
`repositoryRoot` for JVM tests. Explicitly register module-local main/test/androidTest Kotlin
roots via `android.sourceSets` Kotlin directories, not the Java source directory mapping.
Remove `android.builtInKotlin=false`; enable Compose, its matching compiler plugin, AndroidX,
and AndroidJUnitRunner. Retain Java target8 and aligned Kotlin JVM8, not runtime-JDK17 bytecode.
Do not add desugaring unless a directly used library/API actually requires it.

Use same-identity Kotlin ComponentActivity hosts for Main, DisplayLyric, Settings and About.
Replace each `.java` definition only when its `.kt` replacement is wired; never compile both
with the same fully qualified name. Keep public Java-callable String constants. Retain native
Activity navigation and the Java/View-based VideoSearch, changing its superclass to
ComponentActivity if needed for lifecycle-aware Back dispatch.

**Rationale:** [Built-in Kotlin migration](https://developer.android.com/build/migrate-to-built-in-kotlin)
documents Kotlin-specific source directories and target alignment. Android's
[incremental strategy](https://developer.android.com/develop/ui/compose/migrate/strategy) and
[interop guide](https://developer.android.com/develop/ui/compose/migrate/interoperability-apis/compose-in-views)
support replacing screens within an existing architecture. The pilot still sends the original
lyric/title extras to the legacy reader.

**Alternatives considered:** Colocating Kotlin in `../src` is valid with explicit Kotlin wiring,
but module-local Kotlin is simpler for new sources. A bulk source move, single-Activity graph,
or embedding the existing full WebView inside Compose creates unnecessary simultaneous changes.

## R3. Catalog, Letter Navigation and Loading Ownership

**Decision:** Extract a small production `SongCatalogLoader` and an immutable result, retaining
XMLParser/ChineseCharComp/HanziHelper. Read the entire UTF-8 asset with a closed stream, remove
only U+0020 across the raw XML before parsing, use the existing parser value extraction, populate
a HashMap in source order (last lyric wins), and sort its effective keys with the existing comparator.
One list ViewModel loads off-main once per owner; expose Loading/Ready/Error, with an explicit
retry. Do not swallow cancellation or trigger reload on recomposition, theme change, or resume.
Use effective title strings as lazy keys after deduplication. Do not save the catalog in a Bundle.

Extract the current section lookup into a small testable `LegacySectionIndex` without replacing
its lookup algorithm with a new first-initial map. Retain A–Z and all valid observed present/absent
letter destinations. Add empty/out-of-range guards; unsafe or unresolvable destinations are no-ops,
not fabricated valid rows. Capture fixtures on API23 and API37 before lookup changes. If parity
and correct present-letter navigation conflict, document the exact mismatch and obtain approval
before changing section semantics. Other screen migration need not invent an absent-letter policy.

**Rationale:** Source inspection shows full-XML space removal, HashMap duplicate resolution,
Collator ordering separate from pinyin, and index code that assumes they align. Source hazards
are not proof of a device failure. Retaining lookup first reduces unrelated algorithm churn.
[Coroutine guidance](https://developer.android.com/kotlin/coroutines/coroutines-best-practices)
requires explicitly moving blocking work off-main; merely marking a function suspend is insufficient.
[Lazy-list guidance](https://developer.android.com/develop/ui/compose/lists) supports stable keys.

**Alternatives considered:** A first-initial scan could simplify indexing, but would silently
change absent-letter and possibly present-letter behavior before baseline evidence. A process-wide
repository/cache, database or alternate parser is unnecessary for 414 effective titles.

## R4. Insets, Theme and UI State

**Decision:** Each migrated Activity gets a no-platform-action-bar window theme, `enableEdgeToEdge`,
one Material3 Scaffold/top app bar, and a single content boundary that applies/consumes Scaffold
padding for both list and sidebar. Reserve sidebar width. Account for horizontal cutouts and
bottom navigation, not just the status bar. Avoid double safe-area padding and hardcoded heights.
Size sidebar drawing/hit testing from its measured content bounds, not display height. In fit
mode all26 measured rows fit without compression; tap/drag selects sections and the rail does not
scroll. Otherwise compact mode uses a scrollable rail: taps select on release within platform
touch slop, while vertical drags past slop scroll only the rail with no song-list jump or release
selection. Stop/cancel/size-or-mode changes cancel pending input and highlight without rolling back
committed fit-mode selection. This avoids ambiguous dual-purpose drags; see U2-RESIZE for tests.

Use `rememberLazyListState` and title-keyed `rememberScrollState`; restore only against Ready
content. Preserve normal Activity stack/selection and small saved indices/offsets across recreation.
Clamp restored offsets after real content/viewport reflow; no exact pixel-location guarantee after
font/layout changes. No new persistent resume-last-song behavior on a fresh task launch. Explicit
user callbacks navigate/share once; do not save/replay pending launches. Replace WindowManager's
letter popup with an in-composition indicator and a 2-second lifecycle-aware cancellable timeout.

**Rationale:** [Insets](https://developer.android.com/develop/ui/compose/system/insets),
[Material inset ownership](https://developer.android.com/develop/ui/compose/system/material-insets),
[state saving](https://developer.android.com/develop/ui/compose/state-saving), and
[side effects](https://developer.android.com/develop/ui/compose/side-effects) support these boundaries.
The reported top-bar overlap is not yet reproduced; source risks do not establish its exact cause.

**Alternatives considered:** Compose does not automatically fix insets; duplicate Holo/Compose
bars and nested full inset modifiers are rejected. A saved whole catalog/lyric copy is unnecessary;
existing single-lyric Intent payload remains supported without additional Bundle duplication.

## R5. Preferences and Accessibility

**Decision:** A small application-context-backed `ReaderPreferences` adapter observes the existing
SharedPreferences file, with a strongly held listener registered before taking its fresh snapshot,
paired unregister and lifecycle-aware collection. Initialize disk-backed reads off-main. Legacy
Settings writes must update the Compose list during the pilot. Use sparse user-triggered `apply()`
writes; do not claim asynchronous persistence is a durability guarantee. Read font values in sp,
keep raw stored values without silent rewriting, and use stored boolean nightMode for dark/light
Material3. The authorized remediation defines display-only effectiveFontSize: integers1–200
inclusive render unchanged in sp; missing/wrong-type/out-of-range values render at20sp without
clamping/persistence. The upper bound is a deliberately generous safety policy, not a platform
maximum. Existing UI choices remain16–30step2/reset20; the full boundary/invalid fixture table
is in data-model.md. Wrong-type nightMode uses its in-memory defaulttrue only; do not clear or
repair the file automatically. Remove Settings' stack-clearing theme workaround once observation
allows normal Back without losing the list.

Standard controls expose labels, roles and state. Test labels/large text; no full TalkBack/sidebar
accessibility gate, per owner clarification. Existing Chinese labels/content remain resources.
About's current text and clickable website/email behavior remain reachable.

**Rationale:** [SharedPreferences](https://developer.android.com/reference/android/content/SharedPreferences)
and [Editor](https://developer.android.com/reference/android/content/SharedPreferences.Editor)
document listener and write behavior. State observation avoids remembered stale preferences.

**Alternatives considered:** DataStore migration, system-controlled theme, forced preference
normalization and writing during composition violate scope or introduce unnecessary churn.

## R6. Retained Video and Failure Boundaries

**Decision:** Keep video in its Java Activity/WebView, repair temporary pause/resume versus final
detach/destroy, guard late callbacks and invalid extras, deny unsolicited geolocation, and preserve
normal SSL rejection. Back cancels foreground loading, otherwise exits fullscreen, otherwise uses
WebView history, otherwise finishes to lyrics. Use AndroidX Back dispatch instead of KEYCODE_BACK-only
handling. Replace nullable Wi-Fi dereferencing with API23 network/capabilities checks and preserve
mobile-data confirmation. No network means a safe error/cancel path, not an assumed reachable site.

Audit all three legacy HTTP providers with Chinese queries on actual WebViews. Apply only verified
same-provider HTTPS/UTF-8 encoding corrections; provider removal/replacement/external-browser changes
and accepted availability limitations need owner approval. Loading cancellation/main-frame errors
must stop or settle the current navigation and allow Back; never relax TLS or global cleartext policy.

**Rationale:** [WebView ownership](https://developer.android.com/reference/android/webkit/WebView),
[predictive Back](https://developer.android.com/guide/navigation/custom-back/predictive-back-gesture),
and [network security](https://developer.android.com/privacy-and-security/security-config).
Source shows destroy-in-onPause followed by later use, unguarded dialog dismissal, unconditional
geolocation approval and HTTP prefixes. These establish work to validate, not live provider outcomes.

**Alternatives considered:** Keeping those hazards because the UI is unchanged fails preservation;
rewriting WebView internals in Kotlin or moving to external browsers is not required by Compose.

## R7. Verification and Remaining Execution Risks

**Decision:** Retain/complete existing Java characterization tests; test extracted production
loading/indexing, coroutine cancellation and preference behavior. Use real Activity Compose tests
and device scenarios for extras, shares, state, insets and interop. Apply contracts/ui.md's
checkpoint matrix: full Main behavior and defined retained-screen safety/interoperability at US1,
full reader/settings/about restoration/reflow at US2, video verification at US3, all flows at final
acceptance. Later-scope unexecuted checks are not MVP passes; no introduced regression is waived. A StateRestorationTester is not
proof of process death; wait for Ready explicitly because asset jobs are outside Compose's clock.
Use semantics labels, not test tags as accessibility substitutes; scroll to lazy rows before checks.

**Rationale:** [Testing patterns](https://developer.android.com/develop/ui/compose/testing/common-patterns),
[APIs](https://developer.android.com/develop/ui/compose/testing/apis), and
[synchronization](https://developer.android.com/develop/ui/compose/testing/synchronization).
Existing tests lack full order/duplicate-pair fixtures and strong helper characterization; old
passing tests do not establish runtime parity. See predecessor `evidence/us2-reader.md`.

**Alternatives considered:** JVM-only sign-off, previews, a new benchmark suite, fake provider
success, or merging evidence from different APKs are not acceptable substitutes.

**Planning resolution:** All technical selections have explicit decisions. No unresolved
clarification blocks Phase 1. Actual dependency resolution/locks, compile/lint, device availability,
per-runtime sidebar fixtures, overlap reproduction, provider approval outcomes and real upgrade
signing remain execution gates. Do not claim them passed. Stop the affected implementation slice
if metadata/resolution contradicts this plan; do not silently raise minSdk or change algorithms.

## Research Provenance

Two bounded read-only child briefs informed this synthesis: workflow
`7b25a65d-e15c-4a91-bd87-e03990dc03fd`; dependency child
`25ad581f-d6db-4ee0-8750-47ad7046dce5`; design child
`f4efbc32-4efe-4769-ae36-50a27cf59762`. Their output references were
`dependency-research.md` and `compose-design-research.md` in workflow-managed session artifacts.
The parent resolved the compiler unknown and directly inspected the artifacts listed in R1;
all material decisions/evidence needed by the project are preserved here rather than depending
on those private session files. Source-check automation was inconclusive; official source reads
and artifact inspections, not an automated certification, underpin the conclusions.
