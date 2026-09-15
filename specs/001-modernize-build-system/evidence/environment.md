# Environment Inventory

Status: T001 complete; T002 partially complete (JDK17 installed locally) but blocked on Android SDK CLI provisioning pending owner approval; T003 complete with device blockers.

## Scope
- Repository: `/home/hanli3/GIT/holysongs-android`
- Branch: `master`
- Original HEAD: `634f98624b654fe9926c7b1c8186fbad56df6673`
- Feature: `001-modernize-build-system`
- Date: 2026-09-15

## Git state (historical setup inventory, not current PR state)
- `git status --short --branch` output after setup:
  - `## master...origin/master`
  - `?? .gitignore`
  - `?? .specify/`
  - `?? specs/`
- No tracked file modifications or staged changes are present in this working tree.
- Existing untracked `.specify/` and planning documents are preserved. `.gitignore` retains its original `.pi/` entry with additive protections; new evidence files are added under `specs/`. No app/build source files were edited.

## Recoverable pre-change checkpoint
- Parent captured all tracked and non-ignored untracked files before the setup writer started, without committing, stashing, cleaning or altering the index.
- Private local archive: `/tmp/holysongs-pre-implementation.F6XO97/worktree.tar.gz`.
- Archive SHA-256: `0afeb1cb2f5ab1d6ef10c0797e63e4737ae3ee5f3dce716c78ef549a6ad3e4d1`.
- The same private directory contains `status.txt`, `files.list`, `unstaged.patch` and `staged.patch`. Both patches were empty at capture. The archive includes the original untracked planning work and is a local recovery aid, not clean-checkout or build evidence. Do not commit or publish it; temporary-directory retention is not guaranteed.

## Ignore-file verification
- Required early verification confirms `.gitignore` retains the existing `.pi/` entry and adds the minimal Android/Java local build and signing exclusions required for this stage.
- The added patterns do not hide the wrapper or the vendored `libs/pinyin4j-2.5.0.jar` file and do not retire legacy files or `local.properties`.
- Verified via `git check-ignore -v` against representative paths described below.

## Java and toolchain status (historical setup inventory)
- `java`, `javac`, `adb`, `gradle` are not on the active `PATH`.
- `JAVA_HOME`, `ANDROID_HOME`, and `ANDROID_SDK_ROOT` are unset in the current environment.
- Existing JDK search:
  - `/usr/lib/jvm` was empty or absent.
  - `/opt/android-studio/jbr/bin/java` exists and reports `openjdk version "25.0.3"` (`OpenJDK Runtime Environment (build 25.0.3+-15898627-b508.16)`).
  - `/opt/android-studio/jbr/bin/javac` exists and reports `javac 25.0.3`.
  - A checksum-verified, user-local JDK 17 is now installed at `$HOME/.local/tools/jdk-17` from the official Temurin 17.0.14+7 release, downloaded from: `https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.14%2B7/OpenJDK17U-jdk_x64_linux_hotspot_17.0.14_7.tar.gz`.
  - Verified archive SHA-256: `a3af83983fb94dd7d11b13ba2dba0fb6819dc2caaf87e6937afd22ad4680ae9a`.
  - Verified local runtime: `/home/hanli3/.local/tools/jdk-17/bin/java -version` -> `openjdk version "17.0.14"` and `/home/hanli3/.local/tools/jdk-17/bin/javac -version` -> `javac 17.0.14`.
- The downloaded patch is an older official JDK17 release; its checksum and runtime were verified, but it is not claimed to be the latest security patch. Current-patch availability was not established by this setup slice.
- Studio supplies its own JBR; IDE execution has not been verified. Gradle will use the separate local JDK17, not the bundled JDK25.

## Android Studio identity and SDK inventory (setup history plus later AVD observation)
- Android Studio installation: `/opt/android-studio`
- Build identity in `/opt/android-studio/build.txt`: `AI-261.26222.65.2614.16204760`
- Selected IDE: Quail 4 (2026.1.4). The installed build ID above was inspected, but the owner's reported run has not yet been tied to an About/version capture; the plan alone does not verify that mapping.
- Studio runtime uses JetBrains JBR 25.0.3, which is a build/runtime aid but not the project-required JDK 17.
- Android SDK root exists at `$HOME/Android/Sdk`.
- Installed SDK content observed by inventory (read-only, no package installation invoked):
  - `platforms/android-37.0`
  - `build-tools/36.0.0`
  - `platform-tools/adb`
  - `emulator/emulator`
  - `licenses/`
- `cmdline-tools` is not present in `$HOME/Android/Sdk`, so `sdkmanager` is unavailable at the command line.
- `adb` binary is present and reports: `Android Debug Bridge version 1.0.41` and `Version 37.0.1-15733141`.
- `emulator` binary is present and reports: `Android emulator version 37.1.11.0`.
- Local AVD inventory: `/home/hanli3/.android/avd/Pixel_10_Pro_XL.avd/config.ini` reports `target=android-37.1` and `tag.id=google_apis_playstore`, so a current-target simulator image is present, but it has not been used for a full app acceptance run in this environment.
- Owner-reported runtime note: Android Studio build and simulator execution succeeded in the owner's environment, but this is not equivalent to complete API14/API37 acceptance, because the exact device/API matrix, app install flow, and required end-to-end runtime checks remain unverified in the recorded evidence.
- Parent verified protections using `git check-ignore --no-index -v` for `.gradle/cache`, `app/build/test.apk`, `app/out/test.class`, `target/test`, `bin/test`, `gen/test`, `local.properties`, `debug.keystore` and `.env.local`.
- At initial setup, wrapper files did not yet exist and local.properties was still tracked. Those statements are historical: wrapper inputs now exist, and local.properties untracking is staged at the pre-PR checkpoint. See `staging-policy.md` and `pr-checkpoint.md` for later state.

## T002 status
Status: partially satisfied for JDK17; blocked on Android SDK command-line provisioning pending explicit owner approval.

Completed:
- A checksum-verified JDK 17 is installed under `$HOME/.local/tools/jdk-17` and passes `java -version` and `javac -version`.

Remaining blockers:
- Android command-line tools are not installed (`cmdline-tools` absent) and the SDK license approval step has not been executed.
- Per instructions, `sdkmanager --licenses` is intentionally not invoked without owner approval.
- `ANDROID_HOME`/`ANDROID_SDK_ROOT` are unset in the parent environment; session-local exports using existing installations are authorized and require no global configuration changes.

Portable configuration examples (not yet applied globally):
```bash
mkdir -p "$HOME/.local/tools"
export JAVA_HOME="$HOME/.local/tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$HOME/Android/Sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
```

This is session-local environment configuration only; no global shell startup files were edited.

Setup inventory and local JDK17 provisioning are complete, but T002 remains incomplete because SDK CLI provisioning and full IDE/toolchain verification are outstanding. Baseline capture and test preparation may proceed under parent review. Missing SDK CLI tools do not by themselves prove that Gradle cannot use installed SDK components; the parent may authorize a build attempt with existing tools. No new SDK license may be accepted, and no blocked IDE/device/build check may be reported as passed.

## US2 characterization note
- Source-only US2 characterization checks were added under `app/src/test/java/com/goodtrendltd/HolySongs` and `app/src/test/java/com/goodtrendltd/HolySongs/helpers` to preserve the original catalog and helper behavior under a local JVM harness.
- These checks are explicitly not a runtime acceptance signal for API14/API37 devices or end-user app behavior. No device install, launch, or UI acceptance claim is being made.
