# US1 build acceptance evidence

Status: working-tree CLI build/tests/lint and APK inspection passed in the parent replay below. Clean exact-revision checkout, IDE and install/launch acceptance remain blocked/unexecuted; US1 is incomplete. The initial pre-build records below are retained as history, not current missing-wrapper claims.

Environment clarification (parent review, 2026-09-15): JDK17, Android Studio and SDK platform 37/Build Tools 36.0.0/adb are installed, but only the JDK and tool inventory have been verified. SDK command-line tools are missing; IDE execution and Gradle use of installed SDK components remain unverified. Session-local exports are permitted without license acceptance. Existing `local.properties` contains a stale SDK path and must not be lost or committed with a new machine path. See `environment.md` and `devices.md` for authoritative inventory.

## Current verification possible in this slice

The planned JUnit property for repository-root discovery is `repositoryRoot` (System property). The T011 build file will set it to the checked-out repository root before the test harness runs. In the current tree, the only verification possible without a Gradle/JUnit harness is direct repository inspection and source-hash checks using the existing repository files.

Observed current-source checks:

```bash
cd /home/hanli3/GIT/holysongs-android
python3 - <<'PY'
import hashlib, xml.etree.ElementTree as ET
song_sha = hashlib.sha256(open('assets/songs.xml','rb').read()).hexdigest()
jar_sha = hashlib.sha256(open('libs/pinyin4j-2.5.0.jar','rb').read()).hexdigest()
xml = ET.parse('res/values/strings.xml').getroot()
values = {node.get('name'): node.text for node in xml.findall('./string')}
print('songs', song_sha)
print('jar', jar_sha)
print('app_pref', values.get('app_pref'))
print('font_size_pref_key', values.get('font_size_pref_key'))
print('night_mode_pref_key', values.get('night_mode_pref_key'))
PY
```

Observed output:

```text
songs 88eb0db602e018b49a327947dd8607f04e6159e58f39ec38ed59f20c39af9d89
jar 6576dea7d351a0f5df1595b9c432ba7cf9246ca0ab6f7019b9ca4e6d500b0e68
app_pref appPrefFile
font_size_pref_key fontSize
night_mode_pref_key nightMode
```

This is repository verification only. It does not replace the missing build harness or the later Gradle/JUnit execution under T011/T014.

## C1 developer-build acceptance records

### C1-CLI-001 [FR-001|US1|CLI|clean-checkout]

- requirement/scenario ID: C1-CLI-001 [FR-001|US1|CLI|clean-checkout]
- date: 2026-09-15
- source revision: 634f98624b654fe9926c7b1c8186fbad56df6673
- APK SHA-256: n/a; no debug APK has been generated yet
- toolchain: JDK 17 required; Gradle wrapper and Android SDK tooling not configured in the current host environment
- device/emulator identifier: n/a
- Android API/version: n/a
- WebView version where applicable: n/a
- prerequisites: documented JDK 17 + Android SDK + Android Studio Quail 4 + Android API 37 + Build Tools 36.0.0; clean repository checkout and local `local.properties` only through documented setup
- steps: follow `specs/001-modernize-build-system/quickstart.md` clean-checkout build path from repository root, run `./gradlew --version`, then `./gradlew clean :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`
- expected result: Gradle 9.6.0 is reported; `app/build/outputs/apk/debug/app-debug.apk` is created; tests/lint exit successfully
- observed result: blocked; this repository still lacks the generated Gradle wrapper, `:app` module, and configured SDK environment; `./gradlew` is not currently executable in the working checkout
- status: blocked
- evidence path: `specs/001-modernize-build-system/evidence/us1-build.md`
- blocker/limitation: host environment has no desired wrapper/build files and no verified SDK environment; environment blocker, not a product failure
- maintainer approval reference: n/a

### C1-IDE-001 [FR-001|US1|IDE|Android Studio sync/build]

