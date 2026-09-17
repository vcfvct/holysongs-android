# Feature002 build evidence

## T006/T007 enablement — 2026-09-15

**Result:** implementation accepted by master after source review and independent host validation.
**Scope:** Kotlin/Compose configuration and compiled theme probe only; legacy screens are unchanged.
T009 full graph/lock/IDE gate, T010 native baseline and all story acceptance remain incomplete.

### Delegated implementation and parent review

- Worker model: `github-copilot/gpt-5.6-luna` (explicit owner-approved routing).
- Workflow: `a68a7711-b034-421e-8f2e-327c7dce0202`.
- Child: `9ab5fd3d-3770-4498-9fb3-f30ad9d43a70`.
- Managed output: `compose-enablement.md` under that workflow's session output directory.
- Child changed only build.gradle, gradle.properties, app/build.gradle,
  app/src/main/kotlin/com/goodtrendltd/HolySongs/ui/HolySongsTheme.kt and res/values/styles.xml.
- Master reviewed all five files against tasks/research/build contract; no screen, manifest,
  Java helper, owner layout or persistent storage changes. No files staged/committed.

AGP built-in Kotlin is enabled, with Compose plugin2.2.10 applied and declared at root.
AndroidX, Compose, AndroidJUnitRunner and all R1 direct library/configuration pins are present.
No Kotlin Android plugin, legacy compiler-extension DSL, Jetifier, new framework or suppression.
Main/test/androidTest Kotlin roots are separate from legacy Java and mapped manifest/res/assets.
minSdk23 is the approved support change; compile/target37, Build Tools36.0.0, identity/version8/2.5,
Java17 compilation toolchain declaration and Java8 application targets remain. Kotlin inherits
aligned JVM8 from Android configuration, confirmed in compiled bytecode.

HolySongsTheme explicitly accepts nightMode and selects dark/light Material3 colors. It does not
read system/dynamic themes or override font scaling. The narrowly named platform no-action-bar
style is not wired into any Activity yet; existing Holo styles and manifest mappings remain intact.
An unused-style lint warning is expected until the pilot wiring, not justification to remove it.

### Environment failure and successful retest

Worker's first build with the inherited Homebrew formula-wrapper JAVA_HOME failed:
`No class roots are found in the JDK path`. The actual JDK root is the installation's nested
`libexec` directory. A command-local JAVA_HOME pointing there succeeded. No global/user Gradle
settings, local.properties or the owner's Java25 daemon criteria were changed.

Use an actual JDK17 installation root (containing the JDK modules), rather than a Homebrew formula
wrapper, for Kotlin compilation. In this environment the tested command-local selection was:

```sh
JAVA_HOME="$(brew --prefix openjdk@17)/libexec" ./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --rerun-tasks --no-build-cache --console=plain
```

The parent used the resolved absolute equivalent of this path. Launcher: Homebrew17.0.20.1;
daemon: Studio JetBrains25.0.3 under the owner's explicit local exception. This is not a
JDK17-only B1 pass. No SDK installation/license acceptance was performed.

### Master validation

- Source: f62519b4bff420ab3f78b1d544732c25683f4772 plus current uncommitted feature002 build/tests.
- Parent command above: **BUILD SUCCESSFUL**, 52 actionable tasks executed (10 seconds),
  explicitly without task-output reuse/build cache. Log: `/tmp/holysongs-002-parent-enablement-validation.log`.
- JUnit: **12 tests, 0 failures, 0 errors, 0 skipped**, regenerated XML reports.
- Lint: **0 errors, 23 warnings**. Reviewed warning IDs: retained preference commit calls,
  locale/inflation, labels, tool-version advice, portrait lock/deprecated API, JavaScript,
  drawing allocation, unused resources/namespaces/icons, custom-sidebar accessibility and RTL.
  No lint suppression added. Later screen/video tasks retain their safety/behavior gates.
- Production `HolySongsThemeKt.class` and legacy `MainActivity.class`: major52 (JVM8).
- Debug merged manifest: min23/target37; uses-permissions exactly INTERNET, ACCESS_NETWORK_STATE,
  and com.goodtrendltd.HolySongs.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION.
  The sole declared permission has that exact name and signature protection; merger report
  attributes declaration/use to AndroidX Core1.18.0. No permission removal/widening used.
