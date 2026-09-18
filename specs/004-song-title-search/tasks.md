# Tasks: Song Title Search

**Input**: Design documents from `specs/004-song-title-search/`

**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md), [data-model.md](data-model.md), [contracts/ui.md](contracts/ui.md), [quickstart.md](quickstart.md)

**Tests**: Tests are included because the specification defines independent tests and measurable correctness, lifecycle, performance, and regression outcomes. Write each automated test task before its corresponding implementation and confirm that it fails for the intended missing behavior.

**Organization**: Tasks are grouped by user story so each story can be implemented and validated as a bounded increment. Existing user changes and feature 001/002/003 historical evidence must remain intact.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel because it changes a different file and does not depend on incomplete work
- **[Story]**: Maps the task to a user story from `spec.md`
- Every task names the exact file it changes or writes

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add shared user-facing labels without changing behavior or dependencies.

- [X] T001 Add resource-backed Search menu, title-query hint, clear-query, exit-search, and no-results strings with distinct accessible wording in `app/src/main/res/values/strings.xml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Confirm the existing architecture remains the foundation for every story.

No new database schema, dependency, permission, module, repository abstraction, or persistent store is required. The existing `SongCatalog`, SQLite reader, Main Activity, Compose screen, and ViewModel seams are the approved foundation; story work begins after T001.

**Checkpoint**: Shared strings exist and the implementation can proceed without infrastructure changes.

---

## Phase 3: User Story 1 - Find a Song by Title (Priority: P1) 🎯 MVP

**Goal**: Enter a focused search mode from the Main overflow menu, filter the complete local catalog by title as text changes, preserve catalog order, hide alphabet navigation, and open a selected result through the existing lyric flow.

**Independent Test**: From a Ready Main screen, select Search, verify the field has input focus and the sidebar is absent, enter a known partial title, verify only matching titles appear in established order, and select one to verify the exact existing title/lyric extras reach `DisplayLyricActivity`.

### Tests for User Story 1

- [X] T002 [P] [US1] Add failing pure matching tests for empty/whitespace-only queries, trimmed boundaries, partial/full Chinese titles, punctuation, digits, mixed-case Latin, supplementary Unicode, long/no-match input, stable order, and no input mutation in `app/src/test/kotlin/com/goodtrendltd/HolySongs/ui/TitleSearchTest.kt`; enforce `needle = raw.trim()` and `title.contains(needle, ignoreCase = true)` with no lyric, pinyin, fuzzy, normalization, history, or ranking behavior (FR-004–FR-008, FR-016, SC-002)
- [X] T003 [P] [US1] Add failing ViewModel tests for `SearchSession` default and enter/query transitions in `app/src/test/kotlin/com/goodtrendltd/HolySongs/ui/SongListViewModelTest.kt`; enforce `isActive == false` implies `query.isEmpty()`, entering from inactive creates active/empty, raw query text is retained, query updates do not reload the catalog, and existing load/retry/cancellation tests remain unchanged (FR-004, FR-012, FR-015, FR-018)
- [X] T004 [P] [US1] Add failing real-Activity Compose tests for the labeled menu entry, focused editable field, live matching/order, empty-query full list, hidden sidebar, configuration recreation without a second catalog load, and exact lyric navigation payload in `app/src/androidTest/kotlin/com/goodtrendltd/HolySongs/SongTitleSearchScreenTest.kt` (FR-001–FR-009, FR-013, FR-015–FR-018)
- [X] T005 [P] [US1] Add failing full-catalog edit-to-settled-results timing and rapid-edit stale-result tests in `app/src/androidTest/kotlin/com/goodtrendltd/HolySongs/SongTitleSearchPerformanceTest.kt`, measuring only a Ready 414-song catalog and requiring each representative edit to settle within 200 ms with the final UI matching the final query (FR-018, SC-003, SC-005)

### Implementation for User Story 1

- [X] T006 [P] [US1] Implement the pure stable filter in `app/src/main/kotlin/com/goodtrendltd/HolySongs/ui/TitleSearch.kt`: return all ordered titles for an empty trimmed query; otherwise retain titles satisfying `title.contains(query.trim(), ignoreCase = true)` without sorting, mutation, asynchronous work, database access, or lyric inspection (FR-004–FR-008, FR-016, FR-018)
- [X] T007 [P] [US1] Add immutable `SearchSession` state plus `enterSearch()` and `updateSearchQuery(rawQuery)` transitions to `app/src/main/kotlin/com/goodtrendltd/HolySongs/ui/SongListViewModel.kt`; keep catalog load state/retry ownership unchanged, retain the raw query, and perform no preference/database/Bundle write (FR-004, FR-012, FR-015–FR-018)
- [X] T008 [US1] Collect `SearchSession` and wire enter/query callbacks alongside the existing catalog state and destination callbacks in `app/src/main/kotlin/com/goodtrendltd/HolySongs/MainActivity.kt`, preserving `catalogLoaderFactory`, one load per ViewModel owner, exact lyric intent extras, and settings/About/share behavior (FR-009, FR-015, FR-017)
- [X] T009 [US1] Implement the normal/search top-bar branch, Search overflow action, transition-keyed `FocusRequester`, live derived title list, separate saveable browse/search `LazyListState` values, result-list reset on effective query change, stable title keys, existing result callback, and complete active-mode sidebar/indicator suppression in `app/src/main/kotlin/com/goodtrendltd/HolySongs/ui/SongListScreen.kt` (FR-001–FR-009, FR-013, FR-015–FR-018)
- [ ] T010 [US1] Run the US1 JVM and focused instrumentation scenarios from `specs/004-song-title-search/quickstart.md` and record commands, revision/device, observed focus/order/navigation/timing results, failures, and blocked checks without converting assumptions into passes in `specs/004-song-title-search/evidence/us1-search.md`

**Checkpoint**: User Story 1 independently supports entering search, focused live title filtering in catalog order, hidden alphabet navigation, and opening an exact existing lyric.

---

## Phase 4: User Story 2 - Clear or Exit Search (Priority: P2)

**Goal**: Clear a query without leaving search, or exit through the top-bar action/system Back and restore normal browsing with a fresh future session.

**Independent Test**: Enter a query, clear it and verify the full catalog remains in search mode with no sidebar, enter another query, exit using each supported path, and verify normal top bar/full catalog/sidebar/browse position return and reopening starts empty.

### Tests for User Story 2

- [X] T011 [P] [US2] Add failing `clearSearchQuery()` and `exitSearch()` transition tests in `app/src/test/kotlin/com/goodtrendltd/HolySongs/ui/SongListViewModelTest.kt`; enforce clear => active/empty, exit => inactive/empty, redundant clear/exit idempotence, and reopening after exit => active/empty (FR-010–FR-012)
- [X] T012 [US2] Add failing real-Activity Compose tests for conditional clear visibility, one-action clear, retained focus/search mode/full list, top-bar exit, system Back exit, query discard, sidebar/top-bar restoration, preserved browse position, and empty reopened session in `app/src/androidTest/kotlin/com/goodtrendltd/HolySongs/SongTitleSearchScreenTest.kt` (FR-002, FR-010–FR-013, FR-017, SC-004, SC-006)

### Implementation for User Story 2

- [X] T013 [US2] Implement `clearSearchQuery()` and `exitSearch()` in `app/src/main/kotlin/com/goodtrendltd/HolySongs/ui/SongListViewModel.kt`, preserving the invariants “clearing changes only `query` to empty and leaves `isActive == true`” and “exiting produces inactive/empty state” without reloading the catalog (FR-010–FR-012, FR-016)
- [X] T014 [US2] Add the conditional labeled clear action, always-available labeled exit action, retained field focus after clear, and search-only `BackHandler` routed to the same exit callback in `app/src/main/kotlin/com/goodtrendltd/HolySongs/ui/SongListScreen.kt`; restore normal top bar/sidebar/full catalog/browse state and do not consume Back when inactive (FR-002, FR-010–FR-013, FR-017)
- [X] T015 [US2] Wire clear and exit events from `SongListScreen` to the ViewModel without persisted/replayed commands in `app/src/main/kotlin/com/goodtrendltd/HolySongs/MainActivity.kt` (FR-010–FR-012, FR-015, FR-017)
- [ ] T016 [US2] Run the US2 focused tests and both exit paths from `specs/004-song-title-search/quickstart.md`, recording query discard, restoration, browse-position, recreation, and regression evidence in `specs/004-song-title-search/evidence/us2-clear-exit.md`

**Checkpoint**: User Stories 1 and 2 work independently; clear remains within search, while either exit path restores normal browsing and a future search begins empty.

---

## Phase 5: User Story 3 - Understand Empty Results (Priority: P3)

**Goal**: Show an explicit no-results state for a non-empty unmatched query while keeping search editing, clear, and exit controls usable.

**Independent Test**: Enter a query absent from all titles, verify the dedicated no-results message and continued search controls, then edit to a matching query and clear to the full catalog.

### Tests for User Story 3

- [X] T017 [US3] Add failing real-Activity Compose tests that distinguish a non-empty normalized no-match query from an empty query and from the existing empty-catalog/loading/error states, then verify edit-to-match and clear-to-full recovery with the sidebar still hidden in `app/src/androidTest/kotlin/com/goodtrendltd/HolySongs/SongTitleSearchScreenTest.kt` (FR-010, FR-013–FR-014, FR-017–FR-018)

### Implementation for User Story 3

- [X] T018 [US3] Implement resource-backed no-results content in `app/src/main/kotlin/com/goodtrendltd/HolySongs/ui/SongListScreen.kt` only when `SongCatalogUiState.Ready`, the trimmed query is non-empty, and filtered titles are empty; retain the active search field/clear/exit controls and leave existing catalog Loading/Error/empty handling unchanged (FR-014, FR-017–FR-018)
- [ ] T019 [US3] Run the US3 focused scenarios from `specs/004-song-title-search/quickstart.md` and record no-result distinction, edit/clear recovery, loading/error behavior, and rapid-edit observations in `specs/004-song-title-search/evidence/us3-no-results.md`

**Checkpoint**: All three stories are independently demonstrable, including an understandable and recoverable zero-match state.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Complete constitution-required compatibility, accessibility, performance, offline, and regression verification without expanding scope.

- [ ] T020 [P] Review all new Search/input/clear/exit/no-results semantics and Chinese/large-font layout behavior against `specs/004-song-title-search/contracts/ui.md`, correcting only concrete findings in `app/src/main/kotlin/com/goodtrendltd/HolySongs/ui/SongListScreen.kt` and updating focused assertions in `app/src/androidTest/kotlin/com/goodtrendltd/HolySongs/SongTitleSearchScreenTest.kt`
- [ ] T021 [P] Conduct the SC-001 usability verification and record participant count, first-attempt enter/find/open outcomes, assistance required, computed percentage, environment, and limitations without inferring unobserved results in `specs/004-song-title-search/evidence/usability.md`
- [ ] T022 Execute the clean host, generated-catalog, focused instrumentation, retained Main/catalog regression, offline, lifecycle, navigation-mode, window/font, latency, crash/ANR, and device checks in `specs/004-song-title-search/quickstart.md`; record exact revision/APK/device attribution and API23/API37 tested-or-blocked status in `specs/004-song-title-search/evidence/verification.md`
- [ ] T023 Review the complete implementation diff against `.specify/memory/constitution.md`, `specs/004-song-title-search/spec.md`, `specs/004-song-title-search/plan.md`, `specs/004-song-title-search/data-model.md`, and `specs/004-song-title-search/contracts/ui.md`; record file-specific scope, simplicity, offline, compatibility, and regression findings plus remediation status in `specs/004-song-title-search/evidence/review.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies; T001 starts immediately.
- **Phase 2 (Foundational)**: Depends on T001 and confirms no new infrastructure is needed.
- **Phase 3 (US1)**: Depends on T001. Write T002–T005 before T006–T009; T010 follows the US1 implementation.
- **Phase 4 (US2)**: Depends on the active search mode delivered by US1. Write T011–T012 before T013–T015; T016 follows implementation.
- **Phase 5 (US3)**: Depends on US1's live result list and uses US2 clear behavior for full recovery. Write T017 before T018; T019 follows implementation.
- **Phase 6 (Polish)**: Depends on all selected stories. T020 and T021 can proceed independently once feature behavior exists; T022 follows all implementation/test corrections; T023 is the final review.

