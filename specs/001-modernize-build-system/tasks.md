---
description: "Executable tasks for the HolySongs build-system modernization"
---

# Tasks: Modernize HolySongs Build System

**Input**: Design documents from `specs/001-modernize-build-system/`

**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md),
[data-model.md](data-model.md), [contracts/compatibility.md](contracts/compatibility.md),
[quickstart.md](quickstart.md), and `.specify/memory/constitution.md`.

**Tests**: The specification explicitly requires preservation and runtime validation; the plan
selects local JUnit characterization tests, lint, APK inspection and manual device checks.
Test/scenario tasks precede relevant changes. Characterization tests may pass on legacy behavior;
only defect regression tests need an observed pre-fix failure. An unavailable harness/device
is a blocker, not a failing test or proof of success.

**Organization**: Three user stories, in specification priority order: US1 (P1), US2 (P1),
US3 (P2). No new domain model, database, server or UI framework is introduced.
All tasks are initially unchecked; task generation does not execute them.

## Format: `[ID] [P?] [Story] Description`

- `[P]` identifies independent tasks in the same ready batch, touching different files.
- `[US1]`, `[US2]`, `[US3]` identify story work; common tasks have no story label.
- Complete prerequisites before launching a batch. See the dependency table for explicit barriers.
- One writer per working tree: parallel-writing examples require separate worktrees and parent
  integration. In this checkout, serialize writers even for tasks marked `[P]`.
- Leave blocked acceptance tasks unchecked and record their blockers. A completed environment
  inventory can report missing devices; it does not complete their downstream runtime tests.

## Path Conventions

- All task paths are repository-relative to `/home/hanli3/GIT/holysongs-android`.
- Existing Java remains under `src/com/goodtrendltd/HolySongs/`; resources, assets, library and
  manifest retain their current root locations.
- New build module: `app/build.gradle`; local tests: `app/src/test/java/`.
- Sanitized implementation evidence: `specs/001-modernize-build-system/evidence/`.
  Evidence files below are future deliverables, not reports of already executed work.
- Raw APKs, screenshots, logs, SDK paths and keys stay outside tracked source. Do not copy
  credentials into evidence. Parent owns task status and acceptance.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Establish safe implementation boundaries and the development/test environment.

- [X] T001 Capture the current branch/revision, staged/unstaged/untracked state and available tool versions in `specs/001-modernize-build-system/evidence/environment.md`; preserve existing user files and record a recoverable checkpoint before implementation, without committing or cleaning unrelated changes.
- [ ] T002 Provision or configure JDK 17, Android Studio Quail 4 (2026.1.4), SDK command-line/platform tools, platform 37 and Build Tools 36.0.0 using local environment configuration; document verified commands and missing access in `specs/001-modernize-build-system/evidence/environment.md` and obtain owner acceptance of SDK licenses rather than accepting them silently; do not install a global Gradle or commit local SDK paths.
- [X] T003 Inventory dedicated API 14 and API 37 test devices/emulators, an API 37 large-screen configuration, navigation modes and share-recipient availability in `specs/001-modernize-build-system/evidence/devices.md`; record access blockers and safe device selection, never substituting API 15/23 for API 14 or uninstalling a production app.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Capture immutable preservation and evidence contracts before source changes.

**Critical**: Complete this phase before user-story implementation. Device availability may be
recorded as blocked, but build provisioning must be sufficient for tasks that invoke tools.

- [X] T004 [P] Capture the original `assets/songs.xml`, `libs/pinyin4j-2.5.0.jar`, `src/com/goodtrendltd/HolySongs/MainActivity.java` and `res/values/strings.xml` baseline in `specs/001-modernize-build-system/evidence/baseline.md`: verify research hashes, record "Preserve original order and all 422 entries", "Preserve bytes; no normalization or new IDs", "Preserve bytes and representative rendered output", and "414 distinct keys; used by the current title-to-lyric map"; record all eight duplicate titles, last-entry-wins lyrics, ASCII-space removal and representative Chinese/long-lyric expectations without editing shipped data.
- [X] T005 [P] Define the evidence record and requirement/scenario naming rules in `specs/001-modernize-build-system/evidence/README.md` using the data-model fields verbatim: "requirement/scenario ID; date; source revision; APK SHA-256; toolchain; device/emulator identifier; Android API/version; WebView version where applicable; prerequisites; steps; expected result; observed result; status (`passed`, `failed`, `blocked`); evidence path; blocker/limitation; maintainer approval reference for accepted provider changes or limitations"; enforce "Unexecuted checks have no success result" and retain dated prior failures when retesting.
- [X] T006 [P] Record pinyin4j 2.5.0 provenance, observed license metadata and distribution risks in `specs/001-modernize-build-system/evidence/dependencies.md`, inspecting `libs/pinyin4j-2.5.0.jar` without modifying it; retain checksum `6576dea7d351a0f5df1595b9c432ba7cf9246ca0ab6f7019b9ca4e6d500b0e68` and do not claim license clearance when evidence is unavailable.

