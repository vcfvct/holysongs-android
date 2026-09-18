# Implementation Plan: Canonical XML and SQLite Song Catalog

**Branch**: `003-song-database` | **Date**: 2026-09-17 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/003-song-database/spec.md`

## Summary

Retain `app/src/main/assets/songs.xml` as the sole human-edited catalog, remove the eight earlier duplicate entries while preserving every current winning lyric, and generate a validated SQLite database during the Gradle build. Package the generated database as an asset and load it through a small direct-framework SQLite reader that copies it to a temporary private file, reads all 414 rows off-main, closes it, and deletes the copy. Preserve the existing immutable `SongCatalog`, ordering/index helpers, ViewModel state, UI, sharing, preferences, and offline behavior.

Use the unique effective title directly as the SQLite primary key. Keep title and lyric as separate text columns, but defer search UI, FTS/tokenization, and ranking to a later feature. Historical 422-entry baselines remain unchanged and become the independent oracle for proving exact 414-song parity.

## Technical Context

**Language/Version**: Kotlin 2.2.10 application code targeting JVM 11; Python 3 generator using standard-library `xml.etree.ElementTree`, `sqlite3`, and `unittest`; JDK 17 Gradle toolchain/runtime.

**Primary Dependencies**: Existing AGP 9.4.0, Gradle 9.6.0, Kotlin Compose plugin 2.2.10, Android framework `android.database.sqlite.SQLiteDatabase`, coroutines 1.11.0, pinyin4j 2.5.0, and existing Compose/AndroidX stack. No Room, KSP, JDBC, DI, or new runtime library.

**Storage**: Canonical UTF-8 `app/src/main/assets/songs.xml`; generated untracked `app/build/generated/songCatalog/assets/songs.db`; temporary private cache copy while reading; unchanged `appPrefFile` SharedPreferences for reader settings. SQLite schema/transform version 1.

**Testing**: Python `unittest` generator/fixture checks; existing JUnit 4 JVM catalog/ViewModel tests adapted to row input; Android instrumentation for the actual packaged SQLite reader and retained Compose flows; Gradle unit, lint, APK, androidTest assembly, and connected-device tasks.

**Target Platform**: Single-module Android application, minSdk 23, compile/target 37; required runtime validation on the owner-authorized API37 endpoint. API23 runtime remains historically waived/untested unless the owner requests renewed execution; API23 compatibility remains required.

**Project Type**: Offline-first Android mobile application with a build-time catalog generator.

**Performance Goals**: Preserve responsive first offline launch and scrolling; perform one bounded database copy/query per `SongListViewModel` owner on `Dispatchers.IO`; materialize exactly 414 songs; observe no catalog-related main-thread I/O, crash, or ANR. No new numerical latency SLA is introduced.

**Constraints**: Exact parity with all 414 pre-migration effective title/lyric pairs; preserve U+000A line breaks, blank lines, UTF-8 text, and non-ASCII whitespace while removing U+0020 as before; retain current Chinese collation, pinyin initials, sections, intents, sharing, settings, identity, permissions, and UI. Canonical XML edits are limited to removing the eight shadowed entries. No runtime XML parsing, user-editable database, network dependency, search UI, FTS, backend, release signing, or publication.

**Scale/Scope**: 414 unique songs; two SQLite tables; one metadata row; one full-table runtime query; eight duplicate-source removals; one generator task; one Android reader seam; existing list/reader UI unchanged.

## Constitution Check

*Pre-research gate: PASS. Post-design gate: PASS. No constitutional exception is requested.*

| Principle / constraint | Pre-research assessment | Post-design evidence |
|---|---|---|
| Preserve the app's core mission | Pass: feature changes storage, not reader capabilities | Offline packaged catalog, exact 414-song parity, existing UI/settings/sharing/navigation retained |
| Modernize incrementally | Pass: one bounded data slice after completed UI modernization | XML cleanup, generator, reader, and runtime switch are separable checkpoints with the old effective baseline retained |
| Compatibility and offline reliability | Pass: no backend or cloud dependency | Database is packaged, copied privately for one read, and replaced naturally with each APK; no persistent user-data migration |
| Simple and maintainable architecture | Pass if database machinery remains proportional | Direct framework SQLite, two tables, one query, no Room/KSP/DI/repository framework, XML remains reviewable |
| Quality through verification | Pass: current exhaustive catalog fixtures exist | Generator fixture tests, 414-row parity, actual SQLite instrumentation, retained UI/runtime regressions, build/lint gates |
| Data and lyric compatibility | Pass only with exact transformation evidence | U+0020-only removal and all other whitespace/newline fidelity are specified and compared against the frozen legacy effective baseline |
| Supported tooling | Pass subject to build integration | Existing Gradle/JDK/Android pins retained; Python 3 is already a documented local prerequisite; no host `sqlite3` executable required |
| Scope and governance | Pass: feature 003 explicitly authorizes the database/format change forbidden in feature 002 | Feature 001/002 source/evidence stays historical; no constitution amendment or unrelated product expansion |

### Gate evaluation

- The database adds justified complexity because the owner approved SQLite and future search readiness; the design removes compensating complexity by avoiding persistent database state and new frameworks.
- Keeping XML under `app/src/main/assets/` means it may remain packaged alongside the generated database, but normal runtime code never parses it. Its compressed APK cost is accepted to preserve the explicit canonical path and avoid brittle packaging exclusions.
- The prior feature's no-database/no-format-change clauses are scope boundaries of feature 002, not constitution rules. Feature 003 explicitly supersedes them while preserving historical records.
- No unresolved clarification or unsupported dependency remains. Implementation must stop for plan correction if exact parity, build-generated asset wiring, or API23-compatible framework use cannot be demonstrated.

## Project Structure

### Documentation (this feature)

```text
specs/003-song-database/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── canonical-xml.md
│   ├── generation.md
│   ├── sqlite-schema.sql
│   └── runtime-catalog.md
└── tasks.md                 # Created later by /speckit.tasks, not this plan
```

### Source Code (repository root)

```text
app/
├── build.gradle.kts                         # Generator task, generated asset source, verification wiring
├── gradle.lockfile                          # Expected unchanged unless implementation adds a dependency
└── src/
    ├── main/
    │   ├── assets/
    │   │   └── songs.xml                    # Canonical, deduplicated 414-entry source
    │   └── kotlin/com/goodtrendltd/HolySongs/
    │       ├── MainActivity.kt              # Construct SQLite-backed loader at existing factory seam
    │       └── data/
    │           ├── SongCatalog.kt           # Existing immutable UI-facing model
    │           ├── SongCatalogLoader.kt     # Assemble rows into existing order/initial/index model
    │           ├── StoredSong.kt             # Small storage-neutral generated-row value
    │           ├── BundledSongDatabase.kt    # Copy/open/validate/query/close/delete SQLite asset
    │           └── LegacySectionIndex.kt     # Existing behavior retained
    ├── test/
    │   ├── kotlin/.../data/                  # Assembly/parity/immutability/error tests
    │   └── resources/
    │       └── catalog-baseline.xml          # Untouched historical 422-entry oracle
    └── androidTest/
        ├── kotlin/.../data/                  # Actual packaged database reader tests
        ├── kotlin/.../SongListScreenTest.kt  # Retained end-to-end 414-song/reader tests
        └── assets/legacy-api37.json          # Untouched effective runtime fixture

