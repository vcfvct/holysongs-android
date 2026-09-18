---

description: "Implementation tasks for the canonical XML and SQLite song catalog"
---

# Tasks: Canonical XML and SQLite Song Catalog

**Input**: Design documents from `specs/003-song-database/`

**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md), [data-model.md](data-model.md), [contracts/](contracts/), [quickstart.md](quickstart.md)

**Tests**: Required by FR-012–FR-014. Write each story's listed tests first, confirm that they fail for the intended missing behavior, then implement the story.

**Organization**: Tasks are grouped by user story. Historical feature 001/002 evidence and `app/src/test/resources/catalog-baseline.xml` remain immutable migration inputs rather than being rewritten to match the new 414-entry canonical source.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel in separate files/worktrees without depending on an incomplete task in the same phase
- **[Story]**: User story from `spec.md`
- Every task names its implementation or evidence path

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Freeze attributable pre-change facts and create the implementation evidence record before catalog mutation.

- [X] T001 Record the current revision, `songs.xml` SHA-256, 422 raw/414 effective counts, all eight duplicate positions/titles/current winners, existing test commands, and unexecuted implementation outcomes in `specs/003-song-database/evidence/baseline.md` without modifying `app/src/test/resources/catalog-baseline.xml` or feature 001/002 evidence

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish the executable schema and independent legacy oracle used by every story.

**⚠️ CRITICAL**: Complete this phase before story implementation so generator/runtime results are checked against an independent source.

- [X] T002 [P] Create executable schema v1 in `tools/catalog/schema.sql` matching `specs/003-song-database/contracts/sqlite-schema.sql`: `songs.title` is `TEXT PRIMARY KEY NOT NULL` and non-empty, `lyric` is `TEXT NOT NULL` and non-empty, `source_order` is unique/non-negative, and singleton metadata requires schema/transform version 1, lowercase 64-character source SHA-256, and non-negative count
- [X] T003 [P] Create an independent historical oracle in `tools/catalog/tests/baseline_oracle.py` that reads `app/src/test/resources/catalog-baseline.xml`, removes every U+0020 and no other code point, applies historical last-entry-wins behavior, and exposes all 414 effective title/lyric pairs plus the eight winner mappings without importing production generator code

**Checkpoint**: Schema and independent parity oracle are ready; no production catalog behavior has changed.

---

## Phase 3: User Story 1 — Read the Same Deduplicated Offline Catalog (Priority: P1) 🎯 MVP

**Goal**: Ship a SQLite-backed offline catalog containing the same 414 effective titles/lyrics, ordering, initials, navigation, and reader behavior as before migration.

**Independent Test**: From a clean build, compare all packaged SQLite rows with the frozen effective baseline, launch offline on the authorized API37 device, enumerate the established list/order/letter destinations, and open all eight former duplicate titles plus newline/blank-line/full-width-space/long-lyric fixtures.

### Tests for User Story 1

- [X] T004 [P] [US1] Add failing production-generation parity tests in `tools/catalog/tests/test_production_catalog.py` for exactly 414 canonical entries, 414 unique effective titles/identities, schema/transform version 1, metadata/source digest, contiguous `source_order` 0–413, `PRAGMA integrity_check = ok`, and exact equality of all title/lyric strings to `tools/catalog/tests/baseline_oracle.py`
- [X] T005 [P] [US1] Replace XML-input expectations with failing storage-neutral row assembly tests in `app/src/test/kotlin/com/goodtrendltd/HolySongs/data/SongCatalogLoaderTest.kt`, requiring `StoredSong.title` non-empty/unique, `lyric` non-empty, `sourceOrder` unique/non-negative/contiguous, all 414 baseline values, established `ChineseCharComp` order, guarded initials, `LegacySectionIndex`, immutability, cancellation, and no partial catalog on invalid rows
- [X] T006 [P] [US1] Add failing packaged-reader tests in `app/src/androidTest/kotlin/com/goodtrendltd/HolySongs/data/BundledSongDatabaseTest.kt` for asset copy/open/read, read-only access, metadata/schema/count validation, exact 414-row parity, all eight prior winners, newline/U+3000/long lyrics, corrupt or mismatched database rejection, and temporary-file cleanup on success/failure/cancellation
- [X] T007 [P] [US1] Rewrite pending source guards in `app/src/test/kotlin/com/goodtrendltd/HolySongs/SongCatalogTest.kt` and `app/src/test/kotlin/com/goodtrendltd/HolySongs/BuildInputsTest.kt` to fail until production XML has exactly 414 unique effective titles and only the retained winners, while continuing to assert that the untouched 422-entry `app/src/test/resources/catalog-baseline.xml` records both historical duplicate lyrics and derives the same effective map

