# Feature Specification: Modernize HolySongs Build System

**Feature Branch**: `master` (existing branch; no branch-creation hook configured)

**Created**: 2026-09-15

**Status**: Draft

**Input**: User description: "Modernize the HolySongs Android build system. Replace the legacy
Ant/Eclipse build with a maintained Gradle build and checked-in wrapper, usable from current
stable Android Studio and the command line. Preserve Java, bundled songs, application identity,
preferences, and core behavior. Limit compatibility fixes, audit video search, document setup
and signing constraints, and verify the app on the minimum supported and current stable Android
versions. No rewrite, redesign, new features, or store publishing."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Build From a Clean Checkout (Priority: P1)

As a developer reviving HolySongs, I can follow the repository instructions to build an
installable debug app without recovering a historical development environment or asking the
original developer for local configuration files.

**Why this priority**: A repeatable development entry point is the prerequisite for maintaining
and testing the app.

**Independent Test**: Start with a clean checkout and documented prerequisites, follow the
command-line instructions, and install and launch the resulting debug app on a supported device.

**Acceptance Scenarios**:

1. **Given** a clean checkout and the documented prerequisites, **When** a developer follows
   the command-line build instructions, **Then** the build produces a debug APK without
   Eclipse/ADT, obsolete SDK tools, or undocumented machine-specific configuration.
2. **Given** the same checkout, **When** a developer opens it in the documented current stable
   Android Studio version and builds it, **Then** project import and the debug build succeed.
3. **Given** generated outputs have been removed, **When** the developer rebuilds, **Then**
   required outputs are regenerated without relying on versioned generated application files.
4. **Given** the documented environment, **When** the developer performs setup and a build,
   **Then** no local SDK paths, credentials, signing keys, or build outputs become required
   version-controlled changes.

---

### User Story 2 - Continue Using the Offline Song Library (Priority: P1)

As an existing HolySongs user, I can browse and navigate Chinese song titles, read lyrics,
change display settings, and share content without losing the experience I already rely on.

**Why this priority**: A successful build is not useful if modernization breaks the app's
primary purpose or discards existing content and preferences.

**Independent Test**: Install the debug app on each required test version and exercise the
core flows, including song and lyric use with connectivity disabled.

**Acceptance Scenarios**:

1. **Given** a fresh installation with no network access, **When** the user opens the song
   list and selects songs through scrolling and letter navigation, **Then** Chinese-title
   ordering and navigation retain baseline behavior and the correct lyrics appear.
2. **Given** a selected song, **When** the user scrolls its lyrics, **Then** Chinese characters,
   lyric formatting, and scrolling remain readable and functional.
3. **Given** changed font-size and theme preferences, **When** the user closes and reopens
   the app, **Then** those values persist and are applied to the relevant screens.
4. **Given** representative preferences stored using the legacy identifiers, **When** the
   modernized app reads them, **Then** it honors their values without requiring a reset.
5. **Given** a selected song and an installed receiving app, **When** the user invokes lyric
   sharing, **Then** the receiving flow contains the selected song's expected content.
6. **Given** an open list or lyric screen, **When** the device rotates or the app returns
   from the background, **Then** the user can continue browsing or reading without a crash
   or corrupted content.
7. **Given** the modernized app, **When** the user opens settings, about, or app sharing,
   **Then** the existing navigation and available content remain accessible.

---

### User Story 3 - Understand Compatibility and Verification Limits (Priority: P2)

As a maintainer, I can see which Android versions and user flows were verified, what remains
blocked, and whether legacy video providers still work before approving the migration.

**Why this priority**: Explicit evidence prevents compilation success from being mistaken for
a working product and avoids silently dropping external functionality.

**Independent Test**: Review the compatibility report and repeat its documented verification
steps, including an unavailable-network case for video search.

**Acceptance Scenarios**:

1. **Given** a selected song and network access, **When** each existing video-search action
   is tested, **Then** the report records the provider, attempted behavior, environment,
   outcome, and any failure or limitation.
