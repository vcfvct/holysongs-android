---
description: "Executable tasks for the incremental HolySongs Kotlin and Compose migration"
---

# Tasks: Incremental Kotlin and Jetpack Compose UI

**Input**: Design documents from `specs/002-kotlin-compose-ui/`.

**Prerequisites**: [spec.md](spec.md), [plan.md](plan.md), [research.md](research.md),
[data-model.md](data-model.md), [contracts/build-and-verification.md](contracts/build-and-verification.md),
[contracts/ui.md](contracts/ui.md), [contracts/navigation-and-video.md](contracts/navigation-and-video.md),
[quickstart.md](quickstart.md), [transition.md](transition.md), `AGENTS.md`, and
`.specify/memory/constitution.md`.

**Tests**: Explicitly required by FR-012 and the constitution. Define tests before the relevant
replacement. Characterization tests may pass against existing behavior; a regression test needs
an observed failing case or source-supported defect, not an invented failure. New-type compilation
failures can establish missing implementation, but are not evidence of a runtime regression.
Unavailable tools/devices are blockers, not passing or deliberately failing tests.

**Organization**: Setup → shared foundation → US1 (P1) → US2 (P1) → US3 (P2) → final acceptance.
Each story has its own acceptance slice; the stories share Activity boundaries and are not
independent concurrent writers. A narrow early-video-safety path is explicitly defined below
so a known legacy safety failure cannot be deferred merely to make the MVP appear complete.

**Generation status**: All 46 tasks were initially unchecked. Task generation executed no implementation or
tests. IDs are local to feature002, not replacements for feature001's historical checkbox IDs.

**Closure update (2026-09-16):** The owner reported testing the complete app on a phone, accepted
that it looks great, and directed creation of the final PR and closure of this phase. This is the
product acceptance decision. Detailed unchecked gates below remain an honest historical record of
checks not independently attributable as automated passes; they are not silently rewritten.
The final PR candidate additionally passed `clean :app:assembleDebug :app:testDebugUnitTest
:app:lintDebug :app:assembleDebugAndroidTest` with 85 tasks executed.

**Execution update (2026-09-16):** Current US1/T024 verification is materially improved but still
unchecked. The worktree is dirty and behind `origin/master` (`HEAD` = `f62519b`, `origin/master` =
`50658b7`), so the exact clean-source revision is not available for final artifact lock. The current
build/test evidence was captured against the local dirty worktree and remains bounded to the
current parent-observed API37 run, not a clean exact source revision.

Parent regression evidence in `/tmp/holysongs-current-regression` shows the current API37 artifact:
`SongListScreenTest` default `OK (12 tests)` with five explicit assumption skips later covered by
focused opt-in runs; `fit` `OK (1)`, `compact` `OK (1)`, `resize` `OK (2)`, settings sync `OK (1)`,
and the independent fixture/order/all-winner retry `OK (1)`. Parent VideoSearch follow-up
`/tmp/holysongs-video-parent-followup-device.log` passed `VideoSearchTest` `OK (5 tests)`. Parent
build evidence in `/tmp/holysongs-video-parent-build.log` records a clean no-cache rerun
`BUILD SUCCESSFUL` with `84 actionable tasks: 84 executed`.

The final current integrated artifact hashes are app `e6a3e08e7e03255d47351dc85c460cc0fce3c4563a3894c7054d86fe2b9d9344`
and test APK `4ba6c9760f2f555369397569f5f1cbbf5c9a07fe282839da7bd0fad5c5fa8fd5`.
They are bound to the dirty source by `/tmp/holysongs-final-current/final-attribution.txt`
(SHA-256 `3fb3d410dac3b620f2b44fc8c775314dcd0bc1acddcec22573c8752eb90cbc30`)
and its source manifest (SHA-256 `473ee4155a231842e9d4c26ed726135879bc2e3d26120efcdeb2f1e8a173dd20`).
The API37 device fingerprint is `google/sdk_gphone16k_x86_64/emu64xa16k:17/CP31.260623.012/16064790:user/dev-keys`,
1344x2992 at density480/font1.0, with WebView `149.0.7827.5`.

Manual current evidence under `/home/hanli3/.local/state/holysongs-android/verification/us1-current-manual-20260916T135338`
shows a disposable app cleared and network-disabled cold Main launch under gesture font1, three-button font1,
and gesture+tall-cutout font2; each uiautomator dump reached title `阿爸父`, screenshots/window dumps/configs
were preserved, and host poll elapsed 15/9/12 seconds including `am start` and uiautomator polling. Master
visually inspected all three screenshots and observed the title/first row/sidebar/app action unobscured,
the three-button bar below the content, and the tall-cutout/font2 title/content below the system/cutout area.
At font2 the compact rail showed `A-S` onscreen; full reachability remains dependent on the current compact
instrumentation rather than screenshot-only evidence. The later final-contrast directory
`/home/hanli3/.local/state/holysongs-android/verification/us1-final-contrast-20260916T141141` confirms the
rail contrast correction and an offline verified path (`offline-verified-state.txt`, `offline-start.txt`,
`offline-ready.txt`); the first network-disable snapshot remains stale and is not treated as verified
offline evidence. The retained full logcat plus `offline-crash-anr-filter.txt` found no matching app
`FATAL`/`ANR` during that bounded interval; this is not a universal proof. Device state was restored to gestural/font1/network-on and the app was stopped. A
separate shell launch of the non-exported `DisplayLyric` activity was rejected by `SecurityException`,
confirming the non-exported boundary but providing no video `Back` result.

T024/T041 remain unchecked. API23 remains waived/unexecuted, actual recipient/no-recipient share delivery remains
blocked, the current-pilot IDE build remains blocked, exact clean-source revision remains blocked, and provider/UTF-8
work is explicitly deferred to T035/T040. The direct instrumentation is real current-artifact evidence for a subset
of the API37 path, but it does not constitute full US1 or final completion for the feature.

Post-review source corrections add Compose-host themes, explicit safe About activation, Activity-owned
share/video guards, effective-font rendering, query encoding, WebView scheme/cancel handling, and a
documented same-URL callback limitation. No task checkbox was promoted for these source changes alone:
T024, T032, T040, T041, and final acceptance still require their complete artifact/device/provider
evidence and remain governed by the existing gates.

## Format: `[ID] [P?] [Story] Description`

- `[P]` means independent files in the specified ready batch, after the batch's prerequisites.
  It never bypasses the dependency graph or permits simultaneous mutation of one working tree.
