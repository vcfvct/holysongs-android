# US2 reader characterization evidence

Status: source-only characterization is documented and the JUnit checks are present, but runtime acceptance for API14/API37 verification remains blocked and is not claimed here.

- requirement/scenario ID: C3-RDR-000 [FR-008|US2|JVM-characterization]
- date: 2026-09-15
- source revision: base 634f98624b654fe9926c7b1c8186fbad56df6673 plus uncommitted build migration and new characterization tests; not an exact-revision replay
- APK SHA-256: n/a; no device-runtime artifact is claimed or generated in this step
- toolchain: JDK17 was selected by session environment and Gradle 9.6.0 was invoked. The later pre-PR review found a local daemon-JVM criteria file selecting JDK25, so the actual daemon runtime of this earlier test run is not established by JAVA_HOME alone. No API14/API37 runtime execution was recorded.
- device/emulator identifier: n/a
- Android API/version: n/a
- WebView version where applicable: n/a
- prerequisites: repo checkout; local JDK17; `./gradlew :app:testDebugUnitTest`
- steps: run the catalog/helper JVM characterization checks under `app/src/test/java`, then do not infer app runtime acceptance from source-only assertions
- expected result: source-level catalog count, duplicate-title inventory and helper collation tests record the existing legacy behavior without modifying the app logic; runtime acceptance remains a separate blocked check
- observed result: these characterization tests are present and pass under the local JVM harness; no devices, API14/API37 execution, or end-to-end app runtime acceptance are claimed here
- status: blocked
- evidence path: `app/src/test/java/com/goodtrendltd/HolySongs/SongCatalogTest.java`, `app/src/test/java/com/goodtrendltd/HolySongs/helpers/HanziHelperTest.java`, `app/src/test/java/com/goodtrendltd/HolySongs/helpers/ChineseCharCompTest.java`, and this file
- blocker/limitation: no device/emulator app scenario was executed; these new tests provide partial JVM characterization only. The local AVD inventory does not establish runtime acceptance.
- maintainer approval reference for accepted provider changes or limitations: n/a; no runtime acceptance claim

## Pre-PR review correction — 2026-09-15

T018–T020 were previously checked prematurely and are now unchecked (15/40 complete).
Passing the current tests does not satisfy their full task descriptions:

- T018 lacks assertions for both source positions of every duplicate pair, full source-order coverage and sufficiently exact representative lyric/line-break fixtures.
- T019 needs an unambiguous multi-reading character fixture and stronger comparator-property coverage; native Android ordering remains a device check.
- T020 is only a stub: the complete C3/sharing scenarios, preference fixtures, per-runtime sidebar expectations, timing/scroll observations and crash/ANR checks have not been defined/executed as required.
- T021 and all later US2 tasks remain unchecked. No production reader changes are included in this preparation slice.

The prior `blocked` record is retained rather than rewritten as a runtime pass. See `pr-checkpoint.md` for the subsequent clean committed-source host replay.

## Preservation-only notes

- `appPrefFile`, `fontSize`, and `nightMode` remain legacy identifiers and are preserved as data-model references only; no runtime preference acceptance is claimed here.
- The catalog and helper tests document actual source behavior only. They intentionally do not replace the required API14/API37 device checks or any runtime acceptance work.