**Checkpoint**: Baseline and evidence rules are recorded; unchanged data/JAR inputs are protected.

---

## Phase 3: User Story 1 - Build From a Clean Checkout (Priority: P1) — MVP

**Goal**: Developers can use the pinned wrapper and stable IDE to build, install and launch
a debug APK without Ant/ADT, committed machine configuration or generated application inputs.

**Independent Test**: From a clean implementation checkout, use only README prerequisites and
wrapper commands, complete IDE sync/build, inspect the APK and install/launch it on an available
supported dedicated target. This is a developer MVP, not final both-endpoint product sign-off.

### Tests for User Story 1

- [X] T007 [P] [US1] Define executable C1/C2 build acceptance steps and initial unexecuted results in `specs/001-modernize-build-system/evidence/us1-build.md`, covering CLI and IDE entry points, missing prerequisites, regenerated outputs, APK identity/minimum/target/version, API 14-compatible debug signing and install/launch; reference `specs/001-modernize-build-system/quickstart.md` and distinguish environment blockers from test failures.
- [X] T008 [P] [US1] Add JUnit 4 input-preservation tests in `app/src/test/java/com/goodtrendltd/HolySongs/BuildInputsTest.java` that read repository inputs via the explicit test root configured in T011, assert the baseline song and JAR SHA-256 values from `specs/001-modernize-build-system/research.md`, and check the preserved preference resource identifiers; do not edit assets or assert a fabricated successful APK build.

### Implementation for User Story 1

- [X] T009 [P] [US1] Generate the Gradle 9.6.0 wrapper at `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar` and `gradle/wrapper/gradle-wrapper.properties` using a verified official distribution in a temporary bootstrap location; verify wrapper/distribution provenance and set the official distribution SHA-256, executable bit and pinned HTTPS URL without adding a global Gradle prerequisite.
- [X] T010 [P] [US1] Add `settings.gradle`, root `build.gradle` and `gradle.properties` for one `:app` module, pin AGP 9.4.0, use Google Maven/Maven Central and portable settings, and disable unnecessary built-in Kotlin through the supported AGP configuration; do not enable legacy DSL, force final resource IDs, add dynamic versions or introduce Kotlin/AndroidX UI plugins.
- [X] T011 [US1] Configure `app/build.gradle` with JDK 17/Java 8 source-target compatibility, compileSdk/targetSdk 37, minSdk 14, Build Tools 36.0.0, applicationId/namespace "com.goodtrendltd.HolySongs" and version "8 / 2.5 for this debug migration"; map `../AndroidManifest.xml`, `../src`, `../res`, `../assets`, local `../libs/pinyin4j-2.5.0.jar`, standard test sources and JUnit 4.13.2, set a portable repository-root test property for T008, and exclude `../gen` with no debug applicationId suffix or release signing configuration.
- [X] T012 [US1] Modernize `AndroidManifest.xml` to take namespace/SDK/version metadata from `app/build.gradle`, declare launcher exported and internal Activities non-exported, preserve launcher "com.goodtrendltd.HolySongs.MainActivity" and other component identities, and retain only existing INTERNET/ACCESS_NETWORK_STATE permissions; do not add cleartext bypasses or new deep links.
- [X] T013 [US1] Replace `case R.id.*` menu dispatch with equivalent comparisons in `src/com/goodtrendltd/HolySongs/MainActivity.java` and `src/com/goodtrendltd/HolySongs/DisplayLyricActivity.java`, preserving every menu action and return value so compilation no longer depends on final generated resource IDs.
- [X] T014 [US1] Run `./gradlew --version` and `./gradlew clean :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`, recording results and narrowly scoped compile/startup corrections in `specs/001-modernize-build-system/evidence/us1-build.md`; inspect legacy calls in `src/com/goodtrendltd/HolySongs/HTML5WebView.java` and the launcher in `src/com/goodtrendltd/HolySongs/MainActivity.java`, change only demonstrated blockers, and document reviewed pre-existing lint exceptions without blanket suppression or treating deprecation as API removal.
- [X] T015 [US1] After the modern build checkpoint, preserve existing entries while updating `.gitignore` for local SDK configuration, `.gradle/`, module build outputs and signing secrets; retire tracked `gen/`, `build.xml`, `ant.properties`, `project.properties` and unused `proguard-project.txt`, and untrack `local.properties` without deleting the developer's local file; keep wrapper and pinyin JAR inputs trackable.
- [X] T016 [US1] Update `README.md` with tested JDK/SDK/IDE prerequisites, license-review step, wrapper commands, debug APK location, dedicated-device install/launch instructions and troubleshooting for missing tools/downloads; preserve project background and require no undocumented tracked-file edits, global Gradle or Ant environment.
- [ ] T017 [US1] Execute T007 from a clean Git checkout of the implementation revision, with no copied untracked/ignored configuration or generated outputs (create local setup only through the documented prerequisites), and record provenance in `specs/001-modernize-build-system/evidence/us1-build.md`: verify Gradle 9.6.0/JDK 17, regenerate outputs, pass tests/lint, inspect `app/build/outputs/apk/debug/app-debug.apk` identity and v1-compatible signature, separately sync/build with Android Studio, and install/launch on a supported dedicated target; leave the task unchecked if any required check is blocked.

