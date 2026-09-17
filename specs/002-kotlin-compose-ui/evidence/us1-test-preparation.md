# US1 test preparation / master review

## 2026-09-15 — isolated parallel test-authoring batch

Workflow4e4d5728-8156-4a6d-a777-080f3cf98bf9 prepared T011–T015 in three private non-Git source
snapshots. No stash, commits or managed worktrees were created from the dirty owner checkout.
Lane board/input hashes: `$HOME/.local/state/holysongs-android/lanes/us1-tests-20260915T225555/`.
Workers only authored their assigned new test/scenario files; master verified every original
snapshot input hash unchanged in all three lanes before integration.

Initial drafts were not accepted automatically. Master found missing short-read/immutability/
nonempty-index/in-progress-cancellation coverage, faulty preference defaults/scheduler/cleanup,
and UI assertions that could pass broken letter navigation/replay or fail valid fit-mode gestures.
Corrective workflow c146e376-043a-46e1-989d-429f6eec7b67 resumed the same-model children.

## T011/T013 accepted as test definitions, not passing runtime results

Luna child e46c6822-8006-4d0e-8b99-03d42c85714e supplied corrected tests; master read all three
files and integrated only:

- app/src/test/kotlin/com/goodtrendltd/HolySongs/data/SongCatalogLoaderTest.kt
- app/src/test/kotlin/com/goodtrendltd/HolySongs/data/LegacySectionIndexTest.kt
- app/src/test/kotlin/com/goodtrendltd/HolySongs/ui/SongListViewModelTest.kt

Coverage includes full independent raw422/effective414/duplicate-winner fixtures, literal U+0020,
short2-byte reads with available0 and stream cleanup, I/O/cancel/partial-invalid input, immutable
collections, real parser first-TEXT_NODE behavior, HashMap/comparator key order, all26 captured
API37 destinations, nonempty invalid sections and safe edge catalogs. ViewModel tests cover
Loading/Ready/Error/retry rules, injected dispatcher delegation, owner retention, subscriptions,
queued and in-progress owner cancellation plus store cleanup. Dispatcher delegation is not itself
a physical-thread measurement. Native-order golden is never inferred from host Collator.

Worker compile attempts fail on missing planned production types only; those are explicit
pre-implementation blockers, not a runtime regression claim. Existing12 Java tests previously
passed. New complete JVM-suite green requires T016/T017/T019, and device acceptance is still T024.

T012 and T014/T015 drafts remain under master correction/review in isolated snapshots. No
checkbox for those tasks is earned by worker completion alone. T016/T017 may proceed against
accepted T011/T010 prerequisites while independent preference/UI test preparation is corrected;
T018 requires T012, T019 requires T013 and T016/T017, and T020 onward still requires T014/T015.
No screen replacement or UI acceptance has occurred.

## Parent corrections after second test review

The revised MAI preference draft still constructed an independent TestCoroutineScheduler without
starting/draining its collector scheduler, bypassed the tracker through applicationContext, and
kept collector cleanup outside finally. Master rewrote the assigned test as4 bounded actual
storage/Flow tests: real Main-context collection plus IO initialization, timed Channel observations,
full raw fixture table/all choices, wrong types/missing values, no auto writes, real external
preference callbacks, register-before-read and exact paired unregister/resubscribe. A test
ContextWrapper retains applicationContext interception while delegating to actual target storage.
All jobs cancel/join before exact original typed-map restoration in finally; created keys are
removed and unrelated values verified, no original private data logged. Genuine process-death
persistence is still a later runtime scenario, not an adapter re-subscription claim.

The parent-corrected ReaderPreferencesTest.kt was integrated with SHA-256
0bc0d2d779ba77c96695e6c9a7693ce9d6026fbd0e9bdee88b3c2a570b428067.
Snapshot compilation still reports missing ReaderPreferences/ReaderPreferenceSnapshot APIs;
no device pass is claimed. T012 accepted as the test-definition task; T018 must compile and
execute these tests on the disposable API37 target with readerPreferencesMutate=true.

T011's syntax/host-order corrections and actual21-test focused pass are recorded in
catalog-extraction.md. T014/T015 UI drafts remain isolated pending further master review.
