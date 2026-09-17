# US1 list and retained-screen evidence plan (T015)

**Status (2026-09-16):** Current artifact verification for T024/US1 advanced materially but does
not claim completion. The worktree is dirty and behind `origin/master` (`HEAD` = `f62519b`,
`origin/master` = `50658b7`), so there is no exact clean-source revision to lock as the final
current-artifact record. The active evidence below is therefore scoped to the parent-observed API37
current-artifact run and the retained local verification directories, not to a clean exact Git revision.

This document is the exact active scope for the API37-only continuation. The owner waived
Android6/API23 runtime execution, but did not waive minSdk23 compatibility, fixture parity,
retained-screen safety, or reviewer acceptance. API23 records therefore remain explicitly
unexecuted rather than being inferred from API37.

## Current verification evidence (API37 current-artifact snapshot; no T024/T041 pass claim)

- Attribution files and manifest: the final dirty-worktree artifact is bound by
  `/tmp/holysongs-final-current/final-attribution.txt` (SHA-256
  `3fb3d410dac3b620f2b44fc8c775314dcd0bc1acddcec22573c8752eb90cbc30`) and
  `/tmp/holysongs-final-current/final-source-manifest.sha256` (SHA-256
  `473ee4155a231842e9d4c26ed726135879bc2e3d26120efcdeb2f1e8a173dd20`).
  This is attributable dirty-worktree evidence, not a clean exact Git revision.
- Toolchain attribution: Gradle launcher JVM `17.0.20.1` (Homebrew), daemon JVM compatible with
  Java 25 per `gradle/gradle-daemon-jvm.properties`, with the owner-approved Java25 daemon
  exception retained. This is current runtime attribution, not a clean revision claim.
- Build gate: the current parent build evidence in `/tmp/holysongs-video-parent-build.log` records a
  clean no-cache rerun with `BUILD SUCCESSFUL` and `84 actionable tasks: 84 executed`.
  The worktree is dirty and behind `origin/master`, so the exact clean-source revision is still
  explicitly blocked.
- API37 runtime identity: `google/sdk_gphone16k_x86_64/emu64xa16k:17/CP31.260623.012/16064790:user/dev-keys`,
  1344x2992, density480, font1.0, WebView `149.0.7827.5`.
- Current integrated artifact hashes: app `e6a3e08e7e03255d47351dc85c460cc0fce3c4563a3894c7054d86fe2b9d9344`,
  test APK `4ba6c9760f2f555369397569f5f1cbbf5c9a07fe282839da7bd0fad5c5fa8fd5`.
- Parent final regression evidence in `/tmp/holysongs-final-current` recorded `SongListScreenTest`
  default `OK (12 tests)` with five explicit assumption skips covered by focused runs; final-artifact
  fit and compact runs each passed `OK (1)`, settings sync passed `OK (1)`, and the two resize cases
  each passed an exact isolated retry after a retained transient target-window input-injection failure.
  Earlier full fixture/order/winner and focused regression results remain in
  `/tmp/holysongs-current-regression`; the only subsequent production change was the reviewed rail color.
- Parent final video evidence in `/tmp/holysongs-final-current/video-final.log` passed
  `VideoSearchTest` `OK (5 tests)` on API37. This confirms the bounded Java video safety test path,
  but it is not a completion claim for provider audit, T041 or the remaining video/back/fullscreen/
  history/background/resize/provider checks.
- Parent API37 manual evidence: `/home/hanli3/.local/state/holysongs-android/verification/us1-current-manual-20260916T135338`
  captured a disposable app cleared and network-disabled cold launch under gesture font1, three-button
  font1, and gesture+tall-cutout font2. Each uiautomator dump reached title `阿爸父`, screenshots,
  window dumps, and configs were preserved, and host poll elapsed 15/9/12 seconds including `am start`
  and uiautomator polling. Master visually inspected all three screenshots and observed the app title,
  first row, sidebar, and app action unobscured, the three-button bar below the content, and the
  tall-cutout/font2 title/content below the system/cutout area. At font2 the compact rail showed `A-S`
  onscreen; full reachability remains backed by current compact instrumentation, not screenshot-only
  evidence.