**Checkpoint**: US1 delivers a verified developer build/install entry point. An assembled APK
alone is not US1 completion; US2/US3 and final both-endpoint gates remain outstanding.

---

## Phase 4: User Story 2 - Continue Using the Offline Song Library (Priority: P1)

**Goal**: Preserve offline reading, Chinese-title navigation, settings, sharing and lifecycle
behavior on the minimum and current Android versions.

**Independent Test**: On the built artifact, execute C3 and the sharing portion of C4 on API 14
and API 37, including offline first launch and representative legacy preference fixtures.
No working third-party video provider is needed to prove these reader flows.

### Tests for User Story 2

- [ ] T018 [P] [US2] Add `app/src/test/java/com/goodtrendltd/HolySongs/SongCatalogTest.java` using the actual `assets/songs.xml` and `src/com/goodtrendltd/HolySongs/helpers/XMLParser.java` success path; assert "Preserve original order and all 422 entries", 414 distinct source titles, the eight duplicate source pairs in order, and representative source text/line breaks against T004 expectations. Do not instantiate Android Activities or recreate their loading/map pipeline in local JVM tests; production ASCII-space removal, "The last encountered lyric wins in `MainActivity.songLyricMap`", the effective "414 distinct keys; used by the current title-to-lyric map" and actual rendered output are assigned to T020/T027 device tests, without adding a framework or extracting new application layers.
- [ ] T019 [P] [US2] Add `app/src/test/java/com/goodtrendltd/HolySongs/helpers/HanziHelperTest.java` and `app/src/test/java/com/goodtrendltd/HolySongs/helpers/ChineseCharCompTest.java` to characterize actual helper behavior: first pinyin reading, lowercase/no tones/Unicode ü, non-Chinese fallback and comparator properties using `Locale.CHINA`; do not assert host collation proves identical Android ordering or change the sorting algorithm.
- [ ] T020 [P] [US2] Define C3/sharing runtime cases and dedicated-device preference fixture steps in `specs/001-modernize-build-system/evidence/us2-reader.md`, preserving "appPrefFile", `fontSize` type "Integer" default "20", UI values "16, 18, 20, 22, 24, 26, 28, 30; reset writes 20", `nightMode` type "Boolean" default "true" and "true = night/dark Holo; false = light Holo"; quote "No key renaming, type changes, clearing, new store, or migration" and assert String extras "com.goodtrendltd.LYRIC" and "com.goodtrendltd.SONG_NAME", including safe missing-extra handling, lifecycle, font scale, sharing-recipient absence and about/settings return paths. Define device assertions against T004 fixtures for actual space removal, effective catalog count, every duplicate-title winner and rendered lyrics; record expected sidebar positions for present/absent letters on each runtime before fixes, allowing only documented native Chinese collation differences under FR-008. Define process-stopped launch-to-usable-list timing, measurement method, scroll-stall observations and app crash/ANR evidence checks for each endpoint; no latency SLA is added.

