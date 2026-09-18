# Feature Specification: Canonical XML and SQLite Song Catalog

**Feature Branch**: `003-song-database`

**Created**: 2026-09-17

**Status**: Implemented with host verification on 2026-09-17; connected Android runtime and genuine in-place upgrade acceptance remain blocked because no authorized device/prior APK was available. Do not interpret implementation completion as runtime or release acceptance.

**Input**: Use XML as the human-editable canonical song catalog, remove duplicate source titles while preserving the lyrics users currently receive, generate and read a local SQLite catalog, and keep the data model suitable for a later title-and-lyric search feature.

## Context and Scope

The completed Kotlin/Compose feature deliberately preserved the legacy `songs.xml` format, 422 raw entries, eight duplicate-title pairs, last-entry-wins resolution, and runtime XML parsing. This successor feature explicitly authorizes a bounded change to that contract.

The XML file remains the source of truth because it is readable in reviews and convenient for multiline lyric maintenance. Before SQLite becomes authoritative at runtime, the eight duplicate-title pairs are resolved in the XML by retaining each pair's existing last-entry winning lyric and removing the earlier shadowed entry. The canonical source therefore contains 414 songs with 414 unique effective titles, while feature 001 and feature 002 baseline records remain unchanged as historical evidence.

A deterministic build or repository-generation step validates the XML and produces a local SQLite catalog. The installed app reads the generated database offline instead of parsing XML at runtime. Catalog additions, edits, and deletions in this feature mean maintainer changes to the canonical XML; end-user song editing and synchronization are outside scope.

No search user interface is added in this feature. The schema must retain separately queryable title and lyric fields and avoid choices that would require replacing song identity or lyric storage merely to add title/lyric search later. Whether a later feature uses ordinary indexed queries or SQLite full-text search remains a later measured design decision.

## User Scenarios & Testing

### User Story 1 - Read the Same Deduplicated Offline Catalog (Priority: P1)

As a reader, I can open the app offline and browse the same 414 effective songs with the same titles, winning lyrics, line breaks, Chinese ordering, and letter navigation as before the storage change.

**Why this priority**: Replacing storage has no value if it changes or destabilizes the core offline reader experience.

**Independent Test**: Build and install the app, disable connectivity, load all songs from the SQLite-backed catalog, and compare every title, lyric, order, initial, and duplicate-title winner with the frozen pre-migration effective baseline.

**Acceptance Scenarios**:

1. **Given** the current catalog's eight duplicate-title pairs, **When** the canonical XML is deduplicated, **Then** exactly the earlier shadowed entry in each pair is removed and the lyric previously selected by last-entry-wins behavior is retained.
2. **Given** the deduplicated canonical XML, **When** the database is generated, **Then** it contains exactly 414 songs and every effective title is unique.
3. **Given** an offline first launch, **When** the song list loads from SQLite, **Then** all 414 titles appear in the established Chinese order and letter navigation produces the established destinations.
4. **Given** any song, **When** the reader opens it, **Then** the title and effective lyric exactly match the pre-migration effective catalog, including newlines, blank lines, UTF-8 Chinese text, non-ASCII whitespace, and the existing removal of ASCII spaces.
5. **Given** an existing app installation is upgraded, **When** the new version first opens the catalog, **Then** the bundled catalog is available without a network connection, manual reset, or loss of reader preferences.

---

### User Story 2 - Maintain Songs Through Canonical XML (Priority: P2)

As a maintainer, I can add, edit, or delete a song in the canonical XML and reliably produce a validated SQLite catalog without manually editing a binary database.

**Why this priority**: The project needs a reviewable source of truth and must not allow the generated database to drift from it.

**Independent Test**: In isolated fixtures, add, edit, and delete XML songs, run generation, and verify that SQLite reflects only those changes while malformed, empty, or duplicate input is rejected.

**Acceptance Scenarios**:

1. **Given** a valid new unique song in XML, **When** generation runs, **Then** the generated catalog contains its exact effective title and multiline lyric.
2. **Given** an existing XML song's title or lyric is edited, **When** generation runs, **Then** SQLite contains the edited effective values and no stale duplicate row.
3. **Given** a song is removed from XML, **When** generation runs, **Then** it is absent from the generated catalog.
4. **Given** two source entries resolve to the same effective title, **When** validation runs, **Then** generation fails with an actionable duplicate-title error instead of silently selecting a winner.
5. **Given** malformed XML, an empty required field, an identity collision, or a database whose contents do not match the source, **When** validation runs, **Then** the build or generation task fails before packaging.
6. **Given** unchanged XML and tool versions, **When** generation runs repeatedly, **Then** it produces logically identical catalog contents without manual intervention.

