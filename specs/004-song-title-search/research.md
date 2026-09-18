# Research: Song Title Search

**Feature**: `004-song-title-search`
**Date**: 2026-09-18

This research resolves the implementation choices needed by the feature specification. It combines the current repository architecture with official Android, Compose, Kotlin, and project evidence. It does not claim implementation or runtime results.

## Decision 1: Filter the already-loaded immutable catalog in memory

**Decision**: Search `SongCatalog.orderedTitles` synchronously in memory and retain matching entries in that existing order. Continue resolving lyrics through `SongCatalog.lyricsByTitle`. Do not query SQLite per keystroke and do not add FTS, a search index, a repository layer, or a network service.

**Rationale**:

- The catalog contains exactly 414 titles and is already loaded once off-main into an immutable `SongCatalog` by `SongListViewModel`.
- Stable list filtering preserves the established `ChineseCharComp` order automatically.
- The SQLite reader is intentionally a short-lived, one-load implementation detail; reopening or retaining it for UI search would expand lifecycle and failure complexity.
- Direct filtering avoids asynchronous races, so rapid edits cannot publish stale results.
- Compose performance guidance recommends caching derived calculations where useful, but no debounce or background pipeline is justified for this scale: <https://developer.android.com/develop/ui/compose/performance/bestpractices>.

**Alternatives considered**:

- **SQLite `LIKE` per edit**: requires retaining/reopening database state and gives no useful benefit for 414 already-materialized titles. Rejected.
- **FTS5/tokenization**: unnecessary for literal title substrings and risks adding unrequested Chinese tokenization/ranking behavior. Rejected.
- **Debounced coroutine/Flow search**: introduces cancellation and stale-result concerns that synchronous filtering avoids. Rejected.

## Decision 2: Use trimmed, case-insensitive substring matching without normalization

**Decision**: Compute `needle = query.trim()`. An empty needle returns the complete ordered title list; otherwise retain each title for which `title.contains(needle, ignoreCase = true)` is true.

**Rationale**:

- Kotlin documents `CharSequence.contains(..., ignoreCase = true)` as case-insensitive substring matching and `trim()` as leading/trailing whitespace removal: <https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.text/contains.html> and <https://kotlinlang.org/docs/strings.html>.
- Chinese characters, punctuation, and digits remain literal and unchanged; Latin letters compare without case dependence.
- Filtering instead of sorting preserves the catalog order.
- The rule does not imply transliteration, fuzzy matching, accent folding, stemming, tokenization, lyric search, or ranking.

**Alternatives considered**:

- **Lowercase both strings with a locale**: creates avoidable intermediate strings and locale-policy questions. Rejected in favor of the standard `ignoreCase` API.
- **Unicode normalization or accent folding**: not required and could broaden matches beyond the specification. Rejected.
- **Exact/prefix-only matching**: conflicts with the required substring behavior. Rejected.

## Decision 3: Keep search session state in `SongListViewModel`

**Decision**: Add a small immutable `SearchSession` state (`isActive`, raw `query`) to `SongListViewModel`, with explicit enter, update, clear, and exit events. The ViewModel retains the active session through Activity configuration changes. Exiting sets inactive and clears the query. Do not persist the session to preferences or the catalog database, and do not save the catalog or filtered results in a Bundle.

**Rationale**:

- The existing ViewModel already survives configuration changes and owns the Main screen's retained state.
- The specification requires configuration restoration but defines search as temporary and discarded after deliberate exit or a later app session; plain ViewModel state matches that boundary without broadening persistence.
- Only two small values are retained. Filtered titles remain derived from the immutable catalog and query.
- Android guidance reserves `rememberSaveable`/saved state for small UI state and warns against storing large lists: <https://developer.android.com/develop/ui/compose/state-saving>.

**Alternatives considered**:

- **`rememberSaveable` inside `SongListScreen`**: can satisfy recreation, but splits screen behavior from the existing ViewModel state holder and makes unit-level transition testing less direct. Rejected.
- **`SavedStateHandle` process-death persistence**: valid if product requirements later demand it, but stronger persistence than FR-015 requires and may restore a session after process recreation as though it were still active. Deferred.
- **SharedPreferences/SQLite storage**: would make a temporary session durable and violate scope. Rejected.

## Decision 4: Replace only the existing top-bar presentation while search is active

**Decision**: Keep the current `Scaffold` and `TopAppBar`. In normal mode, retain the app title and overflow menu, adding Search as one menu item. In search mode, render an inline Material 3 editable field plus clear and exit actions in the top-bar area. Keep results in the existing content region; do not use a full expanded suggestion surface.

**Rationale**:

- `SongListScreen` already owns the top bar, overflow menu, list, loading/error/empty states, and alphabet sidebar, so this is the smallest reviewable UI seam.
- Material 3 documents `SearchBar`/`SearchBarDefaults.InputField` and text-field APIs with live `onQueryChange` updates: <https://developer.android.com/develop/ui/compose/components/search-bar> and <https://developer.android.com/develop/ui/compose/text/user-input>.
- The requested UX is an inline top-bar mode over the existing result list, not a separate suggestions/expanded-search surface.
- Exact state-based input-field API availability must be compile-checked against the resolved Compose BOM. A value-based Material 3 field remains acceptable because the query is hoisted.

