# Data Model and State Ownership

This is an in-memory UI migration, not a new persistent schema. Existing storage contracts
remain authoritative. Implementation types below are small concrete helpers, not a repository
framework. See [research.md](research.md) for decisions and [contracts/ui.md](contracts/ui.md)
for acceptance behavior.

## 1. Immutable Source Inputs

| Input | Required SHA-256 |
|---|---|
| `assets/songs.xml` | `88eb0db602e018b49a327947dd8607f04e6159e58f39ec38ed59f20c39af9d89` |
| `libs/pinyin4j-2.5.0.jar` | `6576dea7d351a0f5df1595b9c432ba7cf9246ca0ab6f7019b9ca4e6d500b0e68` |

Parent rechecked these hashes on 2026-09-15. Packaging must preserve song bytes too. Retaining
the JAR does not assert license clearance; carry the predecessor dependency provenance risk.

### SourceSong (characterization concept)

| Field | Meaning / validation |
|---|---|
| sourcePosition | Zero-based XML position; all 422 entries stay in original order |
| sourceName | Raw parsed name, before production space-removal preprocessing |
| sourceLyric | Raw parsed lyric, including line breaks and original text |

Do not persist IDs or edit source data. Preserve the existing XMLParser extraction semantics,
not an unreviewed switch to trimmed text/textContent. Characterization distinguishes raw-input
fixtures from production's preprocessed XML.

## 2. SongCatalog (Production, Immutable after Loading)

| Field | Meaning / validation |
|---|---|
| lyricsByTitle | Effective title → winning lyric; 414 keys for the bundled asset |
| orderedTitles | HashMap-derived keys sorted with existing ChineseCharComp; no new tie-breaker |
| initials | Guarded pinyin initial per ordered title; derived using existing HanziHelper |
| sectionIndex | LegacySectionIndex for A–Z lookups, scoped to this catalog and runtime ordering |

**Pipeline:** asset opener → full UTF-8 read with stream close → remove literal U+0020 across
XML → parse → iterate in source order → overwrite HashMap entries by effective title → sort
keys with existing Collator(Locale.CHINA) comparator → derive guarded initials/index state.

Only ASCII spaces disappear. Newlines, ideographic spaces, duplicate entries and source bytes
are not normalized. Rendering expectations are based on this actual pipeline, not raw source
text incorrectly assumed to match output byte-for-byte.

### Duplicate resolution

Each title below has two source entries. The last encountered lyric wins. Positions below are
zero-based winning positions from the predecessor baseline; tests must also cover both source
positions and exact expected lyrics using independent baseline fixtures.

| Effective title | Winning source position |
|---|---:|
| 以色列的圣者 | 373 |
| 像天空的鸽子 | 214 |
| 全地宣告 | 144 |
| 天堂在我心 | 351 |
| 愿您崇高 | 342 |
| 耶稣基督是主 | 123 |
| 耶稣耶稣 | 110 |
| 轻轻听 | 320 |

Use effective title Strings as LazyColumn keys only after deduplication. Do not key raw entries
by duplicate titles or invent hash-based IDs. Keep comparator and HashMap tie behavior during
extraction; native collation can differ between Android runtimes, so order fixtures are per-runtime.

### Section lookup

Alphabet is exactly A–Z. Extract the existing algorithm into `LegacySectionIndex`, replacing
Android-only cache plumbing with equivalent storage if needed for tests. Do not substitute
pinyin sorting or a new first-initial lookup algorithm. For an empty catalog or invalid/out-of-range
result, return no destination and do not scroll. For valid present/absent results, preserve recorded
legacy destinations. Freeze API23/API37 fixtures before lookup fixes; mismatches that require new
section semantics stop for review/approval instead of becoming fabricated parity.

## 3. CatalogUiState and Job Lifetime

`Loading → Ready(SongCatalog)` or `Loading → Error(displayable reason)`.
`Error → Loading` only on an explicit retry. Clearing the owner cancels work; cancellation is not
reported as a parse error and a completed obsolete job cannot publish into a new owner.

`SongListViewModel` owns one load across recompositions/configuration changes. A concrete loader
accepts an asset opener and dispatcher to allow tests of the real pipeline with controlled inputs.
Blocking I/O/parsing/sorting runs off-main; repeated subscriptions, preference changes and normal
resume do not trigger repeated loads. The existing XML parser may return null on malformed input:
handle that and missing required fields as an error rather than dereferencing them.

An empty test catalog is a safe empty UI with no letter navigation; corrupted/malformed input
is an error, not a silently truncated successful catalog. The shipped asset must still yield 414.

## 4. ReaderPreferences (Existing Persistence)

Private SharedPreferences file name passed to Android: **`appPrefFile`**.

| Key | Stored type | Default | User changes |
|---|---|---|---|
| `fontSize` | Integer | 20 | 16, 18, 20, 22, 24, 26, 28, 30; reset writes 20 |
| `nightMode` | Boolean | true | true = dark, false = light |

No key renaming, type changes, clearing, new store, or migration. Correctly typed legacy values
are not rewritten merely because they are absent from selectable choices. For corrupt types use
in-memory defaults without overwriting stored data.

