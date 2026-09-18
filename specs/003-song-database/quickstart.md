# Quickstart: Validate the SQLite Song Catalog

**Feature**: `003-song-database`
**Status**: Planned validation guide; commands that depend on feature implementation are expected targets, not current pass claims.

Use this guide only after the corresponding implementation checkpoint exists. Record revision, working-tree state, tool versions, device identity, APK hash, command, expected result, observed result, and pass/fail/blocked status. A build success does not substitute for catalog parity or runtime evidence.

## 1. Prerequisites

Use the existing project prerequisites plus Python 3 with its standard-library `sqlite3` module:

```bash
export JAVA_HOME="/path/to/jdk-17"
export ANDROID_HOME="$HOME/Android/Sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"

java -version
python3 --version
python3 - <<'PY'
import sqlite3
print(sqlite3.sqlite_version)
PY
./gradlew --version
git rev-parse HEAD
git status --short
```

Expected:

- Gradle runs with JDK 17 and the project's pinned wrapper.
- Python 3 can import `sqlite3`; no separate `sqlite3` command is required.
- Machine-specific paths remain untracked.

## 2. Validate Canonical XML Before Runtime Changes

Run generator tests and validate the production source:

```bash
python3 -m unittest discover -s tools/catalog/tests -p 'test_*.py'
python3 tools/catalog/generate_song_database.py generate \
  --source app/src/main/assets/songs.xml \
  --output /tmp/holysongs-validation.db \
  --expected-count 414
python3 tools/catalog/generate_song_database.py verify \
  --source app/src/main/assets/songs.xml \
  --database /tmp/holysongs-validation.db \
  --baseline app/src/test/resources/catalog-baseline.xml \
  --expected-count 414
rm -f /tmp/holysongs-validation.db
```

Expected:

- canonical source count is 414;
- all effective titles are unique;
- the eight earlier shadowed entries are absent;
- all retained duplicate-title lyrics equal the old last-entry winners;
- all 414 effective title/lyric pairs exactly match the historical baseline;
- newline, blank-line, U+3000, Chinese/Latin, leading/trailing newline, and long-lyric fixtures pass;
- malformed, nested, missing, empty, duplicate, and constraint fixtures fail without publishing partial output.

Do not modify `app/src/test/resources/catalog-baseline.xml` or historical feature 001/002 evidence to make this pass.

## 3. Generate and Inspect the Gradle Output

```bash
./gradlew clean :app:generateSongDatabase :app:verifySongCatalog --no-build-cache --rerun-tasks

DB=app/build/generated/songCatalog/assets/songs.db
python3 tools/catalog/generate_song_database.py verify \
  --source app/src/main/assets/songs.xml \
  --database "$DB" \
  --baseline app/src/test/resources/catalog-baseline.xml \
  --expected-count 414
```

Use Python for a bounded inspection when needed:

```bash
python3 - "$DB" <<'PY'
import sqlite3, sys
with sqlite3.connect(f"file:{sys.argv[1]}?mode=ro", uri=True) as db:
    print("user_version", db.execute("PRAGMA user_version").fetchone()[0])
    print("integrity", db.execute("PRAGMA integrity_check").fetchone()[0])
    print("metadata", db.execute(
        "SELECT schema_version, transform_version, source_sha256, song_count "
        "FROM catalog_metadata WHERE singleton_id = 1"
    ).fetchone())
    print("rows", db.execute("SELECT COUNT(*) FROM songs").fetchone()[0])
    print("bounds", db.execute("SELECT MIN(source_order), MAX(source_order) FROM songs").fetchone())
PY
```

Expected: schema/user version 1, transform version 1, integrity `ok`, metadata count 414, row count 414, and source-order bounds `0, 413`.

Run generation twice and compare through `verify`; do not require binary SHA equality across SQLite versions.

## 4. Run Host Build and Regression Gates

```bash
./gradlew clean \
  :app:verifySongCatalog \
  :app:assembleDebug \
  :app:testDebugUnitTest \
  :app:lintDebug \
  :app:assembleDebugAndroidTest \
  --no-build-cache --rerun-tasks
```

Expected:

- generation runs before asset packaging;
- JVM tests compare all 414 SQLite-bound rows with the frozen effective baseline;
- catalog ordering, initials, section navigation, immutability, one-load ownership, cancellation, and retry tests pass;
- lint and both APK assemblies pass;
- `app/gradle.lockfile` is unchanged because the design adds no dependency.

Confirm clean regeneration rather than relying on stale output:

```bash
rm -rf app/build/generated/songCatalog
./gradlew :app:assembleDebug --no-build-cache --rerun-tasks

test -f app/build/generated/songCatalog/assets/songs.db
```

Expected: the database is regenerated and assembly cannot succeed with invalid canonical XML.

## 5. Inspect the Packaged APK

