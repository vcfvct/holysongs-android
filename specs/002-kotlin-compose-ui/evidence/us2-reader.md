# US2 reader evidence

Date: 2026-09-16
Source: dirty `master` checkout (not a clean revision; no release or acceptance claim)

## Source implementation

- Added Compose `LyricScreen`, `SettingsScreen`, and `AboutScreen` with Activity hosts under
  `app/src/main/kotlin/com/goodtrendltd/HolySongs/`.
- Lyric extras remain `com.goodtrendltd.SONG_NAME` and `com.goodtrendltd.LYRIC`; the Java-callable
  `DisplayLyricActivity.SEARCH_TARGET` remains `com.goodtrendltd.searchTarget`.
- Lyric sharing uses `ACTION_SEND`, `text/plain`, `EXTRA_SUBJECT` equal to the selected title and
  `EXTRA_TEXT` equal to the effective lyric. Missing/wrong-type extras finish without a fallback
  song. Provider entry remains behind the retained Java `VideoSearch` boundary.
- Settings continues to use `appPrefFile`, `fontSize`, and `nightMode`, with choices
  16/18/20/22/24/26/28/30, reset 20, and display-only effective font fallback. The historical
  `nightModeSwitch` view id is retained through the Compose interoperability boundary.
- About content uses the exact legacy text and explicit labeled website/email spans with a safe
  activation seam; no-handler/runtime launch failures leave About visible.
- Added deterministic instrumentation test sources for lyric invalid-input/identity checks,
  settings contract/control presence, about text/link preservation, and reader recreation.

## Validation

Commands run:

- `JAVA_HOME=/home/linuxbrew/.linuxbrew/Cellar/openjdk@17/17.0.20.1/libexec ./gradlew :app:compileDebugKotlin --no-build-cache --console=plain` — **passed**.
- `JAVA_HOME=/home/linuxbrew/.linuxbrew/Cellar/openjdk@17/17.0.20.1/libexec ./gradlew :app:compileDebugJavaWithJavac --no-build-cache --console=plain` — **passed**; retained Java WebView/video sources compile against Kotlin activities.
- `JAVA_HOME=/home/linuxbrew/.linuxbrew/Cellar/openjdk@17/17.0.20.1/libexec ./gradlew :app:compileDebugAndroidTestKotlin --no-build-cache --console=plain` — **passed**.
- `JAVA_HOME=/home/linuxbrew/.linuxbrew/Cellar/openjdk@17/17.0.20.1/libexec ./gradlew :app:lintDebug --no-build-cache --console=plain` — **passed** after reference-checked removal of the unused legacy lyric/settings/menu layouts and migration of the video back fallback lint finding.
- `JAVA_HOME=/home/linuxbrew/.linuxbrew/Cellar/openjdk@17/17.0.20.1/libexec ./gradlew :app:connectedDebugAndroidTest --no-build-cache --console=plain -Pandroid.testInstrumentationRunnerArguments.class=com.goodtrendltd.HolySongs.LyricScreenTest` — **passed** on connected CPH2583 API36 (not the required API37 endpoint).
- The same focused command for `SettingsScreenTest` — **passed** on CPH2583 API36.
- The same focused command for `AboutScreenTest` — **passed** on CPH2583 API36.
- An earlier whole-suite command failed because the available API36/OEM target cannot satisfy the existing API37-scoped US1 matrix and the initial lifecycle test attempted recreation on that non-required endpoint. The lifecycle test now explicitly skips below API37; the failure is retained as environment evidence, not converted to a pass.

## Runtime gate status

A connected CPH2583 device was available and identified with `adb shell getprop` as Android 16,
API36. It is not the required API37 emulator, and no API23 endpoint is available (API23 remains
under the active owner waiver). Focused tests above therefore provide API36 source/runtime smoke
only. API37 instrumentation, recipient/no-recipient share behavior, link activation on the required
endpoint, reflow/scroll restoration, process death, and the full T025-T029 matrix remain
**unexecuted**, not passed. The new activation and effective-font focused tests are source-level
coverage only until rerun on the required final artifact. Provider audit/HTTPS/UTF-8 decisions remain outside this evidence and
are not changed here. T025-T033 remain unchecked until required endpoint/device evidence is captured.

## API37 remediation rerun — 2026-09-16

Current artifact attribution: debug APK `318e8b9be474e30af30d7e42053549108eab2c4f73037b32d006aeba88f1eb7a`; debug androidTest APK `71bbf8ebe6682420ed365bd4eea7d71b547e9fd8d665c82e3aabfbaae82cd5ab`; API37 `emulator-5554` (`Pixel_10_Pro_XL(AVD) - 17`). `AboutScreenTest` **2/2**, `SettingsScreenTest` **4/4**, `LyricScreenTest` **3/3**, `ReaderLifecycleTest` **1/1**, and `ReaderPreferencesTest` with `readerPreferencesMutate=true` **4/4** passed. Logs: `/tmp/holysongs-api37-final/{AboutScreenTest,SettingsScreenTest-final,LyricScreenTest,ReaderLifecycleTest,ReaderPreferencesTest}.log`.

The About lookup now selects the exact `ABOUT_CONTENT` TextView; `configureAboutText` remains unchanged and its `SafeAboutLinkSpan` destinations match the legacy normalized website URL and mailto destination. The Compose manifest dependency is debug-only and BOM-aligned. This is focused instrumentation evidence only; no manual share, provider, process-death, or full acceptance claim is made.