### Implementation for User Story 1

- [X] T008 [US1] Remove only historical zero-based entries 42, 53, 87, 102, 109, 143, 213, and 341 from `app/src/main/assets/songs.xml`, retain the later entries for all eight named duplicate titles byte-for-byte, and update the approved canonical-source hash expectation in `app/src/test/kotlin/com/goodtrendltd/HolySongs/BuildInputsTest.kt`
- [X] T009 [US1] Implement production generation in `tools/catalog/generate_song_database.py`: UTF-8/text-only XML validation, complete direct `name`/`lyric` extraction, U+0020-only removal, non-empty/unique effective titles, title-as-identity inserts with bound parameters, exact production count 414, one transaction, metadata/digest, integrity and ordered-row self-checks, no timestamps/random IDs/host paths, and temporary-output replacement only after success
- [X] T010 [US1] Wire cacheable `generateSongDatabase` output `app/build/generated/songCatalog/assets/songs.db` into the Android `main` asset source and make asset packaging depend on it in `app/build.gradle.kts`, declaring `songs.xml`, `tools/catalog/schema.sql`, and generator sources as task inputs without adding a Room/JDBC/runtime dependency or tracking generated output
- [X] T011 [P] [US1] Add storage-neutral `StoredSong` in `app/src/main/kotlin/com/goodtrendltd/HolySongs/data/StoredSong.kt` and refactor `app/src/main/kotlin/com/goodtrendltd/HolySongs/data/SongCatalogLoader.kt` to assemble validated rows on its injected I/O dispatcher into the existing immutable map/order/initials/section model without Android database types
- [X] T012 [US1] Implement `app/src/main/kotlin/com/goodtrendltd/HolySongs/data/BundledSongDatabase.kt` to copy `assets/songs.db` to a unique `cacheDir` file, open with `SQLiteDatabase.OPEN_READONLY`, validate user/schema/transform version 1, metadata count 414, identity/title values and contiguous order, query explicit columns ordered by `source_order`, and close cursor/database/streams plus delete the copy in every completion/failure/cancellation path
- [X] T013 [US1] Switch only the production construction seam in `app/src/main/kotlin/com/goodtrendltd/HolySongs/MainActivity.kt` from `songs.xml` input to `BundledSongDatabase` rows while preserving `catalogLoaderFactory`, one ViewModel-owned load, existing intents, preferences, navigation, UI, sharing, and error/retry behavior
- [X] T014 [P] [US1] Adapt loader injection, one-load ownership, retry, cancellation, and obsolete-result tests to storage-neutral rows in `app/src/test/kotlin/com/goodtrendltd/HolySongs/ui/SongListViewModelTest.kt` without introducing Robolectric, JDBC, or Android database calls into JVM tests
- [X] T015 [US1] Replace XML-based injected loader fixtures with `StoredSong` fixtures while retaining all list/restoration/error scenarios in `app/src/androidTest/kotlin/com/goodtrendltd/HolySongs/SongListScreenTest.kt`, and make its real production path assert that reader extras still contain exact SQLite-backed title/lyric values
- [X] T016 [US1] Execute the User Story 1 generator, JVM, lint, APK/androidTest assembly, packaged-DB inspection, focused `BundledSongDatabaseTest`, `SongListScreenTest`, and offline API37 reader checks from `specs/003-song-database/quickstart.md`, recording exact commands/artifacts/results and any blockers in `specs/003-song-database/evidence/us1-runtime-parity.md`

