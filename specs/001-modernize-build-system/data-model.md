# Data Model and Preservation Rules

This feature changes the build, not the persistent data model. Names below describe existing
contracts, not new application classes, database tables, or a migration framework.

## Song Library

**Source:** `assets/songs.xml`, root `songs`, repeated `song` elements with `name` and `lyric`
text. The packaged asset MUST remain byte-identical to the repository baseline in [research.md](research.md).

| Attribute | Meaning | Validation |
|---|---|---|
| Source position | Position of a song element in XML | Preserve original order and all 422 entries |
| Name | Chinese title text | Preserve bytes; no normalization or new IDs |
| Lyric | Song text and line breaks | Preserve bytes and representative rendered output |
| Effective title key | Name after the existing ASCII-space removal step | 414 distinct keys; used by the current title-to-lyric map |

**Relationship:** Multiple raw entries may share an effective title. The last encountered lyric
wins in `MainActivity.songLyricMap`. This is preserved legacy behavior, not permission to delete
raw entries. Duplicate titles (each appears twice): 轻轻听, 以色列的圣者, 耶稣基督是主,
耶稣耶稣, 天堂在我心, 全地宣告, 像天空的鸽子, 愿您崇高.

**Derived state:** Load asset → apply existing space-removal behavior → DOM parse → title map
→ sort using Chinese `Collator` → present list → pass selected title/lyric to detail.
Pinyin sidebar conversion is separate from the Collator ordering. No new persistent indexing.

**Validation:** Local JUnit tests call the real XML parser success path and check raw counts,
source order, duplicate source pairs, source text and pinyin/helper behavior. Device checks
exercise actual Activity-owned loading, ASCII-space removal, effective title-map count,
last-entry-wins resolution and rendering against recorded fixtures. Recreating the Activity
pipeline inside a JVM test is not evidence that production behavior is preserved.
Documented native Chinese Collator differences across Android versions are acceptable under
FR-008. On each runtime, present/absent-letter sidebar positions must match recorded baseline
expectations and title selection must open the correct lyric; unresolved differences require
maintainer approval. Empty/malformed-data defensive tests must not modify the shipped asset
or invent a new recovery feature.

## Display Preferences

**Storage:** Private SharedPreferences named `appPrefFile` (the name passed to
`getSharedPreferences`, without `.xml`). On-device file is normally `appPrefFile.xml`.

| Key | Type | Default | Existing UI values |
|---|---|---|---|
| `fontSize` | Integer | 20 | 16, 18, 20, 22, 24, 26, 28, 30; reset writes 20 |
| `nightMode` | Boolean | true | true = night/dark Holo; false = light Holo |

**Transitions:** Missing value → default; change setting → persist value; relaunch/recreate
screen → re-read stored value. No key renaming, type changes, clearing, new store, or migration.
Representative correctly typed legacy values must work without reset. Corrupt external values
are not a new repair feature; record any discovered legacy defect.

## Application Identity and Navigation Payloads

| Attribute | Required value |
|---|---|
| Application ID and namespace | `com.goodtrendltd.HolySongs` |
| Current version code/name | 8 / 2.5 for this debug migration |
| Launcher | `com.goodtrendltd.HolySongs.MainActivity` |
| Lyric extra | `com.goodtrendltd.LYRIC` (String) |
| Song title extra | `com.goodtrendltd.SONG_NAME` (String) |
| Video target extra | `com.goodtrendltd.searchTarget` (String) |
| Video target values | `youtube`, `youku`, `tudou` |

**Relationships:** MainActivity supplies title/lyric to DisplayLyricActivity;
DisplayLyricActivity supplies target/title to VideoSearch. Existing AboutActivity and
SettingsActivity remain reachable. Internal Activities are not public deep-link APIs.

**Validation:** Inspect merged manifest/APK, preserve explicit component names and extras,
exercise normal navigation and missing-extra defensive paths without crashing.
A future real update requires compatible signing and suitable versioning; no historical release
key is available by assumption, and this feature does not publish an update.

## External Search Configuration

| Provider label | Existing endpoint prefix |
|---|---|
| `youtube` | `http://m.youtube.com/results?q=` |
| `youku` | `http://www.soku.com/m/y/video?q=` |
| `tudou` | `http://www.soku.com/m/t/video?q=` |

Song title is the search query. Same-provider HTTPS and explicit UTF-8 query encoding may be
compatibility fixes only after verifying equivalent intent. Provider changes require approval.
No cloud account, imported data, or remote persistence is introduced.

## Verification Record (Documentation Only)

**Fields:** requirement/scenario ID; date; source revision; APK SHA-256; toolchain; device/emulator
identifier; Android API/version; WebView version where applicable; prerequisites; steps; expected
result; observed result; status (`passed`, `failed`, `blocked`); evidence path; blocker/limitation;
maintainer approval reference for accepted provider changes or limitations.

**Transitions:** Unexecuted checks have no success result. Once attempted, record passed/failed/
blocked. Retesting creates a dated result for the new artifact/environment; prior failures are
not erased. Only all required passing core checks and recorded provider decisions permit sign-off.
Raw logs, screenshots, APKs and signing material are not committed; curated, non-sensitive results
and reproducible steps are documented.