- `[US1]`, `[US2]`, `[US3]` map to the specification's stories; setup/foundation/final tasks have no story label.
- The master owns integration, acceptance and checkbox changes. For authorized delegation, use
  the explicit model routing in `AGENTS.md`; do not silently inherit/substitute a child model.
- Preserve existing edits and local configuration. No task grants commit/push/PR publication,
  release signing, license acceptance, provider replacement, or production-device data clearing.
- Future evidence paths below are deliverables, not existing successful results. A blocked final
  acceptance task stays unchecked even if some subchecks passed.

## Path Conventions

All task paths are repository-relative to `/home/hanli3/GIT/holysongs-android`.
Legacy Java stays under `src/com/goodtrendltd/HolySongs/`; replacement Kotlin goes under
`app/src/main/kotlin/com/goodtrendltd/HolySongs/`. Tests use existing `app/src/test/java/`, new
`app/src/test/kotlin/` and `app/src/androidTest/kotlin/`. No bulk source relocation.
Evidence is sanitized Markdown under `specs/002-kotlin-compose-ui/evidence/`; raw logs/APKs/screenshots
stay outside tracked source. Test fixtures below are test-only assets, never modifications to
`assets/songs.xml` or production preference storage.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Establish a recoverable development boundary, tool/device access, and evidence rules.

- [X] T001 Capture branch/revision, dirty/untracked state and a recoverable checkpoint in `specs/002-kotlin-compose-ui/evidence/environment.md`; inventory/configure existing JDK17, SDK37/Build Tools36.0.0/platform-tools and Studio Quail4/2026.1.4 through documented local setup, distinguishing IDE runtime from Gradle launcher/daemon JDK; preserve `res/layout/main.xml`, local.properties, local daemon criteria and unrelated Spec Kit files, record missing tools/licenses as blockers, and do not silently install packages or accept licenses.
- [X] T002 [P] Define scenario/result recording in `specs/002-kotlin-compose-ui/evidence/README.md`, quoting the data-model fields "requirement/scenario ID; date; source revision; APK SHA-256; toolchain; device/emulator identifier; Android API/version; WebView version where applicable; prerequisites; steps; expected result; observed result; status (`passed`, `failed`, `blocked`); evidence path; blocker/limitation; maintainer approval reference for accepted provider changes or limitations" and "Unexecuted checks have no success result"; retain dated prior outcomes and distinguish a checkpoint from final acceptance.
- [X] T003 [P] Inventory dedicated API23/API37 phones, API37 gesture/three-button modes and a large/resizable configuration in `specs/002-kotlin-compose-ui/evidence/devices.md`; record actual API/build/WebView identifiers, fresh offline-install/preference-fixture isolation and recipient/no-recipient availability, verify explicit device ownership/selection, and prohibit production uninstall/data clearing or disabling unrelated apps to manufacture a test case.

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Protect the existing behavior, enable the mixed-language toolchain and capture a
legacy-UI runtime baseline before replacing Activities or section lookup.

**Critical**: Complete this phase before story implementation. If T009/T010 is blocked, leave
it unchecked and stop dependent implementation; unrelated documentation/test preparation is not
acceptance and must not be represented as a completed foundation.

- [X] T004 [P] Complete `app/src/test/java/com/goodtrendltd/HolySongs/BuildInputsTest.java` and `app/src/test/java/com/goodtrendltd/HolySongs/SongCatalogTest.java`, with independent expected fixtures in `app/src/test/resources/catalog-baseline.xml`, asserting song SHA-256 `88eb0db602e018b49a327947dd8607f04e6159e58f39ec38ed59f20c39af9d89`, JAR SHA-256 `6576dea7d351a0f5df1595b9c432ba7cf9246ca0ab6f7019b9ca4e6d500b0e68`, and source fields "Zero-based XML position; all 422 entries stay in original order", "Raw parsed name, before production space-removal preprocessing", "Raw parsed lyric, including line breaks and original text"; cover full source order, both positions of all eight duplicate pairs and exact representative raw lyric/line-break text using the real XMLParser, without generating expected values from the implementation under test or changing shipped bytes.
- [X] T005 [P] Complete `app/src/test/java/com/goodtrendltd/HolySongs/helpers/HanziHelperTest.java` and `app/src/test/java/com/goodtrendltd/HolySongs/helpers/ChineseCharCompTest.java` for first-reading polyphonic behavior, lowercase/no-tone/Unicode-ü output, non-Chinese fallback and comparator reflexivity/antisymmetry/transitivity using the existing helpers; record that host Collator results are not Android ordering fixtures and do not rewrite the sorting/pinyin algorithm.
- [X] T006 Configure `build.gradle`, `gradle.properties` and `app/build.gradle` for AGP built-in Kotlin2.2.10 plus matching `org.jetbrains.kotlin.plugin.compose`2.2.10, remove `android.builtInKotlin=false`, enable AndroidX/Compose and AndroidJUnitRunner1.7.0, register main/test/androidTest Kotlin directories separately from existing Java roots, and adopt every direct dependency/configuration from research.md R1 (BOM2026.09.00/core1.12.1/Material3 1.4.0, Activity1.13.0, Lifecycle2.11.0, coroutines1.11.0, ext JUnit1.3.0, retained JUnit4.13.2/JAR); preserve root manifest/res/assets and repositoryRoot mappings, Gradle9.6.0/JDK17/Java-Kotlin target8/compile-target37/Build Tools36.0.0, set approved minSdk23, keep identity/version8/2.5, and add no separate Kotlin Android plugin, legacy compiler-extension DSL, Jetifier without evidence, permission outside build contract B2's allowlist or speculative library; the only added permission allowed is `com.goodtrendltd.HolySongs.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` with `signature` protection and its matching use from AndroidX Core.
- [X] T007 Add `app/src/main/kotlin/com/goodtrendltd/HolySongs/ui/HolySongsTheme.kt` and a narrowly named no-platform-action-bar theme in `res/values/styles.xml`, preserving existing Holo styles for unmigrated Activities; use stored nightMode for dark/light Material3 rather than system/dynamic theming, keep normal font scaling, and compile this real Compose function as the production Kotlin/source-set probe without changing launcher UI yet.
- [X] T008 Add test-only legacy capture instrumentation in `app/src/androidTest/kotlin/com/goodtrendltd/HolySongs/LegacyBaselineTest.kt` for U1-LOAD/U1-ORDER/U1-LETTER, N1/N2 payloads and U3 preferences/About content; observe the real legacy Activity/adapter/parser pipeline without copying it, capture 414 effective titles, all winning lyrics, A–Z valid/absent/unsafe outcomes, exact share whitespace/links and stored types, snapshot/restore disposable preference fixtures, and keep emitted raw observations outside tracked source; the capture test is only run while the legacy Main/reader definitions are present and will be retired after fixture transfer in T024.
- [X] T009 Resolve debug and androidTest compile/runtime graphs after T006–T008, enable/review app dependency locking in `app/build.gradle` and generate `app/gradle.lockfile`, then record build/IDE/APK checks in `specs/002-kotlin-compose-ui/evidence/build.md`; run `./gradlew --version`, `:app:buildEnvironment`, dependency reports and `clean :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest`, inspect compiled production Kotlin/Java target8 and compiler-plugin2.2.10 separately from stdlib, all resolved AAR constraints/merged manifests, min23/target37/identity/hash/v1-signature compatibility and B2's exact permission allowlist (including AndroidX Core's app-private signature declaration/use, protection level and manifest-merger attribution), and Studio sync/build with JDK17; reject any other manifest permission addition rather than silently widening or stripping the allowlist; fix only concrete enablement blockers, rerun affected checks, and stop rather than force incompatible dependencies, broaden lint suppression or raise minSdk.
- [X] T010 Execute T008 on the T009 legacy-UI artifact on API23 and API37 before Main/index replacement, freezing sanitized expected order/letter/winner/payload fixtures in `app/src/androidTest/assets/legacy-api23.json`, `app/src/androidTest/assets/legacy-api37.json` and `specs/002-kotlin-compose-ui/evidence/baseline.md`; quote "Alphabet is exactly A–Z" and "For valid present/absent results, preserve recorded legacy destinations", record invalid-result no-op expectations separately from actual failures, reproduce the reported top-bar overlap with screenshots/window bounds/theme/bar identification/navigation mode, and capture process-stopped launch-to-usable-list timing/method, scroll stalls and app crash/ANR observations; attribute the changed-toolchain legacy-UI artifact honestly, never claim it is the original checkpoint APK or invent unavailable runtime fixtures.