**Checkpoint**: The independently testable MVP reads the same 414-song offline catalog from SQLite; XML is no longer parsed during normal startup.

---

## Phase 4: User Story 2 — Maintain Songs Through Canonical XML (Priority: P2)

**Goal**: Let maintainers add, edit, rename, and delete songs only in XML, with deterministic generation and actionable rejection of invalid or stale output.

**Independent Test**: Run isolated add/edit/rename/delete fixtures and malformed/empty/nested/duplicate/I/O cases through the documented CLI; confirm correct rows or non-zero failure, no partial output, and unchanged-source logical regeneration.

### Tests for User Story 2

- [X] T017 [P] [US2] Add failing add/edit/rename/delete and multiline Unicode fixture tests in `tools/catalog/tests/test_catalog_mutations.py` with fixture XML under `tools/catalog/tests/fixtures/mutations/`, asserting that title rename removes the old title identity and adds the new one and that every lyric preserves all non-U+0020 code points
- [X] T018 [P] [US2] Add failing malformed XML, wrong root/shape, missing/repeated/nested/empty fields, duplicate-after-U+0020-removal, wrong expected count, schema/metadata/digest mismatch, unwritable output, interrupted/failed generation, and stale-database tests in `tools/catalog/tests/test_catalog_failures.py` with inputs under `tools/catalog/tests/fixtures/failures/`, requiring non-zero exit and no accepted partial output

### Implementation for User Story 2

- [X] T019 [US2] Complete the `generate` and `verify` CLI contracts in `tools/catalog/generate_song_database.py`, including configurable fixture `--expected-count`, optional historical `--baseline` parity, logical schema/metadata/ordered-row comparison instead of database-byte hashes, actionable path/position/title diagnostics, and cleanup that leaves a previously accepted output untouched on failure
- [X] T020 [US2] Add `verifySongCatalog` and generator-test lifecycle wiring in `app/build.gradle.kts` so normal verification checks `tools/catalog/tests/`, logical generated/source parity, and stale output, while `clean` removes `app/build/generated/songCatalog/` and builds do not depend on a global `sqlite3` executable
- [X] T021 [US2] Document the XML-only add/edit/rename/delete workflow, title-identity consequences, generator/verify commands, failure recovery, and prohibition on hand-editing SQLite in `tools/catalog/README.md`, linking to `specs/003-song-database/contracts/canonical-xml.md` and `specs/003-song-database/contracts/generation.md`
- [X] T022 [US2] Execute the isolated maintainer drill and unchanged-regeneration checks from `specs/003-song-database/quickstart.md`, recording fixture commands, expected/observed outcomes, logical equality evidence, and generated-output cleanup in `specs/003-song-database/evidence/us2-maintenance.md`

**Checkpoint**: Maintainers can safely change canonical XML and deterministically validate/generate SQLite without editing a binary file.

---

## Phase 5: User Story 3 — Preserve a Path to Catalog Search (Priority: P3)

**Goal**: Prove schema v1 keeps stable title identity and separately queryable title/lyric fields without prematurely implementing search or FTS.

**Independent Test**: Inspect a generated catalog and verify title is the primary identity, title and lyric can be selected independently, unchanged titles retain identity, renames intentionally replace identity, and no FTS/search UI or canonical-text normalization is introduced.

### Tests for User Story 3

- [X] T023 [P] [US3] Add failing schema/search-readiness tests in `tools/catalog/tests/test_search_readiness.py` asserting `title TEXT PRIMARY KEY NOT NULL`, separate non-empty `lyric TEXT NOT NULL`, deterministic unchanged-title identity, rename-as-delete/add behavior, independent title/lyric queries, and absence of FTS tables, tokenized columns, ranking data, or canonical lyric rewrites

### Implementation for User Story 3

