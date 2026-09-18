# US2 Clear and Exit Evidence

**Date:** 2026-09-18
**Status:** JVM transition checks passed; Android interaction checks blocked by no attached device/emulator.

## Observed host evidence

`SongListViewModelTest` passes for:

- clear => active/empty;
- repeated clear remains active/empty;
- exit => inactive/empty;
- repeated exit remains inactive/empty;
- reopening begins active/empty;
- inactive query updates do not retain stale text.

The real-Activity instrumentation source covering conditional clear, retained focus, top-bar exit, Back exit, restoration, and empty reopened sessions compiles in `:app:assembleDebugAndroidTest`.

## Blocked runtime evidence

No Android target appeared in `/home/hanli3/Android/Sdk/platform-tools/adb devices -l`. Top-bar exit, first system-Back behavior with the IME visible, sidebar/top-bar restoration, browse-position restoration, and no-replay behavior remain unexecuted. In particular, host compilation does not prove the requirement that one Back operation exits while the IME is visible.
