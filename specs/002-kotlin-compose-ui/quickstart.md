# Quickstart: Validate the Kotlin/Compose Migration

**Status:** Validation guide for the current dirty `master` implementation. Main, lyric, Settings,
and About now have Compose hosts; Java catalog helpers and the WebView/video boundary remain.
The checkout is not a clean/exact acceptance revision. Commands and scenarios below are targets
unless a dated evidence record identifies the exact artifact, device/API, and result. Current
review blockers include API23 execution, full lifecycle/reflow and share-recipient behavior,
complete provider audit, and final clean-revision acceptance. Do not infer those passes from a
successful compile or focused API37 run.

Before running a checkpoint, select its scope from
[contracts/ui.md](contracts/ui.md#checkpoint-applicability). US1 requires the Compose Main screen's
full behavior and the specified retained-screen safety/interoperability smoke, not full legacy
reader/settings/about restoration/reflow. US2 adds those core-screen gates; US3 adds the complete
video audit/safety checks; final acceptance requires everything. Keep later-scope checks unexecuted,
not falsely passed. Any migration-introduced regression or encountered safety failure blocks the
current checkpoint regardless of which later screen owns its eventual replacement.

## 1. Prerequisites and Safe Workspace

- Bash, Git, Python3, unzip, network for dependency downloads, local JDK17.
- Android Studio Quail4 / 2026.1.4; select JDK17 for Gradle independently of the IDE's bundled JBR.
- SDK platform37, Build Tools36.0.0, platform-tools/adb. Review SDK licenses yourself before
  installing missing SDK packages; this guide authorizes no unattended license acceptance.
- Dedicated API23 and API37 devices/emulators; an API37 large/resizable configuration and both
  phone navigation modes. Record API/build/WebView versions, not just emulator names.
- Dedicated fresh test profile/installation for first-launch and preference fixtures. Same app ID
  is used for debug: do not uninstall, clear data, or overwrite a real user's installation.
- For exact-revision replay use a fresh checkout with no copied ignored config/generated output.
  Keep any local setup ignored and documented. Do not disturb the owner's layout edit or other
  local files in the development checkout.

From the chosen repository checkout, set session-local paths to actual installations:

```bash
export JAVA_HOME="$HOME/.local/tools/jdk-17"
export ANDROID_HOME="$HOME/Android/Sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
git rev-parse HEAD
git status --short
./gradlew --version
```

Both Gradle launcher and daemon must use JDK17. An existing local.properties can override the SDK
environment; set it deliberately in the test checkout or use README's reversible local override.
A local `gradle/gradle-daemon-jvm.properties` may override JDK selection; do not copy the owner's
untracked file into the clean replay or silently change it. No global Gradle install is required.

## 2. Validate Dependency Resolution and Build

After Kotlin/Compose enablement is implemented:

```bash
./gradlew :app:buildEnvironment
./gradlew :app:dependencies --configuration debugRuntimeClasspath
./gradlew :app:dependencies --configuration debugAndroidTestRuntimeClasspath
./gradlew :app:dependencyInsight --dependency kotlin-stdlib --configuration debugRuntimeClasspath
./gradlew clean :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest
```

Review compile classpaths as well as runtime classpaths when establishing locks. Confirm AGP9.4,
KGP/compiler/plugin2.2.10 and the [selected library pins](research.md). Record any transitive
upgrades separately. Check resolved AAR metadata and merged app/test manifests against API23 and
compile37; do not force incompatible manifests or silently change versions. Record lockfiles once
the app's dependency locking configuration is implemented and the graph is reviewed.

Expected: mixed Java/Kotlin compilation, no duplicate Activities or uncompiled Kotlin sources,
passing actual JVM tests, successful instrumented APK assembly and reviewed lint. Existing partial
Java tests alone are insufficient: production-loader/index/whitespace/winner tests must run too.
Do not suppress broad lint categories to create a pass. Build reports remain under app/build.

## 3. Inspect APK Identity, Data and Signing

```bash
APK=app/build/outputs/apk/debug/app-debug.apk
sha256sum "$APK" assets/songs.xml libs/pinyin4j-2.5.0.jar
unzip -p "$APK" assets/songs.xml | sha256sum
"$ANDROID_HOME/build-tools/36.0.0/aapt" dump badging "$APK"
"$ANDROID_HOME/build-tools/36.0.0/aapt" dump permissions "$APK"
"$ANDROID_HOME/build-tools/36.0.0/apksigner" verify --verbose --print-certs --min-sdk-version 23 "$APK"
```

Check package/namespace `com.goodtrendltd.HolySongs`, launcher MainActivity, min23/target37,
version8/2.5 and non-exported internal Activities. Match build contract B2's exact app permission
allowlist: INTERNET, ACCESS_NETWORK_STATE and
`com.goodtrendltd.HolySongs.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` only. Inspect both the
permission declaration (`signature` protection) and its matching use, plus AndroidX Core origin
in the manifest-merger report; no other permission addition is accepted without review. Inspect
the merged manifest report as well as APK badging/permissions; do not strip the protective
signature permission to make an artificially smaller list. Verify source and packaged song hashes against
[data-model.md](data-model.md), and v1-compatible debug signing for API23.

## 4. Dedicated-Device Install, Test and Launch

Select and verify one dedicated device at a time. Stop on an unexpected device or signing conflict.
Only the selected test device should be connected when using connected-device Gradle tasks; verify
actual test targets in reports rather than assuming ANDROID_SERIAL filters every AGP operation.

```bash
export ANDROID_SERIAL=your-dedicated-test-device-serial
adb devices -l
adb -s "$ANDROID_SERIAL" shell getprop ro.build.version.sdk
adb -s "$ANDROID_SERIAL" shell getprop ro.build.fingerprint
adb -s "$ANDROID_SERIAL" install -r app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:connectedDebugAndroidTest
adb -s "$ANDROID_SERIAL" shell am start -n com.goodtrendltd.HolySongs/.MainActivity
```

Repeat for API23 and API37. Instrumented tests that change preferences must snapshot and restore
fixture state or run only on an explicitly disposable test profile. A real recipient is needed for
share delivery; use an explicitly configured no-recipient test profile for absence handling.
Do not disable unrelated user apps to create that case.

## 5. Establish Before/After Fixtures

Before replacing the list/index:

1. On a clean test installation of the baseline artifact, disable connectivity through device UI
   before first launch. Capture actual effective catalog order, all duplicate winners and A–Z
   destinations, including absent letters, on API23/API37 using test-only instrumentation/inspection.
2. Record process-stopped launch-to-Ready timing with a monotonic method (e.g. screen recording
   from launch action to visibly usable list), and visible scroll stalls. `am start -W` measures
   Activity launch, not necessarily async catalog readiness, so is not sufficient by itself.
3. Reproduce the reported top-bar overlap; record screenshot, status/app bar involved, window
   bounds, theme, API and navigation mode. Do not label an assumed cause as a reproduced defect.
4. Define exact raw-source/full-order/duplicate-pair fixtures and effective lyric expectations
   from predecessor baseline evidence. Test the actual extracted loader, not a copied pipeline.

Unavailable baseline execution must be recorded as blocked. Source inspection can identify a
hazard, but cannot supply invented runtime golden positions. Stop any lookup-semantic correction
that cannot be reconciled with baseline/approval.

## 6. Run Reader and State Scenarios

Follow all U1–U4 scenarios in [contracts/ui.md](contracts/ui.md). Minimum manual walkthrough:

1. Fresh offline launch → scroll → A–Z navigation → open representative and duplicate titles.
2. Reach the first/last list items, every rail letter and final line of a long lyric without clipping.
   Test fit-mode tap/drag selection with a stationary rail, then compact-mode tap selection and
   vertical drag-to-scroll of only the rail. During a compact drag verify the song list never jumps,
   including release; scroll to A/Z and tap to select. Test touch-slop boundary, pointer cancellation,
   Activity stop and resize across modes without extra selection or a stuck highlight.
3. Choose each app font size, reset20, switch both themes and relaunch. Verify stored values and
   actual UI; baseline fixture tests seed legacy Integer/Boolean values through test-only code.
   For migrated lyric/settings screens, run every data-model font fixture: missing, String24,
   Integer.MIN_VALUE/-1/0, 1/15/16/20/30/32/200, and201/Integer.MAX_VALUE. Expect unchanged
   rendering for integers1–200 and20sp otherwise, without raw key/type/value changes; effective
   non-choice values must not be automatically written as a menu selection. Verify only explicit
   choices/reset persist allowed values. The adapter is checked at US1; new rendering behavior is
   checked at US2 rather than imposed on the still-legacy reader during the pilot.
4. During the pilot, change legacy Settings and confirm Compose Main refreshes without a catalog
   reload. Later remove the stack-clearing workaround and confirm normal Back preserves position.
5. Rotate/recreate each screen, including Main while Loading; background/return, rapidly open/Back,
   and return from settings/about/video. No duplicate destination or repeated share launch.
6. For real process recreation on a disposable test task: open a lyric at a known scroll position,
   press Home, run the command below, verify the process ended, then return via Recents. Do not use
   force-stop as proof of saved-task restoration; test cold launch separately.

```bash
adb -s "$ANDROID_SERIAL" shell am kill com.goodtrendltd.HolySongs
adb -s "$ANDROID_SERIAL" shell pidof com.goodtrendltd.HolySongs
```

If the process remains alive or the task cannot be restored, record the scenario as unexecuted/
blocked rather than calling ActivityScenario.recreate a substitute. Instrumented recreation and
Compose saved-state tests remain separate checks.

7. On API37 use gesture and three-button Back, portrait/landscape, a display cutout, and a large
   resizable window. Test font scales1.0/1.3/2.0 per the UI contract; restore prior test-device settings.
8. Inspect standard control labels/roles in semantics tests and verify About text/link behavior.
   Full TalkBack journeys/custom-sidebar accessibility are explicitly deferred, not passed checks.

Inspect test-session logcat/crash buffer and app ANR indications for both endpoints, recording
method/time window and unrelated-process exclusions. Zero app crashes/ANRs are required in these
scenarios. Store raw logs privately; avoid committing other apps' logs or user data.

## 7. Sharing and Video

Run N1–N4 in [contracts/navigation-and-video.md](contracts/navigation-and-video.md):

- Confirm exact extras and lyric/app share payloads, then deliver to a real receiving app and
  exercise a safe no-recipient configuration.
- Attempt each provider with a representative Chinese title; record original/redirect/final URL,
  page/search result, WebView version, network type and limitations. HTTP success alone is not proof
  of working search or video playback.
- Test no-network/Wi-Fi-absent/mobile-data confirmation, cancel/error, fullscreen/history/Back,
  rotation/resizing and background/resume. Return to the selected offline lyric without a crash
  or trapped loading UI.
- Obtain owner approval for provider changes/accepted unavailability before applying them. Never
  bypass SSL, global cleartext policy or geolocation permission handling to make tests pass.

## 8. IDE and Final Exact-Revision Replay

Open the same clean implementation revision in Quail4, select JDK17 for Gradle, sync, build and
launch. Record IDE version, actual Gradle JDK, revision and APK hash separately from CLI evidence.
Repeat affected runtime scenarios on the final integrated artifact. New APK hashes require new
attribution; do not combine partial results from unrelated builds into an all-passing conclusion.

Write sanitized scenario records under this feature's future `evidence/` directory using the
[data-model record fields](data-model.md). Include provider approvals, dependency/upgrade risks
and unavailable checks. Reconcile README's old Java-only/API14 statements during implementation.

Full completion requires [build-and-verification.md](contracts/build-and-verification.md), U1–U4,
N1–N4 and owner decisions to be satisfied. API14 is historical, not a new acceptance endpoint.
A working list pilot is a checkpoint, not permission to mark the entire feature complete.
