# Song Title Search Verification

**Date:** 2026-09-18
**Source state:** `master` working tree based on current HEAD; implementation and feature documents are uncommitted, alongside pre-existing unrelated Specify script changes.

## Host gate

Command:

```text
./gradlew clean :app:verifySongCatalog :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest --no-build-cache --rerun-tasks
```

Result: **PASS** (`BUILD SUCCESSFUL`, 85 actionable tasks executed).

Observed details:

- Python catalog generator: 13 tests passed.
- Generated catalog verification passed.
- Debug APK assembly passed.
- JVM tests passed.
- Android test APK assembly passed.
- Lint completed with 17 warnings and no fatal/error issue; warnings are existing categories such as dependency/version availability, WebView JavaScript review, unused resources, icon density, and KTX suggestions.
- `git diff --check` passed.

Artifacts:

- Debug APK SHA-256: `831598ca84852cc69b715e9935e9c6adbac5f028fd96ea51640aa8aafac9b6cf`
- `app/gradle.lockfile` SHA-256: `fd4f96c9e551600228974b23b859af1c1a3f0b2ba765d367c6e0d91669a9c2fb`
- Canonical `songs.xml` SHA-256: `d6f0e9bed1a2031335613e746e3983842b3462a4d91c4a3d7c04537ba609e85e`

No dependency, manifest, permission, SDK range, catalog source, SQLite schema, preference, or destination Activity was changed by the feature implementation.

## Runtime gate

`adb` was not on PATH, but the SDK binary was found at `/home/hanli3/Android/Sdk/platform-tools/adb`. Running its `devices -l` command produced an empty device list.

The following remain **BLOCKED / unexecuted**:

- focused `SongTitleSearchScreenTest`;
- focused `SongTitleSearchPerformanceTest`;
- retained `SongListScreenTest` and `BundledSongDatabaseTest` runtime suites;
- complete connected instrumentation suite;
- API23 and API37 runtime checks;
- IME-visible one-Back exit behavior;
- gesture/three-button navigation, window/cutout/font-scale checks;
- offline device launch and exact result navigation;
- measured <200 ms device latency;
- runtime crash/freeze/ANR inspection.

Host compilation is not substituted for these checks. Final feature acceptance remains blocked.