- Later offline verification is recorded under `/home/hanli3/.local/state/holysongs-android/verification/us1-final-contrast-20260916T141141`.
  The first network-disable snapshot is stale and is not labeled as verified offline. The retained
  later offline record shows `offline-verified-state.txt` with airplane enabled, Wi‑Fi disabled,
  `mobile_data` set to `0`, and `Active default network: none` before app clear/launch; `offline-start.txt`
  and `offline-ready.txt` both show the app launching `MainActivity` and reporting `ready=1`.
  `offline-crash-anr-filter.txt` contains no matching app `FATAL`/`ANR`; that is a bounded
  logcat check and not universal proof.
- Direct instrumentation on the current artifact:
  - `VideoSearchTest`: `OK (5 tests)`.
  - `SongListScreenTest` default current run: `OK (12 tests)` with 5 explicit assumption skips that are
    later covered by focused opt-in runs; those skips are not counted as passes.
  - Focused opt-in passes: `fitModeMeasuredTapAndDragDoNotScrollTheRail` `OK (1)`,
    `compactModeTapSlopDragAndRailOnlyScrollingKeepSongListStable` `OK (1)`, resize cases `OK (2)`,
    legacy settings sync `OK (1)`, and the independent fixture/order/all-winner retry `OK (1)`.
  - The later `same-mode` and `mode-transition` resize reruns each succeeded `OK (1)` after the
    transient input-injection failure; the earlier failure is retained as historical evidence, not
    reclassified as a pass.
  - A direct shell launch of the non-exported `DisplayLyric` activity was rejected by
    `SecurityException`, confirming the non-exported boundary without proving the video `Back` path.

### Status of remaining blockers and unexecuted requirements

These remain open and are not reclassified as passed without direct evidence:

- Current-pilot IDE build remains open.
- API23 is explicitly waived/unexecuted.
- Actual recipient and no-recipient share delivery remain blocked.
- Video `Back`/fullscreen/history/background/resize/provider audit remains blocked.
- Provider/UTF8 work remains explicitly deferred to T035/T040.
- Exact clean-source revision remains blocked because the checkout is dirty and behind `origin/master`.
- T024 and T041 remain unchecked.
- No app crash/ANR claim is made beyond the bounded logcat checks above.

These blockers are preserved even though the current instrumented subset passed. The current evidence
is sufficient to treat the API37 subset as real and materially improved, but it does not constitute a
full US1 or final T024/T041 acceptance.

## Frozen inputs and record identity

| Item | Value |
|---|---|
| Source/checkpoint | dirty current worktree bound by `/tmp/holysongs-final-current/final-attribution.txt` SHA-256 `3fb3d410dac3b620f2b44fc8c775314dcd0bc1acddcec22573c8752eb90cbc30` and `/tmp/holysongs-final-current/final-source-manifest.sha256` SHA-256 `473ee4155a231842e9d4c26ed726135879bc2e3d26120efcdeb2f1e8a173dd20`; not a clean exact revision |
| Required API37 app APK | current parent-observed API37 app APK SHA-256: `e6a3e08e7e03255d47351dc85c460cc0fce3c4563a3894c7054d86fe2b9d9344` |
| API37 test APK | current parent-observed API37 test APK SHA-256: `4ba6c9760f2f555369397569f5f1cbbf5c9a07fe282839da7bd0fad5c5fa8fd5` |
| API37 frozen fixture | [`app/src/androidTest/assets/legacy-api37.json`](../../../app/src/androidTest/assets/legacy-api37.json), SHA-256 `cfae17540f5e958a9b32125158197cc55df5c93f54aed25ef5b84ce2047c5437` |
| Fixture provenance | T010 actual API37 legacy capture, `specs/002-kotlin-compose-ui/evidence/baseline.md`; all 414 effective titles/winners and all 26 raw destinations independently matched to the raw-source snapshot |
| Runtime selected by owner | `emulator-5554`, Pixel_10_Pro_XL, Android 17/API37, `sdk_gphone16k_x86_64`, WebView `com.google.android.webview/149.0.7827.5`; reverify before each run |
| API23 | waived for this continuation; no `legacy-api23.json` exists and no API23 pass may be recorded |
| Toolchain | Gradle launcher JVM `17.0.20.1` (Homebrew), daemon JVM compatible with Java25 from `gradle/gradle-daemon-jvm.properties`; owner-approved Java25 criteria retained; report actual SDK/Build Tools and maintain transcript evidence because the full 84-task stdout is not retained in `/tmp` |
| Preference/device isolation | use only the disposable authorized app/device; snapshot and restore app preferences in `finally`; navigation mode, font scale, window bounds and network state require explicit opt-in and restoration |