2. **Given** an unavailable network or broken provider, **When** video search is attempted,
   **Then** the failure does not crash the app or prevent returning to offline lyrics.
3. **Given** a provider cannot be preserved, **When** replacement or removal is proposed,
   **Then** the maintainer's approval is recorded before the behavior is changed.
4. **Given** completed or blocked validation, **When** a maintainer reviews the report,
   **Then** every required flow has a passed, failed, or blocked result for each required
   Android version; missing evidence is not labeled as success.
5. **Given** a future update to an existing installation is being considered, **When** the
   maintainer follows the signing documentation, **Then** it explains identity, compatible
   signing credentials, and versioning prerequisites without supplying or replacing release keys.

---

### Edge Cases

- Missing prerequisites or unavailable dependency downloads prevent a build: setup documentation
  must identify the prerequisites and distinguish environment failures from app failures.
- A clean checkout contains no generated application files: the build must regenerate them.
- Duplicate or unusual Chinese titles and existing formatting quirks are preserved as baseline
  data, not silently corrected or normalized during this build migration.
- A share recipient is unavailable: invoking sharing must not terminate the app unexpectedly.
- External video pages are unreachable, retired, or incompatible: record the limitation and
  preserve offline use; replacement or removal requires approval.
- A legacy installation has incompatible signing credentials: do not promise that a debug APK
  can update it, require uninstallation as a preservation test, or claim its settings were migrated.
- A required emulator or physical device is unavailable: record verification as blocked;
  do not accept compilation as a substitute for runtime verification.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: A developer MUST be able to produce an installable debug APK from a clean checkout
  by following documented prerequisites and command-line build instructions.
- **FR-002**: The repository MUST provide the requested Gradle-based build and checked-in
  wrapper, usable in current stable Android Studio, without requiring the legacy Ant/Eclipse
  environment. These are user-selected delivery constraints, not a mandate for a new app architecture.
- **FR-003**: Planning MUST select and document maintained, mutually compatible build tools and
  SDK versions, an explicit minimum supported Android version, and a runtime test matrix covering
  that minimum and a current stable Android version. The selection date MUST be recorded.
- **FR-004**: The build MUST generate required application build outputs rather than rely on
  checked-in generated outputs. Repository tracking rules MUST exclude local SDK paths,
  credentials, signing keys, and generated build outputs, while retaining required wrapper files.
- **FR-005**: The migration MUST preserve the existing Java implementation and limit source,
  resource, dependency, and manifest changes to those necessary for building and operating within
  the selected support range. Each compatibility change MUST have a documented reason.
- **FR-006**: The application identity MUST remain `com.goodtrendltd.HolySongs`. Existing
  preference-file identifiers, preference keys, and stored-value interpretation MUST remain
  compatible; normal use MUST NOT require resetting preferences.
- **FR-007**: The bundled song library MUST retain its existing data format and content without
  song removal, text rewriting, or title normalization. Baseline comparison MUST verify both
  packaged data integrity and representative displayed lyrics.
- **FR-008**: Song browsing, Chinese-title ordering, letter navigation, and lyric viewing MUST
  preserve baseline behavior and work without internet access, including on first launch.
  Documented ordering differences caused solely by Android's native Chinese collation across
  supported versions are acceptable; the migration MUST NOT substitute a different sorting
  algorithm. On each runtime, the sidebar MUST select the expected valid list position for
  tested letters and selecting a title MUST open its corresponding lyric. Expected positions,
  including absent-letter behavior, MUST be recorded from the baseline before navigation fixes;
  unresolved differences or changes to section semantics require maintainer approval.
- **FR-009**: Font-size and theme settings MUST remain adjustable, persist across relaunch,
  and apply to the relevant screens. Chinese text and lyric scrolling MUST remain usable.
- **FR-010**: Lyric sharing, app sharing, settings, and about navigation MUST remain available.
  Sharing MUST pass the expected content to an available recipient without crashing when no
  recipient is available.
