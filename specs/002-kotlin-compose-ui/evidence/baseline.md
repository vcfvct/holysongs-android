# Legacy baseline capture preparation (T008)

**Date:** 2026-09-15, America/New_York.
**T008:** test definition reviewed and compiled. **T010:** unexecuted/blocked until T009 and both
runtime endpoints are available. No API23/API37 JSON golden, overlap screenshot or timing result
has been produced; no capture scenario is claimed passed.

## Implementation and review provenance

Single-file instrumentation: `app/src/androidTest/kotlin/com/goodtrendltd/HolySongs/LegacyBaselineTest.kt`.
Initial worker `fd2893e9-6187-44ca-a3c1-66492e4c4bdd`, workflow
`f1f5a349-a6d4-4156-8ad0-d7aef31b856e`, model `github-copilot/gpt-5.6-luna`.
The master rejected initial runtime-unreliable argument/monitor/share/letter/threading/cleanup
behavior despite successful compilation. Same-model retained worker follow-up
`9f696768-a82d-443b-8c65-ee348cf33278`, workflow
`b8590256-fd18-4138-9f20-3f9cad9aa213`, corrected those findings.
Managed outputs: `legacy-capture-test.md` and `legacy-capture-review-fixes.md`, respectively.

Master follow-up corrected the remaining Settings/About menu inflation off-main, moved share
menu-title reads on-main, enforced exact title/lyric String constants and selected winner/share
payload agreement, compared the entire restored preference map (including unexpected added keys),
and replaced the generic hand-written JSON serializer with platform JSONObject.

Parent validation, using the approved actual Homebrew JDK17 root in JAVA_HOME:
`./gradlew :app:assembleDebugAndroidTest :app:lintDebug --console=plain` — **BUILD SUCCESSFUL**.
Raw host log: `/tmp/holysongs-002-parent-capture-compile.log`. Compilation still reports narrow
unchecked-cast/legacy ListActivity deprecations in this temporary test; no blanket suppression,
production API change or device execution was used to obtain the build result.

## Observation boundary

- Verifies actual target package/component exports and that Main is the legacy ListActivity.
- Reads actual Main-installed adapter order and populated map of all414 winning lyrics, with
  private reflection only for the legacy map, never a copied loader/parser.
- Calls actual SectionIndexer for all26 letters. "Alphabet is exactly A–Z".
  "For valid present/absent results, preserve recorded legacy destinations" — every in-range raw
  destination is retained, while requested-letter presence/match and out-of-range/exception outcomes
  are distinct. Unsafe no-op expectations belong to future migration, not invented baseline passes.
- Exercises real list-click/Menu callbacks for N1, ShareActionProvider target submenu for lyric
  ACTION_SEND and Main's chooser for app sharing. A default-constructor blocking monitor intercepts
  outbound intents before launch; no external recipient/provider is launched. Missing handlers or
  interception produce explicitly blocked observations. A completed capture test with a blocked
  sub-observation does **not** pass the corresponding T010/N2 gate.
- Captures full displayed About text and actual URLSpans, and Settings spinner/reset/switch actions.
  Direct preference fixture write/read controls are labeled separately from real UI observations.
- Snapshots known preference values/types privately, establishes font20/nighttrue for controlled
  launch, closes tracked test Activities, restores known values in finally and checks the full
  map against the original snapshot including unrelated keys. Failures are reported, not marked
  successful cleanup. No original private preference values are emitted into the JSON/logcat.
- UI observations/actions run on the main thread with instrumentation idle synchronization;
  actual device behavior still needs execution. No T010 acceptance is inferred from compile success.

## Future explicit capture invocation (do not run before T009)

After selecting/verifying the disposable endpoint, installing the attributable app/test artifacts,
and recording their hashes, run for each endpoint separately:

```sh
adb -s "$ANDROID_SERIAL" shell am instrument -w -r \
  -e class com.goodtrendltd.HolySongs.LegacyBaselineTest \
  -e legacyBaselineCapture true -e legacyBaselineMutatePrefs true \
  -e sourceRevision "$TESTED_SOURCE_AND_CHECKPOINT" \
  -e apkSha256 "$TESTED_APK_SHA256" \
  com.goodtrendltd.HolySongs.test/androidx.test.runner.AndroidJUnitRunner
adb -s "$ANDROID_SERIAL" exec-out run-as com.goodtrendltd.HolySongs \
  cat files/legacy-baseline.json > "$PRIVATE_RUN_DIR/legacy-baseline.json"
```

Both opt-ins are String command-line arguments; no-argument generic test invocation skips before
preference access/mutation. Source revision must be nonempty and APK hash64 hex characters.
They are explicitly **caller-supplied attribution**, not independent binary verification by the
test. Parent must bind them to the installed artifact, inspect failure/blocked/cleanup fields and
validate the output before transferring any sanitized golden to tracked test assets.

Schema: `holysongs.legacy-baseline.v1`, with capture attribution, actual runtime metadata,
legacyDefinitions, invariant labels, observations U1/U1-LETTER/N1-N2/U3, preferenceFixtureIsolation,
and optional failure. The known422 raw source count is labeled an invariant, not a fake runtime
parser observation. API23 WebView metadata needs separate adb inspection because the public
package query used by the test is API26+. Copy each private result to its dated artifact directory
before a subsequent capture overwrites the device-local file.

## Still required for T010

