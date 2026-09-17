# UI and State Contracts

Applies to migrated screens and their transitions to retained screens. Scenario IDs are stable
acceptance references for task generation. No HTTP API/backend is introduced.

## U1. Offline Song List (US1; FR-004–FR-007, FR-010, FR-012)

- **U1-LOAD**: With connectivity disabled before first launch, real production loading reaches
  Ready with 414 effective titles from 422 unchanged source entries. Actual space removal and
  every duplicate-title winner match [data-model.md](../data-model.md) and baseline fixtures.
- **U1-ORDER**: Capture effective sorted order and A–Z destinations on API23/API37 before changing
  lookup behavior. Preserve ChineseCharComp/HanziHelper and valid legacy destinations. Native
  collation-only differences between runtimes are documented, not replaced by a host-JVM golden.
- **U1-LETTER**: Selecting A–Z using the active U2-RESIZE interaction mode reaches each recorded
  valid destination and opening the row displays its matching lyric. Empty/invalid/out-of-range lookups do nothing safely. No new
  absent-letter or present-letter semantics without documented review/approval when parity fails.
- **U1-ERROR**: Empty fixtures produce a non-crashing empty UI; malformed/failed loading produces
  an understandable error and explicit retry/back path. Production data is never rewritten.
- **U1-LOAD-ONCE**: Recomposition, theme updates and normal background/return do not reload the
  catalog. Activity recreation retains its owner; process recreation reloads once and restores
  scroll after Ready. Cancelled/obsolete jobs do not publish errors or stale results.

## U2. Insets and Resizing (US1/US2; FR-007, FR-016)

- **U2-OVERLAP**: First reproduce the owner's top-bar overlap with recorded API/device, screenshot,
  navigation mode, theme and window bounds. Distinguish system status bar from app action bar.
  After replacement, title/first row, last row, sidebar and app-bar actions remain unobscured.
- **U2-INSETS**: Migrated screens have one Compose app bar and no duplicate platform action bar.
  Apply/consume Scaffold padding once at the shared list/sidebar content boundary, with horizontal
  cutout and bottom system-navigation safety. Audit retained Activity windows separately.
- **U2-RESIZE**: Validate portrait/landscape and a resizable large-screen window. Sidebar geometry
  is based on available content, not display dimensions; reserve width so titles do not sit under
  the rail. Use **fit mode** only when all 26 measured letter rows fit at the current font scale
  without compression: tap or drag selects valid letter destinations and the rail does not scroll.
  Otherwise use **compact mode** with a vertically scrollable rail: a tap selects on release only
  if movement stayed within platform touch slop and no cancellation occurred; a vertical drag past
  touch slop scrolls only the rail and must not change the song-list position, including on release.
  All A–Z letters remain reachable by rail scrolling then tapping. Do not select on pointer-down
  in compact mode. Pointer cancellation, Activity stop or mode/size change cancels pending input
  and clears highlight; no extra release selection occurs. Already committed fit-mode selections
  need not be rolled back. Test both modes, their boundaries and drag/cancel/resize transitions.
- **U2-FONT**: Test system font scales 1.0, 1.3 and 2.0 (or the device's maximum supported scale
  if below 2.0, explicitly recorded), combined with lyric size 30. Text scrolls, controls remain
  reachable, and long Chinese titles wrap without hiding essential actions. API23/API37 behavior
  need not be pixel-identical. Restore the tester's previous device font scale afterward.
- **U2-LABELS**: Standard interactive controls expose meaningful text/content descriptions and
  appropriate roles/selected state. A test tag alone does not satisfy this. Full TalkBack journeys
  and custom-sidebar accessibility are deferred by owner decision and not claimed as verified.

## U3. Lyric, Settings and About (US2; FR-008–FR-010)

- **U3-LYRIC**: Preserve effective lyric text, Chinese characters and line breaks; scroll a long
  fixture to the end. Validate all eight duplicate winners and representative ASCII-space removal.
- **U3-PREFS**: Honor `appPrefFile` Integer fontSize/default20 and Boolean nightMode/defaulttrue.
  Test every size 16–30 step2, reset-to20, both themes and normal process relaunch. Representative
  preseeded legacy values work without resetting storage. The theme is not overridden by the OS.
  Verify the display-only 1–200 inclusive font safety range and 20-sp fallback using the exact
  data-model fixture table, without adding UI choices or rewriting stored values.
- **U3-SYNC**: Changes in legacy Settings update Compose Main during the pilot. After Settings
  migration, returning applies font/theme changes without restarting Main or losing list state.
