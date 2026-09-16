# T003 — Dedicated runtime inventory

**Date:** 2026-09-15 (America/New_York), follow-up after 21:27.
**Inventory complete; runtime acceptance blocked/incomplete.** Recording a missing configuration
is not evidence that its scenarios passed. This inventory permits host-side preparation; T010
still requires baseline capture on both endpoints before Main/index replacement.

## Ownership and explicit selection

The owner identified the Android Studio Pixel10 Pro XL AVD and answered "yes, it is disposbale"
to permission to install the debug app and run preference-changing tests. This authorizes testing
on that disposable AVD only; no production phone, other AVD, global app disabling, release signing
or publishing is authorized. Original approval spelling retained for attribution.

`adb devices -l` reported one connected device, `emulator-5554`.
`adb -s emulator-5554 emu avd name` returned `Pixel_10_Pro_XL` / `OK`.
Every direct device command must explicitly select that serial and reverify identity on reconnection.
Before AGP connected-device tasks, verify no unintended devices are connected and inspect the
actual report targets; do not assume ANDROID_SERIAL filters every AGP operation.

## Observed endpoint

| Item | Actual read-only observation |
|---|---|
| AVD | Pixel_10_Pro_XL |
| Selected serial | emulator-5554 |
| Model/product | sdk_gphone16k_x86_64 |
| Runtime API | 37 |
| Android release | 17 |
| Build fingerprint | google/sdk_gphone16k_x86_64/emu64xa16k:17/CP31.260623.012/16064790:user/dev-keys |
| WebView | com.google.android.webview 149.0.7827.5; versionCode782700509; targetSdk36 |
| Display | Physical1344×2992 |
| Navigation setting | secure navigation_mode=2 (gesture-mode setting; behavior not exercised) |
| Installed app | com.goodtrendltd.HolySongs, version8/2.5, min14/target37 |
| Installed app attribution | Unknown APK hash/source; not adopted as the test baseline |
| Share intent query | 11 activities returned for ACTION_SEND text/plain; actual delivery unexecuted |

Static AVD configuration targets android-37.1/Play Store/16KB pages, but the runtime API37 and
Android17 observations above, not that configuration string, determine endpoint identity.
WebView provider inspection is not a live video-provider audit.

## Matrix, fixture isolation and blockers

| Required configuration | Availability / next action |
|---|---|
| API23 Android6 phone/emulator | No API23 AVD listed or device connected. **Blocked** pending dedicated target/provisioning and ownership confirmation. Do not substitute API37 or host Collator fixtures. |
| API37 phone, gestures | Authorized Pixel AVD connected. Capture artifact-specific runtime behavior at T010. |
| API37 phone, three-button navigation | Not exercised/configured. Record original setting, switch only on disposable target, verify mode and restore afterward. |
| API37 large/resizable | No separate target selected; resizable/large-window configuration on the authorized AVD not yet established. Required window cases remain blocked until verified. |
| Fresh install offline before first launch | Existing app detected. No fresh-install assertion made. Before execution, select attributable debug APK, preserve test fixtures, and establish a deliberately fresh disposable installation/profile with networking disabled before launch. Stop on signing conflicts rather than silently uninstalling. |
| Legacy preference fixtures | Allowed only on selected disposable AVD. Instrumentation must snapshot all original values/types and restore in finally/teardown; snapshot storage stays private. Do not add fixture keys/data to production assets/storage. |
| Share recipient | Handler query shows candidates, not delivered-payload proof. Choose an actual recipient and verify payload without sending to external contacts/accounts. |
| No-recipient case | Current image has multiple handlers. Dedicated no-recipient test profile/fixture not available; **blocked** for actual absence scenario. Do not disable unrelated apps to manufacture it. |

No installation, data clearing, preference mutation, network/navigation/window-setting change,
provider probe or app launch was performed during this inventory. API37 ownership is verified;
API23 ownership and all absent runtime configurations remain explicit blockers for their tasks.

## Scenario record

- Requirement/scenario: T003 / FR-013 inventory (not B/U/N runtime acceptance).
- Source: f62519b4bff420ab3f78b1d544732c25683f4772 plus working-tree checkpoint
  `20260915T212750` (environment.md); no APK under test.
- Toolchain: SDK adb/platform-tools37.0.1; emulator installed; Studio2026.1.4 metadata.
- Prerequisites/steps: owner confirmation; adb device list, AVD name, getprop API/release/build/model,
  dumpsys webviewupdate, wm size, navigation_mode read, package metadata and share-handler query.
- Expected/observed: selected disposable API37 target identified as above; full required matrix
  not available. Inventory passed; missing-target runtime scenarios blocked, not passed.
- Evidence: this sanitized record; approval reference is the current-session owner confirmation.
