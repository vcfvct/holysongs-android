# holysongs-android
Automatically exported from code.google.com/p/holysongs

My Chinese hymns Android app. Check it out in Google play store :)

## Project background

This repository contains a lightweight, offline-first Android app for browsing and reading Chinese hymn lyrics. The modernization work keeps the original Java app structure, bundled song catalog, application identity, and preference semantics while replacing the legacy Ant/Eclipse build with a maintained Gradle-based workflow.

## Current status

Historical working-tree checkpoint (before the additional partial US2 tests):

- JDK 17 and Gradle 9.6.0 were used successfully
- `./gradlew clean :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --no-build-cache --rerun-tasks` completed with `BUILD SUCCESSFUL`
- unit tests: 2 tests, 0 failures, 0 errors, 0 skipped
- lint: 0 errors, 25 warnings
- APK hash: `df6c6d66d395c23e284091e15c2f0dc378e5076a48168ee30ac2fa909a02f174`

Not executed or blocked and therefore not claimed as passed:

- clean exact-revision checkout from a fresh Git checkout
- exact-source Android Studio Quail 4 sync/build proof (owner reports build/simulator execution succeeded; artifact, IDE version and tested-flow provenance remain incomplete)
- dedicated API 14 and API 37 device install/launch validation
- provider audit and video-path validation

Production signing, release-key generation and publishing are excluded from this feature, not required checks awaiting execution.

This is a draft build-migration checkpoint, not complete feature acceptance. See
[`pr-checkpoint.md`](specs/001-modernize-build-system/evidence/pr-checkpoint.md) for the latest review and verification record. T018–T020 contain partial preparation only and remain unchecked.

## Toolchain and compatibility

- AGP: 9.4.0
- Gradle: 9.6.0
- JDK: 17
- Java for the app: 8 source/target compatibility
- Android SDK: `platforms;android-37` and `build-tools;36.0.0`
- minimum Android API: 14; compile/target API: 37 (not a maximum-installation restriction)
- Android Studio: Quail 4 (2026.1.4)
- Kotlin: the project keeps Java-only builds and the Kotlin opt-out remains visible. The opt-out is already deprecated in AGP 9.4 and scheduled for removal in AGP 10; it is kept visible rather than suppressed.

Android Studio itself may run on its bundled JBR runtime; the project still requires a separate local JDK 17 selected as the Studio Gradle JDK. The IDE runtime and the Gradle JDK are distinct settings. Keep machine-specific Java discovery paths in your user-local Gradle configuration, not the project's `gradle.properties`. A locally generated `gradle/gradle-daemon-jvm.properties` can override daemon selection; verify both launcher and daemon JVM information rather than relying on `JAVA_HOME` alone. No daemon-JVM criteria file is included in this checkpoint.

## Local prerequisites

- Linux x86_64, Bash, Git, Python 3, and unzip
- Network access for initial downloads and dependency resolution
- Local JDK 17 installed under a developer-owned path
- Android SDK with `platforms;android-37`, `build-tools;36.0.0`, and `platform-tools`
- Android Studio Quail 4 with the Gradle JDK set to the same local JDK 17
- Dedicated API 14 and API 37 devices or emulators for runtime validation
- `adb` available via `platform-tools` on PATH
- Human review and acceptance of Android SDK licenses before invoking `sdkmanager` package installs

The official command-line tool setup is documented here:

- https://developer.android.com/studio/command-line
- https://developer.android.com/studio/command-line/sdkmanager

If the command-line tools are missing, provisioning is blocked until the developer installs them locally. Missing `sdkmanager` does not invalidate a build that is already succeeding with an installed SDK; it simply means the full provisioning path is still pending.

## Session-local environment setup

Run from the repository root. Adjust the example JDK/SDK paths below to your installations. Use session-local variables and do not commit a machine-specific `local.properties` or any personal SDK path. A stale `local.properties` overrides `ANDROID_HOME`; use the temporary override below, or intentionally set `sdk.dir` in your ignored local file.

```bash
export JAVA_HOME="$HOME/.local/tools/jdk-17"
export ANDROID_HOME="$HOME/Android/Sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
```

Use a self-contained temporary override for one command instead of leaving a changed file on disk:

```bash
(
  set -eu
  tmpdir=$(mktemp -d)
  cleanup() {
    if [ -f "$tmpdir/local.properties" ]; then
      cp -p "$tmpdir/local.properties" local.properties
    else
      rm -f local.properties
    fi
    rm -rf "$tmpdir"
  }
  if [ -f local.properties ]; then
    cp -p local.properties "$tmpdir/local.properties"
  fi
  # Arm restoration only after a successful backup; never delete an original
  # file if copying it failed.
  trap cleanup EXIT
  printf 'sdk.dir=%s\n' "$ANDROID_HOME" > local.properties
  ./gradlew --version
  ./gradlew clean :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --no-build-cache --rerun-tasks
)
```

This keeps the repository free of global changes and leaves the original file content alone unless the developer intentionally wants a local override for an active shell session.

## Build commands

For a checkout with no `local.properties`, or with a correct ignored `sdk.dir`, use these commands directly. If the original stale file remains, use the complete temporary-override block above instead; it runs the same build and restores the original immediately afterward.

```bash
export JAVA_HOME="$HOME/.local/tools/jdk-17"
export ANDROID_HOME="$HOME/Android/Sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"

./gradlew --version
./gradlew clean :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --no-build-cache --rerun-tasks
```

If the local SDK is already installed and complete, this path can still work without a global Gradle install. The project does not require a global installation and does not auto-provision missing Android tool packages.

## Artifact verification

```bash
APK=app/build/outputs/apk/debug/app-debug.apk
sha256sum "$APK" assets/songs.xml libs/pinyin4j-2.5.0.jar
unzip -p "$APK" assets/songs.xml | sha256sum
"$ANDROID_HOME/build-tools/36.0.0/aapt" dump badging "$APK"
"$ANDROID_HOME/build-tools/36.0.0/apksigner" verify --verbose --print-certs --min-sdk-version 14 "$APK"
```

Check the following:

- package is `com.goodtrendltd.HolySongs`
- minSdk is `14` and targetSdk is `37`
- version code/version name remain `8` / `2.5`
- packaged song asset hash matches the source asset hash in project evidence
- vendored `libs/pinyin4j-2.5.0.jar` stays trackable and unchanged unless separately approved
- v1 signature compatibility is present, not just v2/v3 signing output

## Install and launch on dedicated devices

Use explicit device selection with `adb -s <serial>` and do not uninstall an existing production app to get past a signature mismatch.

```bash
export ANDROID_SERIAL=your-test-device-serial
adb devices -l
adb -s "$ANDROID_SERIAL" shell getprop ro.build.version.sdk
adb -s "$ANDROID_SERIAL" install -r app/build/outputs/apk/debug/app-debug.apk
adb -s "$ANDROID_SERIAL" shell am start -n com.goodtrendltd.HolySongs/.MainActivity
```

Required validation caution:

- use dedicated API 14 and API 37 targets only
- inventory device state before installation
- do not substitute a different API level
- stop on signing conflicts rather than uninstalling a production app
- keep the app offline for first-launch verification when a local catalog run is required

## Troubleshooting and missing tools

Missing Java or Android tools:

```bash
java -version
which adb
which sdkmanager
ls "$ANDROID_HOME"
```

If the Android SDK is not fully installed, review the applicable license terms yourself before installing packages. The following are manual provisioning instructions, not commands executed by this migration:

```bash
sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-37" "build-tools;36.0.0"
```

If `sdkmanager` is not present, the local environment is still valid only if the required SDK components already exist. Missing command-line provisioning is a local setup blocker, not a product success claim.

## Release signing and upgrade boundaries

- This project does not generate or publish release keys
- debug APKs are for local testing only
- a genuine in-place upgrade requires compatible signing identity, reviewed version progression, and explicit maintainer approval
- no new runtime permissions, SDK range changes, or provider replacements are accepted without clear review

## Evidence and dependency risk

Use the curated project evidence and review records:

- `specs/001-modernize-build-system/evidence/recovery.md`
- `specs/001-modernize-build-system/evidence/us1-build.md`
- `specs/001-modernize-build-system/evidence/review.md`
- `specs/001-modernize-build-system/evidence/handoff-blocker.md`
- `specs/001-modernize-build-system/evidence/dependencies.md`
- `specs/001-modernize-build-system/plan.md`
- `specs/001-modernize-build-system/quickstart.md`

The vendored `libs/pinyin4j-2.5.0.jar` remains a retained dependency and its upstream license provenance is part of the project risk record rather than a claim of legal clearance.

Third-party video provider behavior remains an audit topic rather than a passed validation result.