**Checkpoint**: Existing behavior has attributable fixtures and mixed Kotlin/Java/Compose compilation
has been verified without a core screen replacement. Enablement alone is not the user-visible MVP.

## Phase 3: User Story 1 — Build and Browse a Compose Song List (Priority: P1) — MVP

**Goal**: Replace Main/list/sidebar, correct demonstrated inset overlap, and preserve transitions
to the still-legacy reader/settings/about/video screens.

**Independent Test**: From a clean implementation checkout, build through CLI/IDE and run offline
first launch, scroll/letter navigation, correct lyric selection and Back on API23/API37. Verify
first/last row/rail reachability, font/window cases, list state, labels and retained-screen smoke.
No Compose reader/settings/about replacement is needed to exercise this story.

### Tests for User Story 1

- [X] T011 [P] [US1] Add production-contract tests in `app/src/test/kotlin/com/goodtrendltd/HolySongs/data/SongCatalogLoaderTest.kt` and `app/src/test/kotlin/com/goodtrendltd/HolySongs/data/LegacySectionIndexTest.kt` before extraction, consuming independent T004/T010 fixtures and asserting "Effective title → winning lyric; 414 keys for the bundled asset", "HashMap-derived keys sorted with existing ChineseCharComp; no new tie-breaker", "Guarded pinyin initial per ordered title; derived using existing HanziHelper", and "LegacySectionIndex for A–Z lookups, scoped to this catalog and runtime ordering"; test U+0020-only preprocessing, complete/closed reads, all duplicate winners, malformed/missing-field/empty/non-Chinese inputs, no raw duplicate lazy keys, and safe no-destination results without treating host collation as device parity.
- [X] T012 [P] [US1] Add `app/src/androidTest/kotlin/com/goodtrendltd/HolySongs/ReaderPreferencesTest.kt` for original `appPrefFile` storage, "fontSize"/"Integer"/default "20"/"16, 18, 20, 22, 24, 26, 28, 30; reset writes 20" and "nightMode"/"Boolean"/default "true"/"true = dark, false = light"; cover listener lifetime/fresh snapshot/legacy writes, no initial-composition writes, normal relaunch persistence, the data-model font fixture table (missing/String24/Integer.MIN_VALUE/-1/0/1/15/16/20/30/32/200/201/Integer.MAX_VALUE), derive "effectiveFontSize: Integer 1–200 inclusive unchanged; otherwise20" without changing any raw key/type/value, preserve existing UI choices/reset20 and wrong-type nightMode defaulttrue, and obey "No key renaming, type changes, clearing, new store, or migration", restoring test-only preference state afterward; test the adapter here, with migrated lyric/settings rendering due in T025/T026.
- [X] T013 [P] [US1] Add `app/src/test/kotlin/com/goodtrendltd/HolySongs/ui/SongListViewModelTest.kt` with controlled asset opener/dispatcher, asserting "Loading → Ready(SongCatalog)", "Loading → Error(displayable reason)", "Error → Loading" only on explicit retry, one off-main load per owner, no repeated load on subscriptions/preference updates/resume, cancellation not becoming an error and obsolete results not publishing after owner clearing.
- [X] T014 [P] [US1] Add `app/src/androidTest/kotlin/com/goodtrendltd/HolySongs/SongListScreenTest.kt` for U1 and Main-only U2/U4 plus retained-screen smoke under `specs/002-kotlin-compose-ui/contracts/ui.md#checkpoint-applicability`, including U2-RESIZE fit-mode tap/drag selection and compact-mode tap-selection/drag-only-rail-scroll with slop/cancel/resize tests, and N1-LYRIC/N1-SETTINGS/N1-ABOUT/N2-APP using the actual Main Activity after replacement, stable effective-title keys, explicit Ready waits, scroll-before-assert for lazy rows, T010 runtime fixtures, standard semantic labels/roles, measured rail tap/drag/short-window behavior, recreation while Loading, 2-second indicator/background dismissal, legacy Settings theme return and non-replayed navigation/share actions; do not overwrite content already set by the Activity or assert all 414 rows exist simultaneously in semantics.
- [X] T015 [P] [US1] Define exact US1 runtime scenarios scoped by `specs/002-kotlin-compose-ui/contracts/ui.md#checkpoint-applicability` (all U1, Main-only U2/U4, legacy preference-to-Main sync and defined retained-screen N1/N2 safety/interoperability smoke), with later-screen restoration/reflow due at T033 rather than treated as a pilot pass or gate, expected fixture links and unexecuted records in `specs/002-kotlin-compose-ui/evidence/us1-list.md`, including API23/API37 offline first launch, full effective order/all winners/A–Z navigation, legacy reader/settings/about/app-share/video entry-return smoke, overlap before/after, API37 gesture/three-button/cutout/large-resizable windows and font scales1.0/1.3/2.0 (record device maximum if lower), startup-to-Ready timing/scroll observations/crash-ANR checks; no full TalkBack/sidebar-accessibility acceptance is implied.

