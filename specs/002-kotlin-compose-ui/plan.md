# Implementation Plan: Incremental Kotlin and Jetpack Compose UI

**Feature context**: `002-kotlin-compose-ui` | **Date**: 2026-09-15 | **Spec**: [spec.md](spec.md)

**Git branch**: `master`; no feature branch created. Baseline: `f62519b4bff420ab3f78b1d544732c25683f4772`.

**Status**: Implemented and owner-accepted on 2026-09-16. The detailed task/evidence record remains
historical and intentionally distinguishes automated evidence from the owner's final phone acceptance.

**Input**: [Feature specification](spec.md), [scope transition](transition.md), and
[constitution](../../.specify/memory/constitution.md).

## Active execution override

Apply the owner's [2026-09-15 continuation decisions](evidence/execution-decisions.md): accept
owner-attested Studio build and existing Java25 daemon, waive API23 runtime/fixture execution,
retain minSdk23/dependency compatibility and all API37 scenario/safety gates. The original
two-endpoint plan below remains historical; never describe the waived endpoint as tested.

## Summary

Introduce Kotlin and Compose without first translating all legacy UI classes. Keep one app
module and the existing multi-Activity navigation, replace core screen hosts with same-name Kotlin
ComponentActivities, and use Material3 Compose content with explicit window-inset ownership.
Deliver the song-list/sidebar pilot first, retaining the Java lyric/settings/about/video paths
until their own verified slices. The owner approved API23 minimum and limited accessibility
acceptance to standard-control labels and large text, deferring full TalkBack/sidebar support.

Extract only small production loader/index/preference adapters needed to remove Activity-owned
work and test preserved behavior. Keep Java XML/pinyin/Collator helpers, the bundled asset/JAR,
SharedPreferences and WebView. Correct observed/source-supported compatibility hazards within
the affected slices, not through a blanket cleanup. No source/build implementation is part of
this planning command, and the owner's existing `res/layout/main.xml` edit remains untouched.

## Technical Context

**Language/Version**: Kotlin2.2.10 via AGP built-in Kotlin; Compose compiler plugin2.2.10;
Java8 source/target and aligned Kotlin JVM8; JDK17 build runtime/toolchain; Groovy Gradle scripts.

**Primary Dependencies**: AGP9.4.0, Gradle9.6.0; Compose BOM2026.09.00 (UI/Foundation/Runtime/tests1.12.1,
Material3 1.4.0); Activity Compose1.13.0; Lifecycle runtime-compose/viewmodel-compose/viewmodel-savedstate2.11.0;
coroutines Android/test1.11.0; retained pinyin4j2.5.0 JAR. Exact coordinates and inspected metadata:
[research.md](research.md). No Navigation/DI/database/KSP/kapt/benchmark requirement.

**Storage**: Unchanged `assets/songs.xml`; private SharedPreferences `appPrefFile`, integer
`fontSize`, boolean `nightMode`. No persistent schema/store migration or new song IDs.

**Testing**: Retain Java JUnit4.13.2 tests; add actual-loader/index/ViewModel/Flow tests and real
Activity Compose tests with AndroidJUnitRunner1.7.0, AndroidX ext JUnit1.3.0 and BOM-aligned UI tests.
Manual runtime/integration checks complement instrumentation. No testing was executed during planning.

**Target Platform**: Android API23+ (Android6 minimum); compile/target37, Build Tools36.0.0.
Dated current endpoint: Android17/API37; phones in gesture/three-button modes and a large/resizable
configuration. Android Studio Quail4 / 2026.1.4 with Gradle JDK17. Actual device/IDE validation pending.

**Project Type**: Single-module, offline-first Android mobile app.

**Performance Goals**: Load/parse/derive catalog off-main once per state owner; preserve usable
scrolling and record launch-to-usable-list elapsed time, visible stalls and crash/ANR observations
per endpoint. No numerical latency SLA or benchmark suite. Zero crashes/ANRs in required scenarios.

**Constraints**: Preserve application/component identity, 422 raw entries/414 effective titles,
last-entry duplicate resolution, ASCII-space removal, sorting/pinyin semantics, existing user
settings and actions. No minSdk above23 without renewed approval; no provider change/accepted
unavailability without approval, transport-security bypass, release signing or publication.
Manifest permissions follow build contract B2's exact allowlist: the two existing network
permissions plus AndroidX Core's app-private signature declaration/use only. No other new
permissions, including dangerous/runtime permissions, are authorized.