**Alternatives considered**:

- **Full Material 3 `SearchBar` with expanded content**: adds a second result surface and expanded-state behavior not requested. Rejected.
- **New Activity or screen destination**: would complicate result navigation and state restoration for a mode that belongs to Main. Rejected.

## Decision 5: Request focus once when search mode opens

**Decision**: Attach a remembered `FocusRequester` to the search field and invoke `requestFocus()` from an effect keyed to entering active search. Optionally request the software keyboard after focus, without making keyboard visibility a correctness dependency. Do not re-request focus on every recomposition.

**Rationale**:

- Compose focus guidance requires `FocusRequester` to be attached with `Modifier.focusRequester` and recommends calling `requestFocus()` outside composition: <https://developer.android.com/develop/ui/compose/touch-input/focus/change-focus-behavior>.
- A transition-keyed effect makes initial focus deterministic without stealing focus after every query update.
- IME implementations may differ; focus is the enforceable requirement and keyboard behavior requires device verification.

**Alternatives considered**:

- **Autofocus from the composable body**: causes repeated side effects during recomposition. Rejected.
- **Require a tap before typing**: conflicts with FR-003. Rejected.

## Decision 6: Route close and system Back through one exit event

**Decision**: Enable Compose `BackHandler` only while search is active. Both system Back and the top-bar exit action invoke the same ViewModel exit event, which deactivates search and clears the query. When search is inactive, do not consume Back.

**Rationale**:

- Android documents `BackHandler` for custom Compose back behavior and dispatches to the innermost enabled handler: <https://developer.android.com/guide/navigation/custom-back>.
- One exit path guarantees identical discard/restore semantics.
- Disabling the handler outside search preserves the Activity's established Back behavior.

**Alternatives considered**:

- **Activity-level key/back overrides**: broader than the Compose screen seam and easier to conflict with existing navigation. Rejected.
- **Back clears text before exiting**: conflicts with FR-011, which requires Back to close search and discard the query. Rejected.

## Decision 7: Use separate browsing and search list state

**Decision**: Preserve the existing saveable browsing `LazyListState` and use a separate search-results `LazyListState`. Reset the search-results list to the beginning when a new search session opens and when the effective query changes. Leaving search restores the unchanged browsing position.

**Rationale**:

- A filtered list can have fewer rows than the full catalog; sharing one list position risks clamping to an unrelated result or changing the user's browse position.
- Separate state keeps search temporary and avoids regressions to existing list restoration.
- Resetting to the first match makes each updated result set immediately understandable and is deterministic for rapid edits.

**Alternatives considered**:

- **One shared `LazyListState`**: simpler but lets filtered scrolling disturb normal browsing and can strand the viewport after result-count changes. Rejected.
- **Persist per-query positions**: unnecessary complexity and not requested. Rejected.

## Decision 8: Keep loading/error behavior authoritative and distinguish no matches

**Decision**: Catalog `Loading` and `Error` continue to render their existing content behavior even if search mode is active; the search top bar remains available and the query is retained. Once a complete catalog is `Ready`, an empty/whitespace-only query shows all songs, a non-empty query with matches shows results, and a non-empty query with no matches shows a dedicated message. The alphabet sidebar is absent for every active-search state.

**Rationale**:

- Search cannot expose partial rows because `SongCatalogUiState.Ready` is published only after complete database validation and catalog assembly.
- A distinct no-results state satisfies FR-014 without conflating it with the existing empty-catalog state.
- Hiding the rail throughout search avoids applying full-catalog section destinations to a filtered list.

**Alternatives considered**:

- **Convert load failure to no results**: hides a catalog failure as successful search. Rejected.
- **Rebuild section indices for results**: explicitly unnecessary because the sidebar must be hidden. Rejected.

## Decision 9: Verify matching separately from UI/lifecycle integration

**Decision**: Add pure JVM tests for matching and search-session transitions, plus focused Compose instrumentation for menu entry, focus, live results, order, clear, exit/back, no results, sidebar suppression, recreation, result navigation, and the 200 ms requirement. Retain existing catalog, sidebar, navigation, and offline regression suites.

**Rationale**:

- Pure tests give exhaustive deterministic coverage of Unicode/title/query rules and stable ordering.
- Real MainActivity instrumentation proves focus, semantics, Back, configuration restoration, and unchanged navigation.
- Official documentation does not guarantee a universal filtering latency or keyboard-visible result, so the measurable target needs device evidence rather than inference.

**Alternatives considered**:

- **Instrumentation only**: slower and less precise for edge-case matching. Rejected.
- **JVM only**: cannot prove focus, Back, sidebar visibility, or recreation behavior. Rejected.

## Resolved Unknowns

All planning unknowns are resolved:

- existing in-memory `SongCatalog` is the search source;
- no new dependency, database schema, FTS index, service, or permission is required;
- query semantics are trimmed, literal, case-insensitive substring matching;
- ViewModel owns the temporary session across configuration changes;
- the current top bar is replaced inline in search mode;
- system Back and exit share one discard event;
- search and browsing use separate list positions;
- filtering is synchronous and stable for 414 titles;
- loading/error behavior remains distinct from no results.