Every executed row must include the README fields: requirement/scenario ID, date, source
revision, APK SHA-256, toolchain, device/emulator identifier, Android API/version, WebView
where applicable, prerequisites, steps, expected result, observed result, status, evidence
path, blocker/limitation, and maintainer approval reference where applicable. Raw screenshots,
logs, APKs and preference snapshots remain outside tracked source.

## Checkpoint applicability

T024/US1 blocks on all U1, Main/list/sidebar U2/U4, legacy Settings-to-Main preference sync,
and the defined retained-screen N1/N2 safety/interoperability smoke. It does **not** block on
full migrated lyric/settings/about restoration or reflow; those are T033. It does block on a
new crash, ANR, unsafe outbound action, trapped video/loading return, lost list state, or
migration-introduced overlap regression. Full TalkBack and custom-sidebar accessibility are
deferred by owner decision; standard text/content-description labels and roles are still tested.

## Exact active-scope records

The following records inherit the date, dirty source manifest, app/test APK hashes, toolchain,
API37 device/WebView identity, and evidence paths from the frozen current-artifact record above;
individual rows name any earlier or additional artifact where applicable. Until a run has produced
an artifact-specific observation, its status is `blocked` (missing execution), not `passed`.

### U1 — offline catalog, order, selection, retry and lifecycle