### Implementation for User Story 2

- [ ] T021 [US2] Execute the characterization tests with `./gradlew :app:testDebugUnitTest` and attempt the relevant pre-fix runtime scenarios, recording baseline passes, reproducible defects and device blockers in `specs/001-modernize-build-system/evidence/us2-reader.md`; require each subsequent correction to map to observed code evidence or a failing required scenario, not invented regression expectations.
- [ ] T022 [US2] Correct asset-stream and list-indicator lifecycle hazards in `src/com/goodtrendltd/HolySongs/MainActivity.java`: close/read UTF-8 input completely while preserving space removal and map semantics, guard empty scroll callbacks and malformed loading failures, cancel delayed callbacks and track window attachment before removal; preserve list selection/navigation and introduce no new repository/database layer.
- [ ] T023 [US2] Preserve lyric rendering/settings and add safe missing-extra/share handling in `src/com/goodtrendltd/HolySongs/DisplayLyricActivity.java` and app-sharing handling in `src/com/goodtrendltd/HolySongs/MainActivity.java`; keep `ACTION_SEND`, `text/plain`, expected subject/text payloads, Chinese formatting and String intent identifiers unchanged, and verify a missing recipient cannot crash the app.
- [ ] T024 [US2] Make theme-return and system Back behavior lifecycle-safe in `src/com/goodtrendltd/HolySongs/SettingsActivity.java`, using API-guarded modern callbacks with legacy fallback as required; retain private `appPrefFile`, existing `fontSize`/`nightMode` types/defaults/UI values from T020, reset-to-20 behavior and persistence across relaunch without clearing preferences.
- [ ] T025 [US2] Address only demonstrated empty-list, index-bound or resize-related navigation failures in `src/com/goodtrendltd/HolySongs/SongTitleAdapter.java` and `src/com/goodtrendltd/HolySongs/Sidebar.java`, retaining Collator ordering, pinyin conversion and existing letter-navigation intent; record a no-change conclusion in `specs/001-modernize-build-system/evidence/us2-reader.md` if checks show no correction is required, and escalate algorithm/data changes instead of silently rewriting them.
- [ ] T026 [US2] Validate system-bar insets, increased font scale and API 37 resizing for `res/layout/main.xml`, `res/layout/lyric_view.xml`, `res/layout/settings_view.xml` and their owning Activities, plus `src/com/goodtrendltd/HolySongs/AboutActivity.java`; apply only demonstrated compatibility fixes with guarded APIs, retain Holo styles and navigation, and record exact changed paths or no-change findings in `specs/001-modernize-build-system/evidence/us2-reader.md`.
- [ ] T027 [US2] Rebuild and execute T020 on both API 14 and API 37, recording each scenario and final APK hash in `specs/001-modernize-build-system/evidence/us2-reader.md`: actual Activity-owned loading/space removal and effective catalog count, all duplicate-title winner lyrics/rendering against T004 fixtures, ordering/sidebar positions under FR-008, every font size/reset, both themes, representative preseeded preferences, process relaunch/rotation/rapid navigation, both shares and absent recipient, about/settings, and API 37 gesture/three-button Back and large-screen checks. Record process-stopped launch-to-usable-list elapsed time/method and scroll stalls per endpoint, inspect test-session logs/system ANR indications, and require zero app crashes/ANRs; missing evidence or a blocked core check cannot count as a pass.

**Checkpoint**: US2 is independently validated for offline reader/settings/sharing behavior;
third-party provider audit and WebView-specific lifecycle work are handled in US3.

---

## Phase 5: User Story 3 - Understand Compatibility and Verification Limits (Priority: P2)

**Goal**: Make provider behavior/failure handling and verification/signing limitations visible
without dropping functionality or weakening network security.

**Independent Test**: Review and repeat the provider audit, network-failure and WebView lifecycle
steps, confirm return to offline lyrics on both API endpoints, and inspect the evidence/signing
guidance. Provider availability must be reported honestly and does not excuse app crashes.

### Tests for User Story 3

