# UI Contract: Main-Screen Song Title Search

**Feature**: `004-song-title-search`
**Surface**: Existing `MainActivity` / `SongListScreen`

## 1. Normal Main Mode

- The existing app title, three-dot menu, full catalog list, and alphabet sidebar remain unchanged.
- The three-dot menu adds a resource-backed **Search** item without removing or reordering away existing Settings, About, and Share actions unless implementation constraints require a documented accessible ordering.
- Selecting Search closes the menu and emits `EnterSearch` exactly once.
- System Back is not consumed by search while search is inactive.

## 2. Entering Search

On `EnterSearch`:

1. `SearchSession` becomes active with an empty query.
2. The normal title/menu presentation is replaced by an inline editable title-query field.
3. The field receives input focus from a transition-keyed focus request.
4. The search exit action is available.
5. The clear action is absent or disabled while the raw query is empty; it becomes available when raw text is present.
6. The alphabet sidebar and letter indicator are absent for the entire active session.
7. The content shows the complete catalog because the normalized query is empty.
8. The search result list begins at its first item; the prior browse position remains unchanged in separate state.

The software keyboard should be requested after focus where supported, but a third-party IME's refusal to appear does not permit an unfocused field.

## 3. Query Input and Matching

- Every text edit emits `QueryChanged(rawText)`; no submit action is required.
- The field displays raw text exactly as entered.
- Matching derives `needle = rawText.trim()` and retains titles satisfying `title.contains(needle, ignoreCase = true)`.
- If `needle` is empty, all catalog titles are shown.
- Results retain their relative order from `SongCatalog.orderedTitles` and use existing title keys.
- Only titles are inspected. Lyrics, pinyin/transliteration, tokenization, history, and ranking are excluded.
- Each changed effective query resets the search-results viewport to the first result.
- Filtering is synchronous over the immutable catalog; the visible result set must correspond to the latest displayed query and may not lag behind a newer edit.

## 4. Result Selection

- Each result has the same button/click semantics as its browse-mode row.
- Selecting a result emits the existing `onSongSelected(title, lyric)` callback using the exact catalog title and `lyricsByTitle[title]` value.
- The resulting `DisplayLyricActivity` intent contract remains unchanged:
  - `com.goodtrendltd.SONG_NAME`: selected title string
  - `com.goodtrendltd.LYRIC`: selected lyric string
- No separate search-result destination, ID, database query, or lyric copy is introduced.

## 5. Clear

When raw query text is present, the clear action:

1. emits `ClearQuery`;
2. sets raw query to empty;
3. keeps search active and the field focused;
4. restores the complete catalog in search mode;
5. keeps the alphabet sidebar hidden;
6. places the search-results list at its first item.

Clearing performs no catalog reload and no persistent write.

## 6. Exit and System Back

The search-mode exit action and system Back invoke the same `ExitSearch` event:

1. clear the query;
2. deactivate search;
3. restore the normal top bar and overflow menu;
4. restore the full catalog and alphabet sidebar;
5. restore the preserved browse list position;
6. do not launch, replay, or finish another destination as part of the search event.

A later search session always starts empty. When search is inactive, search-specific Back handling is disabled.

## 7. Empty, Loading, and Error States

- A `Ready` catalog plus a non-empty normalized query with zero matches displays a dedicated resource-backed no-results message.
- The search field, clear action, and exit action remain usable in the no-results state.
- A `Ready` empty catalog with an empty query retains the existing catalog-empty behavior and is not mislabeled as a search miss.
- `Loading` and `Error` retain the existing loading/error/retry content. Search mode/query may remain active above that content, but no partial rows are shown as successful search results.
- Retry remains explicit and continues through the existing `SongListViewModel.retry()` contract.

## 8. Lifecycle

- Activity configuration recreation while search is active retains active mode and raw query and derives the same results after the complete catalog is ready.
- Recreation must not trigger an extra catalog load for the retained ViewModel owner.
- Deliberate exit before recreation restores inactive/empty state and must not resurrect the prior query.
- Process-death restoration is not promised by this feature; no catalog or result list is saved to a Bundle.

## 9. Accessibility and Semantics

Resource-backed, meaningful labels or semantics are required for:

- Search menu action
- Title-query input and hint
- Clear-query action
- Exit-search action
- No-results message

The input must expose editable text semantics. Clear and exit must expose click actions with distinct labels. The UI must not rely on a glyph alone to communicate either action. Existing song-row roles and normal sidebar letter semantics remain unchanged outside search.

## 10. Performance and Offline Contract

- Search performs no network request and requires no new permission.
- Results or no-results content must settle within 200 ms after each edit for the complete 414-song catalog on the supported validation device.
- Very long and rapid edits must not freeze, crash, produce an ANR, or show a result set for an older query.
- Enter, clear, update, and exit do not reload or mutate the catalog.