| ID | Prerequisites and steps | Expected result | Current record |
|---|---|---|---|
| U1-LOAD | Disable connectivity on API37 before a clean first launch; install the integrated APK; launch Main and wait for the explicit Ready state | 422 unchanged source entries yield 414 effective titles; literal ASCII spaces are removed, duplicate winners match the fixture, and the screen is usable without network | `passed (instrumented load path only)` — current artifact contains direct `SongListScreenTest` load/retry/empty-catalog and recreate cases; no manual timing or full offline-UI user-flow measurement is recorded |
| U1-ORDER | After Ready, scroll/wait each lazy row before assertion; compare the effective order to `legacy-api37.json` and independently inspect first/last reachability | ChineseCharComp/HanziHelper and legacy runtime order are preserved; no host Collator golden is substituted; all 414 rows can be reached without requiring simultaneous semantics nodes | `passed (instrumented order path)` — `api37FixtureOrderWinnersAndEveryLegacyLetterDestinationAreReachable` passed; manual full row-by-row visual inspection remains unexecuted |
| U1-WINNERS | Open each fixture duplicate winner through actual Main selection: 以色列的圣者, 像天空的鸽子, 全地宣告, 天堂在我心, 愿您崇高, 耶稣基督是主, 耶稣耶稣, 轻轻听 | Main sends the exact effective title and winning lyric to retained DisplayLyric; no first-entry/raw-duplicate lyric is shown | `passed (instrumented winner path)` — same direct API37 fixture test passed; manual lyric-screen visual inspection remains unexecuted |
| U1-LETTER | Exercise A–Z using the measured active rail mode. For each letter, compare the selected row to the fixture's raw destination, including absent I/O/U/V destinations | Alphabet is exactly A–Z; every captured valid raw destination is preserved; invalid/empty/out-of-range results safely do nothing and do not scroll | `passed (instrumented A–Z path)` — same direct API37 fixture test passed; invalid/manual no-op visual checks are not independently documented |
| U1-ERROR | Inject the approved `MainActivity.catalogLoaderFactory` opener to fail once; observe Error, then activate the visible retry control exactly once | Understandable error/back state; no implicit second load; explicit retry transitions Error → Loading → Ready; cancellation is not displayed as parse failure | `passed (instrumented explicit-retry path)` — `loadingSurvivesRecreationAndErrorRetriesOnlyAfterExplicitRetry` passed; no manual error-dialog screenshot |
| U1-EMPTY | Inject an empty `<songs></songs>` fixture through the approved loader seam | Non-crashing empty UI, no letter navigation and no attempted invalid scroll | `passed (instrumented empty-catalog path)` — `emptyCatalogIsSafeAndHasNoLetterNavigation` passed; no manual UX review of empty state |
| U1-LOAD-ONCE | Count the real loader opener; launch offline, observe Ready, scroll to a nonzero row, capture `song-list` VerticalScrollAxisRange value, recompose/theme-update, background/return, recreate Activity while Ready | One load per ViewModel owner; no load from subscriptions, preference changes, resume or recomposition; process recreation restores the nonzero semantic list position after Ready | `passed (instrumented load-once/recreate path)` — `catalogLoadsOnceAndRestoresLazyPositionAcrossRecreation` passed; no manual time/position capture |
| U4-RECREATE-LOADING | Hold the controlled opener in Loading, recreate Main, release it, then wait explicitly for Ready | Activity recreation does not publish stale/error state or duplicate work; the retained owner reaches Ready once and list restoration occurs only after Ready | `passed (instrumented recreation path)` — combined load-once and explicit-retry tests passed; no manual hold-in-loading reproduction |

### U2 — Main insets, labels, resizing and rail gestures

| ID | Prerequisites and steps | Expected result | Current record |
|---|---|---|---|
| U2-OVERLAP | Reproduce/retain T010 API37 gesture-mode bounds and screenshot metadata; install integrated Main and compare title/first row/app bar/sidebar/last row | The legacy top-bar overlap is corrected by one Compose inset boundary; first and last content, rail and actions are unobscured | `partial manual evidence` — current gesture/three-button/tall-cutout screenshots were visually inspected by master and showed no obvious app overlap, but this is not a full T024 pass because actual geometry/reachability under all modes remains incomplete |
| U2-INSETS | Run API37 gesture mode, then separately three-button mode and a cutout/large-resizable configuration; inspect window bounds and scroll reachability | One Scaffold inset boundary is applied/consumed; no duplicate platform action bar; horizontal cutout and bottom navigation areas do not cover content | `partial manual evidence` — gesture, three-button and tall-cutout screenshots all showed the title/content below the system/cutout area, but full three-button/cutout/large-window reachability remains unexecuted |
| U2-LABELS | Query real Main semantics after Ready. Locate settings/about/app-share by their string labels or meaningful content descriptions; locate each rail letter by visible text plus click action/selected state | Tags are geometry seams only; standard interactive controls have meaningful labels/roles/selected state. No full TalkBack/custom-sidebar claim is made | `passed (instrumented semantics path)` — `sidebarLabelsAndCancellationUseStandardSemantics` passed; no full TalkBack/custom rail accessibility claim |
| U2-RESIZE-FIT | With `sidebarMode=fit` on a measured fit window (all 26 rows are present and fit inside measured rail bounds; rail vertical range is absent/zero), tap and measured-center drag A→D; then run the opt-in resize transition with `sidebarMode=compact allowSidebarResize=true` | Fit mode selects A/D actual captured destinations, rail range remains zero, cancellation preserves the committed destination, and resize/mode transition cancels pending input without a second navigation | `passed (instrumented fit-mode path)` — `fit-after-clock.log` passed `OK (1 test)`; no manual large-window/inset observation |
| U2-RESIZE-COMPACT | With `sidebarMode=compact` on a measured compact window (rail vertical range max > 0), perform explicit down/under-slop move/up, past-slop move/up, cancellation, then rail-scroll to Z | Compact mode's drag changes only rail range, preserves song-list semantic offset and creates no indicator; under-slop release reaches A, A/Z remain reachable after rail movement; cancellation is inert | `passed (instrumented compact-mode path)` — `compact-after-clock.log` passed `OK (1 test)`; no manual three-button/cutout/overlap check |
| U2-INDICATOR | Select a letter; observe the indicator, wait at least 2 seconds; background Main while the indicator is visible | Indicator dismisses at 2 seconds and is cancelled/dismissed on stop/disposal; no WindowManager popup remains | `passed (instrumented indicator path)` — `indicator-after-test-fix.log` passed `OK (1 test)`; this is not a manual screen-recorded timing claim |
| U2-FONT | With lyric size 30, execute API37 system font scales 1.0, 1.3 and 2.0 (or record device maximum); scroll long titles and controls; restore scale in finally | Titles/controls remain usable and reachable; no claim of pixel identity across scales | `partial manual evidence` — font2 screenshot showed the compact rail and content below the cutout/system area, but full font-scale reachability still depends on compact instrumentation and remains not a T024 pass |

