# API37 Espresso compatibility correction — 2026-09-16

Read-only Luna investigation8e0b3d0b-8c00-49f8-9f5e-6126f967f86d, workflow
b6936f7b-58af-433e-b365-68aa8bcccf5e, traced the actual pre-assertion
InputManager.getInstance NoSuchMethodException to transitive Espresso3.5.0.
Compose ui-test1.12.1 published POMs request that old version. This is not a product assertion
failure and does not justify skipping UI tests or increasing minSdk.

Master accepts a concrete **test-only** direct pin:
`androidTestImplementation 'androidx.test.espresso:espresso-core:3.7.0'`.
Normal resolution should also select espresso-idling-resource3.7.0; runner1.7.0/core1.7.0/
monitor1.8.0 already match the candidate POM. Review exact lock delta, retain all production pins,
minSdk23/JVM11 and protection permissions. No broad force/override or reflection spoofing.
This supersedes the original direct-test-dependency baseline only for this demonstrated blocker;
it is an implementation compatibility correction, not an owner provider/UX waiver.

## Primary source evidence

Canonical Google Maven prefix:
https://dl.google.com/dl/android/maven2/androidx/test/espresso/espresso-core/

- `3.5.0/espresso-core-3.5.0-sources.jar`: InputManagerEventInjectionStrategy unconditionally
  reflects InputManager.getInstance in initialization, matching the captured API37 stack trace.
- `3.7.0/espresso-core-3.7.0-sources.jar`: same class getInputManager uses
  getContext().getSystemService(InputManager.class) for SDK>=23; only older SDKs use getInstance.
  Master read the downloaded source method independently. Candidate still reflects injectInputEvent;
  compatibility must be proven by actual execution, not assumed from this source change.
- Candidate AAR manifest minSdk21, below retained23; POM matches current runner/monitor stack.
  No aar-metadata.properties was declared; no invented minimum-AGP metadata is claimed.
- Investigation compared3.5.1/3.6.0/3.6.1 sources, which retain the failing initialization;3.7.0
  is the first identified stable correction and the release listed in the inspected metadata.
- Local Android37 InputManager source has no getInstance declaration and does expose the typed
  service used by the candidate. Private artifact/source directory:
  `/tmp/espresso-api37-investigation-20260916/`.

The prior11 generic UI failures and explicit fit/Settings failures remain recorded in
pilot-blocker.md. No runtime pass is established by this recommendation. Rebuild app/test APKs,
verify selected Espresso/idling versions and unchanged production graph, and rerun the previously
blocked cases on the verified disposable Pixel with artifact-linked results.

Parallel production reviewer ddbc233a-8cec-4a5b-98a2-34755a367e26 identified exact app-share
whitespace drift, horizontal-cutout protection and large-font action overflow. Master confirmed
share drift and unrestricted action-row source, and requests explicit safeDrawing inset ownership
without double application. Master also requests a source-backed indicator lifecycle correction:
current snapshotFlow/animation work is not stopped when ON_STOP merely clears the visible value.
Corrections and actual UI results remain pending, not accepted by the reviewer report alone.

## Retest — 2026-09-16

Luna worker8f682d3f-a087-4fc9-901e-5a0f993ad8c4 applied the test-only pin and narrow reviewed
share/inset/overflow/indicator corrections. Lock SHA256 is now
 e2692f08013e183b724f54909678b56c5f1914f048dbb59c1e949d390ffbf5a4.
Expected test-graph changes include core/idling3.7.0, removal of old test annotation/javawriter/
hamcrest-integration and updated test-scoped jsr305/error-prone/compile configurations. Parent
independently compared old/current lock entries for debug/release compile/runtime: all four
production graphs unchanged. No minSdk increase, new production library or force rule.

Normal build/lint/app+test assembly and25 JVM tests passed. On the verified default PixelAPI37
fit window, all4 ReaderPreferences tests passed and SongListScreenTest returned9 passed,0 failed,
2 deliberate assumption skips (compact and resize). Parent parsed instrumentation status codes:
9 successes,2 assumption skips; the runner's final OK(11 tests) is NOT reported as11 passing tests.
The old reflection failure did not recur; real catalog/order/winner/letter/recreation/Settings/
About/share/fit-gesture/indicator assertions executed.

App APK681099498b1ac14febabe009609722c5953c5dddfb3790b6458f3e12ea3cc07e;
test APK9313a476392f96cc7c011db591b8f18576584c2f54f65fb415db76f7bbe0bd65.
Both hashes independently rechecked by parent. Raw status logs:
`/tmp/holysongs-api37-run-20260916101500/{SongListScreenTest,ReaderPreferencesTest}.txt`.
Worker verified original appPrefFile/profileInstalled contents restored and stopped app;
no video/provider probe occurred. Compact/resize/font/alternate-window and fullT024 gates remain.