**Display-only font policy:** retain the raw preference unchanged. Derive `effectiveFontSize`
from an Integer in **1–200 inclusive**, otherwise use **20**. This generous bounded range preserves
ordinary non-choice legacy values without passing negative/zero/extreme sizes into text layout;
it is a rendering safety policy, not a claim that the platform cannot draw any value above200.
Missing or wrong-type values also produce20. Do not clamp or persist the fallback automatically.
Lyrics render effectiveFontSize in sp and still respect system font scale. Settings displays the
effective current size; a non-choice value is not coerced into a selectable option. Only explicit
selection of one of the eight existing UI sizes or reset writes fontSize (reset20).

| Stored fontSize fixture | effectiveFontSize (sp) | Storage after read/display |
|---|---:|---|
| Missing | 20 | Key remains absent |
| String `"24"` (wrong type) | 20 | String remains unchanged |
| Integer.MIN_VALUE, -1, 0 | 20 | Original integer unchanged |
| 1, 15, 16, 20, 30, 32, 200 | Same as stored integer | Original integer unchanged |
| 201, Integer.MAX_VALUE | 20 | Original integer unchanged |

The effective value is derived snapshot state, not a new persisted key. Wrong-type nightMode
uses its existing in-memory defaulttrue without overwriting the stored value. Verify no file-wide
repair or unrelated-key write. The adapter policy is testable in US1; rendering by the migrated
lyric/settings screens is a US2 gate, not a reason to rewrite the legacy reader during the pilot.

`ReaderPreferences` uses application context only and publishes immutable snapshots. Register a
strongly held listener and take a fresh snapshot; unregister when observation ends and refresh on
restart. Read disk-backed state off-main. Compose collects lifecycle-aware; legacy Settings writes
must be observed during the pilot. Changes originate only in user actions, never composition.

`apply()` updates in-memory values immediately and persists asynchronously. Verify normal relaunch
persistence; do not promise durability under arbitrary abrupt termination or claim it eliminates
all lifecycle blocking. Reset affects fontSize only. Stored nightMode, not the system/dynamic theme,
controls Material3 colors. Lyrics use effectiveFontSize as sp and respect system font scale.

## 5. Screen State and Navigation

| Owner | State | Survival / boundary |
|---|---|---|
| MainActivity / list ViewModel | Loading/catalog/error | Retained across configuration; reloaded after process recreation |
| SongListScreen | Lazy first visible index/offset | Saved UI state, restored only when catalog is Ready |
| MainActivity | Selected title / launch guard | Title is small reconstruction state if needed; guard is transient, reset on return |
| DisplayLyricActivity | Validated title/lyric extras | Existing Intent remains reconstruction input; no additional full-catalog Bundle |
| LyricScreen | Scroll offset keyed by selected title | Saved across recreation/return; clamp on reflow, reset for a genuinely new title |
| Settings/About | Current persisted preferences/content | Reload/observe existing storage; no new persistent destination |
| Letter indicator | Current initial and hide deadline | Transient; 2-second hide, cancelled/dismissed on stop/disposal |
| VideoSearch | Provider/title, live WebView state | Save/restore while WebView is live; no destroyed instance reuse |

Activity stack, not a new navigation graph, owns the destination. Back normally finishes to the
existing caller. No `FLAG_ACTIVITY_CLEAR_TOP` restart is needed to refresh a migrated theme.
Cold launch into a fresh task still starts at the song list; restored task/process state preserves
the existing selected screen. Do not introduce persistent resume-last-song behavior.

Only small UI state is saved. Avoid restoring/clamping scroll against a temporary empty loading
list. Font/viewport changes may reflow text: retain/clamp valid position without requiring exact
pixel equivalence after reflow. Navigation/share operations and loading popups are not saved
commands and must never replay after recomposition or recreation.

## 6. External Action Payloads

| Interface | Exact contract |
|---|---|
| Main → DisplayLyric | String `com.goodtrendltd.SONG_NAME`, String `com.goodtrendltd.LYRIC` |
| DisplayLyric → VideoSearch | String `com.goodtrendltd.searchTarget`, String `com.goodtrendltd.SONG_NAME` |
| Search target | `youtube`, `youku`, or `tudou` |
| Lyric share | ACTION_SEND, text/plain, EXTRA_SUBJECT title, EXTRA_TEXT effective lyric |
| App share | Existing subject/text/link payload from MainActivity; preserve whitespace/newlines |

Keep Java-callable constants in Kotlin hosts. Reject missing/wrong-type/invalid arguments with a
safe error/back path; do not launch external actions with null payloads or select an arbitrary song.
Provider prefixes and approval boundaries are in [contracts/navigation-and-video.md](contracts/navigation-and-video.md).

## 7. Verification Record (Documentation Only)

Fields: requirement/scenario ID; date; source revision; APK SHA-256; toolchain;
device/emulator identifier; Android API/version; WebView version where applicable; prerequisites;
steps; expected result; observed result; status (`passed`, `failed`, `blocked`); evidence path;
blocker/limitation; maintainer approval reference for accepted provider changes or limitations.

Unexecuted checks have no success result. Retests append dated artifact-specific outcomes; do
not erase earlier failures or combine different APK results into unsupported final acceptance.
Raw screenshots/logs/APKs and keys stay outside tracked source. Curated summaries must contain
no secrets or unnecessary personal data. See [quickstart.md](quickstart.md) for validation steps.
