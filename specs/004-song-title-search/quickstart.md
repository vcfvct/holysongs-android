# Quickstart: Validate Song Title Search

**Feature**: `004-song-title-search`
**Status**: Planned validation guide; commands and scenarios are expected checks, not current pass claims.

Record the Git revision, working-tree state, tool/device versions, APK hash, command or manual steps, expected result, observed result, and pass/fail/blocked status. Do not report API23, IME, timing, or device behavior as passed unless it was actually exercised.

## 1. Prerequisites

From the repository root:

```bash
export JAVA_HOME="/path/to/jdk-17"
export ANDROID_HOME="$HOME/Android/Sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"

java -version
python3 --version
./gradlew --version
git rev-parse HEAD
git status --short
```

Expected:

- Gradle uses JDK 17 and the pinned wrapper/toolchain.
- Android SDK 37 and build-tools 36.0.0 are available.
- Existing user changes are identified and preserved.
- No machine-specific path is committed.

## 2. Run Pure Search and State Tests

Run the focused JVM tests after implementation:

```bash
./gradlew \
  :app:testDebugUnitTest \
  --tests 'com.goodtrendltd.HolySongs.ui.TitleSearchTest' \
  --tests 'com.goodtrendltd.HolySongs.ui.SongListViewModelTest' \
  --no-build-cache --rerun-tasks
```

Expected matching coverage:

- empty and whitespace-only query return all titles;
- leading/trailing query whitespace is ignored;
- Chinese full/partial title, punctuation, digits, and Latin mixed case behave as specified;
- a very long or absent query returns no matches;
- results preserve the input/catalog order;
- titles and lyrics are not mutated;
- no lyric-content, pinyin, fuzzy, normalized, or ranked match is introduced.

Expected session coverage:

- inactive/empty initial state;
- enter starts active/empty;
- update preserves raw text;
- clear stays active and empties text;
- exit/back transition clears and deactivates;
- reopening after exit starts empty;
- existing catalog loading, explicit retry, cancellation, and one-load tests remain passing.

If Gradle's test filtering syntax differs for the installed version, run `./gradlew :app:testDebugUnitTest` and record the executed classes from the XML reports rather than assuming selection.

## 3. Run Clean Host Gates

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

- generated SQLite verification still proves all 414 canonical songs;
- no dependency-lock, schema, permission, package identity, SDK-range, or catalog-content change is needed for search;
- JVM tests, lint, debug APK, and androidTest APK assembly pass;
- the existing catalog/sidebar/navigation tests still compile.

## 4. Run Focused Instrumentation

Select an owner-approved API23-or-newer disposable target explicitly:

```bash
export ANDROID_SERIAL=your-authorized-device
adb devices -l
adb -s "$ANDROID_SERIAL" shell getprop ro.build.version.sdk

./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.goodtrendltd.HolySongs.SongTitleSearchScreenTest
```

Expected focused checks:

1. The overflow menu exposes the labeled Search action.
2. Selecting Search replaces the normal top bar and focuses the editable query field.
3. Typing updates results without submit.
4. Chinese, Latin mixed-case, punctuation, digits, and boundary-whitespace cases match the pure contract.
5. Multiple matches retain catalog order.
6. The alphabet sidebar is absent even when the active query is empty.
7. Clear empties the field, retains search mode/focus, and shows the full catalog.
8. A non-empty unmatched query shows the explicit no-results message with clear/exit still usable.
9. Selecting a result launches the existing lyric screen with exact title/lyric extras.
10. Search exit and system Back both discard the query and restore normal top bar, sidebar, full catalog, and preserved browse position.
11. A new search session starts empty.
12. Activity recreation retains active mode/query/results and does not create a second catalog load for the retained owner.
13. Rapid edits and a long query never show stale results, crash, freeze, or produce an ANR.

If the implemented test class name changes, update this guide and use the actual fully qualified class; do not silently skip the focused suite.

## 5. Measure the 200 ms Requirement

Use the complete packaged 414-song catalog on each exercised endpoint. For representative queries (match, no match, whitespace-trimmed, mixed-case Latin, and rapid edit sequence), record elapsed time from committed input change to settled matching rows/no-results semantics.

Expected:

- every recorded edit settles within 200 ms;
- the final visible list corresponds to the final query;
- measurement excludes initial database loading and measures only an already-Ready catalog;
- results are reported per device/API/IME rather than generalized from host unit-test duration.

An instrumentation idling assertion may prove settled UI, but retain timing output or trace evidence for SC-003. Official framework guidance does not itself prove the target.

## 6. Run Main-Screen and Catalog Regressions

```bash
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.goodtrendltd.HolySongs.SongListScreenTest
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.goodtrendltd.HolySongs.data.BundledSongDatabaseTest
```

Then, when the target is available and authorized, run the complete instrumentation suite:

```bash
./gradlew :app:connectedDebugAndroidTest
```

Expected:

- normal browsing order and all 414 songs remain unchanged;
- A–Z navigation and sidebar behavior remain unchanged outside search;
- loading/error/retry behavior remains unchanged;
- settings, About, app share, lyric navigation, preferences, offline loading, and lifecycle regressions remain passing;
- generated SQLite remains read-only and no search query is persisted.

Existing opt-in compact-sidebar, resize, or preference-mutating tests still require their documented runner arguments and disposable-device setup; an assumption skip is not a pass.

## 7. Manual End-to-End Scenarios

On every exercised device configuration:

1. Launch offline and wait for the full catalog.
2. Scroll to a recognizable browse position.
3. Open Search from the three-dot menu; confirm immediate field focus and usable IME.
4. Search a distinctive Chinese fragment and open the expected lyric.
5. Return, search mixed-case Latin plus punctuation/digits where represented by fixtures, and verify literal case-insensitive behavior.
6. Enter leading/trailing whitespace around a known fragment and verify the same matches.
7. Clear with one action; verify full catalog remains in search mode and the rail remains hidden.
8. Enter an absent string; verify an explicit no-results message, then edit to a valid query.
9. Rotate/recreate; verify active mode and query remain and no stale rows appear.
10. Exit with the top-bar action; reopen and verify an empty field.
11. Repeat, then exit with system Back; verify normal top bar, full catalog, sidebar, and the earlier browse position return.
12. Exercise rapid typing/deleting and a very long query; inspect responsiveness, crash/ANR state, and the final result set.

Repeat relevant layout checks under gesture and three-button navigation, large/resizable windows, cutouts, and supported font scales where the current project validation matrix requires them. Record unavailable cases as blocked or unexecuted.

## 8. Final Evidence

Before acceptance, record:

- exact revision and clean/dirty state;
- JDK, Gradle, AGP, Kotlin/Compose, SDK, device/API, and IME versions;
- focused JVM, clean build, lint, APK/androidTest assembly, focused instrumentation, and retained regression outcomes;
- APK hash and unchanged package/version/permission/catalog checks;
- query fixtures and observed ordered results;
- focus/IME, clear, exit, Back, sidebar, no-results, recreation, and result-navigation outcomes;
- per-edit latency evidence for the full catalog;
- offline status and crash/freeze/ANR inspection;
- API23 and API37 as tested, blocked, waived, or unexecuted—never inferred.

Only attributable observed results may be marked passed.