Run the reviewed capture on the T009 legacy-UI artifact on API23 and API37, review all414 titles,
winning lyrics, letter outcomes and payloads, and freeze separately attributed runtime goldens.
Capture the reported overlap with screenshots/window bounds/theme/bar identification/navigation
mode, startup-to-usable-list timing/method, visible scroll stalls and crash/ANR observations.
No runtime targets or observations may be substituted with host collation or guessed fixtures.

## T010 API37 execution and acceptance — 2026-09-15, 22:42–22:47 local

**Passed for the active API37-only baseline scope.** API23 remains untested under the explicit
owner waiver in execution-decisions.md; the earlier unexecuted records above remain historical.

Parallel workflow `9fd572da-e730-45ff-b0d4-0ff1c710d182`: Luna device runner
`685f59f5-84e2-4e4f-99d5-2264e7dc6112`; fresh MAI reviewer
`c36c6a12-4b8b-4615-ad8a-edf4d2edfefa`. Reviewer found no blockers; master reviewed the actual
JSON and overlap screenshot instead of treating reviewer approval as automatic acceptance.
The prior workflow a351bfec failed script parsing before child launch; the same-protocol retry
was validated first. No failed-attempt device result is claimed.

- Target verified before use/cleanup: emulator-5554, Pixel_10_Pro_XL, Android17/API37,
  sdk_gphone16k_x86_64, fingerprint google/sdk_gphone16k_x86_64/emu64xa16k:17/CP31.260623.012/16064790:user/dev-keys.
- WebView: com.google.android.webview149.0.7827.5; physical window1344×2992; gesture mode2.
- App APK: 3661c49c61e377548129e098215ac6e3c2d96c946b8170a3572f8c4ebfd8c39e.
- Test APK: 39716a31f7cbfbaf98cf0afe9ae2f058cbe02a6ecf3f6546f4c0320ca1174df2.
- Source: f62519b plus dirty owner/feature002 work; capture-source hash
  0a0cc913a603846c917d11769bc4040c37d3fb79c06d01f45e9cbba2fc16f94d.
- Both selected APK installs returned Success without uninstall/signature conflict.
- Explicit opt-in instrumentation returned **OK (1 test)**, INSTRUMENTATION_CODE -1;
  no capture failure, blocked share observation or fixture cleanup error.

### Observed parity data

All414 ordered positions/titles/winning lyrics were captured. Master independently compared every
winning lyric and title against the frozen raw-source snapshot after literal U+0020 preprocessing
and last-entry resolution: exact agreement, including all duplicate winners. Raw422 remains a
labeled input invariant, not an invented runtime parser count.

All26 A–Z lookups returned in-range destinations:22 present letters matched their requested
initial; absent I→60, O→126, U→231 and V→231 retain their actual legacy destinations. No unsafe
or exception result occurred. Future empty/invalid-result no-op expectations remain separate.
"Alphabet is exactly A–Z"; "For valid present/absent results, preserve recorded legacy destinations".

Actual N1 routes, lyric share and app chooser/nested share were captured. Lyric subject/text match
selection/winner; app subject is 敬拜赞美诗 and text/links/whitespace match the original builder.
No recipient/provider was launched (payload capture is not actual delivery acceptance). About's
full156-character text and two URLSpans were observed. Settings selected16/18/20/22/24/26/28/30,
reset20 and toggled nightMode true→false→true through actual controls.

Sanitized frozen fixture: `app/src/androidTest/assets/legacy-api37.json`, SHA-256
cfae17540f5e958a9b32125158197cc55df5c93f54aed25ef5b84ce2047c5437.
Only the share-target display label was removed; no original private preferences were present.
Raw JSON SHA-256109eb0c010d70483eb3278d5f2156277c26fa12f78bee138adcb66dfb3f75d4f.
No API23 fixture was created. This is the changed-toolchain legacy UI, not the original merged APK.

### Overlap, responsiveness and cleanup

Fresh offline screenshot/hierarchy demonstrates ListView bounds(0,0)–(1344,2992); first row
(45,45)–(225,133) lies in the status region; next row(45,225)–(465,313) intersects the Holo app
bar at(0,159)–(1344,303). Master visually confirmed obscured/overlapping title/list content.
Stored-nighttrue dark Holo, gesture mode and window bounds are recorded. No assumed Compose fix.

Process-stopped fresh-data offline launch-to-first-observed-usable-list was3626.283ms, measured
with host monotonic_ns around launch and UIAutomator hierarchy polling. This includes adb/dump
observation overhead and is an upper-bound observation, not precise app startup latency or an SLA;
am start -W alone was not used as Ready timing. Five bounded swipe observations progressed through
the list with no visible stall/focus loss. App/time-window-scoped diagnostics had0 FATAL EXCEPTION
and0 ANR-in markers; this is limited scenario evidence, not a universal no-crash claim.

Before fresh setup the worker privately backed up app shared_prefs/files. Only the authorized
app's disposable data was cleared; Wi-Fi/mobile data were disabled for offline checks. Original
private files were restored with matching archive hash, network settings restored, font/navigation
unchanged, app stopped. No unrelated app/data/global logs were cleared or live provider probed.

Private evidence root: `$HOME/.local/state/holysongs-android/verification/
api37-legacy-capture-20260915T224205-0400-1690361/`; includes instrumentation-raw.txt,
legacy-baseline.json, fresh-first-launch-usable.png, fresh-first-launch-timing.txt,
scroll-observations.txt, cleanup-restoration-summary.txt and artifact-sha256sums.txt.
API37 three-button/large-resizable/font cases are still required for their migrated-screen gates;
this baseline does not claim those future scenarios or the complete feature passed.