### Implementation for User Story 1

- [X] T016 [US1] Implement `app/src/main/kotlin/com/goodtrendltd/HolySongs/data/SongCatalog.kt` and `app/src/main/kotlin/com/goodtrendltd/HolySongs/data/SongCatalogLoader.kt` against T011: quote/obey "asset opener → full UTF-8 read with stream close → remove literal U+0020 across XML → parse → iterate in source order → overwrite HashMap entries by effective title → sort keys with existing Collator(Locale.CHINA) comparator → derive guarded initials/index state"; preserve parser extraction, 414 effective keys and last-entry winners 以色列的圣者373/像天空的鸽子214/全地宣告144/天堂在我心351/愿您崇高342/耶稣基督是主123/耶稣耶稣110/轻轻听320 (zero-based), immutable Ready data and guarded initials, with injected off-main dispatcher, no silent partial-success parse or rewritten source data.
- [X] T017 [US1] Extract `app/src/main/kotlin/com/goodtrendltd/HolySongs/data/LegacySectionIndex.kt` from `src/com/goodtrendltd/HolySongs/SongTitleAdapter.java` against T011/T010, preserving valid present/absent destinations and existing lookup algorithm with equivalent scoped cache storage; obey "For an empty catalog or invalid/out-of-range result, return no destination and do not scroll", retain "Alphabet is exactly A–Z", and stop for recorded owner review before changing section semantics when correct navigation and baseline parity conflict; do not substitute pinyin sorting or a first-initial map.
- [X] T018 [US1] Implement `app/src/main/kotlin/com/goodtrendltd/HolySongs/data/ReaderPreferences.kt` against T012 with application context, immutable snapshots, off-main disk initialization, strongly held listener/fresh snapshot and paired unregister/refresh, lifecycle-aware Flow collection and sparse user-triggered apply writes; preserve exact `appPrefFile`/Integer fontSize/default20/choices16–30step2/reset20 and Boolean nightMode/defaulttrue semantics, obey "Reset affects fontSize only" and "For corrupt types use in-memory defaults without overwriting stored data", derive "effectiveFontSize: Integer 1–200 inclusive unchanged; otherwise20" for display only, preserve raw missing/wrong-type/out-of-range and valid non-choice values without clamping or automatic writes, keep the eight UI choices/reset20 unchanged, expose the effective current size without coercing a choice, and add no whole-file repair, stored effectiveFontSize key or durability claims.
- [X] T019 [US1] Implement `app/src/main/kotlin/com/goodtrendltd/HolySongs/ui/SongListViewModel.kt` against T013, owning "Loading/catalog/error" state "Retained across configuration; reloaded after process recreation", initiating one main-safe T016 load, exposing explicit retry only, and preserving cancellation/obsolete-owner behavior; save no catalog Bundle and never reload in composable bodies, restarting subscriptions, theme changes or normal resume.
- [X] T020 [US1] Implement `app/src/main/kotlin/com/goodtrendltd/HolySongs/ui/LetterSidebar.kt` against T014/T017 with A–Z drawing/hit testing from measured available content and reserved width, following U2-RESIZE exactly: fit mode only when all26 measured rows fit without compression, with tap/drag selecting and no rail scrolling; otherwise compact mode with tap-on-release selection within platform touch slop and vertical drag past slop scrolling only the rail, never the song list or an extra release selection. Cancel pending input/highlight on pointer cancellation, stop or size/mode change without undoing committed fit-mode selections; clamp coordinates, dispatch only valid destinations, test both modes via T014 and retain the custom-sidebar TalkBack deferral.
- [X] T021 [US1] Implement `app/src/main/kotlin/com/goodtrendltd/HolySongs/ui/SongListScreen.kt` against T014/T015 with Loading/Ready/empty/error/retry UI, lazy effective-title keys, single Scaffold inset boundary applied/consumed for list and sidebar, no double safe-area padding, and "Lazy first visible index/offset" as "Saved UI state, restored only when catalog is Ready"; preserve "Transient; 2-second hide, cancelled/dismissed on stop/disposal" indicator behavior in composition instead of WindowManager, apply stored theme, expose labels/roles and user-event callbacks without replayed effects.
- [X] T022 [US1] Replace `src/com/goodtrendltd/HolySongs/MainActivity.java` with `app/src/main/kotlin/com/goodtrendltd/HolySongs/MainActivity.kt` and update only its theme in `AndroidManifest.xml`, keeping exported launcher identity and Java-callable constants "com.goodtrendltd.SONG_NAME"/"com.goodtrendltd.LYRIC"; wire ComponentActivity/enableEdgeToEdge/T018/T019/T021, preserve exact selected-title/lyric String extras to legacy DisplayLyric, Settings/About entry points and original app-share subject/text/link whitespace with safe missing-recipient handling, use "Title is small reconstruction state if needed; guard is transient, reset on return", never compile duplicate same-name classes, and keep "Cold launch into a fresh task still starts at the song list".
- [X] T023 [US1] Make the smallest required pilot compatibility correction in `src/com/goodtrendltd/HolySongs/SettingsActivity.java`: after T018/T022 observation is active, remove the FLAG_ACTIVITY_CLEAR_TOP/new-Main theme-return workaround and preserve ordinary system Back to the existing Main with list state intact; retain existing preference writes, types/defaults, Holo UI and reset behavior, run the T014 legacy-settings-return regression and do not perform a temporary full Java-to-Kotlin Settings conversion before T031.
- [ ] T024 [US1] First transfer all still-needed capture assertions to the production tests and retire `app/src/androidTest/kotlin/com/goodtrendltd/HolySongs/LegacyBaselineTest.kt` without deleting frozen fixtures, then rebuild and execute T011–T015 plus clean-source CLI/IDE/install checks for the integrated pilot, recording results in `specs/002-kotlin-compose-ui/evidence/us1-list.md` and `specs/002-kotlin-compose-ui/evidence/build.md`; compare real production loading/order/all duplicate winners/A–Z outputs and overlap against T010 on both endpoints, verify Main-only U2/U4 recreation/list state/reflow/labels/fonts/insets/resize plus the exact retained-reader/settings/about/share/video smoke and legacy preference-to-Main synchronization in the checkpoint applicability matrix, recording pre-existing non-safety later-screen restoration deficiencies for T030–T033 rather than requiring their full fix now or marking them passed; require zero app crashes/ANRs and no migration-introduced regression with timing/scroll observations, and leave the gate unchecked if any required check or encountered video safety issue blocks it (use the early-safety path below, not a waiver).

