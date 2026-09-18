# Data Model: Song Title Search

**Feature**: `004-song-title-search`
**Date**: 2026-09-18

## Model Overview

```text
SongCatalog (existing immutable 414-song snapshot)
       +
SearchSession (temporary active flag + raw query)
       │ derive normalized TitleQuery
       ▼
stable title-only filter
       ▼
SearchResult list (references existing songs in catalog order)
```

Search introduces no persistent database entity. `songs.xml`, generated SQLite tables, `StoredSong`, `SongCatalog`, reader preferences, and navigation payloads remain unchanged.

## Entity: SearchSession

Temporary Main-screen state owned by `SongListViewModel`.

| Field | Type | Rules |
|---|---|---|
| isActive | `Boolean` | `false` initially; `true` only while the Main screen is in search mode |
| query | `String` | Raw reader-entered text; empty initially and whenever inactive |

### Invariants

- `isActive == false` implies `query.isEmpty()`.
- Entering from inactive creates an active session with an empty query.
- Clearing changes only `query` to empty and leaves `isActive == true`.
- Exiting through the top-bar action or system Back produces inactive/empty state.
- Reopening after exit starts active/empty; no prior query is restored.
- Activity configuration changes retain both fields through the existing ViewModel owner.
- The session is not written to SQLite, `SharedPreferences`, files, intent extras, or catalog metadata.
- The catalog and filtered result collection are not stored in saved-instance state.

### State transitions

```text
Inactive(query="")
  └─ EnterSearch ──> Active(query="")

Active(query=q)
  ├─ QueryChanged(q2) ──> Active(query=q2)
  ├─ ClearQuery ────────> Active(query="")
  └─ ExitSearch/Back ───> Inactive(query="")
```

Invalid/redundant events are idempotent: clearing an empty query remains active/empty, and exiting an inactive session remains inactive/empty.

## Value: TitleQuery

Derived matching value; it is not persisted separately.

| Field | Type | Derivation/rule |
|---|---|---|
| raw | `String` | `SearchSession.query`, shown unchanged in the input field |
| needle | `String` | `raw.trim()` |
| isEffectivelyEmpty | `Boolean` | `needle.isEmpty()` |

### Matching rule

For an existing title `title`:

```text
matches = needle.isEmpty() OR title.contains(needle, ignoreCase = true)
```

Rules:

- Only query-boundary whitespace is ignored; title text is not trimmed or modified.
- Matching is literal substring containment.
- Latin letters compare without case dependence.
- Chinese characters, punctuation, digits, whitespace inside the query, and other Unicode text remain literal.
- No transliteration, fuzzy match, tokenization, stemming, accent folding, regex/query language, lyric inspection, history, or ranking is performed.

## Entity: SearchResult

A derived reference to an existing catalog song.

| Field | Source | Rules |
|---|---|---|
| title | `SongCatalog.orderedTitles[index]` | Existing unique effective title and stable lazy-list key |
| lyric | `SongCatalog.lyricsByTitle[title]` | Existing exact lyric; never copied into search state |
| catalogPosition | index in `orderedTitles` | Used only to define retained relative order |

### Invariants

- Every result refers to exactly one existing catalog title.
- Results are produced by stable filtering of `orderedTitles`; if result A precedes result B, A also precedes B in the full catalog.
- Search never changes title, lyric, source order, collation, pinyin initial, section index, or database identity.
- Selecting a result supplies the same title and lyric to the existing navigation callback as normal browsing.
- Empty/whitespace-only queries derive all catalog titles.
- A non-empty normalized query with no results derives an empty result collection and the UI displays the explicit no-results state.

## Existing Entity: SongCatalog (unchanged)

| Field | Role in search |
|---|---|
| `lyricsByTitle` | Resolves the exact existing lyric after a result is selected |
| `orderedTitles` | Sole ordered input to stable filtering |
| `initials` | Unused while search is active; unchanged |
| `sectionIndex` | Unused/hidden while search is active; restored in browse mode |

The complete catalog is still validated and materialized before `SongCatalogUiState.Ready`. Search never exposes partial database rows or converts a loading/error state into an empty successful result.

## Derived UI State

| Catalog state | Search state/query | Content | Sidebar |
|---|---|---|---|
| Loading | inactive | Existing loading content | Not rendered by existing state branch |
| Error | inactive | Existing error/retry content | Not rendered by existing state branch |
| Ready, empty catalog | inactive | Existing catalog-empty content | Hidden |
| Ready, non-empty | inactive | Full catalog | Visible |
| Loading | active/any | Existing loading content under search top bar | Hidden |
| Error | active/any | Existing error/retry content under search top bar | Hidden |
| Ready | active/effectively empty | Full catalog in search list | Hidden |
| Ready | active/matches | Stable filtered results | Hidden |
| Ready | active/non-empty, no matches | Explicit no-results message | Hidden |

## UI List State

Two transient saveable list positions are kept separate:

- **Browse position**: the existing full-catalog `LazyListState`, preserved while search is active and restored on exit.
- **Search position**: used only for results; begins at the first result for a new session and resets to the first result when the effective query changes.

Neither position changes catalog data or search identity. No per-query history is retained.