### U3/N1/N2 — retained reader, Settings, About, app share and video safety smoke

These are Main-only retained-screen checks at the US1 checkpoint. Full reader restoration and
new display-only font rendering remain T033 scope.

| ID | Prerequisites and steps | Expected result | Current record |
|---|---|---|---|
| N1-LYRIC | From a fixture winner, use the real Main row click; monitor `DisplayLyricActivity` and inspect String extras; Back to Main | Exact `com.goodtrendltd.SONG_NAME` and `com.goodtrendltd.LYRIC` values; readable representative Chinese lyric; selected Main/list state remains and no duplicate Main is created | `passed (instrumented lyric-extra path)` — the direct fixture/order/winner assertions validate the retained lyric title/lyric extras, but manual retained-screen visual/full delivery remains a separate gap |
| N1-SETTINGS | Scroll Main to a nonzero row and capture `song-list` VerticalScrollAxisRange; open Settings through the real labeled action; dispatch genuine system Back; compare resumed lifecycle, same Main instance and list range/title | Existing Settings launches; Back returns to the same Main instance without CLEAR_TOP/new-Main restart; list position and stored choices remain legacy-compatible | `passed (instrumented settings-return path)` — `songlist-settings-sync.log` passed `OK (1 test)` and is the direct settings-to-Main sync evidence; full manual list-range title reconciliation remains open |
| N1-ABOUT | Open About through the real action; inspect full captured text and URLSpan destinations (including the website trailing slash), then return; link click/intent launch is a separate unexecuted subcheck | Existing About content and exact fixture URLSpan destinations remain preserved; Back returns safely | `partial (instrumented text/URLSpan/Back path)` — the retained destination test inspects About text and URLSpan destinations and returns with Back; it does not click a link or monitor an outbound link intent |
| U3-SYNC | With explicit disposable preference authorization, change a legacy Settings font/theme control; return to Main; restore the preference map in `finally` | Main observes the existing `appPrefFile` value/theme without clearing, renaming or rewriting unrelated keys; no list reload or state loss | `passed (instrumented settings-sync path)` — settings sync log passed `OK (1 test)`; this is a direct same-instance revert, not full manual preference matrix |
| N2-APP | Install/use the actual Main app-share action with a default-constructor blocking `ActivityMonitor`; inspect nested chooser/send intent and do not launch a recipient | `ACTION_CHOOSER` wrapping `ACTION_SEND`, `text/plain`, subject `敬拜赞美诗`, and exact baseline whitespace/link text; no recipient/provider is launched | `passed (instrumented share-action path)` — the direct default run includes `retainedDestinationsAndAppShareUseRealActivitiesWithoutReplayedActions` and passed; actual recipient/no-recipient delivery remains unexecuted |
| N2-NONE | Execute on an authorized disposable API37 image with no recipient handler, using an intercepted outbound intent; restore device/application state | Missing recipient/launch failure is handled safely and returns or reports unavailable; no crash. Do not claim delivery from a captured intent | `blocked` — no-recipient runtime configuration has not been authorized/executed in this lane |
| N1-VIDEO/N4-safety | From a selected retained lyric, attempt each existing provider only under the video safety procedure; intercept/cancel where possible, press Back, and return offline | Invalid/missing target is rejected; cancellation/Back returns promptly to selected lyric/Main with no trapped loading, crash, ANR or unsafe external action. Provider availability is not claimed | `blocked` — provider audit and full N3/N4 remain unexecuted |
| U4-EFFECTS | Complete one intercepted app-share launch, recreate/resume Main, and inspect the monitor's launch count; repeat retained Back returns | Exactly one intercepted outbound launch occurs; recreation/resume does not replay sharing/navigation and does not create duplicate destinations | `passed (instrumented no-replay action path)` — direct share-action test passed; specific recipient/no-recipient and recreate-after-share manual steps remain open |
| U4-COLD | Stop process and launch a fresh task offline | Cold launch starts Main/list; no persistent resume-last-song behavior is introduced | `partial` — the verified-offline record shows `LaunchState: COLD` and Ready; no cold relaunch after first selecting a song/nonzero list position was executed |