**Checkpoint**: US1 is the developer/user-visible MVP. The first PR may contain enablement and
this pilot in separately reviewable changes, but only after this gate; this checklist authorizes
no publication. US1 is not completion of the Compose reader/settings/about or video audit.

## Phase 4: User Story 2 — Read Lyrics and Keep Existing Settings (Priority: P1)

**Goal**: Replace lyric/settings/about UI without data, preference, sharing or state regressions.

**Independent Test**: On the built artifact, execute U3/U4 and N1/N2 on API23/API37 using fresh
defaults and legacy fixtures, all font sizes/themes, actual lyric output, share recipients and
state restoration. No working third-party site is needed for the offline story, but safe video
entry/return remains an affected-flow requirement.

### Tests for User Story 2

- [ ] T025 [P] [US2] Add `app/src/androidTest/kotlin/com/goodtrendltd/HolySongs/LyricScreenTest.kt` for U3-LYRIC/U3-INVALID, N1-LYRIC/N1-VIDEO/N2-LYRIC/N2-NONE using real DisplayLyricActivity, String "com.goodtrendltd.SONG_NAME"/"com.goodtrendltd.LYRIC" reconstruction, "ACTION_SEND, text/plain, EXTRA_SUBJECT title, EXTRA_TEXT effective lyric", all duplicate winners, Chinese/line-break/long-text fixtures, title-keyed scroll and font reflow, plus rendered effectiveFontSize for every data-model font-policy fixture with raw storage unchanged; reject missing/wrong-type/invalid extras without arbitrary fallback songs or external actions, and verify Java VideoSearch still reads the exact search-target constant.
- [ ] T026 [P] [US2] Add `app/src/androidTest/kotlin/com/goodtrendltd/HolySongs/SettingsScreenTest.kt` for U3-PREFS/U3-SYNC/U3-INVALID/U2-LABELS against T012 fixtures, asserting `appPrefFile`, Integer/default20 fontSize with "16, 18, 20, 22, 24, 26, 28, 30; reset writes 20", Boolean/defaulttrue nightMode with "true = dark, false = light", reset-only-font/no unsolicited writes, semantic control labels/states, immediate cross-screen updates, normal relaunch persistence and no reset/migration of legacy data; test the data-model font-policy table in the migrated UI, showing effectiveFontSize without automatically selecting/writing a menu choice, with only explicit size/reset actions persisting allowed values.
- [ ] T027 [P] [US2] Add `app/src/androidTest/kotlin/com/goodtrendltd/HolySongs/AboutScreenTest.kt` for U3-ABOUT/N1-ABOUT/U2, preserving baseline full about text, website/email link destinations and safe return/launch-failure behavior with readable large-font/inset layouts and standard control labels; use real AboutActivity and do not add new destinations or public deep links.
- [ ] T028 [P] [US2] Add `app/src/androidTest/kotlin/com/goodtrendltd/HolySongs/ReaderLifecycleTest.kt` for U4 across list/lyric/settings/about: rotation/recreation, theme/background return, rapid taps/Back, saved selection and "Scroll offset keyed by selected title" with "Saved across recreation/return; clamp on reflow, reset for a genuinely new title", no stack-clearing restart and no saved/replayed navigation/share commands; define genuine background process-recreation steps separately because Activity recreation is not process-death proof.
- [ ] T029 [P] [US2] Define US2 U2/U3/U4/N1/N2 runtime cases and unexecuted outcomes in `specs/002-kotlin-compose-ui/evidence/us2-reader.md`, including all eight sizes/reset/both themes/legacy fixtures/corrupt-type safety, all duplicate winner rendering and long lyrics, actual shares with and without recipients, about links, API23/API37 lifecycle and API37 gesture/three-button/cutout/large-window/font cases, process recreation from Recents, timing/scroll and zero-crash/ANR observations; preserve the FR-016 TalkBack/sidebar deferral and identify per-screen slice checks before proceeding to the next replacement.

### Implementation for User Story 2

- [ ] T030 [US2] Replace `src/com/goodtrendltd/HolySongs/DisplayLyricActivity.java` with `app/src/main/kotlin/com/goodtrendltd/HolySongs/DisplayLyricActivity.kt`, add `app/src/main/kotlin/com/goodtrendltd/HolySongs/ui/LyricScreen.kt` and update only its theme in `AndroidManifest.xml`; implement T025/T028 contract-preserving ComponentActivity/Scaffold/insets, validated original String extras and Java-callable "com.goodtrendltd.searchTarget", title-keyed saved scroll, effective lyric formatting/effectiveFontSize in sp using the data-model display-only1–200/otherwise20 policy without storage rewrites, reactive stored theme and exact safe share/video callbacks; replace nullable Wi-Fi access with null-safe API23 capabilities and preserve mobile-data confirmation for "youtube", "youku", or "tudou", obey "Existing Intent remains reconstruction input; no additional full-catalog Bundle" and never save duplicate lyric Bundles, and run its host/device slice checks from T029 before the next screen replacement.
- [ ] T031 [US2] Replace `src/com/goodtrendltd/HolySongs/SettingsActivity.java` with `app/src/main/kotlin/com/goodtrendltd/HolySongs/SettingsActivity.kt`, add `app/src/main/kotlin/com/goodtrendltd/HolySongs/ui/SettingsScreen.kt` and update only its theme in `AndroidManifest.xml`; use T018 observation/writes and T026/T028 tests with "Reload/observe existing storage; no new persistent destination", retain exact sizes/default20/reset20/Boolean theme meanings without changing storage, expose labels/roles/selection state, apply inset/large-text handling and ordinary Back with no CLEAR_TOP/new-Main workaround, then execute its T029 slice checks before replacing About.
- [ ] T032 [US2] Replace `src/com/goodtrendltd/HolySongs/AboutActivity.java` with `app/src/main/kotlin/com/goodtrendltd/HolySongs/AboutActivity.kt`, add `app/src/main/kotlin/com/goodtrendltd/HolySongs/ui/AboutScreen.kt` and update only its theme in `AndroidManifest.xml`; preserve full baseline content and clickable website/email behavior, use T007/T018 theme observation, ComponentActivity/Scaffold/insets and readable scrolling/standard labels, provide safe link-launch failure and normal Back, and run T027/T028 and its T029 slice checks without changing activity/export identities.
- [ ] T033 [US2] Rebuild and execute T025–T029 plus retained US1 regressions on API23/API37, recording final reader-slice results in `specs/002-kotlin-compose-ui/evidence/us2-reader.md`; verify actual all-winner lyrics/space removal, every font/reset/theme and preseeded preference value, both shares/recipient absence/about links, real background process recreation distinct from Activity recreation, valid scroll after reflow, rapid Back/no repeated effects and all specified inset/window/font/label cases, plus video entry/offline-return smoke and per-endpoint timing/scroll/crash-ANR observations; missing core evidence blocks this gate, not just full-feature acceptance.