- [X] T024 [US3] Extend logical schema verification in `tools/catalog/generate_song_database.py` to reject missing/renamed/extra application columns, non-title primary identity, search/FTS application objects, or metadata/schema drift while accepting only SQLite-internal objects outside `tools/catalog/schema.sql`
- [X] T025 [US3] Add the explicit future-search boundary to `tools/catalog/README.md`: schema v1 supports separate title/lyric queries, but Chinese tokenization, substring semantics, ranking, highlighting, FTS version choice, and search UI require a later feature and must not mutate canonical lyrics or replace title identity
- [X] T026 [US3] Run `tools/catalog/tests/test_search_readiness.py` and inspect the generated APK database schema/query behavior, recording commands and results plus confirmation that no search control is exposed in `specs/003-song-database/evidence/us3-search-readiness.md`

**Checkpoint**: The storage schema is demonstrably search-ready at the data boundary, with speculative search behavior still excluded.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Remove obsolete runtime code only after parity, update project guidance, and perform final attributable verification.

- [X] T027 Remove `app/src/main/kotlin/com/goodtrendltd/HolySongs/helpers/XMLParser.kt` and obsolete runtime XML-loader branches/imports only after `rg 'XMLParser|songs.xml' app/src/main/kotlin` proves no production dependency, while retaining historical XML fixtures and independent test/generator parsing
- [X] T028 [P] Update `README.md` build prerequisites, catalog architecture, generation/verification commands, APK asset inspection, XML maintenance workflow, SQLite failure behavior, and future-search deferral, replacing obsolete claims that runtime reads only `assets/songs.xml`
- [X] T029 [P] Audit `app/gradle.lockfile`, `app/src/main/AndroidManifest.xml`, and merged-manifest expectations in `specs/003-song-database/evidence/final-verification.md`, requiring no new dependency/permission and preserving application ID, SDK floor, component identity, and preference storage
- [X] T030 Run `./gradlew clean :app:verifySongCatalog :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest --no-build-cache --rerun-tasks` and offline dependency replay, recording exact revision, environment, outputs, failures, and hashes in `specs/003-song-database/evidence/final-verification.md`
- [X] T031 Inspect `app/build/outputs/apk/debug/app-debug.apk` for packaged `assets/songs.db`, logically verify its schema/metadata/all 414 rows against XML and the historical baseline, confirm expected handling of packaged `assets/songs.xml`, and append commands/results to `specs/003-song-database/evidence/final-verification.md`
- [X] T032 Execute focused and full instrumentation plus offline API37 list/reader/navigation/share/recreation/timing/stall/crash-ANR scenarios from `specs/003-song-database/quickstart.md`, recording device/build/APK identity and passed/failed/blocked outcomes in `specs/003-song-database/evidence/final-verification.md` without relabeling waived API23 execution as tested
- [X] T033 Perform a genuine compatible-signing in-place upgrade check when an attributable prior APK and disposable device are available, verify the fresh packaged catalog and unchanged `appPrefFile` values, and record pass or an explicit blocker in `specs/003-song-database/evidence/final-verification.md` without uninstalling or clearing data
- [X] T034 Reconcile FR-001–FR-018 and SC-001–SC-008 against `specs/003-song-database/evidence/`, run `git diff --check` and generated-file/reference scans, and update status/limitations in `specs/003-song-database/spec.md` only to the level supported by observed evidence and owner acceptance

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: starts immediately and freezes the pre-change record.
- **Foundational (Phase 2)**: depends on T001; blocks all user-story implementation.
- **User Story 1 (Phase 3)**: depends on T002–T003 and is the MVP/runtime storage replacement.
- **User Story 2 (Phase 4)**: depends on the US1 generator/database base (T008–T012), but its fixture workflow is independently testable without UI/runtime execution.
- **User Story 3 (Phase 5)**: depends on the US1 schema/generator base (T002, T009), but its schema/search-readiness contract is independently testable without implementing search.
- **Polish (Phase 6)**: T027 waits for US1 parity; final verification waits for the desired story set, normally all three.

### User Story Dependency Graph

