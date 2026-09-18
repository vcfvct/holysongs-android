# Implementation Plan: Song Title Search

**Branch**: `004-song-title-search` | **Date**: 2026-09-18 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/004-song-title-search/spec.md`

## Summary

Add a temporary title-search mode to the existing Compose Main screen. Search is entered from the current overflow menu, replaces the normal top-bar content with a focused field plus clear/exit actions, filters the already-loaded immutable 414-title catalog synchronously with trimmed case-insensitive substring matching, preserves catalog order and the existing lyric-navigation callback, and hides the alphabet sidebar until search exits. `SongListViewModel` owns only the active flag and raw query across configuration changes; filtered results remain derived and are never persisted. No database, network, permission, dependency, or external-interface change is required.

## Technical Context

**Language/Version**: Kotlin 2.2.10 application code targeting JVM 11; JDK 17 Gradle toolchain/runtime.

**Primary Dependencies**: Existing Jetpack Compose Material 3 through Compose BOM 2026.09.00, Activity Compose 1.13.0, Lifecycle runtime/ViewModel Compose 2.11.0, coroutines 1.11.0, and current immutable `SongCatalog`. No new dependency.

**Storage**: No new storage. Search reads the already-loaded in-memory catalog backed by the generated read-only SQLite asset; temporary search state is not written to SQLite or `SharedPreferences`.

**Testing**: JUnit 4 JVM tests for matching/state transitions; AndroidX/Compose instrumentation against real `MainActivity` for input focus, menu, clear/exit/Back, live filtering, ordering, sidebar visibility, no results, recreation, navigation, and latency; retained catalog/UI regression tests.

**Target Platform**: Single-module Android app; minSdk 23, compile/target SDK 37. API37 is the currently exercised runtime endpoint; API23 compatibility remains required and must not be reported as runtime-tested unless executed.

**Project Type**: Offline-first Android mobile application.

**Performance Goals**: Publish matching results or no-results UI within 200 ms of each query edit for all 414 bundled songs; no stale result set, UI freeze, crash, or ANR during long or rapid edits.

**Constraints**: Title-only local search; trim leading/trailing query whitespace; case-insensitive Latin matching; preserve Chinese, punctuation, and digit matching; preserve catalog relative order and song identity; retain existing catalog load/error behavior; hide A–Z sidebar for all active-search states; retain search/query through configuration changes; discard query on deliberate exit; no FTS, ranking, transliteration, lyric search, history, backend, new permission, or catalog mutation.

**Scale/Scope**: 414 unique local songs; one Main screen; one search session with two fields; one derived list of at most 414 title references; existing lyric/settings/about/share/video destinations unchanged.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

**Pre-research gate: PASS. Post-design gate: PASS. No constitutional exception is requested.**

| Principle / constraint | Pre-research assessment | Post-design evidence |
|---|---|---|
| Preserve the app's core mission | Pass: title search improves access to the existing lyric catalog | Same immutable local songs and existing lyric-opening path; browsing, settings, sharing, and video remain intact |
| Modernize incrementally | Pass: one bounded Main-screen Compose enhancement | Changes are limited to search state, matching helper, Main list UI, resources, and focused tests; no screen or storage rewrite |
| Compatibility and offline reliability | Pass: the feature needs no backend or network | Search uses the already-loaded local `SongCatalog`; no permission, schema, or network change |
| Simple and maintainable architecture | Pass if filtering remains synchronous and derived | Two-value ViewModel session, one pure filter, existing top bar/list callbacks, no FTS/Room/repository/debounce pipeline |
| Quality through verification | Pass: requirements map to pure and device tests | JVM matching/state tests plus real MainActivity Compose tests and retained catalog/sidebar/navigation regressions |
| Chinese readability and usability | Pass subject to Unicode and focus checks | Literal Unicode title handling, case-insensitive Latin comparison, explicit no-results text, semantic actions, device focus/IME checks |
| Lifecycle correctness | Pass subject to configuration testing | ViewModel retains active/query state; close and Back use one clearing transition; catalog is never bundled |
| Scope and governance | Pass: clear user value with bounded behavior | No lyric search, transliteration, fuzzy ranking, search history, catalog edit, dependency, publication, or unrelated modernization |

### Gate evaluation

- The feature introduces no architecture or dependency violation requiring justification.
- The existing SQLite/database contract remains unchanged: the complete catalog is loaded and validated before search derives results.
- The existing A–Z sidebar is hidden, not adapted or re-indexed, while search is active.
- All technical clarifications were resolved in [research.md](research.md). Implementation must stop for plan correction if compile-time Material 3 APIs, measured latency, configuration restoration, or existing browsing/navigation regressions cannot satisfy this design.

## Project Structure

### Documentation (this feature)

```text
specs/004-song-title-search/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── ui.md
└── tasks.md                 # Created later by /speckit-tasks, not this plan
```

### Source Code (repository root)

```text
app/src/
├── main/
│   ├── kotlin/com/goodtrendltd/HolySongs/
│   │   ├── MainActivity.kt                  # Collect and forward search state/events
│   │   └── ui/
│   │       ├── SongListViewModel.kt         # SearchSession state and transitions beside catalog state
│   │       ├── SongListScreen.kt            # Search top bar, Back, result/no-result content, sidebar suppression
│   │       └── TitleSearch.kt                # Pure trimmed stable title filtering
│   └── res/values/strings.xml                # Search, hint, clear, exit, and no-results labels
├── test/kotlin/com/goodtrendltd/HolySongs/ui/
│   ├── SongListViewModelTest.kt              # Existing load tests plus session transitions
│   └── TitleSearchTest.kt                    # Matching/order/Unicode/whitespace tests
└── androidTest/kotlin/com/goodtrendltd/HolySongs/
    ├── SongTitleSearchScreenTest.kt          # Focus/menu/live results/clear/Back/recreation/navigation/latency
    └── SongListScreenTest.kt                 # Existing browse/sidebar/navigation regression suite