- Debug APK SHA-256: `d243d6f59149b1fcb833e7d2de5ab18074812f80323e155a17454a5cb7da67ce`.
- Source and packaged songs SHA-256: `88eb0db602e018b49a327947dd8607f04e6159e58f39ec38ed59f20c39af9d89`.
- Vendored pinyin JAR SHA-256: `6576dea7d351a0f5df1595b9c432ba7cf9246ca0ab6f7019b9ca4e6d500b0e68`.
- apksigner verify --min-sdk-version23: verifies; v1=true, v2=true. It warns that META-INF
  version/license/service metadata entries are not v1-protected. Retained output is a debug
  compatibility check, not release-signing or production-upgrade proof; no metadata stripped.
- Targeted diff whitespace check passed; no staged files. Pre-existing owner's main.xml whitespace
  remains outside this change, as recorded in environment.md.

Worker debugCompileClasspath report resolves the selected BOM2026.09.00, core1.12.1,
Material3 1.4.0, Activity1.13.0, Lifecycle2.11.0 and coroutines1.11.0. Stdlib2.2.20 is a
transitive library version, not a compiler upgrade. `:app:buildEnvironment` reports the daemon
but its app classpath is empty; it does not by itself prove the root/AGP compiler dependency.
T009 still must inspect full compiler/plugin resolution and all required compile/runtime graphs,
resolved Android metadata, lockfile and instrumented manifests/build, plus IDE execution.

### Remaining gates

No device install, native ordering, overlap reproduction, baseline capture, IDE sync/build or
instrumentation was performed for this artifact. API23 target remains missing; API37 ownership
is established in devices.md. This is a toolchain checkpoint, not the user-visible Compose MVP.

## T009 host audit and lock replay — 2026-09-15, 21:58 America/New_York

**Host checks passed; T009 remains unchecked pending current-source Android Studio sync/build
verification.** API23 availability and API23/API37 baseline execution belong to subsequent T010;
they are not retroactively added to T009's host/IDE checkpoint. The owner-approved Java25 daemon
exception is retained. The child report's date uses the following UTC calendar date; this entry
records the actual local timestamp and does not erase earlier evidence.

Worker `github-copilot/gpt-5.6-luna`, workflow `52b0b64a-2ec2-4186-a62e-f424cca7cbfd`,
child `fb432705-8e28-4d70-9794-6feed3690009`; output `foundation-build-audit.md` in the managed
workflow output directory. Only added `dependencyLocking { lockAllConfigurations() }` to
app/build.gradle and generated app/gradle.lockfile; master reviewed both. Existing pins/mappings
and all owner files remain intact. No staged files, commits, device operations or release actions.

### Commands / results

All Gradle commands used command-local JAVA_HOME pointing to the actual JDK17 `libexec` root,
with the existing Java25 daemon unchanged:

- `buildEnvironment --rerun-tasks`: root AGP9.4.0, KGP2.2.10, Compose compiler plugin2.2.10.
  App buildEnvironment separately reports an empty classpath and is not used as compiler proof.
- `:app:dependencies --write-locks --rerun-tasks`: deliberately generated app lock state; no
  force/downgrade/version substitution. Lock includes debug/test/release, lint, compiler and
  other resolved app configurations. Root plugin versions remain explicitly pinned in build.gradle.
- Four `:app:dependencies --configuration <name> --offline` reports: debugCompileClasspath,
  debugRuntimeClasspath, debugAndroidTestCompileClasspath, debugAndroidTestRuntimeClasspath passed.
- Compiler/plugin configuration reports: compiler-embeddable and Compose plugin2.2.10, distinct
  from app library stdlib2.2.20 and Gradle's embedded Kotlin2.3.21.
- `clean :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest
  --no-build-cache --rerun-tasks`: passed, 83 actionable tasks.
- `:app:assembleDebug :app:assembleDebugAndroidTest --offline --no-build-cache --rerun-tasks`:
  passed, 68 actionable tasks, no lock-writing flag.