tools/catalog/
├── generate_song_database.py                # Validate XML and create/check SQLite
└── tests/
    ├── test_generate_song_database.py       # Generator behavior and failure fixtures
    └── fixtures/                            # Small add/edit/delete/malformed/duplicate fixtures

app/build/generated/songCatalog/assets/
└── songs.db                                 # Untracked Gradle output packaged into APK
```

**Structure Decision**: Keep the single Android app module and current UI/data ownership. Add one host-side generator directory because Python is not Android application code. Do not create a database module or generic repository layer. The runtime reader and storage-neutral row value live beside the existing catalog loader; all generated output stays under `app/build/`.

## Phase 0: Research Outcomes

[research.md](research.md) resolves every technical unknown:

1. Generate an untracked SQLite asset from XML during Gradle builds using Python 3 standard libraries.
2. Use direct Android SQLite APIs rather than Room for the one immutable full-table read.
3. Copy the asset to a unique cache file per load, open read-only, then close/delete it; avoid persistent database migrations.
4. Use the effective title directly as the primary key; hashes and random IDs add no useful stability.
5. Remove only the earlier member of each duplicate pair and retain every established last-entry winner.
6. Preserve the existing U+0020 removal and all other text/newline bytes at the effective-string level.
7. Use schema/transform version 1 plus source digest/count metadata; verify logical determinism rather than binary SQLite hashes.
8. Keep FTS and Chinese tokenization out of scope while preserving ordinary title/lyric columns.
9. Preserve `SongCatalog`/ViewModel/UI boundaries and verify generator, assembly, database read, and UI at separate layers.

## Phase 1: Design and Contracts

### Canonical source and generation

The [canonical XML contract](contracts/canonical-xml.md) restricts entries to one text-only `name` and `lyric`, defines U+0020 transformation and uniqueness, and records the exact duplicate removals. The XML remains in its established path and historical fixtures remain untouched.

`generateSongDatabase` declares the XML and generator sources as inputs and `app/build/generated/songCatalog/assets/songs.db` as output. The generator builds a temporary database, validates schema/integrity/metadata/ordered rows, closes it, and atomically replaces the task output only after success. Failed generation leaves no accepted partial output. A separate check/test mode supports fixture and logical-parity validation.

The generated asset source is attached to `main`; Android pre-build/asset merge depends on generation. Clean, debug, release, unit-test, lint, and androidTest assembly paths therefore cannot package stale catalog output. Generated DB bytes are not committed or used as a golden hash.

### Runtime ownership

`BundledSongDatabase` owns only the database-copy lifecycle:

1. create a unique path in app-private `cacheDir`;
2. copy `assets/songs.db` completely;
3. open with `OPEN_READONLY`;
4. verify `user_version`, singleton metadata, table/column expectations, count, identity/title equality, non-empty fields, and contiguous source order;
5. query all rows with explicit column names and deterministic `ORDER BY source_order`;
6. close cursor/database and delete the temporary file on success, failure, or cancellation.

`SongCatalogLoader` remains the cancellable `Dispatchers.IO` boundary and converts returned `StoredSong` values into the existing immutable map, `ChineseCharComp` title order, pinyin initials, and `LegacySectionIndex`. It rejects duplicate or malformed rows rather than publishing a partial catalog. `MainActivity.catalogLoaderFactory` remains the test/construction seam.

### Data and identity

[data-model.md](data-model.md) defines the canonical song, generated row, metadata, generated artifact, and in-memory catalog. The [SQLite schema contract](contracts/sqlite-schema.sql) uses title as `TEXT PRIMARY KEY`, lyric as required `TEXT`, and source order as a unique non-negative integer. Metadata records schema version 1, transform version 1, source SHA-256, and count 414.

A title edit changes identity by design. The feature stores no favorites, user edits, or relationships that require identity migration. Future search may create a derived index keyed/rebuilt from title; it must not alter canonical lyric values.

### Verification strategy

- **Generator layer**: Python fixture tests for valid generation, add/edit/delete, duplicate effective title, malformed/missing/nested/empty content, Unicode/newline fidelity, source digest, collision/constraint failures, deterministic logical output, partial-output cleanup, and unchanged historical effective parity.
- **JVM layer**: row assembly, duplicate/malformed database-row rejection, all 414 title/lyric values, ordering, initials, section index, immutability, cancellation, retry, and one-load ownership. Keep the 422-entry baseline fixture unchanged but update tests that previously asserted the production XML itself still had 422 entries.
- **Android database layer**: generated asset exists in the APK; copy/open/read/cleanup works; wrong schema/metadata/corrupt DB fails without partial data; no write succeeds through the read-only reader; API-compatible cursor access.
- **UI/runtime layer**: retained API37 list fixture, all prior duplicate winners, representative newline/blank/full-width-space/long lyrics, navigation, sharing, recreation, offline first launch, timing/stall observation, and crash/ANR inspection.
- **Build layer**: clean generation, tests, lint, debug APK/androidTest APK assembly, APK asset inspection, and repeat logical-generation comparison. Dependency lock must remain unchanged unless an approved implementation change introduces a dependency.

## Incremental Delivery Checkpoints

1. **Baseline and deduplication**: add generator tests and effective parity oracle; remove only the eight shadowed XML entries; prove canonical source count/uniqueness and winner parity before runtime changes.
2. **Generation and packaging**: implement schema/generator/Gradle task; prove clean generation, stale-input prevention, logical reproducibility, and packaged database inspection while runtime still uses XML.
3. **SQLite reader slice**: add storage-neutral row assembly and instrumented database reader checks; keep existing UI contracts.
4. **Runtime switch**: change only `MainActivity` construction to the SQLite-backed loader; execute retained JVM/Compose/runtime scenarios and confirm no normal startup XML parse.
5. **Cleanup and documentation**: remove obsolete production XML parser dependency only if no references remain, retain historical fixtures, update README/verification guidance, and run final exact-revision gates.

Each checkpoint must compile and preserve a usable app. Do not delete the XML runtime loader or rewrite old baseline tests before the replacement layer has independent parity evidence.

## Post-Design Constitution Re-evaluation

**PASS**. The design remains local-first, preserves all reader-visible behavior, uses one small generated database and direct framework reader, adds no backend or speculative search implementation, and provides reviewable checkpoints with exhaustive parity verification. The database complexity is justified by the owner-approved storage/search direction and is bounded by temporary read-only access rather than a persistent mutable store. No Complexity Tracking exception is required.
