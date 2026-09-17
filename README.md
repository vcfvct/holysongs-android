# holysongs-android
Automatically exported from code.google.com/p/holysongs

My Chinese hymns Android app. Check it out in Google play store :)

## Project background

This repository contains a lightweight, offline-first Android app for browsing and reading Chinese hymn lyrics. The modernization work keeps the bundled song catalog, application identity, and preference semantics while replacing the legacy Ant/Eclipse build with a maintained Gradle-based workflow. The app module now uses the standard Android layout under `app/src/main/` for its manifest, Kotlin sources, resources, and assets.

## Current status

The Kotlin/Compose modernization phase is complete and owner-accepted. Main, lyric, Settings,
and About now use Compose hosts, while the Java WebView/video integration and catalog helpers
remain intentionally retained boundaries. The migration preserves the application identity,
bundled catalog, legacy preferences, share payloads, and provider identities.

The supported install floor is API 23: API 14–22 devices are no longer eligible to install/update
this build. On 2026-09-16 the owner reported completing phone testing across the app and accepted
the result as looking great. The final PR candidate also passes a clean CLI debug build, JVM tests,
lint, and androidTest APK assembly. API23 runtime execution remains explicitly waived and the
tracked evidence retains narrower automated-test and provider-audit limitations; phase acceptance
does not claim release signing or Play Store publication.

Historical Feature 001 build evidence remains available in
[`pr-checkpoint.md`](specs/001-modernize-build-system/evidence/pr-checkpoint.md); it describes the earlier build checkpoint and is not evidence of this Compose migration's runtime acceptance.

## Toolchain and compatibility

- AGP 9.4.0; Gradle 9.6.0; host JDK 17
- App Java and Kotlin JVM targets: 11
- AGP built-in Kotlin 2.2.10, matching Compose compiler plugin 2.2.10, and Compose BOM 2026.09.00
- Android Studio Quail 4 (2026.1.4)
- Android SDK `platforms;android-37`, `build-tools;36.0.0`; minSdk 23, compile/target 37
- AndroidX is enabled. Espresso 3.7.0 is pinned to avoid the API 37 InputManager mismatch.

The launcher remains `com.goodtrendltd.HolySongs.MainActivity`; application identity and version remain `com.goodtrendltd.HolySongs`, versionCode 8 / versionName 2.5. The APK has the two legacy permissions (`INTERNET` and `ACCESS_NETWORK_STATE`) plus the AndroidX-generated app-scoped `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` protective permission. The added app-private permission has `signature` protection; no new dangerous/runtime permission is introduced.

Android Studio may use its bundled JBR, but Gradle must use a local JDK 17. On Homebrew, `JAVA_HOME` points to the JDK root's `libexec` (for example `$(brew --prefix openjdk@17)/libexec`), not the formula directory itself. Keep discovery paths in the shell/user-local configuration, never in tracked `gradle.properties`; do not copy a daemon-JVM criteria file.

## Local prerequisites

- Linux x86_64, Bash, Git, Python 3, and unzip
- Network access for initial downloads and dependency resolution
- Local JDK 17 installed under a developer-owned path
- Android SDK with `platforms;android-37`, `build-tools;36.0.0`, and `platform-tools`
- Android Studio with its Gradle JDK set to the same local JDK 17
- `adb` available via `platform-tools` on PATH (device execution is not claimed here)
- Human review and acceptance of Android SDK licenses before invoking `sdkmanager` package installs

The official command-line tool setup is documented here:

- https://developer.android.com/studio/command-line
- https://developer.android.com/studio/command-line/sdkmanager

If the command-line tools are missing, provisioning is blocked until the developer installs them locally. Missing `sdkmanager` does not invalidate a build that is already succeeding with an installed SDK; it simply means the full provisioning path is still pending.

## Session-local environment setup

Run from the repository root. Adjust the example JDK/SDK paths below to your installations. Use session-local variables and do not commit a machine-specific `local.properties` or any personal SDK path. A stale `local.properties` overrides `ANDROID_HOME`; use the temporary override below, or intentionally set `sdk.dir` in your ignored local file.

```bash
# Portable example; on Homebrew use "$(brew --prefix openjdk@17)/libexec".
export JAVA_HOME="/path/to/jdk-17"
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
# Use the same portable environment setup shown above.
./gradlew --version
./gradlew clean :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest --no-build-cache --rerun-tasks
```

Replay the app/test build offline without changing the lockfile:

```bash
sha256sum app/gradle.lockfile
./gradlew --offline :app:assembleDebug :app:testDebugUnitTest --no-build-cache --rerun-tasks
sha256sum app/gradle.lockfile  # must match the first hash; do not use --write-locks
```

If the local SDK is already installed and complete, this path can still work without a global Gradle install. The project does not require a global installation and does not auto-provision missing Android tool packages.

## Artifact verification

```bash
APK=app/build/outputs/apk/debug/app-debug.apk
sha256sum "$APK" app/src/main/assets/songs.xml
unzip -p "$APK" assets/songs.xml | sha256sum
"$ANDROID_HOME/build-tools/36.0.0/aapt" dump badging "$APK"
"$ANDROID_HOME/build-tools/36.0.0/apksigner" verify --verbose --print-certs --min-sdk-version 23 "$APK"
```

Check the following:

- package is `com.goodtrendltd.HolySongs`; version code/name remain `8` / `2.5`
- minSdk is `23` and targetSdk is `37`
- only `INTERNET`, `ACCESS_NETWORK_STATE`, and AndroidX's app-scoped protective dynamic-receiver permission are packaged
- packaged `assets/songs.xml` hash matches the source `app/src/main/assets/songs.xml` hash
- Maven Central dependency `com.belerweb:pinyin4j:2.5.0` remains pinned in the dependency lockfile
- v1 signature compatibility is present, not just v2/v3 signing output

## Install and launch

The modernization phase has owner phone acceptance and bounded automated runtime evidence.
For additional validation, use explicit device selection with `adb -s <serial>` on API 23 or
newer; API 14–22 are below this build's install/update floor. Do not uninstall an existing
production app to get past a signature mismatch.

```bash
export ANDROID_SERIAL=your-test-device-serial
adb devices -l
adb -s "$ANDROID_SERIAL" shell getprop ro.build.version.sdk
adb -s "$ANDROID_SERIAL" install -r app/build/outputs/apk/debug/app-debug.apk
adb -s "$ANDROID_SERIAL" shell am start -n com.goodtrendltd.HolySongs/.MainActivity
```

Inventory device state before installation, stop on signing conflicts, and keep a local catalog run offline when appropriate.

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

Pinyin processing uses the pinned Maven Central dependency `com.belerweb:pinyin4j:2.5.0`. Historical records under `specs/001-modernize-build-system/` document the formerly vendored JAR and remain unchanged as evidence of that earlier baseline.

Third-party video provider behavior remains an audit topic rather than a passed validation result.
