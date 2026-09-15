# Quickstart: Build and Validate the Modernized App

**Status:** Prospective validation guide. Build files and tests referenced here are implementation
deliverables; they do not exist yet. No commands below are claimed to have passed during planning.
Run from the repository root after implementation. Full acceptance is defined in
[contracts/compatibility.md](contracts/compatibility.md).

## 1. Prerequisites

- Linux x86_64, Bash, Git, Python 3 and unzip for the documented command-line path.
- JDK 17; set `JAVA_HOME` to your local installation and include its `bin` on PATH.
- Android Studio Quail 4 (2026.1.4); configure its Gradle JDK to the same JDK 17.
- Android SDK command-line tools, platform-tools, platform 37, Build Tools 36.0.0.
- Internet access for initial tool/dependency downloads. Review and accept SDK licenses yourself.
- A dedicated API 14 test device/working legacy emulator and an API 37 test environment.
  The current endpoint also needs phone navigation-mode and large-screen validation.
- A known share recipient on test devices. Never use a production installation as a disposable fixture.

Configure `ANDROID_HOME` to your local SDK, and put `platform-tools` and
`cmdline-tools/latest/bin` on PATH. Use local environment configuration or untracked
`local.properties`, not a committed machine path. The implementation README must document
installation prerequisites without requiring a global Gradle installation.

After provisioning command-line tools:

```bash
java -version
sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-37" "build-tools;36.0.0"
adb devices -l
```

Create/start an API 37 emulator through Android Studio Device Manager, selecting a supported
image for the host architecture. Prepare the API 14 endpoint separately; current emulator
packages are not assumed to run every legacy image. If that endpoint is unavailable, stop its
validation and record it as blocked rather than substituting a different Android version.

## 2. Clean-checkout Build

Use a fresh Git checkout of the implementation revision, with no copied untracked/ignored
configuration or generated outputs. Configure local prerequisites only through the documented
setup steps; an arbitrary working-directory snapshot is not clean-checkout evidence.
Record the revision and environment:

```bash
git rev-parse HEAD
git status --short
./gradlew --version
./gradlew clean :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Expected: Gradle 9.6.0 with JDK 17, all tasks exit successfully, nonzero characterization tests
executed, and `app/build/outputs/apk/debug/app-debug.apk` exists. Review lint output; undocumented
blanket suppressions do not meet acceptance. Test reports are under `app/build/reports/tests/`.

Open the same repository in Android Studio, verify wrapper/JDK selection, sync and build the
app debug variant. Record IDE version, sync result, and build result independently of CLI success.
A successful CLI build does not prove IDE compatibility.

## 3. Inspect the Artifact and Repository Inputs

```bash
APK=app/build/outputs/apk/debug/app-debug.apk
sha256sum "$APK" assets/songs.xml libs/pinyin4j-2.5.0.jar
unzip -p "$APK" assets/songs.xml | sha256sum
"$ANDROID_HOME/build-tools/36.0.0/aapt" dump badging "$APK"
"$ANDROID_HOME/build-tools/36.0.0/apksigner" verify --verbose --print-certs "$APK"
git status --short
```

Expected asset SHA-256 (both source and packaged):
`88eb0db602e018b49a327947dd8607f04e6159e58f39ec38ed59f20c39af9d89`.

Expected source pinyin JAR SHA-256:
`6576dea7d351a0f5df1595b9c432ba7cf9246ca0ab6f7019b9ca4e6d500b0e68`.
The JAR is compiled into the APK; do not expect the original JAR to be embedded as a file.

Badging must show package `com.goodtrendltd.HolySongs`, SDK 14, target 37, versionCode 8 and
versionName 2.5. Verify signing compatibility includes API 14 (v1 signature), not merely a
modern signature scheme. Check wrapper URL/checksum against the official Gradle distribution.
Local configuration and build outputs must not appear as newly tracked files.

## 4. Install Only on Dedicated Test Targets

Select each device explicitly; do not target an arbitrary connected phone:

```bash
export ANDROID_SERIAL=your-test-device-serial
adb shell getprop ro.build.version.sdk
adb shell getprop ro.build.version.release
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.goodtrendltd.HolySongs/.MainActivity
```

Expected: installation and launch succeed on API 14 and API 37. If an existing installation
has an incompatible signature, stop. Do not uninstall it to bypass the issue; use another
clean test target. For offline-first-launch testing, disable connectivity in device settings
before the first app launch on a fresh test installation.

## 5. Runtime Scenarios on Both Required Versions

Record artifact hash, device/API, steps, outcome and evidence for each scenario.

1. **Offline first launch:** With Wi-Fi and mobile data disabled, open the catalog and verify
   actual Activity loading produces 414 effective titles from the unchanged 422 entries. Check
   all eight duplicate-title winners against recorded baseline fixtures, long lyrics, Chinese
   text, line breaks and existing ASCII-space removal in actual displayed content. Raw XML/JVM
   parser checks do not prove these Activity-owned behaviors. Before navigation fixes, record
   expected sidebar positions for present/absent letters on each runtime; verify valid expected
   positions and correct title-to-lyric selection afterward. Documented native Chinese collation
   differences across Android versions are acceptable under FR-008; unresolved differences or
   changes to section semantics need maintainer approval.
2. **Settings:** Exercise both themes, every selectable font size (16 through 30 by twos), and
   reset to 20. Return to the list/lyrics, stop and relaunch the app, and confirm persistence.
   Test increased system font scale; essential controls must remain usable.
3. **Legacy preference fixture:** On a disposable debug test target, stop the app and use
   `adb shell run-as com.goodtrendltd.HolySongs` to place a well-formed SharedPreferences
   `appPrefFile.xml` containing integer `fontSize=28` and boolean `nightMode=false` in the
   app's `shared_prefs` directory. Launch without first opening settings; verify both values
   are honored. Repeat with `fontSize=16`, `nightMode=true`. Record fixture content/steps.
   Do not alter another user's preferences; this demonstrates identifier/value compatibility,
   not a production in-place upgrade.
4. **Lifecycle:** Open/close list and lyrics repeatedly, rotate, press Home, return, and use
   Back. Repeat after toggling the theme. There must be no crash, leaked overlay, blank stuck
   screen, or destroyed-WebView reuse. Capture relevant logcat evidence without user data.
5. **Sharing:** Send a selected lyric and app-install text to a known recipient; verify payloads.
   Exercise a missing-recipient environment where feasible and record any test blocker.
6. **About/settings:** Confirm both remain reachable and return navigation works.
7. **Video audit:** With network access, test each provider using a Chinese song title. Record
   configured URL, redirect/final URL, results or error, WebView version, and navigation/fullscreen
   behavior. Then repeat failure/cancellation and background/resume checks without network.
   Returning to offline lyrics must always work. No provider replacement/removal without approval.
8. **API 37 additions:** Test gesture and three-button Back, system-bar overlap, and a resizable
   large-screen configuration. Do not assume the portrait lock will be honored on large screens.
9. **Responsiveness evidence on each endpoint:** Stop the app process, launch it, and record
   elapsed time until the song list is visible and responds to scrolling/selection. State the
   timing method (for example, timestamped screen recording); this is a process-stopped launch,
   not a claim that filesystem caches were cleared. Traverse the catalog and record visible
   scrolling stalls, including an explicit "none observed" when appropriate. Check test-session
   logs and system indications for app crashes or ANRs (Application Not Responding events).
   Any app crash/ANR fails its scenario; missing observations block sign-off. No numerical
   launch/scroll threshold or benchmark suite is added. Repeat on the final artifact.

To relaunch without clearing preferences:

```bash
adb shell am force-stop com.goodtrendltd.HolySongs
adb shell am start -n com.goodtrendltd.HolySongs/.MainActivity
```

For logs, start `adb logcat` scoped to the test session and record reproducible timestamps.
Do not treat unrelated device/system warnings as app failures or collect unnecessary personal data.

## 6. Evidence and Sign-off

Create a sanitized verification summary during implementation using the record format in
[data-model.md](data-model.md). Record `passed`, `failed`, or `blocked` for every executed or
blocked required check; never mark an unrun check as passed. Link local raw evidence separately.

Before sign-off, confirm:
- The final APK passes the clean-checkout, IDE, automated, and both-endpoint runtime gates.
- All asset, identity, preference and sharing contracts are preserved.
- Every provider has an audit outcome and any limitation/change has recorded maintainer approval.
- No global security bypass, new permission, application rewrite, or secret entered the diff.
- README contains the tested setup, build/install, verification and future-signing instructions.

If tools, IDE, API 14 device, API 37 device, or required evidence are missing, the feature remains
incomplete. Record the blocker and ask for access or an explicit reviewed support-policy change.

## 7. Future Release Signing (Not Executed Here)

The debug APK is for testing. A genuine update to an installed production app requires the same
application ID, compatible signing identity or valid signing lineage, and appropriate versionCode
progression. Do not infer key availability from an old repository. Locate and manage historical
keys through the owner's secure process; never add them or passwords to this project. No release
signing key generation/replacement, production update, or store publication is part of this work.