- requirement/scenario ID: C1-IDE-001 [FR-001|US1|IDE|Android Studio Quail 4]
- date: 2026-09-15
- source revision: 634f98624b654fe9926c7b1c8186fbad56df6673
- APK SHA-256: n/a; no debug APK generated
- toolchain: Android Studio Quail 4 + JDK 17 + Android SDK; current environment not yet provisioned
- device/emulator identifier: n/a
- Android API/version: n/a
- WebView version where applicable: n/a
- prerequisites: local SDK installation, API 37 platform, Build Tools 36.0.0, and the future Gradle plugin/module configuration
- steps: open the repository in Android Studio, sync Gradle, build debug variant, confirm no machine-specific tracked files are required
- expected result: sync and debug build succeed using the same JDK 17 / wrapper setup documented in `quickstart.md`
- observed result: blocked; the project does not yet contain the Gradle wrapper or `app/build.gradle`, so IDE sync/build is not currently executable
- status: blocked
- evidence path: `specs/001-modernize-build-system/evidence/us1-build.md`
- blocker/limitation: missing Gradle build files and SDK configuration; no IDE pass can be claimed before T009-T011 are present
- maintainer approval reference: n/a

## C2 APK identity and install/launch acceptance records

### C2-APK-001 [FR-006|US1|APK|identity-and-signing]

- requirement/scenario ID: C2-APK-001 [FR-006|US1|APK|identity-and-signing]
- date: 2026-09-15
- source revision: 634f98624b654fe9926c7b1c8186fbad56df6673
- APK SHA-256: n/a; no APK artifact exists yet
- toolchain: future debug signing from Android SDK; not yet provisioned
- device/emulator identifier: n/a
- Android API/version: n/a
- WebView version where applicable: n/a
- prerequisites: successful debug build, SDK command-line tools, and a valid local debug signing environment
- steps: set `APK=app/build/outputs/apk/debug/app-debug.apk`; run `sha256sum "$APK"`, `unzip -p "$APK" assets/songs.xml | sha256sum`, `"$ANDROID_HOME/build-tools/36.0.0/aapt" dump badging "$APK"` and `"$ANDROID_HOME/build-tools/36.0.0/apksigner" verify --verbose --print-certs --min-sdk-version 14 "$APK"`. Verify package `com.goodtrendltd.HolySongs`, minSdk 14, targetSdk 37, versionCode 8, versionName 2.5, unchanged packaged asset hash and `Verified using v1 scheme ...: true`.
- expected result: APK metadata matches identity and API signatures; no release key or generated signing credentials are committed
- observed result: blocked; no debug APK exists and the Gradle toolchain has not yet been added
- status: blocked
- evidence path: `specs/001-modernize-build-system/evidence/us1-build.md`
- blocker/limitation: build harness not yet created; signing compatibility cannot be checked until T009-T014 run
- maintainer approval reference: n/a

### C2-APK-002 [FR-001|US1|APK|install-launch]

- requirement/scenario ID: C2-APK-002 [FR-001|US1|APK|install-launch]
- date: 2026-09-15
- source revision: 634f98624b654fe9926c7b1c8186fbad56df6673
- APK SHA-256: n/a; no APK artifact exists yet
- toolchain: future `adb`, Android Debug Bridge, and dedicated API 14/API 37 devices or emulators; current host lacks a verified SDK setup
- device/emulator identifier: n/a
- Android API/version: n/a
- WebView version where applicable: n/a
- prerequisites: installed debug APK on a dedicated API 14 and API 37 target, plus documented local setup and a share recipient where required
- steps: confirm a dedicated target serial with `adb devices -l`; set `ANDROID_SERIAL` explicitly, check `adb shell getprop ro.build.version.sdk`, disable connectivity for the first-launch case, then run `adb install -r app/build/outputs/apk/debug/app-debug.apk` and `adb shell am start -n com.goodtrendltd.HolySongs/.MainActivity`. Stop on signature conflict without uninstalling or clearing data. Record launch success and first-run behavior as in `quickstart.md`. US1 needs an available supported dedicated target; both API14/API37 endpoints remain mandatory for final feature acceptance.
- expected result: install succeeds and launch opens the app; missing network and missing resources do not cause a crash
- observed result: blocked; no APK artifact and no dedicated device/emulator setup are currently available in this slice
- status: blocked
- evidence path: `specs/001-modernize-build-system/evidence/us1-build.md`
- blocker/limitation: environment and runtime access are incomplete; no install/launch success claim is allowed until the modern build checkpoint exists
- maintainer approval reference: n/a

