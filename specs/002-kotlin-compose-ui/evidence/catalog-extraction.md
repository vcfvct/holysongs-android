# T016/T017 — catalog/index extraction

**2026-09-15 implementation continuation.** Luna worker2c6edd20-e159-4f0c-976d-ca38097b94b1,
workflow a3c49fbf-28ab-4cd8-a115-56df435163ad. Master reviewed all3 production files and ran
fresh focused tests on the actual checkout. No screen, helper, asset, storage, manifest or build
configuration was changed for this extraction.

- SongCatalog defensively copies/freezes map, title and initial lists.
- SongCatalogLoader performs full UTF8 read/close on injected dispatcher, literal U+0020 removal,
  actual XMLParser extraction, source-order HashMap overwrite, existing ChineseCharComp ordering,
  guarded HanziHelper initials and catalog-scoped LegacySectionIndex. Errors/cancellation are not
  silently truncated into success; empty input catalog is safe.
- LegacySectionIndex retains actual approximation/binary-search/cache semantics. The approximation
  loops deliberately retain case-sensitive comparisons while the binary search ignores case, as
  in Java. A worker first draft changed that detail; the frozen absent-I fixture failed61 vs60.
  Correcting the extraction back to source semantics restored all26 positions. No sorting/index
  algorithm redesign or invented absent-letter policy was introduced. Invalid section inputs,
  empty catalogs, unresolvable reads and unsafe out-of-range destinations yield null.

Master found the integrated loader test had three Kotlin string-concatenation syntax errors and
an incorrect hardcoded first-initial A assumption on the host. Fixed the operator placement and
removed that runtime-order assumption (full comparator and initials assertions remain). Earlier
worker claims of only missing-type compile diagnostics were incomplete; this record corrects them.

Master validation:

```sh
JAVA_HOME="$(brew --prefix openjdk@17)/libexec" ./gradlew \
  -I /tmp/holysongs-parent-catalog-focused.init.gradle \
  :app:testDebugUnitTest --no-build-cache --rerun-tasks --console=plain
```

**Passed21 tests,0 failures/errors:** original Java12, loader6, section index3;26 actionable tasks
executed. This uses actual repository tests/production, not the worker's separately patched temp
copy. The parent init script excludes ONLY pending ui/SongListViewModelTest.kt from this focused
compile; no test exclusion is committed. The worker's earlier exclude script also excluded the
loader test, so its output was not adopted as joint coverage. Full-suite green still requires T019;
this is explicitly focused T016/T017 evidence. Raw log `/tmp/holysongs-parent-catalog-focused.log`.

T016/T017 accepted; later integrated Main runtime must recheck actual native order, all winners
and section destinations against the API37 fixture. Host Collator is not Android parity. No device
execution or new UI pass is claimed by these host results; API23 remains owner-waived.
