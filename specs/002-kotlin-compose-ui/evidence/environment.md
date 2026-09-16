# T001 — Setup inventory and blocked execution

**Recorded:** 2026-09-15 (America/New_York), starting 21:05.
**Status:** blocked; T001 remains unchecked. No dependent implementation task was executed.
This is a recoverable setup checkpoint, not build, IDE, device or feature acceptance.

## Source and recovery boundary

- Branch: `master`; HEAD: `f62519b4bff420ab3f78b1d544732c25683f4772`.
- Existing tracked modifications: `res/layout/main.xml`,
  `specs/001-modernize-build-system/spec.md`, and
  `specs/001-modernize-build-system/tasks.md`.
- Existing untracked entries: `.specify/.gitignore`, `.specify/init-options.json`,
  `.specify/integration.json`, `.specify/integrations/`,
  `.specify/memory/.constitution-template.json`, `.specify/scripts/`,
  `.specify/templates/`, `.specify/workflows/`, `AGENTS.md`,
  `gradle/gradle-daemon-jvm.properties`, and `specs/002-kotlin-compose-ui/`.
- Ignored `local.properties` exists and points to the inventoried local SDK. Its contents
  and the owner's daemon criteria/layout/feature001 changes were preserved.
- Before edits, created a private recovery checkpoint under
  `$HOME/.local/state/holysongs-android/checkpoints/20260915T210616/`.
  It contains `workspace.tar.gz`, `files.sha256.json`, `tracked.patch`, `status.txt`, and
  `revision.txt`. The archive contains all existing tracked and non-ignored untracked files,
  plus `local.properties`; generated build/cache directories and Git internals are excluded.
- Verified all 100 archived files against individual SHA-256 values by reopening the archive.
  Archive SHA-256: `8293a7570c2aa74d30baeb2ecba0fb944c444df5dbebb127330c74fd6a7a8cb8`.
  Directory permissions are 0700 and checkpoint files 0600. This is local recovery evidence,
  not a published artifact or a replacement for repository history.
- Recovery: inspect/extract the archive into a separate private directory, compare its manifest
  and patch, and restore only deliberately selected files. Do not blanket-extract over subsequent
  owner work. No stash, reset, branch creation, commit or publication was performed.

## Prerequisites and project setup

The prerequisite script returned the feature directory `specs/002-kotlin-compose-ui` under
this repository and available docs `research.md`, `data-model.md`, `contracts/`,
`quickstart.md`, and `tasks.md`. No feature checklists directory or extension registry existed.
Read the full tasks/plan/spec/research/data-model/quickstart/contracts/transition and constitution.
The task graph requires T001 before T002/T003, then the serial foundation/build/baseline gates
before story replacement. No completed phase or story is claimed.

Git detection succeeded. No Docker, ESLint, Prettier, npm, Terraform or Helm configuration was
found outside generated/cache directories. Existing `.gitignore` already covered Kotlin/Java
build outputs, local SDK configuration, IDE files, logs, environment files, signing secrets and
universal temporary files. Added only `*.jar`, with explicit exceptions for the required Gradle
wrapper and vendored pinyin JAR. No other ignore files were needed.

## Observed toolchain

| Check | Observed result | Interpretation |
|---|---|---|
| PATH `java -version` | Homebrew OpenJDK 17.0.20.1 | JDK17 launcher available |
| Existing alternate local JDK | Temurin 17.0.14+7 | Another JDK17 installation available |
| Studio product metadata | AndroidStudio2026.1.4; build 261.26222.65.2614.16204760 | Installed expected IDE series; sync/build unexecuted |
| Studio bundled runtime | OpenJDK 25.0.3, build 25.0.3+-15898627-b508.16 | IDE runtime, not the required Gradle JDK |
| `./gradlew --version` | Gradle9.6.0, launcher17.0.20.1 | Command exited successfully, but required JVM pairing did not pass |
| Same command: daemon criteria | Compatible with Java25, from local `gradle/gradle-daemon-jvm.properties` | **Blocks required JDK17 daemon setup**; this output is criteria, not proof of a launched daemon |
| Same command: Kotlin banner | 2.3.21 | Gradle embedded Kotlin; not evidence of the app compiler/Compose plugin version |
| SDK platform metadata | Android17, API37.0, revision2, extension22, preview0 | Installed under SDK `platforms/android-37.0`; build resolution for compileSdk37 not yet tested |
| Build Tools metadata | 36.0.0 | Required installation present |
| platform-tools metadata | 37.0.1 | adb available via explicit SDK path, not initial PATH |
| SDK command-line tools | No sdkmanager on PATH or in inspected SDK installation inventory | Provisioning unavailable through the documented command; no install attempted |
| SDK license inventory | `android-sdk-license` file exists | Presence only; no assertion of complete applicable license acceptance |