## Missing-prerequisite and regeneration procedure (unexecuted)

For C1-CLI-001, begin with an exact implementation-revision checkout, not a copy of this dirty working directory. Run `git rev-parse HEAD`, `git status --short`, `java -version` and `test -d "$ANDROID_HOME/platforms"` before the wrapper commands. Missing Java/SDK or failed official downloads are environment blockers with the exact command/exit/output recorded; do not substitute versions or accept licenses. For the later negative setup check, use a disposable shell/environment, not edits to global configuration, and confirm missing Java/SDK is diagnosed rather than hidden by local state.

The `clean :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` run must regenerate application outputs without `gen/` as a source input. After the first successful build, rerun that clean command, inspect regenerated APK/test/lint reports, then run `git status --short` and `git ls-files -- local.properties gen app/build .gradle` to establish that local configuration and generated application outputs are not required tracked inputs. Wrapper files and the retained pinyin JAR are required versioned inputs. Do not use destructive repository cleanup as a substitute for `clean`.

For each attempt add a dated record with all fields above; current source inspection is not C1 success. The negative prerequisite check and regeneration replay are presently **blocked** by the absent wrapper/module and have not been executed.

## Evidence boundary and next step

This document is intentionally a blocked/unexecuted acceptance record. It distinguishes environment blockers from product failures and explicitly does not claim a successful build. The next required step is T009-T011: create the Gradle wrapper, root build files, and the `:app` module/wiring for the `repositoryRoot` property before re-running the acceptance cases.

## Executed T009/T014 validation (2026-09-15)

Environment used for this slice:
- `JAVA_HOME` set to the local Temurin JDK 17 installation under the user's home directory to match the selected JDK17 contract. This is not a claim that every other JDK is incompatible with AGP.
- `ANDROID_HOME` / `ANDROID_SDK_ROOT` set to the local Android SDK installation; the repository-local `local.properties` file was backed up and temporarily replaced with `sdk.dir=$ANDROID_HOME` for execution and then restored immediately.
- No SDK license acceptance, package install, or tracked project cleanup was performed.

Commands executed:

```bash
export JAVA_HOME=<local Temurin JDK 17>
export ANDROID_HOME=<local Android SDK>
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew --version
./gradlew clean :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Observed result:

```text
$ java -version
openjdk version "17.0.14" 2025-01-21
OpenJDK Runtime Environment Temurin-17.0.14+7 (build 17.0.14+7)
OpenJDK 64-Bit Server VM Temurin-17.0.14+7 (build 17.0.14+7, mixed mode, sharing)

$ ./gradlew --version
Gradle 9.6.0
Launcher JVM: 17.0.14 (Eclipse Adoptium 17.0.14+7)
Daemon JVM:    <local Temurin JDK 17>

