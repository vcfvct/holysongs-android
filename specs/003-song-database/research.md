# Research: Canonical XML and SQLite Song Catalog

**Feature**: `003-song-database`
**Date**: 2026-09-17

This research resolves the technical choices left open by the feature specification. It combines current repository inspection with Android and SQLite primary documentation. No implementation or runtime result is claimed here.

## Decision 1: Keep XML canonical and generate SQLite during the Gradle build

**Decision**: Keep `app/src/main/assets/songs.xml` as the only human-edited catalog. Add a Python 3 generator under `tools/catalog/` and a cacheable Gradle task that writes `songs.db` beneath `app/build/generated/songCatalog/assets/`. Register that directory as a generated `main` asset source and make Android packaging depend on generation. Do not check the database binary into Git.

**Rationale**:

- The repository already documents Python 3 as a prerequisite, so the generator introduces no new host-language requirement.
- A build-generated output cannot silently drift from XML: every clean build validates and regenerates it from declared inputs.
- SQLite is required only in Python's standard library at generation time; the host does not need a separately installed `sqlite3` executable.
- Generated binaries do not create noisy or SQLite-version-dependent Git diffs. Reproducibility is defined at the schema and row-content level, not as byte-for-byte equality between SQLite library versions.
- The source XML remains readable in code review and preserves convenient multiline lyrics.

**Alternatives considered**:

- **Check in `songs.db`**: makes an ordinary build independent of Python execution, but creates a second tracked artifact that can become stale and produces opaque binary diffs. Rejected because Python 3 is already required and Gradle can own the generated output.
- **Import XML into SQLite on first launch**: avoids build tooling, but retains runtime XML parsing, adds persistent initialization/upgrade failure modes, and contradicts FR-008/SC-007. Rejected.
- **Check in SQL statements rather than a DB**: reviewable but still requires runtime database creation and duplicates the canonical representation. Rejected.
- **Use a host `sqlite3` command**: simple locally but introduces an undeclared executable/version/path dependency. Python's standard `sqlite3` module is more portable within the project's stated Linux/Python environment.

## Decision 2: Use direct Android SQLite APIs for one bounded read-only operation

**Decision**: Use framework `android.database.sqlite.SQLiteDatabase` directly behind one small bundled-catalog reader. Do not add Room, KSP, a repository framework, or dependency injection for this feature.

**Rationale**:

- Android recommends Room for general application databases because it provides compile-time query verification, annotations, and migration support: <https://developer.android.com/training/data-storage/room>.
- This catalog is a special narrow case: one generated table, 414 immutable rows, one full-table read into the existing immutable `SongCatalog`, no writes, no relational graph, and no user data.
- Room would add runtime, compiler/KSP, schema-export, DAO, and prepackaged-database identity requirements while providing little benefit for one fixed query.
- Direct SQLite keeps the existing `SongCatalog` and `SongListViewModel` ownership intact and meets the constitution's proportional-architecture requirement.

**Alternatives considered**:

- **Room with `createFromAsset()`**: officially supported for prepackaged databases (<https://developer.android.com/training/data-storage/room/prepopulate>) and attractive if the app later gains multiple mutable entities and migrations. Rejected for this bounded read-only catalog because it adds more machinery than the single query needs.
- **`SQLiteOpenHelper`**: supported for create/upgrade lifecycle (<https://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper>), but the app does not create or mutate its runtime catalog. A temporary read-only copy avoids persistent schema migration entirely.
- **Keep the XML runtime loader**: lowest immediate effort but does not deliver the approved SQLite runtime feature.

## Decision 3: Copy the packaged database to a temporary private file per catalog load

**Decision**: `BundledSongDatabase` copies `assets/songs.db` to a uniquely named file in `cacheDir`, opens it with `SQLiteDatabase.OPEN_READONLY`, validates metadata and rows, materializes the existing in-memory catalog, closes the database, and deletes the temporary file in `finally`. It never keeps or mutates a persistent installed database.

**Rationale**:

- SQLite needs a filesystem path; an APK asset cannot be assumed to be directly openable as a database.
- The catalog is loaded once per `SongListViewModel` owner, is small, and is already materialized in memory. A bounded copy avoids persistent copy/version/replacement state.
- App upgrades automatically use the new packaged asset. There is no stale installed catalog to migrate, and no user data can be destroyed by catalog replacement.
- Cache deletion, process death, or interrupted copying is safe: the next load creates a fresh unique temporary file. Partial data is never published.

**Alternatives considered**:

- **Persistent copied database in `getDatabasePath()`**: avoids repeated copying but requires source-version comparison, atomic replacement, downgrade behavior, and stale-copy recovery. Rejected as unnecessary state for approximately 414 read-only rows.
- **Open the asset directly through a file descriptor**: not a portable `SQLiteDatabase` contract and may fail when APK assets are compressed. Rejected.
- **Copy once per installation using Room**: valid but inherits the Room complexity and catalog-version replacement rules discussed above.

## Decision 4: Use the effective title as the SQLite primary key

**Decision**: Define `songs.title TEXT PRIMARY KEY NOT NULL`. Do not add a title hash or random GUID. The generator removes U+0020 ASCII spaces before uniqueness validation and insertion; duplicate effective titles fail generation.

**Rationale**:

- The owner explicitly accepts the title or a deterministic title hash.
- After the approved cleanup there are 414 unique effective titles, and the current application already treats title as song identity in maps, navigation extras, test fixtures, and sharing.
- A hash adds opacity and collision handling without making rename identity stable, because a title-derived hash changes when the title changes.
- A title correction intentionally behaves as deletion plus addition in this maintainer-controlled, read-only catalog; there are no in-scope user references requiring aliases.

**Alternatives considered**:

- **SHA-256 of effective title**: deterministic but opaque, still rename-sensitive, and requires collision/error diagnostics that point back to titles. Rejected.
- **Random UUID stored in XML**: rename-stable but explicitly outside the owner-approved identity choices and unnecessary without user-owned relationships. Rejected.
- **Source position/integer primary key**: compact but changes when entries are inserted or reordered and is not a stable catalog identity. Rejected.

## Decision 5: Resolve duplicates by deleting only the earlier shadowed entries

**Decision**: Edit the canonical XML once, removing positions 42, 53, 87, 102, 109, 143, 213, and 341 from the historical zero-based 422-entry source. Retain the later entries for `轻轻听`, `以色列的圣者`, `耶稣基督是主`, `耶稣耶稣`, `天堂在我心`, `全地宣告`, `像天空的鸽子`, and `愿您崇高` unchanged.

**Rationale**:

- The current loader inserts in source order into a map, so the later member of each pair is the reader-visible winner.
- All eight pairs have different lyrics; selecting arbitrarily would change user-visible content.
- Removing only the shadowed entry produces 414 source entries and preserves the complete current effective catalog.
- Historical `catalog-baseline.xml`, feature evidence, and captured API37 fixtures remain untouched and continue to prove the old 422-to-414 behavior.

**Alternatives considered**:

- **Keep duplicates and let the generator use last-wins**: retains the ambiguity this feature is intended to remove. Rejected.
- **Keep the earlier entry**: changes all eight established winners. Rejected.
- **Merge lyrics**: creates new editorial content without approval. Rejected.

## Decision 6: Preserve the legacy effective whitespace transform exactly

**Decision**: For each text-only `<song>`, extract the direct text value of `<name>` and `<lyric>`, then remove every U+0020 ASCII space from both values. Preserve all other Unicode code points and line breaks exactly. Reject nested markup, missing fields, empty effective values, malformed XML, and duplicate effective titles.

**Rationale**:

- The existing loader removes ASCII spaces from the full XML string before parsing. Applying the transform to extracted text gives the same effective fields for the current text-only source and is safer than rewriting XML syntax bytes.
- Existing tests require preservation of `\n`, blank lines, full-width U+3000 spaces, Chinese text, and trailing/leading line breaks.
- An exhaustive parity test against the frozen 422-entry baseline proves equivalence rather than relying on visual inspection.

**Alternatives considered**:

- **Store raw XML text unchanged**: would expose indentation and ASCII spaces currently removed from titles and lyrics. Rejected as a behavior change.
- **Trim or normalize whitespace/newlines**: simpler-looking data but changes lyric formatting and share payloads. Rejected.
- **Retain first-text-node behavior for nested XML**: the canonical format does not need nested lyrics; rejecting nested content avoids silent truncation. Rejected in favor of validation.

## Decision 7: Use a minimal versioned schema and logical determinism

**Decision**: Use SQLite `PRAGMA user_version = 1`, one `catalog_metadata` singleton row, and one `songs` table. Store schema version, transform version, canonical-source SHA-256, and song count in metadata. Store `title`, `lyric`, and zero-based `source_order` in each song row. Generate rows in source order inside one transaction, run integrity/contract checks, and close without WAL sidecars or timestamps.

**Rationale**:

- Metadata lets runtime and tests reject the wrong schema, transform, source, or count with actionable errors.
- `source_order` supports diagnostics and deterministic generation even though UI order remains the established `ChineseCharComp` order.
- Avoiding timestamps, random values, and machine paths makes logical outputs reproducible.
- SQLite database bytes may differ across SQLite versions or page layouts even when content is identical; tests therefore compare schema, metadata, and ordered rows rather than hashes of generated DB bytes.

**Alternatives considered**:

- **Database-file SHA as the primary reproducibility check**: too sensitive to SQLite implementation/version details and not a content contract. Rejected.
- **Only `songs(title, lyric)` with no metadata**: minimal but cannot diagnose stale schema/transform/source output. Rejected.
- **Store pinyin/section indexes in SQLite**: duplicates existing tested derivation and risks runtime-dependent semantic drift. Rejected; derive them through current helpers after reading rows.

## Decision 8: Keep FTS and search UI out of this feature

**Decision**: Store title and lyric as ordinary separate `TEXT` fields. Add no FTS table, search DAO, ranking, tokenizer, or UI now.

**Rationale**:

- SQLite FTS5 supports configurable tokenizers, including `unicode61` and `trigram`: <https://www.sqlite.org/fts5.html>.
- Chinese substring and word search semantics require explicit requirements and corpus testing; default Unicode tokenization is not automatically a Chinese-language search design.
- A future feature can add a derived FTS table without changing canonical XML, title identity, or base lyric storage.
- A plain title primary key already supports exact title lookup; 414 rows are small enough that future prototypes can measure ordinary substring search before selecting FTS.

**Alternatives considered**:

- **Add FTS5 now**: speculative and may not be uniformly appropriate for the API23 SQLite build or desired Chinese matching semantics. Rejected.
- **Add an ordinary lyric index**: B-tree indexes do not accelerate leading-wildcard substring search and no current query uses it. Rejected.
- **Normalize/tokenize Chinese into canonical data**: would couple source content to an unapproved search behavior. Rejected.

## Decision 9: Preserve the existing loader/UI boundary and split verification by layer

**Decision**: Keep `SongCatalog`, `SongListViewModel`, and UI contracts unchanged. Replace XML input with a small row-source seam: the runtime SQLite reader produces immutable row values, and the loader/assembler applies existing sorting, initials, and section-index construction. Test generation with Python fixtures, assembly with JVM tests, actual SQLite reading with instrumentation, and end-to-end behavior with retained Compose tests.

**Rationale**:

- `MainActivity.catalogLoaderFactory` is already the construction seam and `SongListViewModel` already owns one cancellable off-main load.
- Keeping database APIs out of the pure catalog-assembly tests avoids adding Robolectric or JDBC solely for JVM tests.
- Layered tests localize failures: canonical transformation/generation, database reading, catalog derivation, then UI behavior.

**Alternatives considered**:

- **Expose SQLite cursors or entities to UI**: leaks storage concerns and rewrites stable UI contracts. Rejected.
- **Add a generic repository/domain framework**: disproportionate to one local catalog. Rejected.
- **Test only through instrumentation**: slower and weakens exact malformed/edge-case fixture coverage. Rejected.

## Resolved Unknowns

All technical unknowns are resolved. Planning uses these fixed choices:

- direct framework SQLite, not Room;
- effective title primary key;
- build-generated, untracked database asset;
- Python 3 standard-library generator;
- temporary private read-only copy per load;
- schema/transform version 1;
- no FTS/search implementation in this feature;
- retained API23 minimum and existing Kotlin/Compose/JVM toolchain.