**Scale/Scope**: Four core reader Activities, one retained Java/View video Activity, 26-letter
sidebar, eight duplicate source-title pairs, eight font sizes, two themes and three video providers.

## Constitution Check

*Pre-research and post-design gates: PASS for the planned design. This is not implementation or
runtime acceptance. No constitutional exception is requested.*

| Principle / constraint | Pre-research | Post-design evidence |
|---|---|---|
| Preserve core mission | Pass: offline reader/actions retained | Unchanged data/preferences, U1–U4/N1–N4 contracts; provider failure cannot block offline return |
| Incremental modernization | Pass: working slices | Same-name Activity hosts; list pilot interoperates with legacy screens; no bulk conversion |
| Compatibility/reliability | Pass: approved support change | API23 owner approval, exact primary-artifact compiler/AAR research, both-endpoint verification gates |
| Simple architecture | Pass: no speculative layers | One module, concrete loader/index/preferences, per-screen state; no DI/database/navigation graph |
| Quality through verification | Pass: baseline before edits | Actual-loader tests, device fixtures, final-artifact attribution, no false pass for missing evidence |
| Accessibility/Chinese readability | Pass: bounded owner decision | Standard semantics labels/large text required; full TalkBack/sidebar explicitly deferred |
| Deprecated APIs when touched | Pass: scoped modernization | Remove WindowManager popup and stack-reset workaround in migrated UI; safe network/Back/WebView ownership |
| Governance/scope | Pass: new spec records expansion | Feature001 history retained; feature002 carries outcomes without requiring discarded Java UI fixes |

The existing ratification-date TODO is unrelated and does not create an implementation exception.
If implementation contradicts an inspected dependency constraint or preserved behavior, stop the
affected slice for plan correction/approval rather than bypassing the gate.

## Project Structure

### Documentation (this feature)

```text
specs/002-kotlin-compose-ui/
├── spec.md
├── transition.md
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/
    ├── build-and-verification.md
    ├── ui.md
    └── navigation-and-video.md
```

[tasks.md](tasks.md) is the subsequently generated `/speckit.tasks` artifact. Future `evidence/` holds
scenario definitions and sanitized results; absence of runtime records is not a passing outcome.

### Source Code (existing and proposed)

```text
AndroidManifest.xml                     # Root mapping retained; per-migrated-screen window themes
build.gradle / gradle.properties         # Matching Compose plugin, built-in Kotlin, AndroidX
app/build.gradle                        # min23, Compose, Kotlin roots, tests and dependency locks
src/com/goodtrendltd/HolySongs/
├── helpers/*.java                      # Retained XMLParser / ChineseCharComp / HanziHelper
├── VideoSearch.java / HTML5WebView.java # Retained, scoped lifecycle/security/Back corrections
└── legacy UI *.java                    # Remove each only with verified same-name Kotlin replacement
app/src/main/kotlin/com/goodtrendltd/HolySongs/
├── MainActivity.kt
├── DisplayLyricActivity.kt
├── SettingsActivity.kt
├── AboutActivity.kt
├── data/
│   ├── SongCatalog.kt
│   ├── SongCatalogLoader.kt
│   ├── LegacySectionIndex.kt
│   └── ReaderPreferences.kt
└── ui/
    ├── SongListViewModel.kt
    ├── SongListScreen.kt / LetterSidebar.kt
    ├── LyricScreen.kt / SettingsScreen.kt / AboutScreen.kt
    └── HolySongsTheme.kt
app/src/test/java/                      # Existing Java tests retained and completed
app/src/test/kotlin/                    # Production loader/index/state tests
app/src/androidTest/kotlin/             # Real Activity UI/preferences/interop tests and fixtures
res/ / assets/ / libs/                   # Existing mapped roots; remove only verified unused UI resources
```

**Structure decision:** New Kotlin uses conventional module-local roots explicitly registered on
Android source sets' Kotlin directories. Java remains `../src`; no bulk relocation. Root manifest,
res/assets mapping and JVM-test repositoryRoot property remain. Public constants remain Java-callable;
never compile a legacy Java Activity and replacement Kotlin Activity with the same fully qualified name.

## Phase 0: Research Outcomes

[research.md](research.md) resolves all technical selections with decision/rationale/alternatives:

1. Primary AGP9.4 POM/module confirms KGP2.2.10; matching Compose compiler artifact exists.
   BOM/POM and direct AAR inspections support the selected API23/compile37 toolchain.
2. Built-in Kotlin uses explicit Kotlin source-set wiring; keep Java/Kotlin target8, no separate
   Kotlin Android plugin or legacy compiler-extension setting. Enable AndroidX without Jetifier
   unless an actual dependency requires it; the vendored pinyin JAR is not an Android support library.
3. Keep multi-Activity architecture; migrate full screen content, not navigation/storage frameworks.
4. Extract the real load/index pipeline and preserve fixtures before fixes; do not silently substitute
   a first-initial scan or pinyin sort. Unsafe lookup destinations are safe no-ops, not fake parity.
5. Scaffold/inset boundaries, saved UI state and observable legacy preferences replace window/stack
   workarounds. Kotlin/Compose adoption alone is not a verified fix for the owner's overlap.
6. Retain Java WebView while repairing ownership/network/Back/security; audit providers separately.
7. Use production/JVM and real runtime tests with artifact-linked evidence and known accessibility scope.

Full transitive resolution/lock verification, mixed compilation, IDE/device checks and provider
outcomes are planned execution gates, not successes established by research. No unresolved product
clarification blocks Phase1. Incompatible dependency findings or section-semantic changes require review.

## Phase 1: Design and Contracts

### Runtime ownership

- **Main:** Thin ComponentActivity; Activity-scoped list ViewModel initializes one main-safe catalog
  load. Ready data is immutable. Preserve HashMap/comparator/parser semantics; use effective title
  lazy keys. Loading/empty/error handling is explicit and cancellation-safe.
- **UI:** Single Material3 app bar/no native duplicate; apply and consume innerPadding once for
  shared content including sidebar. If all26 measured letter rows fit without compression, taps/drags
  select sections; otherwise compact-mode taps select on release and vertical drags past platform
  touch slop scroll only the rail. Cancel pending input on stop/cancel/size-or-mode change, never
  emitting an extra release selection. Preserve in-composition current-letter indication with
  lifecycle-cancelled 2-second timeout; exact interaction tests are in U2-RESIZE.
- **State:** Save small selection/scroll state, restore after Ready; no whole catalog Bundle or
  replayable navigation flags. Native Activity stack restores destinations. Fresh task starts at Main.
- **Preferences:** Application-context concrete adapter/listener; fresh snapshot on subscription,
  lifecycle-aware collection, sparse user-event writes. Legacy writers work during the pilot;
  normal Settings Back replaces CLEAR_TOP once reactive updates are in place. Derive display-only
  effectiveFontSize from integers1–200 inclusive, otherwise20; never rewrite raw invalid/missing
  values automatically or expand the eight selectable UI sizes. The data-model fixture table is
  authoritative; adapter tests run in US1 and migrated lyric/settings rendering tests in US2.
- **Reader/about:** Same extras and content; title-keyed lyric scrolling, stored font sp and theme.
  Standard components expose labels; preserve about links and existing shares/menu actions.
- **Video:** Java ComponentActivity/View ownership as needed, null-safe network checks, validated
  extras, controlled loading/fullscreen/history Back and final detach/destroy. Provider URLs remain
  approval/audit-bound; no safety waiver for an unavailable site.

### Generated interfaces

- [data-model.md](data-model.md): Immutable catalog, duplicate winners, preferences, state lifetimes,
  exact extras and verification records.
- [contracts/build-and-verification.md](contracts/build-and-verification.md): Build/source/dependency,
  identity/signature and final/slice acceptance interfaces.
- [contracts/ui.md](contracts/ui.md): U1–U4 loading, indexing, inset/font/labels, state and runtime matrix.
- [contracts/navigation-and-video.md](contracts/navigation-and-video.md): N1–N4 Activity/share/provider,
  failure/security/Back and lifecycle contracts.
- [quickstart.md](quickstart.md): Prospective runnable validation commands and manual scenarios,
  dedicated-device safety and artifact evidence. No full implementation code or test suites embedded.

## Delivery Boundaries for Subsequent Task Generation

These are design stages, not checked execution tasks or authorization to publish PRs.

