# US3 retained Java video safety evidence

> **Historical runtime record:** The five-test API37 instrumentation result described in this
> document predates the current eight-test `VideoSearchTest` source and must not be used as
> evidence that the current test class passes. No current instrumentation run is claimed here.
> The current source-level UTF-8 contract is `VideoSearch.searchUrlFor` applying `Uri.encode`
> to the title query while preserving each provider prefix; current runtime verification remains
> unexecuted.
>
> **Provider change after this historical record:** On 2026-09-17 the owner approved replacing
> Bilibili with Douyin in [GitHub issue #13](https://github.com/vcfvct/holysongs-android/issues/13).
> The accepted prefix is `https://so.douyin.com/s?keyword=` and the target is `douyin`. The
> replacement compiles and its URL/encoding assertions compile, but no connected device was
> available for live Douyin WebView or playback verification; no provider-success claim is made.

## Scope and attribution

This record covers the bounded early-video-safety path (T034/T036 preparation and the
Java-path portions of T037–T039). It does not claim provider availability, a provider
transport change, or completion of the later provider audit (T035/T040/T041).

- Source revision: `f62519b4bff420ab3f78b1d544732c25683f4772` (dirty working tree; the
  implementation changes in this record are not committed).
- APK SHA-256 (build after these changes):
  `cdf2a7d1549117ec2035b6eb3d09bdd00dc7ab1162656226f5ec341e911459b4`
- Android-test APK SHA-256:
  `08ec7299e9e38c95dd853631d54f598033c8dfa37824a912783a90d75700498a`
- Toolchain: Gradle 9.6.0, launcher `JAVA_HOME=/home/linuxbrew/.linuxbrew/opt/openjdk@17/libexec`,
  compile/target SDK 37, Java/Kotlin compile target as already configured by the checkout.
- Device/API: API37 `emulator-5554`, Android 17/API37, device fingerprint recorded by the
  connected-test report as `Pixel_10_Pro_XL(AVD) - 17`. API23 was not available.
- WebView version: `com.google.android.webview 149.0.7827.5` on API37.
- Historical/pre-change API37 runtime: five `VideoSearchTest` tests passed after correcting
  test-APK asset lookup to `InstrumentationRegistry.getInstrumentation().context`. This result
  predates the current eight-test source and is retained only as historical evidence; it does
  not substantiate the current class. The prior parent run had 3 passed and 1 failed because it
  used `targetContext.assets` and raised `FileNotFoundException: video-fixtures/loading.html`;
  that failure is also historical and is not reclassified.

## Deterministic test assets and seams

`app/src/androidTest/kotlin/com/goodtrendltd/HolySongs/VideoSearchTest.kt` exercises the
real declared `VideoSearch` boundary for invalid extras and uses package-private
`HTML5WebView` callback seams for local, non-provider callback behavior. Fixtures are under
`app/src/androidTest/assets/video-fixtures/` (`loading.html`, `ok.html`,
`error-main-frame.html`, `fullscreen.html`, `geolocation.html`, and `ssl-error.html`), and are
opened through `InstrumentationRegistry.getInstrumentation().context.assets` because they belong
to the instrumentation APK.
The test-only `android.webkit.TestSslErrorHandler` exists only to observe cancellation
because the framework handler constructor is package-private. No test loads a provider URL.

## Scenario matrix

Each row includes the required evidence fields. Runtime rows remain unexecuted/blocked unless
explicitly marked otherwise; fixture/build success is not provider success. The active current
artifact record below uses the parent-observed API37 build hash `cdf2a7d1549117ec2035b6eb3d09bdd00dc7ab1162656226f5ec341e911459b4`
and test APK `08ec7299e9e38c95dd853631d54f598033c8dfa37824a912783a90d75700498a`; no row uses
ambiguous `same` placeholders.

| Requirement/scenario ID | Date | Source revision | APK hash | Toolchain | Device/API | WebView | Expected result | Observed result | Status | Evidence / limitation |
|---|---|---|---|---|---|---|---|---|---|---|
| N1-INVALID | 2026-09-16 | `f62519b4bff420ab3f78b1d544732c25683f4772` dirty | `cdf2a7d1549117ec2035b6eb3d09bdd00dc7ab1162656226f5ec341e911459b4` | Gradle 9.6/JDK17 | API37 emulator-5554 | WebView 149.0.7827.5 | Missing, wrong-typed, unsupported, or empty target/title finishes without a WebView or outbound action | Historical/pre-change API37 `VideoSearchTest.invalidExtrasFinishWithoutCreatingAWebView` passed | passed | Historical artifact hash is the parent-observed API37 artifact; provider/UTF-8 work remains deferred to T035/T040 and API23 remains unexecuted |
| N3-NETWORK | 2026-09-16 | `f62519b4bff420ab3f78b1d544732c25683f4772` dirty | `cdf2a7d1549117ec2035b6eb3d09bdd00dc7ab1162656226f5ec341e911459b4` | Gradle 9.6/JDK17 | API37 emulator-5554; API23 unavailable | WebView 149.0.7827.5 | No active network leaves lyric reader; active non-Wi‑Fi retains exact confirmation; Wi‑Fi navigates directly | Null-safe active-network/capabilities branch is compiled; API37 offline/Wi‑Fi/mobile runtime not executed | blocked | API23 and actual confirmation timing remain unexecuted |
| N3-SECURITY | 2026-09-16 | `f62519b4bff420ab3f78b1d544732c25683f4772` dirty | `cdf2a7d1549117ec2035b6eb3d09bdd00dc7ab1162656226f5ec341e911459b4` | Gradle 9.6/JDK17 | API37 emulator-5554 | WebView 149.0.7827.5 | Geolocation denied; SSL handler cancelled and never proceeded; no cleartext/mixed-content bypass or external launch | Historical/pre-change API37 geolocation/SSL test passed, including null/stale SSL URL not settling loading and matching URL settling it | passed | API23 and live-provider behavior remain unexecuted; source has no `proceed()` call |
| N4-PAUSE | 2026-09-16 | `f62519b4bff420ab3f78b1d544732c25683f4772` dirty | `cdf2a7d1549117ec2035b6eb3d09bdd00dc7ab1162656226f5ec341e911459b4` | Gradle 9.6/JDK17 | API37 emulator-5554; API23 unavailable | WebView 149.0.7827.5 | Temporary pause/stop keeps WebView live; resume invokes WebView resume without global `pauseTimers()` | Lifecycle implementation compiled; API37 rotation/background runtime not executed | blocked | Needs API23/API37 lifecycle execution |
| N4-SAVE | 2026-09-16 | `f62519b4bff420ab3f78b1d544732c25683f4772` dirty | `cdf2a7d1549117ec2035b6eb3d09bdd00dc7ab1162656226f5ec341e911459b4` | Gradle 9.6/JDK17 | API37 emulator-5554; API23 unavailable | WebView 149.0.7827.5 | Save/restore occurs only while live and does not reuse a destroyed instance | Guarded implementation compiled; API37 recreation runtime not executed | blocked | Needs API23/API37 rotation/recreation |
| N4-DESTROY | 2026-09-16 | `f62519b4bff420ab3f78b1d544732c25683f4772` dirty | `cdf2a7d1549117ec2035b6eb3d09bdd00dc7ab1162656226f5ec341e911459b4` | Gradle 9.6/JDK17 | API37 emulator-5554; API23 unavailable | WebView 149.0.7827.5 | Final teardown dismisses UI, detaches and destroys exactly once; late callbacks are inert | Idempotent release path compiled; API37 runtime count/late-callback observation not executed | blocked | Needs runtime lifecycle trace |
| N4-ERROR | 2026-09-16 | `f62519b4bff420ab3f78b1d544732c25683f4772` dirty | `cdf2a7d1549117ec2035b6eb3d09bdd00dc7ab1162656226f5ec341e911459b4` | Gradle 9.6/JDK17 | API37 emulator-5554 | WebView 149.0.7827.5 | Main-frame error/HTTP/SSL/cancel settles loading and cannot trap/reopen progress UI | Historical/pre-change API37 local main-frame error and SSL callback assertions passed | passed | HTTP callback and API23 remain unexecuted |
| N4-LATE | 2026-09-16 | `f62519b4bff420ab3f78b1d544732c25683f4772` dirty | `cdf2a7d1549117ec2035b6eb3d09bdd00dc7ab1162656226f5ec341e911459b4` | Gradle 9.6/JDK17 | API37 emulator-5554 | WebView 149.0.7827.5 | Page/progress/error callbacks after cancellation or teardown are inert | Historical/pre-change API37 late page-finished callback after settlement passed; full re-navigation/teardown sequencing remains unexecuted | blocked | Partial deterministic coverage only; no broader claim |
| N4-BACK | 2026-09-16 | `f62519b4bff420ab3f78b1d544732c25683f4772` dirty | `cdf2a7d1549117ec2035b6eb3d09bdd00dc7ab1162656226f5ec341e911459b4` | Gradle 9.6/JDK17 | API37 emulator-5554; API23 unavailable | WebView 149.0.7827.5 | Back order is loading cancel, fullscreen exit, WebView history, then normal finish; loading+fullscreen needs two presses | Guarded Java order compiled; API37 gesture and three-button runtime not executed | blocked | Needs API37 gesture/three-button and history/fullscreen runtime |
| N4-RESIZE | 2026-09-16 | `f62519b4bff420ab3f78b1d544732c25683f4772` dirty | `cdf2a7d1549117ec2035b6eb3d09bdd00dc7ab1162656226f5ec341e911459b4` | Gradle 9.6/JDK17 | API37 emulator-5554; API23 unavailable | WebView 149.0.7827.5 | Rotation/large-resizable window does not rely on portrait lock and retains a live usable WebView | Portrait lock removed; API37 resize runtime not executed | blocked | Needs API37 resize/rotation evidence |

## Commands and results (historical artifact record; not current instrumentation)

The connected-device commands below belong to the historical/pre-change five-test artifact.
They are not a current result for the eight-test source. Current source-level UTF-8 behavior is
verified by inspection (`Uri.encode(title)` plus unchanged provider resource prefixes), but no
current connected instrumentation result is recorded.

- `./gradlew :app:compileDebugJavaWithJavac :app:compileDebugKotlin --no-daemon` — failed
  before compilation because the wrapper selected an invalid JDK path (`No class roots are
  found in the JDK path`). This is an environment invocation failure, not a source result.
- `JAVA_HOME=/home/linuxbrew/.linuxbrew/opt/openjdk@17/libexec ./gradlew :app:compileDebugJavaWithJavac :app:compileDebugKotlin --no-daemon` — **passed**.
- `JAVA_HOME=/home/linuxbrew/.linuxbrew/opt/openjdk@17/libexec ./gradlew :app:assembleDebugAndroidTest --no-daemon` — **passed** after the test-only SSL handler helper was added.
- `ANDROID_SERIAL=emulator-5554 PATH=/home/hanli3/Android/Sdk/platform-tools:$PATH JAVA_HOME=/home/linuxbrew/.linuxbrew/opt/openjdk@17/libexec ./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest :app:connectedDebugAndroidTest --no-daemon -Pandroid.testInstrumentationRunnerArguments.class=com.goodtrendltd.HolySongs.VideoSearchTest` — **historically passed** (85 tasks; lint, APKs, JVM tests, and five-test API37 focused instrumentation; pre-change and not current evidence). Raw stdout was saved outside the repository at `/tmp/holysongs-video-followup-final.log`.
- Parent build `/tmp/holysongs-video-parent-build.log` — **passed** 84 tasks.
- Parent follow-up `/tmp/holysongs-video-parent-followup-device.log` — **historically passed**, 5 tests/0 failures on API37; pre-change and not current evidence.
- `ANDROID_SERIAL=emulator-5554 PATH=/home/linuxbrew/.linuxbrew/opt/openjdk@17/libexec ./gradlew :app:connectedDebugAndroidTest --no-daemon -Pandroid.testInstrumentationRunnerArguments.class=com.goodtrendltd.HolySongs.VideoSearchTest` — **historically passed**, 5 tests/0 failures on API37; pre-change and not current evidence. Raw stdout was saved outside the repository at `/tmp/holysongs-video-followup-device-explicit.log`.
- `/home/hanli3/Android/Sdk/platform-tools/adb -s emulator-5554 shell getprop ro.build.version.sdk` — **passed**, API37.
- `/home/hanli3/Android/Sdk/platform-tools/adb -s emulator-5554 shell dumpsys webviewupdate` — **passed**, WebView 149.0.7827.5.

## Post-review source corrections

`VideoSearch.searchUrlFor` now UTF-8 percent-encodes only the title query value with
`Uri.encode`; the three existing HTTP prefixes and provider identities are unchanged. The
retained `HTML5WebView` consumes non-http(s) navigation in both legacy String and modern
`WebResourceRequest` callbacks, while HTTP(S) remains in-WebView. Progress-dialog cancellation
uses the same guarded `cancelLoading` path as Back cancellation. The current source tests
include Chinese/reserved query encoding, both callback forms, and dialog cancellation, but those
assertions have not been run on a connected target in the current source state. The current JVM
`DisplayLyricLaunchGuardsTest` covers repeated share/video triggers and reset transitions only at
the state-machine boundary.

WebView callbacks expose no navigation-generation token, so same-URL stale callbacks cannot be
reliably separated from the active navigation. URL matching remains the non-speculative guard;
this residual limitation is not represented as a pass.

## Manual/provider items intentionally unexecuted

The three baseline provider prefixes remain exactly the existing HTTP resources. No provider
was opened, replaced, removed, HTTPS-upgraded, externally launched, or treated as available.
T035/T040/T041 still require owner-approved API23/API37 provider audit, redirect/final URL,
Chinese-query, WebView-version, playback and limitation evidence. API37 gesture/three-button,
resize, fullscreen/history, background/resume, offline/no-network, active Wi-Fi, and mobile-data
confirmation observations remain unexecuted; the historical focused API37 test only covers
the scenarios listed as historical above. Direct shell launch of the non-exported `DisplayLyric`
activity was
rejected by `SecurityException`, which confirms the non-exported boundary but does not provide
any video `Back` result. API23 remains unavailable.

## Changed files in this implementation slice

- `src/com/goodtrendltd/HolySongs/DisplayLyricActivity.java`
- `src/com/goodtrendltd/HolySongs/VideoSearch.java`
- `src/com/goodtrendltd/HolySongs/HTML5WebView.java`
- `AndroidManifest.xml` (only removal of `.VideoSearch` portrait lock)
- `app/src/androidTest/kotlin/com/goodtrendltd/HolySongs/VideoSearchTest.kt`
- `app/src/test/kotlin/com/goodtrendltd/HolySongs/DisplayLyricLaunchGuardsTest.kt`
- `app/src/main/kotlin/com/goodtrendltd/HolySongs/DisplayLyricLaunchGuards.kt`
- `app/src/androidTest/java/android/webkit/TestSslErrorHandler.java`
- `app/src/androidTest/assets/video-fixtures/loading.html`
- `app/src/androidTest/assets/video-fixtures/ok.html`
- `app/src/androidTest/assets/video-fixtures/error-main-frame.html`
- `app/src/androidTest/assets/video-fixtures/fullscreen.html`
- `app/src/androidTest/assets/video-fixtures/geolocation.html`
- `app/src/androidTest/assets/video-fixtures/ssl-error.html`
- `specs/002-kotlin-compose-ui/evidence/us3-video.md`

The checkout already contained other user/agent modifications; they were not changed or reset.

## API37 cancellation remediation rerun — 2026-09-16

Current artifact attribution: debug APK `318e8b9be474e30af30d7e42053549108eab2c4f73037b32d006aeba88f1eb7a`; debug androidTest APK `71bbf8ebe6682420ed365bd4eea7d71b547e9fd8d665c82e3aabfbaae82cd5ab`; API37 `emulator-5554` (`Pixel_10_Pro_XL(AVD) - 17`). `VideoSearchTest`: **8/8 passed**. The cancellation test now cancels in one main-thread callback, waits for idle, and asserts settled loading state in a later callback. Log: `/tmp/holysongs-api37-final/VideoSearchTest.log`.

This validates only deterministic local Java/WebView safety seams. It does not claim manual share/provider/process-death/fullscreen/history/background/resize or live-network acceptance.