- [ ] T028 [P] [US3] Define provider/error/fullscreen/Back/cancellation/background-resume tests in `specs/001-modernize-build-system/evidence/us3-video.md`, including missing or unexpected target/title extras, Wi-Fi unavailable/mobile-data confirmation and offline return; preserve String extra "com.goodtrendltd.searchTarget" and target values "`youtube`, `youku`, `tudou`", with explicit outcomes for every required API 14/API 37 scenario.
- [ ] T029 [P] [US3] Audit endpoints from `res/values/strings.xml` and the current APK into `specs/001-modernize-build-system/evidence/providers.md`, recording the exact baseline prefixes "http://m.youtube.com/results?q=", "http://www.soku.com/m/y/video?q=" and "http://www.soku.com/m/t/video?q=", Chinese query, status/redirect/final URL and WebView version; evaluate same-provider HTTPS equivalence without source edits, distinguish untested from broken providers, and request recorded approval for any proposed provider replacement/removal/external-browser change or accepted limitation.

### Implementation for User Story 3

- [ ] T030 [US3] Replace unsafe Wi-Fi dereferencing in `src/com/goodtrendltd/HolySongs/DisplayLyricActivity.java` with an API-guarded maintained connectivity query and null-safe legacy fallback; preserve the existing mobile-data confirmation and exact target/title extras, and test no-network, Wi-Fi-absent and mobile-data cases without new permissions.
- [ ] T031 [US3] Correct WebView ownership/state/Back behavior in `src/com/goodtrendltd/HolySongs/VideoSearch.java`: pause/resume temporary absences, save/restore state safely, detach/destroy only at final teardown, exit fullscreen/history before leaving, and handle invalid targets/missing titles safely; use guarded modern Back handling with legacy fallback and preserve the three existing provider identities.
- [ ] T032 [US3] Correct loading-dialog and asynchronous callback lifecycle/error handling in `src/com/goodtrendltd/HolySongs/HTML5WebView.java`, dismiss safely on failure/cancellation/teardown, deny unsolicited geolocation, and remove or replace only demonstrated unavailable/unsafe settings; do not introduce TLS bypasses, global cleartext/mixed-content allowances or new permissions, and keep fullscreen/return behavior usable.
- [ ] T033 [US3] After T029 decisions, apply only verified same-provider HTTPS equivalents in `res/values/strings.xml` and UTF-8 query encoding in `src/com/goodtrendltd/HolySongs/VideoSearch.java`, preserving the search intent; record exact changes or approved no-change/provider limitations in `specs/001-modernize-build-system/evidence/providers.md`, and stop for explicit approval before replacement/removal or external-browser UX changes.
- [ ] T034 [US3] Build the updated artifact and execute T028 on API 14/API 37 including API 37 large-screen/video navigation, recording results in `specs/001-modernize-build-system/evidence/us3-video.md` and retesting affected reader-return flows; unavailable providers need audit/approval evidence, while crashes, trapped loading, unsafe transport workarounds or failed offline return block acceptance.
- [ ] T035 [US3] Add future-signing and update-limit guidance to `README.md`: preserve application identity, explain versionCode progression and compatible signing/lineage, distinguish normal debug signing and representative preference fixtures from genuine production upgrades, and state that this feature generates/replaces no release keys and performs no publication.
- [ ] T036 [US3] Consolidate per-story results into `specs/001-modernize-build-system/evidence/verification.md` using T005 record fields and dated `passed`/`failed`/`blocked` outcomes, link provider approvals and provenance risks, identify all missing evidence, and keep "prior failures are not erased"; do not merge results from different APKs into an unsupported all-passing conclusion.

**Checkpoint**: All three stories have scoped behavior and evidence. US3 completion requires
safe app behavior and recorded provider decisions, not a fabricated claim that every old site works.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Revalidate the final integrated artifact and review scope; no speculative cleanup.

