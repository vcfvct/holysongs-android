# T018/T019 — preferences and loading state

## Implementation and review (2026-09-15, local)

Luna worker38d2b9d1-bd76-456d-bb66-a1c41de5e0ca implemented only ReaderPreferences.kt and
SongListViewModel.kt. T018 device tests preceded T019 implementation. Worker reported4 API37
preference tests and25 full JVM tests passing, with no source exclusions. Main/reader remain Java.

Master review accepted the ViewModel's one-load owner, explicit error-only retry, immutable
StateFlow, cancellation propagation and active-owner publication checks. The worker's adapter
had two source-supported race risks: independent callback read jobs could emit out of order;
registration inside withContext could finish just as cancellation prevented entry into awaitClose.
Master replaced that observation loop with a cold flow on the injected dispatcher, a conflated
refresh-request Channel and serial read/emission inside a registration-paired try/finally.
Android callbacks only enqueue refreshes; no per-callback read job, stale-result overwrite or
registration-to-cleanup suspension gap remains. Duplicate snapshots are distinctUntilChanged.

Constructor retains applicationContext without opening storage. Original appPrefFile and typed
fontSize/nightMode semantics, UI choices/reset, display-only fallback, raw corrupt/missing values
and unrelated state are retained. Explicit writers use sparse apply calls; no durability promise,
new store/key, automatic repair or disk-read-on-composition was introduced.

## Parent validation after correction

- `JAVA_HOME=<actual Homebrew JDK17 libexec> ./gradlew :app:testDebugUnitTest :app:assembleDebug
  :app:assembleDebugAndroidTest :app:lintDebug --console=plain` — passed.
- Full JVM reports:25 tests (Java12, loader6, index3, ViewModel4),0 failures/errors/skips.
  No external init script or pending-test exclusion in this full run.
- Selected API37 device reverified: emulator-5554, Pixel_10_Pro_XL, actual API37.
- App APK94b2c9ee450c9a496750d98e12c16a4431b38ae68f95d4dcf9a74773220172bf;
  test APKa6667a3e3900e3f57bd8736c7b0829c4463ef9e7d807ff06da9038a875f4e2a4.
- Both installed with selected adb install -r; direct `am instrument -w -r -e class
  com.goodtrendltd.HolySongs.ReaderPreferencesTest -e readerPreferencesMutate true
  com.goodtrendltd.HolySongs.test/androidx.test.runner.AndroidJUnitRunner` returned
  **OK (4 tests)**, INSTRUMENTATION_CODE -1, no skips/failures.
- Raw typed-map restoration assertions passed. Parent's first extra check compared tar envelope
  hashes and failed due to changed filesystem timestamps. Inspecting extracted file names and
  per-file SHA256 confirmed exact original preference XML content, not data drift. This metadata
  comparison failure is recorded rather than hidden; no original preference values were logged.
- App was force-stopped before backup and after tests. Direct instrumentation left the installed
  package intact. The worker's earlier connected Gradle runner removed it during cleanup; worker
  reinstalled/restored its private snapshot and reported byte invariance. Use selected direct adb
  runs for subsequent fixtures to avoid surprise runner cleanup, preserving backups regardless.

Private parent artifacts: `$HOME/.local/state/holysongs-android/verification/parent-prefs-20260915T233353/`
(APKs, instrumentation.txt, prefs-before/after.tar, restoration.txt). Host log:
`/tmp/holysongs-parent-prefs-state-validation.log`. Source: HEADf62519b plus current dirty
feature002 and retained owner work. JDK17 launcher/compilation, owner-approved Java25 daemon.

T018/T019 accepted from source review and these actual results. The serial-observation race fix
has source reasoning plus existing device regression coverage, not a dedicated stress-test claim.
API23 remains owner-waived; normal process persistence, reactive Compose rendering, UI labels,
list state across screen transitions and final feature acceptance remain later gates.
