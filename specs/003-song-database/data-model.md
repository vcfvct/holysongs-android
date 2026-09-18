# Data Model: Canonical XML and SQLite Song Catalog

**Feature**: `003-song-database`
**Date**: 2026-09-17

## Model Overview

```text
Canonical songs.xml (414 CanonicalSong entries)
        │ validate + transform + generate
        ▼
GeneratedCatalogArtifact (schema v1, transform v1)
        ├── 1 CatalogMetadata row
        └── 414 GeneratedSongRow rows
                    │ temporary read-only query
                    ▼
StoredSong values
        │ existing collation/pinyin/index derivation
        ▼
SongCatalog (immutable UI-facing snapshot)
```

The XML is authoritative. SQLite and all Kotlin row objects are derived representations. Reader preferences and UI state are unchanged and are not database entities.

## Entity: CanonicalSong

One `<song>` element in `app/src/main/assets/songs.xml`.

| Field | Source | Rules |
|---|---|---|
| rawTitle | direct text content of exactly one `<name>` | Required; text only; UTF-8 |
| rawLyric | direct text content of exactly one `<lyric>` | Required; text only; UTF-8; multiline allowed |
| sourceOrder | zero-based position among canonical `<song>` elements | Unique, contiguous `0..413` after deduplication |
| effectiveTitle | `rawTitle` with every U+0020 removed | Non-empty; unique across all 414 entries; becomes identity |
| effectiveLyric | `rawLyric` with every U+0020 removed | Non-empty; preserve all other code points exactly |

### Validation

- Root element is exactly `<songs>`.
- Direct children are exactly 414 `<song>` entries after the approved cleanup.
- Each `<song>` has exactly one direct `<name>` and one direct `<lyric>`.
- `name` and `lyric` contain text only; nested elements are rejected.
- Effective title and lyric are non-empty.
- Effective titles are unique; no last-wins behavior remains.
- XML declarations/comments may be accepted, but DTD/entity-based external content is not part of the format.

### Identity behavior

`effectiveTitle` is the catalog identity. An unchanged effective title retains identity. Editing a title changes identity and is treated as deletion of the old canonical song plus addition of the new one. No in-scope user-owned record refers to song identity.

## Entity: GeneratedSongRow

One row in SQLite table `songs`.

| Column | SQLite type | Constraints | Derived from |
|---|---|---|---|
| title | TEXT | `PRIMARY KEY NOT NULL`; non-empty | `CanonicalSong.effectiveTitle` |
| lyric | TEXT | `NOT NULL`; non-empty | `CanonicalSong.effectiveLyric` |
| source_order | INTEGER | `NOT NULL UNIQUE`; `>= 0` | `CanonicalSong.sourceOrder` |

### Invariants

- Exactly 414 rows.
- `title` is byte-for-byte equal to the effective title encoded as UTF-8 by the generator/database driver.
- `lyric` yields the exact same Kotlin `String` as the pre-migration effective lyric, including leading/trailing newlines, blank lines, and U+3000 spaces.
- `source_order` values form exactly `0..413` with no gaps.
- Table order is never implied; generation and reads use explicit `ORDER BY source_order` when source order matters.
- UI sort order is not stored here. It remains derived through `ChineseCharComp` to preserve established behavior.

## Entity: CatalogMetadata

Exactly one row in SQLite table `catalog_metadata`.

| Column | Type | Value/rule |
|---|---|---|
| singleton_id | INTEGER | Primary key; exactly `1` |
| schema_version | INTEGER | Exactly `1`; also consistent with `PRAGMA user_version` |
| transform_version | INTEGER | Exactly `1` for U+0020-only removal/text-only XML contract |
| source_sha256 | TEXT | Lowercase 64-character SHA-256 of exact canonical `songs.xml` bytes used for generation |
| song_count | INTEGER | Exactly `414` and equal to `COUNT(*) FROM songs` |

### Purpose

Metadata detects the wrong generated asset, unsupported schema/transform, or incomplete catalog. It is not mutable application state and contains no generation timestamp, machine path, or random value.

## Entity: GeneratedCatalogArtifact

The SQLite file emitted at `app/build/generated/songCatalog/assets/songs.db`.