**Checkpoint**: Core reader screens now use Compose and are independently testable offline.
Java helpers/WebView may remain; there is no all-Kotlin requirement.

## Phase 5: User Story 3 — Retain Safe Video Integration and Verification (Priority: P2)

**Goal**: Preserve three provider entry points, safe Java WebView ownership/Back/error behavior,
and explicit audit/approval evidence without making reader reliability depend on a working site.

**Independent Test**: Exercise N3/N4 from the reader on both endpoints: query/provider audit,
network absence/mobile-data confirmation, cancellation/errors/fullscreen/history/Back/resume/resize
and safe return to the selected offline lyric. Document approved provider limitations separately
from non-waivable application safety. See the early-safety dependency exception below.

### Tests for User Story 3

- [ ] T034 [P] [US3] Add `app/src/androidTest/kotlin/com/goodtrendltd/HolySongs/VideoSearchTest.kt` and test-only deterministic page/error fixtures in `app/src/androidTest/assets/video-fixtures/` for N1-VIDEO/N1-INVALID/N3/N4: validate "youtube", "youku", or "tudou" and nonempty title/String extras, pause/resume/save/detach/destroy ordering, "Save/restore while WebView is live; no destroyed instance reuse", late callbacks, main-frame failures/cancel and foreground-loading→fullscreen→history→finish Back; include geolocation denial/normal SSL rejection and network-confirmation cases using test boundaries without changing production transport policy, and distinguish fixture tests from actual provider availability.
- [ ] T035 [P] [US3] Audit all three baseline prefixes "http://m.youtube.com/results?q=", "http://www.soku.com/m/y/video?q=" and "http://www.soku.com/m/t/video?q=" into `specs/002-kotlin-compose-ui/evidence/providers.md` using `res/values/strings.xml` and an attributable current APK on API23/API37, recording Chinese query, network/WebView versions, baseline/redirect/final URL and actual page/search/playback limitations; evaluate same-provider HTTPS equivalents without editing resources, distinguish blocked/untested/broken results, stop unsafe probes, and obtain owner/date/scope approval before any replacement/removal/external-browser change or accepted unavailability.
- [ ] T036 [P] [US3] Define N3/N4 runtime scenarios and expected/unexecuted results in `specs/002-kotlin-compose-ui/evidence/us3-video.md`, including no-network/Wi-Fi-absent/mobile confirmation, invalid/missing/wrong-type extras, loading cancellation/main-frame errors/obsolete callbacks, fullscreen plus loading coexistence, history/root Back, temporary absence/media behavior, live-state save/restore/final teardown and API37 gestures/three-button/resize/rotation; require zero app crashes/ANRs and immediate safe offline return, with provider approvals distinct from test passes.

### Implementation for User Story 3

- [ ] T037 [US3] Correct `src/com/goodtrendltd/HolySongs/VideoSearch.java` against T034/T036, retaining its Java language/View UI and component identity while adopting ComponentActivity/OnBackPressedDispatcher as needed; validate exact provider/title extras, pause/resume temporarily instead of destroy-in-onPause, save/restore only live state, detach/destroy once at final teardown, and implement "Cancel foreground loading; otherwise exit fullscreen; otherwise navigate WebView history; otherwise finish to the existing lyric" with interception enabled only when needed; review `AndroidManifest.xml` portrait/window behavior for N4-RESIZE without adding exports or permissions beyond build contract B2's already approved allowlist, or relying on portrait lock.
- [ ] T038 [US3] Correct `src/com/goodtrendltd/HolySongs/HTML5WebView.java` against T034/T036 in coordination with T037: scope loading and fullscreen callbacks to live ownership/current navigation, make cancellation stop navigation and main-frame failures settle loading, null-check/dismiss on stop/teardown, reject late events, release custom-view callbacks once, deny unsolicited geolocation and preserve normal SSL rejection; remove/replace demonstrated unsafe deprecated calls without global pauseTimers, TLS/cleartext/mixed-content bypasses or a Kotlin rewrite, and run deterministic ownership/error/Back regressions after both Java files compile together.
- [ ] T039 [US3] Complete N3-NETWORK/N1-INVALID integration in `app/src/main/kotlin/com/goodtrendltd/HolySongs/DisplayLyricActivity.kt` using T034/T036, verifying the T030 null-safe active-network/capabilities implementation, exact target/title payloads and mobile-data confirmation with no video-specific permission additions beyond build contract B2's allowlist; correct only concrete remaining failures and rerun affected reader tests. If invoked on the early-safety path before T030, make only the necessary safety correction in `src/com/goodtrendltd/HolySongs/DisplayLyricActivity.java`, record it in `specs/002-kotlin-compose-ui/evidence/us3-video.md`, and carry that regression/behavior into T030 rather than doing a temporary full conversion.
- [ ] T040 [US3] After T035 audit decisions, apply only verified same-provider HTTPS equivalents in `res/values/strings.xml` and UTF-8 query encoding in `src/com/goodtrendltd/HolySongs/VideoSearch.java`; preserve target identities/search intent, record exact corrections or approved retained limitations in `specs/002-kotlin-compose-ui/evidence/providers.md`, and stop for explicit owner approval before replacing/removing a provider or launching an external browser; no-change completion requires documented audit/approval evidence, not an empty diff.
- [ ] T041 [US3] Build the integrated reader/video artifact and run T034–T036/N3/N4 on API23/API37, recording outcomes in `specs/002-kotlin-compose-ui/evidence/us3-video.md` and `specs/002-kotlin-compose-ui/evidence/providers.md`; include actual provider attempts, error/cancel/fullscreen/history/Back/media/pause/resume/live-save/final-teardown/resize cases, API37 gesture/three-button/large-window handling, regression return to the selected offline lyric/scroll state and app crash/ANR checks; approved provider unavailability cannot excuse unsafe behavior, trapped loading or failed offline return, and missing required evidence leaves the task unchecked.

**Checkpoint**: Video integration and its limitations are attributable and safe. Final integrated
acceptance, cleanup and documentation remain; no release or publishing is authorized.

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Retire only verified obsolete UI, reconcile documentation and validate the final artifact.