---

### User Story 3 - Preserve a Path to Catalog Search (Priority: P3)

As a future maintainer, I can add title-and-lyric search without first redesigning song identity or extracting lyrics from an opaque stored format.

**Why this priority**: Search is not part of this delivery, but avoiding an immediately obsolete schema prevents needless migration work.

**Independent Test**: Inspect and query the generated schema to confirm that every song has deterministic unique identity and independently queryable title and lyric text, while the current app exposes no incomplete search UI.

**Acceptance Scenarios**:

1. **Given** a generated song row, **When** its schema is inspected, **Then** title and lyric are stored as separate Unicode text values and the row has a deterministic unique identity.
2. **Given** all canonical songs, **When** identity generation and uniqueness validation run, **Then** no two songs share an identity and the same unchanged source produces the same identities.
3. **Given** a future search implementation, **When** it evaluates the schema, **Then** it can query title and lyric independently or add a derived full-text index without changing canonical XML lyric content or song identity.
4. **Given** this feature is complete, **When** readers use the app, **Then** no unfinished search control or changed list behavior is exposed.

### Edge Cases

- The existing duplicate titles are `轻轻听`, `以色列的圣者`, `耶稣基督是主`, `耶稣耶稣`, `天堂在我心`, `全地宣告`, `像天空的鸽子`, and `愿您崇高`; all pairs contain different lyrics, so deduplication must retain the current winner rather than choose arbitrarily.
- XML indentation must not become visible lyric indentation. Existing effective ASCII-space removal remains part of the compatibility transformation unless a later content-cleanup feature explicitly changes it.
- Blank lines, leading and trailing newlines, full-width spaces, punctuation, Latin characters, and very long lyrics must survive generation and retrieval exactly as effective baseline text.
- A title that differs in raw XML but becomes equal after the established ASCII-space removal is a duplicate and must be rejected.
- Empty catalogs, empty titles, empty lyrics, malformed XML, unsupported nested lyric markup, invalid database files, schema-version mismatches, and interrupted database preparation must fail safely without presenting a partial catalog.
- Identity derivation must define exact input bytes/encoding and detect collisions. A title correction may intentionally change a title-derived identity; no end-user song references exist in scope that require aliasing the old identity.
- Generated files and build caches must not create unexplained repository churn or make a clean checkout dependent on a developer's machine paths.
- Existing SharedPreferences, selected-song intent extras, sharing payloads, video search actions, themes, and font behavior must remain compatible.

## Requirements

### Functional Requirements

- **FR-001**: `app/src/main/assets/songs.xml` MUST remain the sole human-edited canonical catalog for this feature; maintainers MUST NOT hand-edit the generated SQLite database.
- **FR-002**: The canonical XML MUST be reduced from 422 entries to 414 unique effective titles by removing the earlier shadowed member of each known duplicate pair and retaining the existing last-entry winning member and lyric.
- **FR-003**: Historical baselines and evidence for features 001 and 002 MUST remain attributable and MUST NOT be rewritten to claim that the legacy source originally contained only 414 entries.
- **FR-004**: Generation MUST parse UTF-8 XML and apply a documented compatibility transformation that produces exactly the pre-migration effective title and lyric strings, including existing ASCII-space removal and preservation of other whitespace and newlines.
- **FR-005**: Generation MUST validate XML structure, required non-empty title and lyric fields, unique effective titles, unique generated identities, supported text-only lyric structure, and the expected relationship between source and database rows. Validation failure MUST prevent packaging a partial or stale catalog.
- **FR-006**: The generated SQLite catalog MUST contain exactly one row per canonical song with, at minimum, deterministic unique identity, title text, lyric text, and sufficient deterministic ordering/source metadata to reproduce and diagnose catalog generation.
- **FR-007**: Song identity MUST be based on the unique effective title or a documented deterministic hash of it, as authorized by the owner. The chosen method, encoding, collision handling, and title-rename behavior MUST be fixed in the implementation plan and covered by tests; random IDs are not permitted.
- **FR-008**: The runtime application MUST load the bundled catalog from SQLite without parsing canonical XML during normal app startup.
- **FR-009**: Database creation or packaging MUST be deterministic at the logical schema/data level and reproducible from a clean checkout using documented project commands. The plan MUST decide whether the binary database is generated during the build or regenerated and checked into source, and MUST prevent stale output in either model.
- **FR-010**: The SQLite catalog MUST remain fully available offline on first launch and upgrade. Database versioning or replacement behavior MUST fail safely and MUST NOT require uninstalling the app, clearing preferences, or accessing a network.
- **FR-011**: The resulting `SongCatalog` behavior MUST preserve all 414 pre-migration effective titles and lyrics, Chinese collation, pinyin initials, section navigation, immutable exposed catalog state, sharing payloads, lyric display, and selected-song navigation.
- **FR-012**: Regression tests MUST compare every SQLite-backed effective title and lyric against an independently retained pre-migration effective baseline, with explicit checks for all eight prior winners and representative whitespace/newline cases.
- **FR-013**: Generator tests MUST cover adding, editing, deleting, duplicate rejection, malformed/empty fields, identity collision handling, Unicode and whitespace fidelity, deterministic repeated generation, and stale/mismatched database detection.
- **FR-014**: Runtime verification MUST cover offline first launch, list navigation, opening representative and prior-duplicate songs, sharing, process recreation, app upgrade behavior where applicable, and zero app crashes or ANRs attributable to catalog loading.
- **FR-015**: Title-and-lyric search UI, search ranking/tokenization, user-created songs, in-app catalog editing, favorites, remote catalog updates, and cloud synchronization are explicitly out of scope.
- **FR-016**: The schema MUST keep title and lyric as separate queryable text fields and MUST NOT make a later search feature depend on changing canonical lyrics or replacing song identity. Adding FTS tables or indexes is deferred until search requirements and Chinese tokenization are researched.
- **FR-017**: The implementation MUST remain proportional to this small offline catalog: no backend, network dependency, general-purpose repository framework, dependency-injection framework, or unrelated UI redesign may be introduced.
- **FR-018**: Build, schema, generation, maintenance, and verification instructions MUST be documented, including how a maintainer updates XML and proves the packaged database is current.