| Property | Rule |
|---|---|
| Authority | Derived; never manually edited |
| Lifecycle | Gradle build output; removed by `clean`; regenerated from declared inputs |
| Packaging | Included in APK assets as `songs.db` |
| Journal state | Closed cleanly; no required `-wal`, `-shm`, or journal sidecar |
| Schema | Exactly [contracts/sqlite-schema.sql](contracts/sqlite-schema.sql) |
| Integrity | `PRAGMA integrity_check` returns `ok` during generation verification |
| Determinism | Same source/tool contract produces the same schema, metadata, and ordered rows; binary bytes are not required to match across SQLite versions |

### State transitions

```text
Absent/Stale
  └─ valid generation ──> Temporary Complete
                               ├─ validation succeeds ──> Published Build Output
                               └─ validation fails ─────> Deleted/Rejected

Published Build Output
  └─ XML/generator/schema input changes ──> Stale (Gradle regenerates before packaging)
```

A failure never promotes a temporary or partial file to the task output.

## Entity: StoredSong

Small storage-neutral Kotlin value produced by the SQLite reader.

| Field | Kotlin type | Rules |
|---|---|---|
| title | `String` | Non-empty; unique in one result set; equal to DB identity |
| lyric | `String` | Non-empty; exact database text |
| sourceOrder | `Int` | Unique, non-negative, contiguous in complete catalog |

`StoredSong` contains no cursor, database handle, Android context, pinyin, or Compose state.

## Entity: SongCatalog

Existing immutable UI-facing snapshot.

| Field | Existing type | Construction |
|---|---|---|
| lyricsByTitle | `Map<String, String>` | All 414 `StoredSong` title/lyric pairs; duplicate rows rejected |
| orderedTitles | `List<String>` | Titles sorted using existing `ChineseCharComp` |
| initials | `List<String?>` | Existing guarded `HanziHelper.words2Pinyin` derivation in ordered-title order |
| sectionIndex | `LegacySectionIndex` | Existing index constructed from `orderedTitles` |

### Invariants

- Collections remain defensively immutable.
- `lyricsByTitle.keys == orderedTitles.toSet()`.
- `orderedTitles.size == initials.size == 414`.
- The existing section behavior and API37 fixtures remain unchanged.
- Database source order is diagnostic only; it does not replace established UI collation.

## Runtime Resource: TemporaryDatabaseCopy

A transient file used because `SQLiteDatabase` opens a filesystem path rather than an arbitrary APK asset stream.

| Property | Rule |
|---|---|
| Location | Unique file under application-private `cacheDir` |
| Source | Complete copy of packaged `assets/songs.db` |
| Access | Opened read-only |
| Lifetime | One catalog load only |
| Cleanup | Cursor, DB, stream, and file closed/deleted in all success, failure, and cancellation paths |
| Persistence | None; never treated as user data or an upgradeable installed database |

### State transitions

```text
Not Created
  └─ copy starts ──> Partial Temporary File
       ├─ copy/open/validation fails ──> Closed + Deleted + Error
       └─ copy complete ───────────────> Read-only Open
              ├─ query/assembly succeeds ──> Closed + Deleted + SongCatalog
              └─ query/assembly fails ─────> Closed + Deleted + Error
```

## Duplicate Resolution Mapping

Historical zero-based positions refer to the untouched 422-entry baseline. Only the earlier entry is removed.

| Effective title | Remove historical position | Retain historical position | Retained behavior |
|---|---:|---:|---|
| 轻轻听 | 42 | 320 | Existing last-entry winner |
| 以色列的圣者 | 53 | 373 | Existing last-entry winner |
| 耶稣基督是主 | 87 | 123 | Existing last-entry winner |
| 耶稣耶稣 | 102 | 110 | Existing last-entry winner |
| 天堂在我心 | 109 | 351 | Existing last-entry winner |
| 全地宣告 | 143 | 144 | Existing last-entry winner |
| 像天空的鸽子 | 213 | 214 | Existing last-entry winner |
| 愿您崇高 | 341 | 342 | Existing last-entry winner |

After deletion, canonical `source_order` is renumbered contiguously. Historical positions remain only in frozen evidence/tests.

## Future Search Compatibility

The base table deliberately stores unmodified effective title and lyric text separately. A future feature may:

- test ordinary exact/prefix/substring queries across 414 rows;
- add a separate derived FTS table;
- choose `unicode61`, `trigram`, or another strategy after Chinese-language behavior is specified;
- rebuild a search index from `songs` without modifying canonical XML.

Search ranking, tokenization, highlighting, query syntax, and UI state are not entities in this feature.