| Stage | Scope | Exit gate |
|---|---|---|
| 0. Baseline | Complete existing test gaps; capture API23/API37 order/sidebar, payload and overlap fixtures | Preserve exact expected behavior; explicitly block unavailable runtime evidence |
| 1. Enablement | Built-in Kotlin/Compose, source roots, min23, dependencies/test runner; no broad UI rewrite | Clean mixed build/IDE, actual resolved compiler/graph/locks, metadata, JVM/lint, APK inspection |
| 2. List pilot | Main Kotlin host, real loader/index/state/preferences, list/rail/insets/indicator | All U1; Main-only U2/U4; legacy preference-to-Main sync and retained-screen safety/interoperability smoke per contracts/ui.md checkpoint matrix, on both endpoints |
| 3. Reader | Kotlin lyric host/content, safe extras/shares/network confirmation | Effective lyrics, all duplicate winners, fonts, shares/video interop and restoration |
| 4. Settings/about | Compose replacements, normal Back/reactive theme; preserve text/links | All font sizes/reset/themes, legacy stored fixtures, list/reader state on return |
| 5. Retained video | Java lifecycle/loading/security/Back plus provider audit and approved HTTPS corrections | N3/N4 on both endpoints, approved provider limitations/changes, safe offline return |
| 6. Integration | Remove only unused legacy UI, update README/support/signing, final exact-source replay | All contract gates for final artifact; no unexecuted checks called passing |

Prefer the enablement plus list pilot as the first user-visible implementation checkpoint/PR,
with separable commits and working checks. [Checkpoint applicability](contracts/ui.md#checkpoint-applicability)
explicitly assigns full reader/settings/about restoration, reflow and new font-fallback rendering
to US2, not US1. Later-scope checks remain unexecuted; newly introduced regressions still block
the current checkpoint. Shared file edits are serialized. A concrete legacy
crash/security defect encountered by the pilot must be fixed or block that checkpoint; the later
video stage is not permission to ship unsafe affected flows. No need to finish Java-only T022–T026
prescriptions before replacing their UI; [transition.md](transition.md) maps retained outcomes.

### Requirement coverage

| Spec requirements | Design / verification |
|---|---|
| FR-001–FR-003 | R1/R2, build contract B1, stages0–2 |
| FR-004–FR-007 | Catalog model, U1/U2, stages2–4 |
| FR-008 | Preferences model, U3-PREFS/U3-SYNC, stages2/4 |
| FR-009 | N1/N2, U3-ABOUT/U3-INVALID, stages2–4 |
| FR-010 | State model, U4/N4, all affected screen stages |
| FR-011 | B2 exact permission allowlist, N3/N4 source-supported safety corrections and provider approval gate |
| FR-012–FR-014 | B1–B4, runtime matrix, quickstart, final exact-artifact evidence |
| FR-015 | Constitution/simple architecture and scoped extraction decisions |
| FR-016 | U2-LABELS/U2-FONT; no full TalkBack/sidebar acceptance claim |

## Complexity Tracking

No constitutional violations or exceptions. Three small concrete boundaries (catalog loader,
legacy section index, preference observation) replace existing Activity responsibilities so the
actual production behavior can be tested and used from Compose. They are not generalized service
or repository layers. Activity-scoped state and the existing Activity stack are simpler than a
new single-Activity graph/DI framework for this bounded app.

## Planning Validation and Residual Risks

- Research decisions, artifact constraints and design contracts are specified; post-design
  constitution gate passes. Runtime availability, full graph compatibility and actual UI correctness
  remain unexecuted acceptance work, not evidence of a working Compose app.
- Preserve the existing untracked local tooling/daemon criteria and user layout change. This phase
  writes documentation and the local feature pointer only; no branch/commit/release is created.
- Source-visible index hazards can conflict with intended navigation: compare per-runtime fixtures
  before corrections and seek approval for altered semantics, not a silent sorting/index rewrite.
- The specified display-only font policy/fixture table, compact-sidebar gesture transitions,
  long-text reflow and provider behavior still require execution tests. The narrow AndroidX
  signature-permission allowance requires merged-manifest/protection-level attribution, not a
  blanket dependency exception. No provider availability or production-signing compatibility is claimed.
- API14 history remains incomplete and unchanged; future support is API23+ by explicit approval.
  Full TalkBack/custom-sidebar accessibility is a documented follow-up, not a hidden gate.

Task generation is complete. Next command: `/speckit.analyze` before `/speckit.implement`.