```text
Setup T001
   ↓
Foundation T002–T003
   ↓
US1: SQLite runtime parity (MVP)
   ├──────────────→ US2: XML maintenance workflow
   └──────────────→ US3: Search-ready schema boundary
                         ↓
                  Polish/final verification
```

US2 and US3 can proceed in parallel after the US1 generator/schema base is stable because they modify distinct test files and validate different contracts. Tasks that both edit `tools/catalog/generate_song_database.py`, `app/build.gradle.kts`, or `tools/catalog/README.md` must remain sequential or use isolated worktrees with one integrator.

### Within User Story 1

1. T004–T007 write failing tests in parallel.
2. T008 performs the approved source cleanup after baseline guards exist.
3. T009 and T011 can proceed in parallel after their respective tests and foundation.
4. T010 follows T009; T012 follows T010–T011; T013 follows T012.
5. T014 can run after T011 while T012–T013 proceed; T015 follows T013.
6. T016 is the story gate.

### Within User Story 2

1. T017–T018 write failing independent fixture suites in parallel.
2. T019 satisfies both suites.
3. T020 integrates verification into Gradle.
4. T021 documents the stable workflow; T022 executes and records it.

### Within User Story 3

1. T023 writes the failing schema contract.
2. T024 implements strict verification without adding search behavior.
3. T025 documents the boundary; T026 executes and records it.

---

## Parallel Execution Examples

### User Story 1

```text
Task T004: Write production generator/parity tests in tools/catalog/tests/test_production_catalog.py
Task T005: Write row-assembly tests in app/src/test/kotlin/.../SongCatalogLoaderTest.kt
Task T006: Write actual Android database tests in app/src/androidTest/kotlin/.../BundledSongDatabaseTest.kt
Task T007: Rewrite source/baseline guards in SongCatalogTest.kt and BuildInputsTest.kt
```

After T008 and the relevant tests:

```text
Task T009: Implement tools/catalog/generate_song_database.py
Task T011: Implement StoredSong.kt and refactor SongCatalogLoader.kt
```

### User Story 2

```text
Task T017: Write mutation fixtures/tests in tools/catalog/tests/test_catalog_mutations.py
Task T018: Write invalid/failure fixtures/tests in tools/catalog/tests/test_catalog_failures.py
```

### User Story 3

US3 is intentionally small. T023 is the independent test task; once it fails for the intended missing verifier behavior, T024 implements that behavior. T025 documentation can be prepared in parallel only if it does not overlap another writer editing `tools/catalog/README.md`.

---

## Implementation Strategy

### MVP First: User Story 1

1. Complete T001–T003.
2. Write and observe the intended T004–T007 failures.
3. Deliver T008–T015 in the stated dependency order.
4. Stop at T016 and require exhaustive 414-song parity, packaged SQLite inspection, and offline API37 reader evidence.
5. Do not remove XML runtime code or claim the full feature complete before the MVP gate passes.

### Incremental Delivery

1. **Baseline + foundation**: immutable oracle and executable schema.
2. **US1 MVP**: deduplicated canonical source, generated SQLite asset, runtime reader, exact parity.
3. **US2**: generalized maintainer CLI, failure safety, fixture drill, documentation.
4. **US3**: strict schema/search-readiness verification with search still deferred.
5. **Polish**: obsolete runtime cleanup, README updates, full build/APK/device/upgrade evidence.

### Parallel Team Strategy

- Keep one writer per working directory.
- Use isolated worktrees for concurrent T004–T007 test preparation or T017/T018 fixture suites.
- Do not concurrently edit the canonical XML, generator, Gradle file, MainActivity, or shared evidence file.
- Treat child/reviewer output as evidence; the master integrates, runs gates, and decides acceptance.

## Notes

- `[P]` means file-level independence, not permission for multiple writers in one checkout.
- Every historical 422-entry artifact remains unchanged and attributable.
- Generated database bytes are not a golden contract; schema, metadata, and ordered logical rows are.
- API23 compatibility remains required, but historical runtime waiver is not a pass.
- No task authorizes search UI, FTS, user song editing, backend work, release signing, publishing, or unrelated cleanup.
