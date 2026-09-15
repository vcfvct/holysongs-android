# Implementation Plan: Modernize HolySongs Build System

**Branch**: `master` (actual Git branch) | **Date**: 2026-09-15 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/001-modernize-build-system/spec.md`

**Feature identifier**: `001-modernize-build-system`. Spec Kit setup reports this identifier
as BRANCH; no Git branch was created or switched. The feature directory and branch are independent.

## Summary

Replace the Ant-only build with a single modern Gradle Android application module, retaining
Java sources, XML views, Holo styling, song data, identity, and preference semantics. Map the
existing source directories explicitly rather than relocating the app during this migration.
Make targeted compile and modern-target runtime fixes, then validate the APK on API 14 and
API 37. External video providers receive an audit, not a redesign. Failed/blocked required
runtime checks prevent completion even if the APK builds.

Research and design are complete; implementation and runtime validation have not started.
See [research.md](research.md) for dated toolchain sources, alternatives, and evidence.

## Technical Context

**Language/Version**: Existing Java, explicit Java 8 source/target compatibility; JDK 17 build
runtime/toolchain; Groovy Gradle scripts. No application Kotlin conversion.

**Primary Dependencies**: AGP 9.4.0; Gradle wrapper 9.6.0; SDK Build Tools 36.0.0; Android SDK
platform 37; existing `libs/pinyin4j-2.5.0.jar` retained with checksum. JUnit 4.13.2 test-only.
Android Studio Quail 4 (2026.1.4) is the selected stable IDE. No AndroidX UI dependencies.

**Storage**: Read-only `assets/songs.xml`; private SharedPreferences `appPrefFile` with
`fontSize` integer and `nightMode` boolean. No migrations or backend.

**Testing**: Local JUnit characterization tests, lint, source/APK asset hash comparison,
manifest and signing inspection, documented manual device validation on API 14 and API 37.
API 37 also receives gesture/three-button Back and large-screen resize/insets checks.

**Target Platform**: Android 4.0/API 14 and above; compileSdk/targetSdk 37 (Android 17).
Build host baseline: Linux x86_64 with Bash and JDK 17; standard Windows wrapper is included,
but a cross-host certification matrix is not added to this feature. IDE and CLI must both pass.

**Project Type**: Offline-first Android mobile application, one `:app` module, five Activities.

**Performance Goals**: Preserve responsive browsing/reading for all 414 effective title entries;
zero crashes or ANRs (Application Not Responding events) during required flows. On each endpoint,
record elapsed time from process-stopped launch to a usable song list, the measurement method,
and visible scrolling stalls. Check test-session logs and system ANR indications. No numerical
latency threshold, benchmark suite or performance rewrite is introduced; missing observations
block validation, and any app crash or ANR fails the required scenario.

**Constraints**: Keep applicationId/namespace `com.goodtrendltd.HolySongs`, component and intent
identities, library and data bytes, preference keys/defaults, and Java/XML architecture. No
new runtime permissions or security bypasses for video sites. No release-key changes, store
publishing, new features, bulk refactors, or tool versions selected through dynamic ranges.

**Scale/Scope**: 11 handwritten Java files, 7 layouts, 5 Activities, one vendored JAR, 422 raw
songs/414 distinct titles in a 233,820-byte XML asset. Current legacy target is API 19.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle / constraint | Pre-research gate | Post-design gate |
|---|---|---|
| Preserve mission | Pass: offline lyrics and core flows are required | Pass: data/preference/UI contracts and device checks preserve them |
| Incremental modernization | Pass: no rewrite | Pass: build checkpoint before scoped compatibility work; no language conversion |
| Compatibility and offline reliability | Pass: supported range must be explicit | Pass: preserve API 14 floor; API 37 endpoint; blocked tests cannot pass |
| Simple architecture | Pass: existing Activities and data model retained | Pass: single module, explicit source sets, no framework/database additions |
| Quality through verification | Pass: build alone cannot satisfy acceptance | Pass: local tests, lint, artifact checks, IDE and runtime evidence are mandatory |
| Deprecated APIs when touched | Pass: scoped migration, not blanket replacement | Pass: current API paths plus documented guarded legacy fallbacks where necessary |
| Chinese readability/accessibility | Pass: no loss of core usability | Pass: both themes, font sizes, system text scale, sidebar and content insets checked |
| Review and justified changes | Pass: parent retains review authority | Pass: implementation records compatibility reason and evidence for each source change |

No constitutional exception is requested. The existing ratification-date TODO is unrelated and
unchanged. These are design compliance results, not claims that app tests have passed.

## Project Structure

### Documentation (this feature)

```text
specs/001-modernize-build-system/
├── spec.md
├── checklists/requirements.md
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── compatibility.md
└── tasks.md                  # Future /speckit.tasks output; not generated here
```

### Source Code (repository root)

Planned structure after implementation; new build/test paths below do not exist yet:

```text
settings.gradle               # Declares :app and approved repositories
build.gradle                  # Pins AGP
gradle.properties             # Project-wide portable settings
gradlew
gradlew.bat
gradle/wrapper/
├── gradle-wrapper.jar
└── gradle-wrapper.properties  # Versioned URL and distribution SHA-256
app/
├── build.gradle              # Namespace, SDKs, Java options, explicit legacy source sets
└── src/test/java/            # JUnit characterization tests
AndroidManifest.xml           # Retained manifest path; modern component declarations
src/com/goodtrendltd/HolySongs/ # Existing Java application sources
res/                          # Existing layouts, menus, styles and strings
assets/songs.xml              # Unchanged bytes
libs/pinyin4j-2.5.0.jar        # Unchanged bytes
README.md                     # Setup, build/install/signing guidance
```

**Structure Decision**: `app/build.gradle` maps main manifest to `../AndroidManifest.xml`,
Java to `../src`, resources to `../res`, and assets to `../assets`; runtime dependency points
to `../libs/pinyin4j-2.5.0.jar`. Tests use standard `app/src/test/java`. Never include `../gen`.
This keeps the migration small and permits standard `:app:*` tasks in the IDE and CLI.

After the modern build checkpoint, retire tracked `gen/`, Ant `build.xml`, `ant.properties`,
`project.properties`, and unused legacy ProGuard configuration. Untrack `local.properties`
without deleting a developer's local file. Preserve the user's existing `.gitignore` entries
while excluding local configuration, `.gradle/`, all module build outputs, and signing secrets.
Do not use blanket JAR ignores that would hide the wrapper or the retained pinyin JAR.

## Delivery Sequence

1. **Baseline and environment**: Capture source asset/JAR hashes and behavior fixtures;
   establish JDK/SDK/device prerequisites and record missing access. No release keys required.
2. **Build checkpoint**: Add pinned Gradle files/wrapper/source sets, identity and SDK config,
   exported declarations, and resource-ID branch fixes. Generate a debug APK and local tests.
   Do not claim a runtime checkpoint from compilation alone.
3. **Compatibility checkpoint**: Fix lifecycle, connectivity, Back, dialog, and modern layout
   failures required by the contracts. Keep data formatting and collation semantics. Audit
   video endpoints; stop for approval before any replacement/removal or insecure workaround.
4. **Verification checkpoint**: Run the clean-checkout build, IDE import/build, tests/lint,
   packaging comparisons, preference fixtures, and all device flows. Record blockers honestly.
5. **Cleanup and handoff**: Retire obsolete build artifacts, finish README and verification
   evidence, rerun from clean checkout, and complete parent review of source changes and scope.

The main agent owns design, task assignment, source review, and acceptance. Implementation may
be delegated to `github-copilot/mai-code-1.1-flash` in bounded, dependency-ordered slices with
one writer per working tree. No implementation subagent is launched by this plan. Stop on
runner/tooling failure rather than silently substituting a model or execution mode.

## Validation and Acceptance Gates

- `./gradlew --version` identifies Gradle 9.6.0 and a configured JDK 17 runtime.
- `./gradlew clean :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` succeeds.
  JVM characterization tests cover the actual XML parser success path, raw entry counts/order,
  duplicate source entries, representative source text, pinyin and comparator properties.
  Device tests exercise Activity-owned loading, ASCII-space removal, last-entry-wins map behavior
  and rendering against recorded baseline expectations; do not reimplement that pipeline in JVM
  tests and claim it validates the app. No new extraction or testing framework is required.
  Documented native Chinese collation differences between Android versions are acceptable under
  FR-008; sidebar positions and title-to-lyric selection must still meet each runtime's baseline.
- Review every lint error. Fix compatibility errors; narrowly document unrelated pre-existing
  findings if baselined. No blanket suppression or baseline of a required flow failure.
- APK identity, minimum/target SDK, bundled assets and JAR source checksum meet the contract.
  Verify legacy-compatible debug signing for API 14; modern-only signature schemes are insufficient.
- API 14 and API 37 core flows all pass, with recorded process-stopped launch timing, scroll
  observations, and zero app crashes/ANRs. The current endpoint also passes large-screen resizing
  and system Back checks. Missing device access or required evidence blocks completion.
- Every video provider has a recorded outcome. Approved provider limitations cannot excuse app
  crashes, trapped navigation, lost offline use, or silent provider removal.
- Developer instructions are exercised from a clean checkout, without undocumented tracked
  machine-specific edits; Android Studio import/build is separately verified.
- Release-signing guidance clearly distinguishes debug installs, representative preference tests,
  and genuine in-place upgrades; existing keys are neither assumed available nor replaced.

## Complexity Tracking

No gate violations or additional architectural complexity require an exception.

## Execution Risks and Handoff

The host currently lacks Java/Android commands on PATH and configured SDK environment variables.
Provisioning is an implementation prerequisite. An API 14 runtime is required even if the modern
emulator cannot run an old image; use an available compatible device/emulator or record the gate
as blocked and seek a support-policy decision. Do not quietly raise minSdk.

Third-party video viability and pinyin4j licensing provenance remain audit/distribution risks.
No public release is authorized. See [quickstart.md](quickstart.md) for executable validation
steps and [contracts/compatibility.md](contracts/compatibility.md) for evidence requirements.