No global or user-local Java/Gradle configuration was changed. SDK paths were used explicitly
for read-only inventory; neither SDK packages nor licenses were installed/accepted. Merely
setting JAVA_HOME would not override the existing Java25 daemon criteria.

## Early device availability observations (not completion of T003)

- SDK adb `devices -l` returned an empty device list.
- Emulator `-list-avds` listed only `Pixel_10_Pro_XL`.
- Its configuration targets `android-37.1` with a Play Store/16KB-page x86_64 image.
  This is static AVD configuration, not observed runtime API/build/WebView evidence.
- No API23 AVD was listed. No dedicated target ownership, disposable preference/first-install
  isolation, share recipient/no-recipient profile, gesture/three-button setup, or large/resizable
  target was verified. Runtime build and WebView identifiers remain unknown.
- No emulator was booted, no application installed, no preferences/data cleared, and no unrelated
  app disabled. Missing runtime targets will block T010 and later required device gates.

## Blocker and next action

**T001 stops here:** the required JDK17 daemon configuration conflicts with the owner's existing
local Java25 criteria, which the task explicitly requires preserving. Ask the owner to select
JDK17 for Gradle through a deliberate local change, or approve a reversible isolated validation
setup that excludes the Java25 criteria while preserving the original file. Verify actual
launcher/daemon selection and IDE Gradle JDK afterward; do not treat the IDE's JBR25 as a defect.

Before device work, the owner must identify disposable API23/API37 targets and authorize their
selection, including the required current-runtime navigation/window configurations. Missing SDK
provisioning or licenses require owner-managed installation/review, not automatic acceptance.

No APK was produced or selected for acceptance. No application compilation, unit tests, lint,
IDE sync/build, instrumentation, runtime scenarios or provider probes were executed in this run.
T001–T046 remain unchecked. T002/T003 and foundation work have not been advanced past T001.

## Closing verification

- Compared every checkpoint file to the current workspace: only `.gitignore` changed among
  the 100 originals. This new evidence file is the only additional deliverable.
- Source song and pinyin JAR SHA-256 values still match data-model.md exactly.
- `git check-ignore --no-index` ignores a sample generated JAR, but not the required wrapper
  or pinyin JAR. The targeted `.gitignore` whitespace check passes.
- Full `git diff --check` reports pre-existing trailing whitespace in the owner's
  `res/layout/main.xml` lines6/8–12. Preserved unchanged rather than silently cleaning it.
- Rechecked `.specify/extensions.yml` after execution: absent, so no post-hooks are registered
  for dispatch. No checklist markers or task checkboxes were modified.

## Follow-up — owner-authorized Java25 / disposable Pixel (2026-09-15, 21:27)

The owner said "ignore the java 25, there's a pixel 10 pro xl virtual device in android studio,"
and then confirmed "yes, it is disposbale" when asked about debug installation and preference tests.
For this local implementation run, retain existing Java25 daemon selection and record actual
versions instead of blocking setup on JDK17-only wording. This is a narrow owner exception, not
permission to change compiler/plugin versions, application JVM8 targets, minSdk, global config,
licenses, or production-device data. Final reports must disclose the exception; a Java25 run
must not be described as proof of the original JDK17-only B1 contract. The app's declared Java17
compilation toolchain remains unchanged. No broad planning/historical evidence rewrite was made.

A new pre-edit checkpoint includes the additional owner MainActivity.java edit:
`$HOME/.local/state/holysongs-android/checkpoints/20260915T212750/`.
All 101 archived files were verified. Archive SHA-256:
`b7806cda7689dbda22c68505d9e93c2b60807f2945527585313b686e79c14d2e`.
Recovery files and privacy permissions follow the earlier checkpoint procedure above.

`./gradlew help --info` succeeded (880ms) using the existing daemon. Its attributable
DefaultDaemonContext reports Java25, JetBrains vendor, Studio bundled JBR, PID1302885;
launcher remains Homebrew JDK17.0.20.1. This proves configuration execution with the approved
local exception, not application compilation or IDE sync/build. Raw command output is local-only
at `/tmp/holysongs-002-gradle-help.log`. The original daemon criteria/local.properties remain intact.

**T001 setup inventory/checkpoint is now complete under the explicit local Java25 exception.**
Previously recorded blocked outcomes above remain historical. T002 recording conventions are in
README.md and T003 selected-device inventory/remaining matrix blockers are in devices.md.
Neither missing API23 evidence nor original JDK17 verification is silently converted to a pass.

The new source edit is a trailing `1` after `private boolean mShowing;` in MainActivity.java:36.
It is outside the current test-preparation changes and has been preserved in the checkpoint and
working tree. It is an invalid Java member declaration and will block normal application compilation
unless the owner removes it or authorizes its correction. No other reader implementation changed.