$ ./gradlew clean :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
BUILD SUCCESSFUL in 2s
46 actionable tasks: 37 executed, 9 from cache
```

Assessment:
- The build and test/lint slice has passed in this environment after switching to the correct JDK 17 toolchain.
- This is a bounded T010/T011/T014 verification result only; it does not authorize T015-T017 cleanup, untracking, or repository retirement steps.
- Lint reported no blocking issues in this slice, and the HTML report was produced under `app/build/reports/lint-results-debug.html`.
- The source code and manifest remain within the approved scope for the build migration. No IDE install, device install, or clean-checkout completion claim is being made here.

## Parent replay and artifact inspection — 2026-09-15

- requirement/scenario ID: C1-CLI-002 [FR-001, FR-002, FR-004|US1|working-tree]; C2-APK-001 [FR-006, FR-007|US1|identity-and-signing]
- date: 2026-09-15; unit-test report timestamp `2026-09-15T15:27:46.650Z`
- source revision: base `634f98624b654fe9926c7b1c8186fbad56df6673` plus uncommitted build/manifest/menu/layout/test changes; **not a clean implementation-revision checkout**
- APK SHA-256: `df6c6d66d395c23e284091e15c2f0dc378e5076a48168ee30ac2fa909a02f174`
- toolchain: Temurin 17.0.14+7; Gradle 9.6.0; AGP 9.4.0; platform 37; Build Tools 36.0.0; Java8 source/target and JDK17 toolchain
- device/emulator identifier: n/a; host-only checks
- Android API/version: APK min14/target37; no runtime tested
- WebView version where applicable: n/a
- prerequisites: existing local JDK/SDK; session-local environment; private backup of original local.properties, temporary sdk.dir, exact-byte restoration via shell EXIT trap. SDK and JDK auto-download disabled; no SDK licenses accepted.
- steps: `./gradlew --version`; `./gradlew clean :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --no-build-cache --rerun-tasks`; then the C2 aapt/apksigner/hash commands above; `git diff --exit-code -- local.properties`; `git -c core.whitespace=cr-at-eol diff --check`
- expected result: pinned toolchain; all build/unit-test/lint tasks execute successfully; unchanged packaged songs; expected identity and API14-compatible signing; original local file restored
- observed result: commands exit 0; `BUILD SUCCESSFUL`, 46 actionable tasks, **46 executed**; **2 tests, 0 skipped/failures/errors**; **0 lint errors, 25 warnings**. APK has application ID `com.goodtrendltd.HolySongs`, version8/2.5, min14/target37 and only INTERNET/ACCESS_NETWORK_STATE permissions. v1 and v2 signatures verify; packaged songs hash is `88eb0db602e018b49a327947dd8607f04e6159e58f39ec38ed59f20c39af9d89`. Source JAR retains its recorded hash. Local properties restored unchanged; CRLF-aware diff check passes (legacy files already use CRLF).
- status: passed for these host-only replay/artifact checks; not C1-CLI-001 clean checkout, C1-IDE-001 or C2-APK-002 runtime acceptance
- evidence path: this file; private raw replay logs `/tmp/holysongs-parent-build.1BmVNz/{version.log,build.log}`; generated reports under `app/build/reports/` and `app/build/test-results/`
- blocker/limitation: no committed implementation revision, no IDE sync/build evidence, no dedicated device/AVD. apksigner warns that `META-INF/com/android/build/gradle/app-metadata.properties` is not protected by the v1 signature; this is build metadata, not song content. No installation or production-signing claim. Kotlin opt-out is supported by AGP9.4 but deprecated for removal in AGP10; warning remains visible. Gradle also reports deprecations affecting a future Gradle10 upgrade.
- maintainer approval reference for accepted provider changes or limitations: n/a; no provider change or limitation accepted

### Wrapper provenance (parent independently checked)

Official HTTPS sources: `https://services.gradle.org/distributions/gradle-9.6.0-bin.zip.sha256` and `https://services.gradle.org/distributions/gradle-9.6.0-wrapper.jar.sha256`.
Both were fetched with `curl --fail --location`; local SHA256 values match:

- distribution: `bbaeb2fef8710818cf0e261201dab964c572f92b942812df0c3620d62a529a01`
- wrapper JAR: `497c8c2a7e5031f6aa847f88104aa80a93532ec32ee17bdb8d1d2f67a194a9c7`

The worker generated the standard wrapper using the verified distribution in a temporary bootstrap directory, not a global Gradle installation. Wrapper URL/checksum are pinned, launcher is executable, and wrapper/JAR inputs are not ignored.

### Prior failures retained

The first build-config child (`99686a54-3cc1-49bd-9caa-f8733c24003e`) initially reported **2 failing tests** with `IllegalStateException` for missing `repositoryRoot`. T011's test system-property wiring corrected the harness; this was not asset drift. A subsequent lint run **failed** with one `Orientation` error at `res/layout/video_html5_screen.xml:16`, plus 25 warnings. The initial vertical fix was corrected during parent review to explicit **horizontal**, preserving the original default. These failures remain part of history despite the later successful replay. Raw child transcript is retained under that run's artifacts.

See `review.md` for file-specific reasons, legacy API/startup inspection and all 25 lint-warning dispositions. No lint baseline or warning suppression was accepted.

## Draft PR checkpoint

See `pr-checkpoint.md` for subsequent owner-reported IDE/simulator evidence, task
reconciliation and clean committed-source host replay. The earlier failures and
blocked checks above remain historical evidence; host replay alone cannot complete T017.
