# User Story 1 — SQLite Runtime Parity Evidence

**Date**: 2026-09-17
**Checkout**: `master` at base `1aee35539d08fbd5bf904a88d988083aab88b604` plus uncommitted feature-003 implementation
**Status**: Host parity/build passed; device runtime blocked because `adb devices -l` listed no device

## Host results

| Command/check | Result |
|---|---|
| `python3 -m unittest tools.catalog.tests.test_production_catalog` | PASS, 2 tests; generated logical rows equal all 414 historical effective winners |
| `./gradlew :app:generateSongDatabase :app:verifySongCatalog --no-build-cache --rerun-tasks` | PASS, 3 tasks |
| `./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest --no-build-cache --rerun-tasks` | PASS, 73 tasks |
| Packaged `assets/songs.db` verification against canonical XML and historical baseline | PASS |

## Packaged artifact

- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- SHA-256 after final host recheck: `5e5fd9af74d2aefb55bfc8c454f817705aa81a6f5c4bf201eadca656d333c463`
- `assets/songs.db`: present, 196608 bytes
- `assets/songs.xml`: present, 230174 bytes; canonical source retained but production Kotlin no longer opens it
- SQLite `user_version`: 1
- `PRAGMA integrity_check`: `ok`
- Songs: 414
- Source-order bounds: 0–413
- Metadata: schema 1, transform 1, source SHA-256 `d6f0e9bed1a2031335613e746e3983842b3462a4d91c4a3d7c04537ba609e85e`, count 414

## Runtime status

`adb devices -l` returned no connected target and `ANDROID_SERIAL` was unset. Therefore the following are **blocked/unexecuted**, not passed:

- `BundledSongDatabaseTest` execution on Android;
- `SongListScreenTest` execution against the generated database;
- offline API37 first launch and former-duplicate reader walkthrough;
- runtime timing/stall and crash/ANR observation.

The androidTest APK compiled successfully. Missing device evidence prevents a full US1 runtime pass but does not invalidate the host implementation evidence above.