## Explicitly unexecuted endpoint/configuration records

* **API23/Android6:** `blocked` by the owner waiver recorded in `execution-decisions.md`.
  No fixture or result is fabricated. minSdk23 and API compatibility checks remain required.
* **API37 three-button navigation:** bounded Main screenshot/config evidence exists; full interaction
  and retained-video Back coverage remains blocked.
* **API37 cutout and large/resizable window:** tall-cutout/font2 Main screenshot and focused resize
  evidence exist; complete retained-screen/video reachability remains blocked.
* **API37 system font 1.0/1.3/2.0:** Main visual/compact-rail evidence exists at these scales;
  full retained-screen reflow remains US2 scope and video reflow remains blocked.
* **No-recipient share:** `blocked`; no unrelated apps may be disabled or cleared to manufacture
  the case. A captured outbound intent is not delivery evidence.
* **Full TalkBack/custom rail traversal:** intentionally deferred by owner decision; not a
  T024 pass criterion and not a test gap to conceal with a tag-only assertion.
* **Full lyric/settings/about reflow/restoration and process-death behavior:** unexecuted by
  T024 and due to T025–T033; retained-screen smoke above must not be upgraded into a T033 pass.

## Master integration corrections

After worker b651a2de-13ac-4444-ac88-85597865e27e returned its final draft, master corrected
remaining false assumptions: last-row scrolling clamps instead of making that row first; each
visible window must agree with the fixture order; geometry ignores clipped/prefetched rows;
fit/compact runs skip the other mode rather than failing its asserted mode; compact center lookup
uses only visible letters; rail exposure checks actual display, not merely node existence.
Compact drag now moves upward from a visible midpoint and asserts positive rail scroll in addition
to unchanged song-list state. Resize requests a tall normal-font window and verifies fit geometry
rather than assuming any resize caused a mode change. The current direct instrumentation validates
these corrections for the exercised API37 paths, but the remaining manual subclauses remain blocked.
Snapshot compile remains blocked by missing future MainActivity.catalogLoaderFactory; actual
integrated compile/runtime will validate the seams. Unreliable cross-recreation pointer cases still
must be reported as blocked and exercised manually, never called passed by source alone.

## T014 implementation/gap notes