### User Story Dependency Graph

```text
Setup (T001)
   └── US1 Find by Title (T002–T010) [MVP]
          ├── US2 Clear/Exit (T011–T016)
          └── US3 No Results core (T017–T019)
                 └── US3 clear-recovery verification uses US2

US1 + US2 + US3
   └── Polish/Verification (T020–T023)
```

### User Story Dependencies

- **User Story 1 (P1)**: No dependency on another story; it is the MVP search path.
- **User Story 2 (P2)**: Builds on US1's active mode and independently validates reversible state transitions.
- **User Story 3 (P3)**: Builds on US1's filter; edit recovery is independently testable, while clear recovery additionally uses US2.

### Within Each User Story

- Add the listed failing tests before implementation.
- Implement pure matching/state before Activity and Compose integration.
- Keep one writer at a time for `SongListViewModel.kt`, `MainActivity.kt`, and `SongListScreen.kt`.
- Run focused story validation before moving to the next priority.
- Do not alter SQLite schema/catalog data or replace existing navigation contracts.

## Parallel Opportunities

- T002 and T003 can run in parallel because they modify separate JVM test files; T004 and T005 can then run in parallel because they create separate instrumentation files.
- After the US1 tests exist, T006 can run in parallel with preparation of T007 because they modify separate production files, but T008/T009 wait for the agreed state API.
- In US2, T011 and T012 can run in parallel because they modify JVM and instrumentation files respectively.
- T020 and T021 can run in parallel because accessibility/layout review and usability evidence use different targets.
- Different story phases should otherwise follow priority order because US2 and US3 integrate with the US1 screen; concurrent writers must not edit the same checkout files.

