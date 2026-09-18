# US1 Search Evidence

**Date:** 2026-09-18
**Revision:** `master` working tree based on current HEAD; feature files and pre-existing Specify script changes are uncommitted.
**Status:** Host checks passed; runtime acceptance blocked because no Android device/emulator is attached.

## Test-first evidence

The focused JVM command initially failed at test compilation because `filterTitlesByQuery`, `SearchSession`, and ViewModel search events did not yet exist. This was the expected missing-behavior failure before implementation.

After implementation:

```text
./gradlew :app:testDebugUnitTest \
  --tests 'com.goodtrendltd.HolySongs.ui.TitleSearchTest' \
  --tests 'com.goodtrendltd.HolySongs.ui.SongListViewModelTest' \
  --no-build-cache --rerun-tasks
```

Result: **PASS** (`BUILD SUCCESSFUL`). Pure matching and ViewModel transition tests executed.

`./gradlew :app:assembleDebugAndroidTest --no-build-cache --rerun-tasks` also passed, proving the new real-Activity search and performance instrumentation classes compile.

## Runtime status

`/home/hanli3/Android/Sdk/platform-tools/adb devices -l` reported no attached devices. Therefore these required observations remain **blocked/unexecuted**:

- actual input focus and IME behavior;
- live row order and exact lyric Activity launch on device;
- configuration recreation behavior on Android;
- full 414-song edit-to-settled latency below 200 ms;
- rapid-edit stale-result and crash/ANR observations;
- API23/API37 endpoint evidence.

No runtime pass is claimed from APK assembly.