- [ ] T037 Review the complete implementation diff against `specs/001-modernize-build-system/plan.md` and `.specify/memory/constitution.md`, recording file-specific compatibility reasons in `specs/001-modernize-build-system/evidence/review.md`; verify unchanged data/JAR hashes and identity, no tracked `local.properties`/generated output/secrets, no blanket lint/security bypasses, no Kotlin/Compose/UI rewrite, and no unapproved provider changes; fix only concrete in-scope findings and rerun affected tests.
- [ ] T038 Reconcile `README.md` and `specs/001-modernize-build-system/quickstart.md` with the implemented and actually exercised commands, supported matrix, approved limitations and signing guidance; retain project background and clearly label blocked/unexecuted validation instead of converting planned steps into claims of success.
- [ ] T039 Run the full `specs/001-modernize-build-system/quickstart.md` from a clean exact-revision checkout after all fixes/cleanup, including IDE sync/build, CLI tests/lint, asset/manifest/debug-signature inspection and all API 14/API 37 runtime gates, explicitly repeating T020/T027 production-loading, per-runtime ordering/sidebar, process-stopped launch timing, scroll observations and zero-crash/ANR checks; record final revision/APK hashes and rerun evidence in `specs/001-modernize-build-system/evidence/verification.md`, leaving this task unchecked if any required environment or core-flow check is blocked.
- [ ] T040 Have the main agent review C1–C5 and FR-001–FR-015 coverage in `specs/001-modernize-build-system/evidence/verification.md` and `specs/001-modernize-build-system/evidence/review.md`, then update `specs/001-modernize-build-system/tasks.md` statuses and report completion only when final-artifact gates and approval requirements pass; otherwise report exact blockers with unfinished tasks, without signing/publishing a release.

---

## Dependencies & Execution Order

### Phase Dependencies

```text
Setup T001–T003
  → Foundation T004–T006
  → US1 T007–T017 (build/install MVP)
  → US2 T018–T027 (offline reader preservation)
  → US3 T028–T036 (video safety + compatibility evidence)
  → Final T037–T040 (integrated acceptance)
```

Tool installation/access blockers stop commands requiring those tools. Missing runtime endpoints
may be inventoried while implementation preparation continues, but block T017 if no supported
launch target exists and block T027/T034/T039 whenever a required endpoint is missing. Do not
mark the whole feature complete or silently alter the minimum version.

### User Story Dependencies

- **US1 (P1)** depends on setup/foundation, not a working video service. T014 includes only
  demonstrated compile/startup corrections needed for the developer entry point.
- **US2 (P1)** requires the US1 build artifact and harness; tests are independent of live video.
- **US3 (P2)** requires the build and stable reader-return flow. It edits files also used by US2,
  so the default implementation order is sequential. Audit/test preparation can be isolated,
  but the stories are not falsely advertised as independent concurrent writers.
- **Final acceptance** depends on all stories and revalidates the integrated final artifact.

### Within Each User Story

| Ready batch / task | Prerequisite |
|---|---|
| T001 → T002 → T003 | Repository access, then owner/tool/device access as applicable |
| T004, T005, T006 | Setup inventory completed; these three are independent |
| T007, T008 | Foundation complete; separate test/evidence files |
| T009, T010 | Foundation and test definitions ready; wrapper and root config are separate |
| T011 → T012 → T013 → T014 | Both T009/T010 complete, then module/manifest/menu fixes before build |
| T015 → T016 → T017 | Successful build checkpoint before retirement, docs before clean-checkout verification |
| T018, T019, T020 | US1 checkpoint; separate test/scenario files |
| T021 → T022 → T023 → T024 → T025 → T026 → T027 | Test definitions before reader changes; serialize overlapping source/evidence paths |
| T028, T029 | US2 checkpoint; test definitions and read-only audit have distinct output files |
| T030 → T031 → T032 → T033 → T034 → T035 → T036 | Video scenarios/audit first; T033 also requires any applicable T029 approval |
| T037 → T038 → T039 → T040 | All story work ready; fixes/docs before final replay and parent sign-off |

No conditional task is checked merely because it made no edit. A no-change result must cite
its executed check or inspection evidence. Approved provider limitations must identify approver,
date and scope; they cannot waive safe app behavior or offline-return requirements.

### Parallel Opportunities

T004–T006, T007–T008, T009–T010, T018–T020 and T028–T029 are independent ready batches
with disjoint write paths. `[P]` does not authorize simultaneous writes to this checkout.
Use separate clean worktrees and parent integration if truly parallelizing writers; otherwise
run sequentially. Device tests must also own separate targets to avoid state interference.

---

## Parallel Example: User Story 1

After foundation, prepare the contract cases and input tests independently:

```text
T007 → evidence/us1-build.md
T008 → app/src/test/java/com/goodtrendltd/HolySongs/BuildInputsTest.java
```

After those definitions, wrapper generation (T009) and root Gradle configuration (T010) can
be prepared independently. Integrate both before module wiring/build execution (T011–T014).