```bash
APK=app/build/outputs/apk/debug/app-debug.apk
rm -f /tmp/holysongs-packaged.db
unzip -p "$APK" assets/songs.db > /tmp/holysongs-packaged.db

python3 tools/catalog/generate_song_database.py verify \
  --source app/src/main/assets/songs.xml \
  --database /tmp/holysongs-packaged.db \
  --baseline app/src/test/resources/catalog-baseline.xml \
  --expected-count 414

unzip -l "$APK" | grep -E 'assets/(songs\.xml|songs\.db)$'
sha256sum "$APK"
rm -f /tmp/holysongs-packaged.db
```

Expected:

- `assets/songs.db` exists and passes logical verification;
- `assets/songs.xml` may remain packaged as the canonical source, but runtime code has no path that parses it;
- application ID, SDK range, permissions, and signing behavior remain unchanged from feature 002.

Also inspect the merged manifest and APK permissions using the existing README commands. No permission is added by this feature.

## 6. Run Actual Database Reader Instrumentation

Select only an owner-approved disposable API37 device/emulator:

```bash
export ANDROID_SERIAL=your-authorized-device
adb devices -l
adb -s "$ANDROID_SERIAL" shell getprop ro.build.version.sdk

./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.goodtrendltd.HolySongs.data.BundledSongDatabaseTest
```

Expected tests cover:

- packaged asset copy, read-only open, schema/metadata validation, and all 414 rows;
- exact prior duplicate winners and representative whitespace/newline cases;
- temporary-file cleanup after success, malformed/corrupt DB, query failure, and cancellation;
- no writable handle or partial `SongCatalog` exposure;
- no XML parse/open through the production loader.

If the planned test class name changes, update this guide and use the implemented fully qualified name. A JVM mock is not a substitute for this Android SQLite check.

## 7. Run Retained Reader Regression Scenarios

Run the focused existing suites first, then the full instrumentation set:

```bash
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.goodtrendltd.HolySongs.SongListScreenTest
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.goodtrendltd.HolySongs.LyricScreenTest
./gradlew :app:connectedDebugAndroidTest
```

Manual/API37 checks:

1. Disable connectivity before first launch and confirm the list reaches Ready with 414 songs.
2. Navigate through A–Z and verify the established destinations/order.
3. Open each of the eight former duplicate titles and compare the displayed lyric with the frozen winner.
4. Open representative Chinese, multiline, blank-line, full-width-space, and longest lyrics; inspect beginning, blank lines, and final line.
5. Share a representative lyric and verify the exact existing payload.
6. Rotate/recreate Main while loading and after scrolling; verify one load per ViewModel owner and restored list state.
7. Background/return and perform the existing process-recreation scenario; verify no stale/empty catalog.
8. Exercise explicit retry with a controlled test failure; cancellation must not display a false database error.
9. Record launch-to-usable-list observation and visible scroll stalls; no numerical SLA is invented.
10. Inspect test-session logcat/crash and app ANR evidence. Catalog-attributable crashes/ANRs must be zero.

API23 remains the supported minimum but its feature-002 runtime execution was owner-waived. Do not report API23 as tested unless it is actually executed for this feature.

## 8. Upgrade Check

On an authorized disposable profile with the previously accepted app installed and compatible signing:

```bash
adb -s "$ANDROID_SERIAL" install -r "$APK"
adb -s "$ANDROID_SERIAL" shell am start -n com.goodtrendltd.HolySongs/.MainActivity
```

Expected:

- no uninstall, data clear, network, or manual database reset is required;
- the packaged catalog loads because each load uses a fresh temporary copy;
- `appPrefFile`, `fontSize`, and `nightMode` values remain unchanged;
- reader navigation and shares remain functional.

A debug signing mismatch or unavailable prior artifact blocks genuine upgrade evidence; it must not be relabeled as a pass.

## 9. Maintainer Add/Edit/Delete Drill

Use a temporary copy or isolated worktree, never the production file without an approved content change:

1. Add one unique multiline song; generation succeeds and row count increases in fixture mode.
2. Edit its lyric; regeneration contains only the edited text.
3. Rename it; old title identity disappears and new title identity appears.
4. Delete it; regeneration removes the row.
5. Add an effective duplicate such as a title differing only by ASCII spaces; generation fails with both positions/title identified.
6. Restore the source and verify production count/parity returns to 414.

This drill validates maintainer workflow only. In-app song editing remains out of scope.

## 10. Final Evidence

Before acceptance, record:

- exact Git revision and clean/dirty status;
- Python/SQLite, JDK, Gradle, AGP, SDK, and device versions;
- generator, unit, lint, APK, androidTest assembly, focused runtime, and full runtime outcomes;
- APK hash and logical packaged-database verification;
- all 414 parity result and eight former duplicate winners;
- offline launch, lifecycle, share, timing/stall, crash/ANR, and upgrade outcomes;
- API23 as tested or explicitly untested/waived, never inferred;
- any blocked IDE/device evidence.

Only attributable observed results may be marked passed.