- **FR-011**: The migration MUST audit every existing video-search integration and record its
  observed behavior. Viable behavior MUST be preserved; replacement or removal of a broken
  integration MUST require recorded maintainer approval and MUST NOT become an integration redesign.
- **FR-012**: Failure of a video provider or network connection MUST NOT crash the app or
  prevent the user from returning to offline song and lyric use.
- **FR-013**: Documentation MUST include prerequisites, selected versions, clean-checkout build
  steps, installation steps, verification steps, known limitations, and future update-signing
  prerequisites. Release signing credentials MUST NOT be generated, replaced, or committed.
- **FR-014**: Runtime validation MUST cover all core flows on both required Android versions,
  including offline use, settings persistence, sharing, and lifecycle transitions. Evidence MUST
  identify the tested artifact, device or emulator, OS version, steps, and passed/failed/blocked
  outcomes. Failed or blocked required core-flow checks MUST prevent declaring this feature complete.
- **FR-015**: Compatibility fixes MUST preserve the core experience without bulk Kotlin
  conversion, Compose migration, visual redesign, new product features, backend services,
  song-format changes, or app-store publishing.

### Key Entities *(include if feature involves data)*

- **Song library**: The existing bundled catalog of titles and lyrics; content and format are
  preservation targets, not new data-model design work.
- **Display preferences**: Existing font-size and theme values, their persisted identifiers,
  and their interpretation by the app.
- **Application identity**: The existing installed-app identity and its relationship to signing
  and versioning requirements for future updates.
- **Verification record**: A tested artifact and environment, flow, expected and observed result,
  outcome, and any blocker or approved external-integration limitation.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A developer following only the documented instructions completes a clean-checkout
  build through both supported entry points and installs and launches the resulting debug app,
  with zero undocumented prerequisites or required edits to tracked machine-specific files.
- **SC-002**: All required core-flow checks pass on both the minimum supported and current stable
  test versions, with zero crashes during the documented scenarios; failed or blocked checks
  remain explicitly incomplete.
- **SC-003**: All bundled song content remains unchanged, representative displayed titles and
  lyrics match the recorded baseline, and all offline browsing and reading checks pass with
  connectivity disabled from first launch.
- **SC-004**: All tested font-size and theme values survive relaunch, and representative legacy
  stored values are honored without resetting preferences.
- **SC-005**: Every existing video provider has an audit outcome, and every proposed replacement
  or removal has recorded approval before implementation; no such change is made silently.
- **SC-006**: The maintainer can locate setup, build, install, test results, known limitations,
  and update-signing guidance in the repository; the migration adds zero tracked credentials,
  signing keys, local SDK paths, or generated application outputs.

## Assumptions

- The users in scope are developers restoring the build, existing song readers, and the maintainer
  approving compatibility tradeoffs. No accounts or new user roles are introduced.
- Tool and OS version choices are intentionally deferred to planning, as requested. The plan
  will fix the meaning of "current stable" to a dated selection and document the compatibility
  impact of any minimum-version increase before implementation.
- Initial environment setup and dependency retrieval may require internet access. Offline
  requirements apply to core app usage, not to a first build on an unprovisioned machine.
- The baseline is established from repository content and observable legacy behavior where
  executable. Unknown or untestable legacy behavior is recorded rather than invented.
- Physical devices or emulators are acceptable for the two required runtime versions. Their
  availability is a validation dependency, not permission to skip acceptance checks.
- Access to historical release signing credentials is not assumed. Preference compatibility can
  be checked using representative legacy stored values; a genuine in-place upgrade claim requires
  a compatible signed installation and actual upgrade evidence.
- Third-party provider availability is outside project control. An approved, documented provider
  limitation may remain, but new app crashes, loss of offline use, or unapproved feature removal
  are not acceptable substitutes for preservation.
- Existing defects unrelated to build or selected-platform compatibility are documented for
  separate work rather than silently expanded into this migration.