## Parallel Example: User Story 1

```text
Task: "T002 [US1] Add pure matching tests in app/src/test/kotlin/com/goodtrendltd/HolySongs/ui/TitleSearchTest.kt"
Task: "T003 [US1] Add search-session tests in app/src/test/kotlin/com/goodtrendltd/HolySongs/ui/SongListViewModelTest.kt"

Then:
Task: "T004 [US1] Add core UI integration tests in app/src/androidTest/kotlin/com/goodtrendltd/HolySongs/SongTitleSearchScreenTest.kt"
Task: "T005 [US1] Add latency/stale-result tests in app/src/androidTest/kotlin/com/goodtrendltd/HolySongs/SongTitleSearchPerformanceTest.kt"
```

## Parallel Example: User Story 2

```text
Task: "T011 [US2] Add clear/exit state tests in app/src/test/kotlin/com/goodtrendltd/HolySongs/ui/SongListViewModelTest.kt"
Task: "T012 [US2] Add clear/exit/Back UI tests in app/src/androidTest/kotlin/com/goodtrendltd/HolySongs/SongTitleSearchScreenTest.kt"
```

## Parallel Example: Polish

```text
Task: "T020 Review accessibility and large-font behavior against specs/004-song-title-search/contracts/ui.md"
Task: "T021 Conduct and record SC-001 usability verification in specs/004-song-title-search/evidence/usability.md"
```

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete T001.
2. Write and observe the intended failures in T002–T005.
3. Implement T006–T009 in dependency order.
4. Complete T010 and stop for the US1 checkpoint.
5. Demonstrate focused, ordered title search and exact lyric opening before adding clear/exit or no-results behavior.

### Incremental Delivery

1. **US1 MVP**: Search entry, focus, live title filtering, order, sidebar suppression, and result navigation.
2. **US2**: Clear, exit, Back handling, query discard, and normal-screen restoration.
3. **US3**: Explicit no-results state and recovery.
4. **Cross-cutting**: Accessibility/layout review, usability outcome, clean build/runtime matrix, and constitutional review.

Each increment must preserve a compiling, offline-capable app and must not claim unexecuted runtime evidence.

### Parallel Team Strategy

1. One owner completes T001 and confirms the existing foundation.
2. Separate contributors may prepare distinct test files marked [P].
3. Use one production writer at a time because all stories converge on the same Main screen and ViewModel.
4. Run story checkpoints serially in priority order; final verification uses the integrated artifact.

## Notes

- `[P]` means different files and no dependency on incomplete work; it does not authorize concurrent writes to the same working directory without coordination.
- `[US1]`, `[US2]`, and `[US3]` provide requirement traceability.
- Automated and manual results are evidence, not automatic acceptance.
- API23 support remains required, but a missing API23 runtime execution must be labeled untested/blocked rather than passed.
- Preserve all unrelated user edits and historical feature evidence.
- Do not commit, publish, release-sign, or change dependency locks unless separately requested.