## Parallel Example: User Story 2

After US1, independent test-definition assignments:

```text
T018 → SongCatalogTest.java
T019 → helpers/HanziHelperTest.java + helpers/ChineseCharCompTest.java
T020 → evidence/us2-reader.md
```

Full paths are in the task lines. Join these results before T021. Do not concurrently edit
MainActivity for T022/T023 or the shared reader evidence file during later tasks.

## Parallel Example: User Story 3

After US2, independent audit/preparation assignments:

```text
T028 → evidence/us3-video.md (scenario definitions only)
T029 → evidence/providers.md (read-only source/provider audit)
```

Only T029 owns provider/device interaction in this batch. Join results before code changes;
provider transport/identity changes remain behind the T029/T033 approval boundary.

---

## Requirement and Contract Coverage

| Requirement | Primary tasks |
|---|---|
| FR-001 clean-checkout debug APK | T007, T009–T017, T039 |
| FR-002 wrapper/modern IDE | T009–T011, T016–T017, T039 |
| FR-003 pinned tooling/support range | T002–T003, T009–T012, T017, T027, T034 |
| FR-004 generated outputs/local configuration | T008–T011, T015, T017, T037 |
| FR-005 scoped Java compatibility changes | T013–T014, T021–T026, T030–T033, T037 |
| FR-006 identity/preferences | T008, T011–T012, T017, T020, T023–T024, T027 |
| FR-007 song preservation | T004, T008, T018, T022, T027, T039 |
| FR-008 offline browsing/navigation | T018–T022, T025, T027 |
| FR-009 display settings | T020, T023–T024, T026–T027 |
| FR-010 sharing/about/settings | T020, T023–T024, T026–T027 |
| FR-011 provider audit/approval | T028–T029, T033–T034, T036 |
| FR-012 safe network/provider failure | T028, T030–T034 |
| FR-013 developer/signing documentation | T016, T035–T036, T038 |
| FR-014 runtime evidence/completion gate | T003, T005, T027, T034, T036, T039–T040 |
| FR-015 exclusions | T010–T011, T029–T033, T037, T040 |

C1 maps to US1; C2 maps to US1 and the legacy fixture checks in US2; C3 maps to US2;
C4 sharing maps to US2 and video maps to US3; C5 maps to T005, US3 evidence and final replay.
SC-001/006 are covered by build/docs/repository checks, SC-002 by the final runtime matrix,
SC-003/004 by the catalog/preferences checks, and SC-005 by the audited approval boundary.

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete setup and foundation; record unavailable infrastructure rather than hiding it.
2. Implement and verify US1 through T017: a modern, documented build/install entry point.
3. Stop at this developer checkpoint if desired. Do not label it a completed revival or publish
   an app: offline behavior and both-endpoint acceptance still require later phases.

### Incremental Delivery

1. Foundation → protected baseline and reproducible evidence rules.
2. US1 → working developer build/install checkpoint.
3. US2 → verified offline reader/settings/sharing checkpoint.
4. US3 → safe video flow and transparent provider/signing limitations.
5. Final replay/review → completion only for a verified final artifact, with approved limitations.

### Parallel Team Strategy

The main agent coordinates and reviews. Coding can use `github-copilot/mai-code-1.1-flash`
through the governed subagent workflow, after model/runner preflight. Assign bounded tasks or
small dependency-ordered slices, not all 40 tasks to one unreviewed worker. Serialize writes in
this checkout; isolated writer worktrees require a clean source allocation and parent integration.
No implementation agent is invoked during task generation. Stop on subagent infrastructure
failure and report evidence before retry or owner-approved execution-mode changes.

## Notes

- Expected count: 40 tasks — 3 setup, 3 foundational, 11 US1, 10 US2, 9 US3, 4 final.
- Resource IDs, fixtures and preference values are preservation contracts, not prompts for a redesign.
- Local JVM tests do not establish device UI, WebView, lifecycle or Android collation correctness.
- Never automatically accept SDK licenses, clear a user's app data, substitute unsupported tool
  versions, fabricate a device result or treat a documented blocker as completed acceptance.
- Raw secrets must not be copied into reports; release key generation and publishing are excluded.
- If scope or support policy must change, stop for an explicit decision and update the governing
  spec/plan before changing dependent tasks. No such change is authorized by this task list.