### Key Entities

- **Canonical Song**: One human-maintained XML entry with a unique effective title and multiline lyric. Its effective values are produced by the documented compatibility transformation.
- **Generated Song Row**: The SQLite representation of one canonical song, containing deterministic identity, title, lyric, and generation/order metadata. It is derived output, not an independently edited source.
- **Catalog Identity**: A deterministic unique key derived from the effective title, either directly or through a documented hash. It is stable while the effective title is unchanged.
- **Catalog Generation**: The reproducible validation and transformation from canonical XML to a versioned SQLite catalog.
- **Catalog Schema Version**: The version used to detect incompatible or stale generated databases and select safe installation/upgrade behavior.
- **Effective Baseline**: The independently frozen set of 414 titles and current last-entry-winning lyrics used to prove that deduplication and storage replacement do not alter reader-visible content.

## Success Criteria

### Measurable Outcomes

- **SC-001**: Canonical XML and the generated database each contain exactly 414 songs, with 414 unique effective titles and 414 unique identities.
- **SC-002**: Automated parity comparison reports exact equality for 100% of the 414 effective title/lyric pairs against the pre-migration baseline, including all line breaks and non-ASCII whitespace after the established compatibility transformation.
- **SC-003**: All eight known duplicate-title pairs are absent from the canonical source, and each retained song matches the lyric users received before migration.
- **SC-004**: A clean documented build can regenerate or verify the SQLite catalog and package an app without network access beyond already resolved build dependencies or any manually prepared local file.
- **SC-005**: Add, edit, delete, duplicate, malformed-input, collision, and unchanged-regeneration fixture tests all produce their specified deterministic outcomes.
- **SC-006**: Required JVM, lint, packaging, and runtime catalog regressions pass, and runtime checks observe no catalog-related crash or ANR.
- **SC-007**: Normal application startup performs no runtime parse of `songs.xml`; all reader-visible catalog data comes from the packaged SQLite representation.
- **SC-008**: A maintainer can update one song in XML and use the documented workflow to validate and produce the matching database without directly editing SQLite.

## Assumptions

- Catalog additions, edits, and deletions are made by project maintainers and shipped in app releases; readers do not edit catalog content in the app.
- The current last occurrence of each duplicate title is the approved canonical winner because it is already the lyric exposed by the app and frozen by existing effective-baseline tests.
- The owner accepts either the effective title itself or a deterministic title hash as the SQLite primary identity. The implementation plan will choose one after evaluating debuggability, collision behavior, and future search needs.
- Search is a likely future feature but has no approved interaction, ranking, Chinese tokenization, or performance requirements yet.
- The existing API 23 minimum, application identity, preferences, multi-Activity navigation, and current reader UI remain unchanged.