```

**Structure Decision**: Keep the single Android app module and current Activity/ViewModel/Compose boundaries. Add one pure title-filter helper and a focused instrumentation class; do not modify the generated SQLite schema, `BundledSongDatabase`, `SongCatalog`, `LegacySectionIndex`, or destination Activities.

## Phase 0: Research Outcomes

[research.md](research.md) resolves every technical choice:

1. Filter the already-loaded immutable `SongCatalog` in memory rather than querying SQLite or adding FTS.
2. Use `query.trim()` plus `title.contains(needle, ignoreCase = true)`; stable filtering preserves order.
3. Keep active/query session state in `SongListViewModel` for configuration survival without durable preference/database storage.
4. Replace only the existing top-bar presentation; retain the existing result content region.
5. Request focus once through `FocusRequester` from an activation-keyed effect.
6. Route close and enabled Compose `BackHandler` through one clearing exit event.
7. Keep separate browse and search `LazyListState` values so search cannot disturb browsing position.
8. Preserve loading/error behavior, distinguish no matches, and suppress the sidebar throughout active search.
9. Verify pure matching separately from real-Activity UI, lifecycle, focus, navigation, and performance.

## Phase 1: Design and Contracts

### State and matching

[data-model.md](data-model.md) defines `SearchSession`, `TitleQuery`, and `SearchResult` as temporary/derived state over the existing immutable catalog. `SongListViewModel` exposes explicit enter/update/clear/exit transitions. It retains the raw query so the field displays exactly what the reader typed; normalization is derived only for matching. Exiting always produces `SearchSession.Inactive` with an empty query.

A pure title filter accepts `orderedTitles` and raw query. It trims only query boundaries and applies case-insensitive substring matching without normalization or sorting. Empty/whitespace-only queries return the same ordered title sequence. Filtering is synchronous, so the result shown for a render is always derived from that render's current query and catalog.

### UI behavior

The [UI contract](contracts/ui.md) defines:

- Search as a new entry in the existing three-dot menu.
- Normal top bar versus active search top bar.
- One-time focus request on entry, editable raw query, conditional clear action, and always-available exit action.
- Live filtered results using the same title/lyric selection callback.
- Dedicated no-results content only for a non-empty normalized query with zero matches.
- Alphabet sidebar absence for every active-search state and restoration on exit.
- Search-enabled system Back interception; normal Back behavior when inactive.
- Configuration restoration, separate browse/search list positions, and retained catalog loading/error semantics.
- Meaningful resource-backed labels and testable semantics for Search, field, clear, exit, and no results.

### Verification strategy

- **Pure matching**: empty/whitespace query, leading/trailing whitespace, partial/full title, Chinese, Latin mixed case, punctuation, digits, supplementary Unicode, very long query, no match, stable order, and no mutation.
- **ViewModel state**: inactive default; enter starts empty; update/clear; exit clears; repeated enter does not carry an old query; catalog load/retry/cancellation behavior remains unchanged.
- **Compose integration**: menu entry, initial focus, live edits without submit, clear visibility/action, close and system Back equivalence, no-results state, sidebar suppression/restoration, separate browse position, and semantic labels.
- **Lifecycle/navigation**: Activity recreation retains mode/query/results and does not reload the catalog; selecting a result sends the existing exact title/lyric extras and does not replay navigation.
- **Performance/stability**: measure edit-to-settled-results/no-results under 200 ms for the full catalog and rapid edits; inspect for stale output, crashes, freezes, and ANRs.
- **Regression**: retain offline generated-catalog verification, existing `SongListScreenTest`, lyric opening, settings/about/share actions, alphabet navigation outside search, and load/error/retry behavior.

## Incremental Delivery Checkpoints

1. **Pure state and matching**: add failing JVM tests, then implement `SearchSession` transitions and pure filtering without touching catalog storage.
2. **Search top bar**: add resource strings, overflow entry, active field, focus, clear, exit, and Back behavior while keeping existing content states.
3. **Result integration**: render stable filtered titles, no-results state, separate list position, same lyric callback, and sidebar suppression.
4. **Lifecycle and regression**: add real-Activity instrumentation for recreation, focus, navigation, latency, and rapid edits; rerun existing catalog/sidebar/Main tests.
5. **Final validation**: run the documented clean build and selected API37 scenarios, record API23 accurately as tested or untested, and review the diff for unrelated storage/navigation changes.

Each checkpoint must compile and preserve a usable offline application. No checkpoint may modify the catalog schema or claim device evidence that was not executed.

## Post-Design Constitution Re-evaluation

**PASS**. The design adds a narrow, user-valued Compose mode over the existing local immutable catalog. It preserves offline operation, catalog data, navigation, settings, sharing, and normal alphabet browsing; avoids speculative indexing or persistence; retains configuration state with a two-field ViewModel model; and defines focused automated/runtime verification. No Complexity Tracking exception is required.

## Complexity Tracking

No constitutional violations require justification.