- [ ] T042 After story checks pass, review references to `src/com/goodtrendltd/HolySongs/SongTitleAdapter.java`, `src/com/goodtrendltd/HolySongs/Sidebar.java`, `res/layout/main.xml`, `res/layout/song_item.xml`, `res/layout/list_position.xml`, `res/layout/lyric_view.xml`, `res/layout/settings_view.xml` and obsolete menu/style resources; remove only genuinely unused legacy reader UI and record exact paths/reasons in `specs/002-kotlin-compose-ui/evidence/review.md`, preserving Java helpers, video resources, required strings/assets and identity; explicitly review the owner's modified main.xml before any removal rather than discarding it silently, and rebuild/rerun affected tests after cleanup.
- [ ] T043 Reconcile `README.md` and `specs/002-kotlin-compose-ui/quickstart.md` with actually implemented and exercised mixed-language/Compose setup, dependency/lock/source-root/test commands, artifact locations, API23/API37 matrix and loss of update eligibility for API14–22; retain project background/local setup/license-review cautions, mark blocked/unexecuted checks accurately, document full TalkBack/custom-sidebar deferral and provider limitations, and do not rewrite feature001 historical results as new passes.
- [ ] T044 Review the complete diff against `AGENTS.md`, `.specify/memory/constitution.md`, `specs/002-kotlin-compose-ui/plan.md` and `specs/002-kotlin-compose-ui/data-model.md`, recording file-specific scope/security/dependency/signing findings in `specs/002-kotlin-compose-ui/evidence/review.md`; update `README.md` future-update guidance for identity/version progression/compatible signing, recheck asset/JAR hashes/provenance and no tracked secrets/local/generated output or broad lint/network bypasses, preserve unresolved JAR-license/production-upgrade limits, and fix only concrete in-scope findings with affected tests rerun.
- [ ] T045 Execute the full `specs/002-kotlin-compose-ui/quickstart.md` on a clean exact implementation revision and the final integrated artifact, recording B1–B4/U1–U4/N1–N4 results in `specs/002-kotlin-compose-ui/evidence/verification.md`; repeat CLI/IDE/locks/metadata/JVM/instrumented/lint/signature/data checks and API23/API37 offline catalog/order/all-winner/letter/preferences/shares/about/state/insets/font/labels/video gates, all API37 navigation/large-window cases, and per-endpoint launch-to-Ready timing/scroll/crash-ANR observations; record separate revision/APK/environment evidence, never mix unrelated artifacts into an all-pass claim, and leave unchecked if any required core-flow/approval/environment gate is blocked.
- [ ] T046 Have the master review all FR-001–FR-016 and SC-001–SC-007 coverage, checkpoints and remaining risks in `specs/002-kotlin-compose-ui/evidence/verification.md` and `specs/002-kotlin-compose-ui/evidence/review.md`, then reconcile `specs/002-kotlin-compose-ui/tasks.md` checkboxes and successor status in `specs/002-kotlin-compose-ui/spec.md`/`specs/002-kotlin-compose-ui/transition.md` only from actual evidence; preserve prior failures and feature001's incomplete history, report exact blockers instead of feature completion when gates fail, and perform no release signing/publication or unrequested commits.

## Checkpoint Scope

