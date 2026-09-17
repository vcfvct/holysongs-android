# Feature Specification: Incremental Kotlin and Jetpack Compose UI

**Feature Branch**: Not created; specification authored on `master`

**Created**: 2026-09-15

**Status**: Complete and owner-accepted on 2026-09-16. Kotlin/Compose hosts cover the song list,
lyric reader, settings, and about screens; the retained Java video boundary is hardened. The owner
reported testing the complete app on a phone and accepted the result as looking great. The final PR
candidate passes the clean local CLI build, JVM tests, lint, and androidTest APK assembly. API 23
runtime remains explicitly waived and untested. Earlier evidence records retain their narrower
instrumentation, provider-audit, process-death, and WebView callback limitations rather than being
retroactively rewritten as automated passes.

**Input**: Following the merged Gradle checkpoint, the owner requested Kotlin and Jetpack
Compose adoption rather than completing all legacy UI fixes in Java and converting afterward.

## Context and Scope

The Gradle checkpoint was merged as `f62519b` (PR #3). It establishes a modern build entry
point, not completed runtime acceptance. This feature follows
[001-modernize-build-system](../001-modernize-build-system/spec.md); see
[transition.md](transition.md) for ownership of unfinished tasks and acceptance obligations.

Introduce Kotlin and replace the core reader UI with Compose incrementally: song list and
letter navigation first, followed by lyric reading, settings, and about. Fix the reported
main-view/top-bar overlap as part of the first screen slice, after reproducing its cause.
Maintain working checkpoints with Java, XML, and Compose coexisting during migration.
Do not first convert every legacy Activity to Kotlin merely to discard it for Compose.

Compose is an owner-selected implementation constraint, not approval for unrelated product
features, a wholesale architecture rewrite, a new sorting algorithm, or dropped functionality.
The existing WebView may remain Java/View-based behind a compatible entry point; its safety
and return-to-reader behavior remain acceptance requirements. Checkpoint applicability is defined
in [contracts/ui.md](contracts/ui.md): US1 fully verifies the migrated Main screen and retained-screen
safety/interoperability; full reader/settings/about restoration is due in US2. No newly introduced
regression or concrete safety failure is waived by that staging.

## Active Execution Amendment — 2026-09-15

The owner confirmed the current Studio build and explicitly waived Android6/API23 runtime
execution while requesting continuation. See [execution-decisions.md](evidence/execution-decisions.md).
This supersedes API23 runtime/fixture gates below and in linked implementation documents for this
feature; required runtime execution is API37. Keep minSdk23 and compatibility checks unchanged,
report Android6 as untested, and preserve all other scenario/safety/provider-approval gates.
Prior two-endpoint wording remains historical planned coverage, not a claim of execution.

## Clarifications

### Session 2026-09-15

- Q: Should the migrated screens support completing all core reading tasks with Android’s TalkBack screen reader, including letter navigation? → A: Require accessible labels for standard controls and large-text support; defer full TalkBack navigation and custom-sidebar accessibility to a later feature.

### Analysis Remediation — 2026-09-15

The owner authorized remediation after the analysis report ("yes, just see as fit"). The master
selected these bounded defaults; they are not claims of implementation or runtime verification:

- Allow only the app-private AndroidX signature permission specified in FR-011/B2, not additional
  dangerous/runtime permissions or a blanket exception for dependency manifests.
- Apply the US1/US2/US3 checkpoint matrix in contracts/ui.md; final acceptance still requires all flows.
- Preserve stored font values; render integers 1–200 as sp and use 20 sp for missing, wrong-type,
  zero, negative or larger values. UI choices remain 16–30 step2 and reset20.
- When the full letter rail fits, taps/drags select; in compact scrollable mode, taps select and
  vertical drags scroll only the rail. Cancellation/resize ends the gesture without extra selection.

## User Scenarios & Testing

### User Story 1 — Build and Browse a Compose Song List (Priority: P1)

As a developer and reader, I can build the mixed-language app and browse the unchanged offline
catalog through a Compose main screen, without content or controls hidden behind system or app bars.

**Why this priority**: This provides a bounded first Compose checkpoint and directly addresses
the reported usability issue without waiting for every screen to migrate.

**Independent Test**: Build from a clean checkout, launch with connectivity disabled, browse
and use letter navigation on the Compose list, then open a song in the existing reader and return.
Run on the approved minimum and selected current Android runtime.

**Acceptance Scenarios**:

1. **Given** documented prerequisites and a clean checkout, **When** the developer builds through
   the wrapper and Android Studio, **Then** Java and Kotlin compile together, tests and lint pass,
   and an installable debug APK is produced without Ant or tracked local configuration.
2. **Given** first launch without connectivity, **When** the Compose list appears, **Then** it
   shows the baseline effective catalog and preserves Chinese ordering and letter navigation.
3. **Given** the reported overlap, **When** the user browses on the selected current runtime
   with gesture or three-button navigation, **Then** the first/last rows and sidebar targets
   remain reachable and essential content is not obscured by system bars, cutouts, or the app bar.
4. **Given** rotation, window resizing, or increased system font scale, **When** the list is
   displayed and scrolled, **Then** navigation remains usable without clipped essential controls.
5. **Given** mixed Compose and legacy screens, **When** a song is selected and Back is used,
   **Then** the correct lyrics open and the list position is restored without duplicate screens.

### User Story 2 — Read Lyrics and Keep Existing Settings (Priority: P1)

As an existing reader, I can use Compose lyric, settings, and about screens without losing
content, stored preferences, sharing, or familiar navigation.

**Why this priority**: These screens complete the offline reader journey and avoid continuing
to maintain two implementations of the core reader UI.

**Independent Test**: Exercise the offline reading/settings/sharing flow with fresh defaults
and representative legacy preference fixtures on both required runtime endpoints.

**Acceptance Scenarios**:

1. **Given** any selected title, including duplicate-title fixtures, **When** lyrics open,
   **Then** the expected winning lyric, Chinese characters, and line breaks are preserved.
2. **Given** legacy `appPrefFile` values, **When** Compose screens read preferences,
   **Then** integer `fontSize` and boolean `nightMode` retain their existing interpretation.
   Missing values use font size 20 and night mode true; true means dark and false means light.
3. **Given** settings, **When** each size 16, 18, 20, 22, 24, 26, 28, and 30 is selected,
   or reset is used, **Then** the reader applies the selection, reset writes 20, and the
   setting survives process relaunch. Both themes remain user-selectable and persistent.
4. **Given** an installed share recipient, **When** lyrics or the app are shared, **Then**
   the existing payload is delivered; no recipient or invalid input produces no crash.
5. **Given** rotation, background/resume, process recreation, or rapid Back navigation,
   **When** reading resumes, **Then** the selected song and appropriate list/lyric scroll state
   are restored without stale content, duplicate navigation, or a stranded overlay.
6. **Given** settings or about is opened, **When** the user returns, **Then** existing content
   remains accessible and theme/font changes apply without restarting the app manually.

### User Story 3 — Retain Safe Video Integration and Verification (Priority: P2)

As a reader and maintainer, I can still reach existing video searches from the new reader,
return safely when a provider fails, and see what was actually tested.

**Why this priority**: A UI migration must not silently remove existing actions or hide known
compatibility risks behind a successful build.

**Independent Test**: Exercise every provider entry and network/error/Back path from the Compose
reader, including an offline attempt; review the artifact-linked verification report.

**Acceptance Scenarios**:

1. **Given** a selected song, **When** each existing video action is invoked, **Then** its
   provider identity and title are passed correctly and mobile-data confirmation is preserved.
2. **Given** no network, a failed page, or a missing/invalid argument, **When** search is
   attempted or cancelled, **Then** the app does not crash or trap the user behind a loading UI.
3. **Given** fullscreen, history, or background/resume in the WebView, **When** the user returns,
   **Then** Back and lifecycle handling safely lead to the selected offline lyric.
4. **Given** a broken provider, **When** replacement, removal, or external-browser behavior is
   proposed, **Then** explicit owner approval is recorded before that behavior changes.
5. **Given** completion is proposed, **When** evidence is reviewed, **Then** every required
   scenario has an artifact-linked passed, failed, or blocked outcome; missing device evidence
   is not substituted with JVM results or a successful preview/build.

### Edge Cases

- Empty/malformed catalog loading must fail safely without rewriting bundled data.
- All eight duplicate source titles retain last-entry-wins resolution; Compose list keys must
  not assume all 422 source titles are unique. The effective catalog has 414 titles.
- Absent letters, non-Chinese text, and native collation differences must not cause invalid
  sidebar destinations. Record expected positions on each supported runtime.
- Insets must be applied at the correct boundary without double padding between Compose,
  app bars, and retained legacy screens. Do not use a hardcoded status-bar height as the fix.
- Maximum app font size combined with increased system font scale must keep controls reachable.
- Recomposition must not repeatedly load the catalog, launch navigation, or invoke sharing;
  state restoration must handle invalid or unavailable saved selections safely.
- Existing installations below a newly approved minimum cannot receive this build. Document
  that loss of update eligibility; do not describe a minimum-version increase as transparent.
- Debug signing is not proof of a production upgrade. Do not uninstall a real user's app to
  simulate preserved preferences or generate replacement release keys.

## Requirements

### Functional Requirements

- **FR-001**: Introduce Kotlin and Jetpack Compose using a researched, pinned, mutually
  compatible toolchain. Preserve wrapper-based clean builds, IDE support, unit tests, and lint.
  Reuse the existing Gradle setup unless a concrete compatibility requirement justifies a change.
- **FR-002**: The minimum supported Android version for this feature MUST be API 23
  (Android 6.0), as approved by the owner on 2026-09-15. Devices on API 14–22 lose eligibility
  for this new build. Before implementation, verify selected Kotlin, Compose, and AndroidX
  dependencies against API 23. Any higher minimum requires renewed owner approval; no silent
  increase, manifest override, or outdated dependency workaround.
- **FR-003**: Deliver reviewable working slices: Kotlin/Compose enablement, Compose song-list
  pilot, then remaining reader screens. Java helpers and existing Java tests may remain.
  Convert stable non-UI code only when necessary or when a specific maintenance benefit is recorded.
- **FR-004**: Core list, letter navigation, lyric, settings, and about UI MUST use Compose at
  feature completion. Legacy UI may remain temporarily between slices; a retained WebView is
  allowed. Delete superseded UI only after replacement behavior is verified and references checked.
- **FR-005**: Preserve application ID `com.goodtrendltd.HolySongs`, launcher identity, preference
  compatibility, song bytes, and the bundled pinyin library. Preserve 422 source entries, 414
  effective titles, ASCII-space removal, duplicate resolution, Chinese collation, and pinyin
  semantics according to the existing baseline. No new IDs or song-format changes are authorized.
- **FR-006**: Preserve offline first launch, browse/letter navigation, correct lyrics, and
  responsive scrolling. Record launch-to-usable-list timing, visible stalls, and crash/ANR checks
  on each required runtime; do not fabricate a numerical performance SLA.
- **FR-007**: Correct the observed main-screen overlap and validate insets, cutouts, rotation,
  resizable windows, and increased font scale across migrated screens and legacy transitions.
  Record device/API, reproduction steps, and before/after observations. Letter navigation uses
  fit-mode tap/drag selection or compact-mode tap selection with drag-to-scroll of the rail only,
  as defined in contracts/ui.md; never let one gesture both scroll the rail and select a song section.
- **FR-008**: Preserve the exact preference file, keys, stored types, defaults, selectable sizes,
  and reset behavior. Compose light/dark styling may replace Holo visuals, but the saved boolean
  determines the theme; system or dynamic theming MUST NOT silently override that choice.
  For display only, an integer fontSize in 1–200 inclusive is rendered unchanged in sp; missing,
  wrong-type or out-of-range values render at 20 sp without rewriting storage. This safety range
  does not expand the selectable UI values. Snapshot/fallback/selection behavior is in data-model.md.
- **FR-009**: Preserve lyric formatting and share intents (`ACTION_SEND`, `text/plain`, existing
  subject/text payloads), app-sharing payload, about content, and existing video actions.
  Preserve existing String intent identifiers across retained Activity boundaries; any internal
  route replacement must keep callers working and add no exported entry points.
- **FR-010**: Scope UI state and side effects safely. Preserve selection, scrolling, preference
  updates, and Back behavior across recomposition, rotation, recreation, and screen transitions.
  Cancel obsolete work and release owned callbacks/windows/WebViews at the appropriate lifecycle.
- **FR-011**: Retain safe network checks, video cancellation/fullscreen/history/Back behavior,
  and return to offline lyrics. Audit all existing providers and require owner approval for
  replacements/removals or accepted availability limitations. No TLS bypass, unsolicited
  geolocation grant or global cleartext/mixed-content allowance is authorized. In addition to
  existing INTERNET/ACCESS_NETWORK_STATE permissions, allow only AndroidX Core's app-private
  `com.goodtrendltd.HolySongs.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` declaration and use with
  `signature` protection, as specified in build contract B2. No other new permissions, including
  dangerous/runtime permissions, are authorized; unexpected merged-manifest additions require review.
- **FR-012**: Add regression coverage before changing behavior: retain and complete relevant
  catalog/helper tests, then add Compose interaction/state tests for list navigation, settings,
  and reader flows. Runtime checks must cover insets, actual loading/rendering, lifecycle,
  sharing, and WebView interoperability; local JVM tests are not substitutes.
- **FR-013**: Verify the final artifact on the approved minimum and a dated current Android
  runtime, including current-runtime phone gesture/three-button navigation and a large/resizable
  configuration. Document exact source revision, APK hash, tools, runtime, steps, expected and
  observed results, and passed/failed/blocked outcomes. Required failures/blockers prevent completion.
- **FR-014**: Update build/install/testing, support-range, limitations, and future-signing
  documentation. No release signing, publishing, secrets, tracked machine paths, or generated output.
- **FR-015**: Keep architecture proportional to this local reader. No backend, database or
  preference-store migration, DI framework, new product features, or forced all-Kotlin rewrite.
  Compose component styling is in scope; a separate branding/UX redesign is not.
- **FR-016**: Standard interactive controls on migrated screens MUST expose meaningful accessible
  labels, verified through accessibility semantics checks. Large-text support remains required
  under FR-007. Full TalkBack journey verification and custom-sidebar accessibility are deferred
  to a later feature and are not acceptance gates for this migration; document these limitations.

### Key Entities

- **Song catalog and selected song**: Existing source data and effective title-to-lyric mapping;
  no new persistence model. Selection must survive relevant lifecycle transitions.
- **Reader preferences**: Existing integer font size and boolean dark/light choice.
- **Screen state**: Current destination, selected title, scroll position, and transient loading/error
  state; implementation planning defines ownership and restoration without changing user data.
- **Verification record**: Artifact-linked scenario result, environment, limitations, and approvals.

## Success Criteria

- **SC-001**: A clean checkout builds through documented CLI and IDE entry points, passes unit
  tests/lint, and installs and launches on both approved runtime endpoints.
- **SC-002**: All core reader screens are replaced incrementally and all required offline,
  ordering, duplicate-lyric, navigation, sharing, and settings scenarios pass on both endpoints.
- **SC-003**: No essential list/reader/settings/about content is obscured by system or app bars
  in the documented phone, large/resizable-window, and increased-font-scale cases. All standard
  interactive controls on migrated screens expose meaningful accessible labels; full TalkBack
  journeys and custom-sidebar accessibility are explicitly excluded from this feature's sign-off.
- **SC-004**: All eight font sizes, reset-to-20, and both themes work and persist; representative
  legacy preference fixtures are honored without resets or storage conversion.
- **SC-005**: Required lifecycle, restoration, and network-failure checks show zero app crashes,
  ANRs, trapped loading states, or blocked return to offline reading.
- **SC-006**: Every existing provider has an audit outcome and any accepted change/limitation has
  owner approval. Documentation distinguishes unavailable providers from unsafe application behavior.
- **SC-007**: Verification traces each required outcome to the tested artifact and support matrix;
  no task is declared passed merely because its predecessor PR merged or its implementation changed.

## Assumptions and Approved Support Decision

- Existing Java tests remain useful against Kotlin implementations. No test-language conversion
  is required. Existing partial tests need coverage review, not automatic completion credit.
- Pixel-identical Holo appearance is not required; readable Chinese text, existing capabilities,
  preference semantics, and navigational intent are preservation requirements.
- **Owner approval (2026-09-15):** In response to the proposal to drop API 14 support and
  target Android 6.0 / API 23 minimum, subject to dependency verification, the owner confirmed:
  "yes, dropping old android is fine." API 23 is now the approved support floor for feature 002.
  This is a product-support decision, not evidence of dependency compatibility or runtime tests.
  Existing installations below API 23 are not modified by this decision, but cannot install or
  update to the new build. No Gradle configuration change is performed by this specification.
- Planning will record exact dependency versions and the current-runtime selection date, assess
  AGP built-in Kotlin and custom source-directory integration, and choose a minimal Activity,
  navigation/state, and Compose test setup. This spec does not assume that merely removing the
  existing Kotlin opt-out is sufficient.
- Missing devices or historical release keys remain explicit verification limitations. A raised
  minimum does not retroactively validate or complete the predecessor's API 14 checks.