- Parent replay: `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug
  :app:assembleDebugAndroidTest --offline --console=plain`: passed, 82 actionable tasks
  (5 executed, 77 up-to-date). It confirms locked replay, not another clean build claim.
- Parent rechecked current reports: 12 tests/0 failures/errors/skips; lint0 errors/23 warnings.
  Java Main, Kotlin theme and Kotlin LegacyBaselineTest all have JVM major52.

### Dependency / artifact audit

Resolved AAR occurrences: debug compile34/runtime49, androidTest compile42/runtime52; 60 unique
AAR files across these graphs. All60 AAR manifests parsed as plain XML. Metadata was available
for33/46/36/41 occurrences respectively; absent metadata was counted explicitly. Reported minimum
SDK range14–23, maximum minCompileSdk37 and minimum-AGP requirement9.1.0 fit the approved toolchain.
The worker corrected an initial aapt2 attempt on plain AAR XML with XML parsing; no unreadable
manifest was silently skipped. Exact selected coordinates/paths and audit script remain in private
`/tmp/holysongs-t009-aar-audit.gradle`, `holysongs-t009-aar-audit.log` and
`holysongs-t009-aar-manifest-xml-audit.txt`.

App merged/packaged manifest and aapt inspection preserve package/launcher/version8/2.5,
min23/target37 and the exact three-permission B2 allowlist. AndroidX Core1.18.0 contributes the
sole app-private signature declaration/matching use. ProfileInstaller's dependency receiver is
exported but protected by platform DUMP; no internal Activity export changed. Test package is
com.goodtrendltd.HolySongs.test with test-only REORDER_TASKS/invoker components, not additions
to the app allowlist. Master independently rechecked current app/test permissions.

- Lock SHA-256: `ffb46252f9f5cc1140318e46270b9c23ead4da7880421955e2a838e18271f9ef`.
- Current app APK SHA-256: `3661c49c61e377548129e098215ac6e3c2d96c946b8170a3572f8c4ebfd8c39e`.
- Current androidTest APK SHA-256: `39716a31f7cbfbaf98cf0afe9ae2f058cbe02a6ecf3f6546f4c0320ca1174df2`.
- Source/packaged song and vendored JAR hashes still match the immutable values above.
- Debug apksigner API23 verification passed with v1/v2; META-INF warnings remain recorded,
  not stripped. No production-signing/upgrade claim.

Raw command logs are `/tmp/holysongs-t009-{root-buildEnvironment,app-buildEnvironment,
lock-generation,graph-reports,compiler-config,clean-build,lock-replay-build,apk-inspection,
test-apk-badging}.log`; parent replay log is `/tmp/holysongs-t009-parent-replay.log`.
These are host-only observations of HEAD f62519b plus the current uncommitted implementation,
not a clean exact-revision Git checkout or an installed-runtime baseline.

### Stop / resume boundary

The master stops dependent execution at T009 until the current checkout is synced/built in
Android Studio Quail4/2026.1.4 with attributable IDE/build output. Report actual Gradle JVMs under
the owner's Java25 exception rather than silently claiming the original JDK17-only plan passed.
Then T010 requires actual API23 and authorized Pixel API37 capture, overlap/timing/scroll/crash
observations and reviewed runtime fixtures. No core screen/index replacement is permitted yet.
Post-execution extension registry check: `.specify/extensions.yml` absent; no hooks to dispatch.

## 2026-09-16 current US1/T024 verification status

This section records the current-artifact verification and the exact limits on what may be claimed.

- Required command-local JDK17 root: `/home/linuxbrew/.linuxbrew/opt/openjdk@17/libexec`.
- Gradle launcher JVM: `17.0.20.1` (Homebrew 17.0.20.1+0); daemon JVM: `Compatible with Java 25, any vendor` from `gradle/gradle-daemon-jvm.properties`; the owner-approved Java25 exception remains in force.
- Current integrated artifact hashes: app `e6a3e08e7e03255d47351dc85c460cc0fce3c4563a3894c7054d86fe2b9d9344`,
  test APK `4ba6c9760f2f555369397569f5f1cbbf5c9a07fe282839da7bd0fad5c5fa8fd5`.
  `/tmp/holysongs-final-current/final-attribution.txt` (SHA-256
  `3fb3d410dac3b620f2b44fc8c775314dcd0bc1acddcec22573c8752eb90cbc30`) and its source manifest
  (SHA-256 `473ee4155a231842e9d4c26ed726135879bc2e3d26120efcdeb2f1e8a173dd20`)
  bind this final dirty-worktree artifact; they do not make it a clean Git revision.