Use [contracts/ui.md's checkpoint applicability matrix](contracts/ui.md#checkpoint-applicability)
for T009/T024/T033/T041/T045. US1 fully verifies Main/list/sidebar plus defined retained-screen
safety/interoperability and legacy preference-to-Main updates; full lyric/settings/about restoration,
reflow and new display-only font-fallback rendering are US2 gates. The preference adapter itself
is verified in US1. Later-scope checks remain unexecuted, not silently passed or pulled into the
MVP merely by a broad U2/U4 family reference. Any migration-introduced regression or concrete
safety failure blocks the current checkpoint; the early-video-safety path remains available.

## Dependencies & Execution Order

### Phase and Story Graph

```text
T001 → (T002, T003)
  → (T004, T005) → T006 → T007 → T008 → T009 → T010
  → US1 preparation T011–T015 → implementation T016–T023 → T024 (MVP gate)
  → US2 preparation T025–T029 → T030 → T031 → T032 → T033
  → US3 preparation T034–T036 → T037 → T038 → T039 → T040 → T041
  → T042 → T043 → T044 → T045 → T046
```

This is the default order. Runtime targets and ownership must exist before their tasks run;
missing devices stop the corresponding gate rather than substituting a different API. Source-only
checks cannot fabricate T010's baseline fixture outcomes. Test definitions precede changes; run
focused tests once the implementation exists and record genuine before/after evidence.

### Early Video Safety Exception (No Circular Dependency)

If T024 or T033 encounters a concrete retained-video crash/security/offline-return defect, keep
that story gate unchecked. After T010 and pilot wiring T022 (T023 for Settings-return tests),
T034/T035/T036 may be prepared and T037–T040 performed against the still-legacy or migrated
reader via unchanged String interfaces. These tasks do **not** require T024/T033 to pass first.
T039 explicitly identifies the Java caller path if T030 has not happened. Audit/approval gates
still apply; provider work cannot silently change UX or transport security.

After the bounded correction, rerun the blocked pilot/reader gate. Full US3 gate T041 requires
T033 plus all video tasks and rechecks the final Kotlin-reader/Java-video combination. Re-run
T034's affected tests after T030 even if earlier safety work passed. If safe correction needs
unapproved scope or unavailable approval/evidence, stop the checkpoint and report the blocker.
This exception implements plan.md's explicit early-safety rule; it is not permission for a new
parallel writer or for bypassing tests/owner decisions.

### Task-Level Barriers

| Ready task/batch | Prerequisites and ownership |
|---|---|
| T002, T003 | T001; separate documentation paths; T003 owns device inventory |
| T004, T005 | T002/T003; independent existing Java test files/fixtures |
| T006 → T007 → T008 → T009 → T010 | Serial shared build/runtime baseline chain; T009 verifies toolchain before T010 executes capture |
| T011, T012, T013, T014, T015 | T010; independent test/spec files; tests reference planned production APIs but do not implement them |
| T016 → T017 | T011/T010; loader/model first, legacy lookup then integrated assertions |
| T018 | T012; serialized after T017 by default, no external writer in the same worktree |
| T019 | T013/T016/T017; state owner consumes real catalog |
| T020 | T014/T017; callback-based sidebar before Main wiring |
| T021 | T007/T014/T015/T018/T019/T020; screen state/inset integration |
| T022 → T023 → T024 | All US1 components/tests; serialized manifest/Activity edits then complete pilot gate |
| T025, T026, T027, T028, T029 | T024 by default; separate test/spec files, not simultaneous device execution |
| T030 → T031 → T032 → T033 | US2 tests defined; each replacement runs its slice checks before next shared manifest edit |
| T034, T035, T036 | T033 by default, or T010/T022 under early-safety rule; separate files but exclusive device ownership for audit |
| T037 → T038 → T039 | T034/T036; Java ownership/callback corrections serialized, then caller integration |
| T040 | T035 audit/approvals and T037–T039; serialize VideoSearch/resources |
| T041 | T033 plus T034–T040, even when early safety work ran against legacy reader |
| T042 → T043 → T044 → T045 → T046 | All story gates; shared cleanup/docs/review/final artifact validation serialized |

## Parallel Opportunities and Examples

`[P]` is a ready-batch capability, not an unconditional launch instruction. Concurrent writers
must have isolated worktrees and master integration; in the current shared checkout, serialize
writes. Separate device-using tasks also require separate dedicated targets. Builds/tests consume
integrated code after writers join. Do not mark related implementation tasks parallel merely
because filenames differ when they depend on incomplete production types or interfaces.

### Setup/Foundation

After T001, T002 evidence rules and T003 device inventory are independent. After setup, T004
catalog/input tests and T005 helper tests can be prepared separately, then join before T006.

### User Story 1

After T010, independent preparation is T011 loader/index tests, T012 preference tests, T013
ViewModel tests, T014 list/integration tests and T015 runtime scenario definitions. Example:
T011 owns its two JVM test paths while T012 owns only ReaderPreferencesTest.kt; neither changes
app/build.gradle or shared production files. Join before the serialized implementation chain.

### User Story 2

After T024, T025 lyric tests, T026 settings tests, T027 about tests, T028 lifecycle tests and
T029 scenario definitions have distinct files. Example: prepare LyricScreenTest.kt and
AboutScreenTest.kt in isolation, integrate, then perform the Activity/manifest replacements
serially. Parallel test authoring does not mean independent runtime access or concurrent manifest edits.

### User Story 3

At its ready barrier, T034 deterministic tests, T035 provider audit and T036 scenario definitions
have distinct outputs. The audit owns live devices/network probes; test authoring performs no
simultaneous device mutation. Join before T037/T038's coupled Java WebView ownership changes.

## Implementation Strategy

### MVP First

1. Finish T001–T010 with real build/legacy-baseline evidence.
2. Prepare US1 regressions and implement T016–T023 without converting the legacy reader first.
3. Execute T024; stop at this reviewable Compose list/sidebar/insets checkpoint.
4. If retained video safety blocks it, use only the bounded early-safety path and retest.
5. Report checkpoint evidence and unfinished stories; do not publish or call the entire feature done.

### Incremental Delivery

- US1 establishes the working mixed-language/Compose list and retained-screen boundaries.
- US2 replaces lyric/settings/about one at a time while reusing helpers and original storage.
- US3 completes retained Java video safety/provider audit and final-reader interop evidence.
- Final phase removes only proven-unused legacy UI and replays all requirements on the final artifact.

Keep conversion/structural changes separable from behavior corrections where practical. A small
required legacy safety fix is preferable to blocking a necessary fix behind a full rewrite, but
there is no mandate to generate large new Java UI code and convert it immediately afterward.
Model routing/delegation follows `AGENTS.md`; the master owns decisions, review and acceptance.

## Requirement and Contract Coverage

| Requirement | Primary tasks | Contract / evidence |
|---|---|---|
| FR-001 | T001, T006–T009, T024, T045 | B1 clean CLI/IDE/mixed build |
| FR-002 | T003, T006, T009, T043, T045 | API23 floor/metadata/device matrix |
| FR-003 | T007, T022–T024, T030–T033, T046 | Incremental checkpoints, no wholesale conversion |
| FR-004 | T020–T022, T030–T032, T042 | Compose core UI and verified legacy retirement |
| FR-005 | T004–T005, T009–T011, T016–T017, T022, T030, T044–T045 | B2, catalog/identity/constants |
| FR-006 | T010–T011, T013, T015–T017, T019–T024, T033, T045 | U1, timing/scroll/crash-ANR |
| FR-007 | T010, T014–T015, T020–T024, T025–T033, T037, T041, T045 | U2 overlap/insets/fonts/resize |
| FR-008 | T012, T018, T023, T026, T029–T031, T033 | U3 preference compatibility/theme |
| FR-009 | T008, T010, T014, T022, T025, T027, T029–T033, T034, T039–T041 | N1/N2, about and video payloads |
| FR-010 | T013–T014, T018–T024, T025–T033, T034, T036–T041 | U4/N4 state/callback ownership |
| FR-011 | T006, T009, T030, T034–T041, T045 | B2 permission allowlist; N3/N4 safety, audit and approvals |
| FR-012 | T004–T005, T008, T010–T015, T024–T029, T033–T036, T041, T045 | Real production/JVM/device regressions |
| FR-013 | T002–T003, T009–T010, T015, T024, T029, T033, T035–T036, T041, T045–T046 | Artifact-linked full runtime matrix |
| FR-014 | T001–T002, T009, T043–T046 | Setup/support/locks/signing/limitations docs |
| FR-015 | T016–T019, T022, T030–T032, T037–T038, T042, T044 | Simple extraction, no framework/store rewrite |
| FR-016 | T007, T014–T015, T020–T022, T025–T029, T031–T033, T043, T045 | U2-LABELS/U2-FONT, explicit deferral |
| SC-001 | T009, T024, T045 | Clean CLI/IDE/tests/install both endpoints |
| SC-002 | T010–T011, T014–T024, T025–T033, T045 | Core Compose/offline/parity scenarios |
| SC-003 | T010, T014–T015, T020–T024, T025–T033, T045 | No obscured essential UI; standard labels |
| SC-004 | T012, T018, T026, T029–T031, T033, T045 | All sizes/reset/themes/legacy fixtures |
| SC-005 | T013–T015, T019–T024, T028–T033, T034, T036–T041, T045 | Lifecycle/network, zero crash/ANR/trapped loading |
| SC-006 | T035, T040–T041, T043, T045–T046 | Every provider audit and approval |
| SC-007 | T002, T009–T010, T024, T033, T041, T044–T046 | Attributable evidence, honest status reconciliation |

All 16 functional requirements and 7 buildable success criteria have task mappings. Mapping is
planned coverage, not proof that any acceptance criterion has passed. Run `/speckit.analyze`
before `/speckit.implement`; resolve substantive findings rather than treating this table as approval.