`SongListScreenTest.kt` uses only the master-selected seam
`MainActivity.catalogLoaderFactory: (Context) -> SongCatalogLoader`, real Activity content,
positive Ready/list semantics, `performScrollToIndex` for order enumeration,
`performScrollToNode` only on the rail for compact exposure, and T010's API37 fixture. Letter
assertions never scroll the song list to repair a failed navigation; they compare the semantic
first visible title with the captured raw destination. It does not call `setContent`, add a fake
host, add an exported debug control, or use a second production loader/parser.

The source has separate measured fit/compact tests gated by explicit `-e sidebarMode=fit` or
`-e sidebarMode=compact`, plus a reversible shell-backed resize/font transition gated by
`-e allowSidebarResize=true -e sidebarMode=compact`. Fit asserts all 26 measured rows fit, uses
measured A/D centers for a real drag and checks D's captured destination; compact asserts explicit
down/under-slop/up, past-slop drag/release, cancellation, unchanged list semantic offset and
A/Z reachability. Platform `ViewConfiguration.scaledTouchSlop` supplies the threshold. These
paths are direct current-artifact runs and passed under the relevant opt-ins. The resize helper
snapshots/restores `wm size`, `wm density`, and `system font_scale` in `finally`, and begins with
a pending compact pointer before resizing. If cross-recreation pointer cancellation is unreliable
on a target, the genuine manual T015 reproduction must be recorded rather than upgrading this
source assertion to a pass. Settings theme return uses genuine system Back, and only the explicit
`allowPreferenceMutation=true` test writes then restores the full raw preference map (including
wrong-type/missing values).

Required opt-in invocations for T024 acceptance are explicit and disposable-device-only, for
example:

```text
-e sidebarMode=fit
-e sidebarMode=compact
-e allowSidebarResize=true -e sidebarMode=compact
-e allowPreferenceMutation=true
```

No-argument runs must not mutate preferences or device configuration; an opt-in run is mandatory
for accepting those rows, not a generic skip-based pass.

Current integrated instrumentation did execute real API37 Main runtime tests, including the default
`OK (12 tests)` run and the focused opt-in passes, but this does not make T024 complete. The rows
that remain genuinely unexecuted or purely manual are still tracked as blocked and must not be
counted as passes.

## API37 remediation rerun — 2026-09-16

Artifact attribution: debug APK `318e8b9be474e30af30d7e42053549108eab2c4f73037b32d006aeba88f1eb7a`; debug androidTest APK `71bbf8ebe6682420ed365bd4eea7d71b547e9fd8d665c82e3aabfbaae82cd5ab`. Device: API37 `emulator-5554` (`Pixel_10_Pro_XL(AVD) - 17`).

- `AboutScreenTest`: **2/2 passed**; `SettingsScreenTest`: **4/4 passed**; `VideoSearchTest`: **8/8 passed**; `LyricScreenTest`: **3/3 passed**; `ReaderLifecycleTest`: **1/1 passed**. Logs are under `/tmp/holysongs-api37-final/`.
- `ReaderPreferencesTest -Pandroid.testInstrumentationRunnerArguments.readerPreferencesMutate=true`: **4/4 passed**.
- `SongListScreenTest` default: **12 tests**, 7 passed and 5 explicit assumption rows (fit/compact/resize/preference opt-ins); no source assertion failures. `sidebarMode=fit`: fit row passed; `sidebarMode=compact` reached the compact row but the API37 window measured fit geometry (positive compact range unavailable), so that opt-in is an environment blocker, not rewritten. `allowSidebarResize=true` with compact likewise is blocked by fit geometry. `allowPreferenceMutation=true`: preference-sync row passed; remaining rows were explicit assumptions. Logs: `SongListScreenTest-default-final.log`, `SongListScreenTest-sidebarMode-fit-final.log`, `SongListScreenTest-sidebarMode-compact.log`, `SongListScreenTest-allowSidebarResize.log`, and `SongListScreenTest-allowPreferenceMutation-final.log`.

These results are scoped to the disposable API37 endpoint and do not claim manual share/provider/process-death/fullscreen/history acceptance.