- **U3-ABOUT**: Existing about text, website/email links and return navigation remain usable.
- **U3-INVALID**: Missing/wrong-type extras or malformed preference types never crash or trigger
  sharing/video with invalid content. Preference fallbacks do not clear or auto-rewrite user data.
  For fontSize, missing/wrong-type/out-of-range values use effectiveFontSize20; valid integers1–200
  remain unchanged for rendering. Settings shows the effective current size without silently
  selecting/writing a replacement; only an explicit user choice/reset writes its allowed value.

## U4. Lifecycle and State (US1/US2; FR-010)

- **U4-RETURN**: Select a song after scrolling; open settings/about/video where available; return
  with Back. Preserve the caller, selected song, and list/lyric scroll state without duplicate screens.
- **U4-RECREATE**: Recreate each Activity, including Main while Loading; restore once content is
  Ready. Verify background process recreation separately, not just Compose StateRestorationTester.
- **U4-REFLOW**: After font size/theme/window changes, keep a valid scroll position, clamped to
  the reflowed content; exact pixels need not match. A genuinely different song starts at its top.
- **U4-EFFECTS**: Rapid taps, rotation and resume never replay a share/video launch. Letter indicator
  disappears after 2 seconds or when backgrounded/exited; no added WindowManager popup remains.
- **U4-COLD**: A fresh task starts at Main. No new persistent resume-last-song feature is introduced.

## Checkpoint Applicability

This table defines which scenarios block each checkpoint. A family reference such as U2 or U4
is restricted to the listed screens at that checkpoint; it does not require later replacements
to be completed early. Later-scope scenarios stay unexecuted, never marked passed for an MVP.

| Checkpoint | Required scope | Not yet required |
|---|---|---|
| T009 enablement | B1/B2 build, source/dependency/manifest/IDE checks; no migrated launcher yet | New UI behavior |
| T024 / US1 MVP | All U1; U2/U4 for Compose Main/list/sidebar, including Main recreation while Loading, restored list position and Main's own reflow/labels/effects; U3-SYNC for legacy Settings writes updating Main; N1/N2 retained-screen smoke below | Full legacy lyric/settings/about restoration/reflow/Compose labels and new font-fallback rendering; these are due at T033, while the preference adapter itself is tested at T012 |
| T033 / US2 | All U1–U4 for Main/lyric/settings/about and N1/N2; safe video entry/return to the Kotlin reader | Complete provider audit and N3/N4 verification due at T041; no waiver of an encountered safety failure |
| T041 / US3 | N3/N4 and affected U4/N1 reader-video interoperability on the integrated artifact | Final clean-artifact replay |
| T045 / final | All B1–B4/U1–U4/N1–N4 and required approval/evidence gates | Nothing except the explicitly deferred full TalkBack/sidebar-accessibility work |

**US1 retained-screen smoke:** Open the correct winning lyric and verify readable representative
Chinese text; reach Settings/About and existing links/actions; change a legacy preference and
observe Main update; share the expected app/lyric payload to a recipient and handle absence
safely; attempt video/cancel/Back and return safely to the selected lyric and then the existing
Main/list position. Verify no duplicate screen, lost stored preference or migration-introduced
regression. Full legacy lyric scroll restoration after process death/reflow is not a US1 gate.

Known pre-existing, non-safety retained-screen deficiencies must be recorded against the baseline
and assigned to the later responsible task, not silently passed. Newly introduced regressions,
crashes/ANRs, security failures or trapped/failed offline return block the current checkpoint;
use tasks.md's early-video-safety path where applicable. These are checkpoint boundaries, not
waivers of the final specification.

## Runtime Matrix and Completion

| Endpoint | Required coverage |
|---|---|
| API23 / Android6 phone device or emulator | All U1–U4, sharing, video safety/return and preferences |
| API37 / Android17 phone | All U1–U4 with gesture and three-button Back, cutout/inset checks |
| API37 large/resizable configuration | Insets, rotation/resizing, sidebar reachability, reader and video return |

This runtime matrix is the final-feature requirement; intermediate checkpoints use the screen
scope above on the same endpoints. Selection date: 2026-09-15. Use actual runtime API/build
identifiers, not an AVD name as evidence.
Measure process-stopped launch-to-usable-list time and record visible scroll stalls/crash/ANR
observations on both endpoints. No numerical latency threshold is added. Required failures or
missing evidence block final acceptance; a list pilot passing is not full feature completion.
