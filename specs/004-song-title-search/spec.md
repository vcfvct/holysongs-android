# Feature Specification: Song Title Search

**Feature Branch**: `004-song-title-search`

**Created**: 2026-09-18

**Status**: Draft

**Input**: Add a search feature entered from the main screen's three-dot menu. In search mode, show a text field in the top bar, filter songs by title, allow the query to be cleared, and hide the left alphabet scrollbar to keep search behavior simple.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Find a Song by Title (Priority: P1)

As a reader, I can enter search mode from the main song list and type part or all of a title so that I can quickly find a song without browsing the full catalog.

**Why this priority**: Finding a known song is the core user value of the feature.

**Independent Test**: Enter search from the main screen, type a query that occurs in known song titles, and verify that only matching titles remain in their established catalog order.

**Acceptance Scenarios**:

1. **Given** the main song list is visible, **When** the reader selects Search from the three-dot menu, **Then** the top bar presents a focused search field ready for title input.
2. **Given** search mode is active, **When** the reader enters part or all of a song title, **Then** the visible list updates to contain only titles that include the query.
3. **Given** multiple titles match the query, **When** results are displayed, **Then** they remain in the same relative order as in the full catalog.
4. **Given** a matching result is visible, **When** the reader selects it, **Then** the corresponding lyric opens through the existing song-reading flow.

---

### User Story 2 - Clear or Exit Search (Priority: P2)

As a reader, I can clear my current query or exit search mode so that I can try another search or return to normal catalog browsing.

**Why this priority**: Search must be reversible and must not trap the reader in a filtered list.

**Independent Test**: Enter a query, clear it, enter another query, and then exit search; verify that clearing restores the full list within search mode and exiting restores the normal main screen.

**Acceptance Scenarios**:

1. **Given** the search field contains text, **When** the reader selects the clear action, **Then** the field becomes empty and the full song list is shown while search mode remains active.
2. **Given** search mode is active, **When** the reader uses the search-mode exit action or the system back action, **Then** search mode closes, the query is discarded, and the normal main song list returns.
3. **Given** search mode has been exited, **When** the reader opens search again, **Then** the search field starts empty.

---

### User Story 3 - Understand Empty Results (Priority: P3)

As a reader, I receive a clear result when no title matches so that I know the search completed and can revise or clear the query.

**Why this priority**: A blank unexplained screen could be mistaken for a loading or application failure.

**Independent Test**: Search for a string absent from every title and verify that a no-results state appears with the search and clear controls still usable.

**Acceptance Scenarios**:

1. **Given** a non-empty query matches no song title, **When** filtering completes, **Then** the reader sees an explicit no-results message rather than an unexplained empty list.
2. **Given** the no-results state is visible, **When** the reader edits or clears the query, **Then** matching results or the full catalog appear immediately.

### Edge Cases

- An empty query shows the full catalog while retaining search mode.
- Leading and trailing whitespace in the query does not prevent an otherwise valid match.
- Matching supports Chinese titles, punctuation, digits, and Latin characters; Latin-letter matching does not depend on letter case.
- A query that consists only of whitespace is treated as empty.
- Very long queries and rapid edits do not crash, freeze, or display results from an older query.
- The alphabet navigation sidebar is hidden for the entire time search mode is active, including when the query is empty, and returns after search mode closes.
- Configuration changes preserve the active search mode and current query so the reader does not lose in-progress input.
- Catalog loading and load failures retain their existing behavior; search does not present partial data as a successful result set.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The main screen's three-dot menu MUST include a Search option.
- **FR-002**: Selecting Search MUST replace the normal top-bar presentation with a search mode containing an editable title-query field, a clear action when text is present, and an exit action.
- **FR-003**: The search field MUST receive input focus when search mode opens so the reader can begin typing without selecting the field separately.
- **FR-004**: Search results MUST update as the query changes without requiring a separate submit action.
- **FR-005**: A song MUST match when its displayed title contains the query after ignoring leading and trailing query whitespace.
- **FR-006**: Matching MUST support the catalog's Unicode title text and MUST compare Latin letters without regard to case.
- **FR-007**: Search MUST inspect titles only; lyric-content search, transliteration search, fuzzy matching, stemming, search history, and relevance ranking are outside scope.
- **FR-008**: Matching results MUST retain their relative order from the full catalog.
- **FR-009**: Selecting a search result MUST open the same song and lyric as selecting that title from the normal catalog.
- **FR-010**: Clearing a non-empty query MUST empty the input and show the full catalog while keeping search mode active.
- **FR-011**: Exiting search through the search-mode exit action or system back action MUST discard the query and restore the normal main screen and full catalog.
- **FR-012**: Opening a new search session MUST begin with an empty query.
- **FR-013**: The alphabet navigation sidebar MUST be hidden while search mode is active and restored when search mode exits.
- **FR-014**: A non-empty query with no matching titles MUST produce an explicit no-results state while leaving query editing, clearing, and search exit available.
- **FR-015**: Search mode and its current query MUST survive configuration changes while the main screen remains active.
- **FR-016**: Search MUST operate entirely on the locally available song catalog and MUST NOT require network access or alter catalog data.
- **FR-017**: Existing main-screen browsing, alphabet navigation, song opening, settings, about access, offline behavior, and catalog error handling MUST remain unchanged outside search mode.
- **FR-018**: Search behavior MUST remain responsive for the complete bundled catalog and MUST not expose stale results after rapid query changes.

### Key Entities

- **Search Session**: The temporary main-screen state indicating whether search mode is active and containing the reader's current query. It ends and is discarded when the reader exits search.
- **Title Query**: Reader-entered text used to filter displayed song titles after leading and trailing whitespace are ignored.
- **Search Result**: An existing catalog song whose displayed title contains the title query. It preserves the song's identity, lyric, and relative catalog order.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: In usability verification, at least 90% of readers can enter search, find a known song by entering a distinctive title fragment, and open it on their first attempt without assistance.
- **SC-002**: For every tested query, 100% of titles containing the query are shown, 100% of nonmatching titles are excluded, and matching titles retain catalog order.
- **SC-003**: Search results or the no-results state become visible within 200 milliseconds after each input change for the complete bundled catalog on supported devices.
- **SC-004**: Readers can clear any non-empty query with one action and can exit search with one action or one system-back operation.
- **SC-005**: Search, clear, no-results, result selection, configuration-change restoration, and exit scenarios complete with zero crashes, freezes, or stale result sets.
- **SC-006**: The full catalog, alphabet navigation sidebar, and pre-search top bar are restored immediately after search mode exits in 100% of verification runs.
- **SC-007**: All title searches and result navigation remain fully usable with network connectivity disabled.

## Assumptions

- Search covers displayed song titles only; lyric-content search can be specified separately if desired later.
- Substring matching is more useful than exact-title matching for the small catalog and requires no relevance ranking.
- The existing catalog order is the expected order for filtered results.
- The clear action is available when the query contains text; exiting search is a separate action.
- Hiding alphabet navigation throughout search mode is preferable to introducing section-navigation behavior for a filtered list.
- Search state is temporary and is not retained after the reader deliberately exits search or starts a later app session.
- The existing local catalog contains the complete searchable data set, so no loading service or network dependency is needed.