- Parent build evidence: `/tmp/holysongs-video-parent-build.log` shows a clean no-cache rerun
  `BUILD SUCCESSFUL` with `84 actionable tasks: 84 executed`.
- Parent final regression evidence: `/tmp/holysongs-final-current` records `SongListScreenTest`
  default `OK (12 tests)` with five explicit assumption skips covered by focused opt-in runs;
  final-artifact fit and compact each passed `OK (1)`, settings sync passed `OK (1)`, and each
  resize case passed an exact isolated retry after one retained target-window input-injection failure.
  Earlier exhaustive order/winner results in `/tmp/holysongs-current-regression` remain applicable;
  the only later production change was the reviewed theme-aware rail text color.
- Parent final video run: `/tmp/holysongs-final-current/video-final.log` passed `VideoSearchTest`
  `OK (5 tests)` on API37.
- Manual API37 evidence: `/home/hanli3/.local/state/holysongs-android/verification/us1-current-manual-20260916T135338`
  captured a disposable app cleared and network-disabled cold launch under gesture font1, three-button
  font1, and gesture+tall-cutout font2. Each uiautomator dump reached title `阿爸父`, screenshots/window
  dumps/configs were preserved, and the host elapsed 15/9/12 seconds including the `am start` and
  uiautomator polling. Master visually inspected all three screenshots and observed all essential UI
  elements unobscured, the three-button bar below content, and the tall-cutout/font2 title/content below
  the system/cutout area; the font2 compact rail showed `A-S` onscreen. Full reachability remains backed
  by the current compact instrumentation rather than screenshot-only evidence.
- Later offline verification: `/home/hanli3/.local/state/holysongs-android/verification/us1-final-contrast-20260916T141141`
  retained `offline-verified-state.txt` with `airplane enabled`, `Wifi disabled`, `mobile_data 0`, and
  `Active default network: none` before app clear/launch; `offline-start.txt` and `offline-ready.txt`
  both show a successful `MainActivity` cold launch with `ready=1`. The first network-disable snapshot
  is stale and is not retained as verified offline state.
- A bounded logcat review showed no matching app `FATAL`/`ANR` in the later offline and manual checks;
  this is not universal proof. Device state was restored to gestural/font1/network-on and the app stopped.
- A direct shell launch of the non-exported `DisplayLyric` activity was rejected by `SecurityException`,
  which confirms the non-exported boundary but provides no video `Back` result.

These results are real current-artifact evidence but not final T024/T041 acceptance. The project is
still blocked on the current-pilot IDE build, API23 waived/unexecuted, actual recipient/no-recipient
share delivery, retained video `Back`/fullscreen/history/background/resize/provider audit, exact clean-source
revision because the worktree is dirty and behind `origin/master`, and any provider/UTF-8 work remains
explicitly deferred to T035/T040. No app crash/ANR claim is made beyond the bounded logcat checks above.

## API37 remediation rerun — 2026-09-16

- Current debug APK SHA-256: `318e8b9be474e30af30d7e42053549108eab2c4f73037b32d006aeba88f1eb7a`.
- Current debug androidTest APK SHA-256: `71bbf8ebe6682420ed365bd4eea7d71b547e9fd8d665c82e3aabfbaae82cd5ab`.
- Environment: API37 `emulator-5554`, launcher `JAVA_HOME=/home/linuxbrew/.linuxbrew/Cellar/openjdk@17/17.0.20.1/libexec`, platform-tools `/home/hanli3/Android/Sdk/platform-tools`.
- `:app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug`: **BUILD SUCCESSFUL**; 84 actionable tasks (3 executed, 81 up-to-date). Log: `/tmp/holysongs-api37-final/build-final.log`.
- Dependency lock regenerated after adding BOM-aligned debug-only `androidx.compose.ui:ui-test-manifest:1.12.1`.
