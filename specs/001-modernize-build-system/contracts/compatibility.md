# Build and User Compatibility Contracts

These are acceptance interfaces for a local Android app. No REST API, server, or new external
service contract is introduced. Details of preserved data are in [data-model.md](../data-model.md).

## C1. Developer Build Interface

From repository root, with documented JDK 17 and Android SDK prerequisites:

| Command / entry point | Required result |
|---|---|
| `./gradlew --version` | Gradle 9.6.0; runtime JDK 17 |
| `./gradlew clean :app:assembleDebug` | Exit 0; `app/build/outputs/apk/debug/app-debug.apk` |
| `./gradlew :app:testDebugUnitTest` | Exit 0; actual characterization tests executed |
| `./gradlew :app:lintDebug` | Exit 0 after documented review of findings |
| Android Studio Quail 4 import/build | Successful sync and debug build using the same wrapper/JDK |

`gradlew.bat` supplies the Windows wrapper entry point. No globally installed Gradle or legacy
Ant/ADT installation is required. First dependency downloads may require internet access.
Generated application files are excluded from version control and rebuilt normally. Wrapper
files and the existing pinyin JAR are versioned inputs, not generated outputs to exclude.

## C2. APK and Installation Identity

- Application ID/namespace: `com.goodtrendltd.HolySongs`.
- Minimum/target SDK: 14/37. No applicationId suffix on debug installs.
- Preserve launcher and internal Activity identities, versionCode 8 and versionName 2.5.
- Export launcher; do not expose internal Activities as new external interfaces.
- Existing INTERNET/ACCESS_NETWORK_STATE permissions only; no new runtime permissions.
- Debug signature must be verifiable on API 14 as well as API 37. No release key is generated,
  replaced, committed, or assumed available. Debug signing uses normal local tooling.
- A debug-signed APK cannot be assumed to update a production-signed installation. Do not
  uninstall an existing user's app to make an upgrade test pass.

## C3. Offline Reader and Settings

On each required runtime endpoint:

1. First launch without connectivity loads the complete effective catalog (414 titles from
   422 unchanged source entries). No account, download, or network response is needed.
2. Scrolling and letter navigation select the expected title and lyric. Preserve Chinese
   Collator semantics and existing duplicate resolution. Documented native collation differences
   across Android versions are acceptable under FR-008, not permission to change algorithms.
   For each runtime, record baseline sidebar positions for present and absent letters before
   fixes; require valid expected positions and correct title-to-lyric selection. Unresolved
   differences or altered section semantics require maintainer approval. Device checks must
   verify actual loading/space removal and all duplicate-title winners, not a JVM simulation.
3. Lyrics remain legible and scrollable with line breaks preserved; both themes and all eight
   selectable font sizes work. Reset restores 20. Check increased system font scale and system
   bar insets for clipped essential controls, without redesigning layouts.
4. `fontSize` and `nightMode` persist across process relaunch. Representative existing stored
   values are read using the exact legacy identifiers. Missing values use legacy defaults.
5. Rotation, rapid open/close, and background/foreground transitions do not crash, leak a
   visible window, strand navigation, or corrupt content. Theme changes take effect when
   returning to the relevant screen, including modern system Back navigation.
6. About and settings remain reachable. Missing or unexpected extras fail safely rather than
   crashing an Activity; no new deep-link feature is added.

## C4. Sharing and Video Search

- Lyric sharing uses `ACTION_SEND`, `text/plain`, selected title as subject and lyric as text.
- App sharing preserves its existing text/link intent. External store-link availability is
  reported separately from the correctness of the share payload.
- An available recipient receives the correct payload; missing recipients do not crash the app.
- Audit YouTube, Youku and Tudou separately for query correctness, final URL, provider response,
  WebView behavior, fullscreen/Back, cancellation, and background/resume.
- On no network, server failure, or unsupported page behavior, loading can be dismissed and
  the user can return to lyrics. No TLS-error bypass, unconditional geolocation grant, or global
  cleartext/mixed-content allowance is authorized.
- Verified same-provider HTTPS migration/query encoding is a compatibility fix. A new provider,
  removed action, or external-browser replacement requires recorded maintainer approval first.
- Approved unavailable-provider limitations may remain; app crashes or broken offline return
  cannot be waived as a third-party limitation.

## C5. Evidence and Completion

Required matrix:

| Environment | Required coverage |
|---|---|
| API 14 device or functioning compatible emulator | C2–C4, offline first launch, stored preferences, lifecycle |
| API 37 phone emulator/device | C2–C4, gesture and three-button navigation, offline first launch |
| API 37 large-screen emulator/device | Insets, resizing/rotation, sidebar and video navigation; cannot rely on portrait lock |
| Clean Linux development environment | C1 CLI and Android Studio paths, manifest/signing/data inspection |

Manual device validation is an accepted method and must carry evidence. JVM tests alone do
not satisfy C3–C4. On both API endpoints, record process-stopped launch-to-usable-list elapsed
time and method, visible scrolling stalls, and test-session log/system ANR checks. Any app
crash or ANR fails the scenario; missing observations block sign-off. No numerical latency
threshold or benchmark suite is required. An unavailable API 14 device/emulator is a blocked
acceptance gate; using a newer runtime or raising the minimum requires an explicit reviewed
plan change.

Record each result using the Verification Record fields from the data model. All required core
checks must pass for the final artifact. Provider audits require outcomes and approval for
limitations/changes. A changed APK requires rerunning affected checks and both-endpoint smoke
coverage; do not combine unrelated artifacts into a false all-passing result.

Keep raw screenshots/logs/APKs outside tracked source. Commit only sanitized verification
summaries and instructions. Never record credentials, signing keys, or unnecessary user data.
