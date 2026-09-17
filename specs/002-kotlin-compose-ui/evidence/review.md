# Partial source review (US2 implementation slice)

Date: 2026-09-16. This is not final T042/T044 acceptance; US2/US3 runtime gates remain open.

## Reference checks and cleanup

The following legacy reader resources had no production or test references after the Kotlin hosts
were added, verified with repository-wide `R.layout`/`@layout` searches, and were removed:

- `res/layout/lyric_view.xml` — no remaining `R.layout.lyric_view`/`@layout/lyric_view` use;
  replaced by `LyricScreen`.
- `res/layout/settings_view.xml` — no remaining `R.layout.settings_view`/`@layout/settings_view` use;
  replaced by `SettingsScreen`. Its historical `nightModeSwitch` id is retained in
  `res/values/ids.xml` for the interoperability test/control boundary.
- `res/menu/lyricview_actions.xml` — no remaining menu inflation or menu resource use;
  lyric actions are Compose callbacks.
- `res/layout/list_position.xml` — no remaining layout reference.

At this partial-review checkpoint, `res/layout/song_item.xml`,
`src/com/goodtrendltd/HolySongs/SongTitleAdapter.java`,
`src/com/goodtrendltd/HolySongs/Sidebar.java`, and `res/layout/main.xml` still remained. This is
historical checkpoint evidence, not the final repository inventory.

A later owner-authorized cleanup on 2026-09-17 rechecked repository-wide references, replaced the
active WebView/video and catalog helper Java with same-identity Kotlin implementations, converted
project-owned Java tests to Kotlin, and removed the superseded list/video layout and menu XML.
The final source inventory has no project-owned `.java`, `res/layout`, or `res/menu` files. The root
manifest, song catalog XML, string/style/id resources, bundled pinyin JAR, app identity, providers,
and preference/data contracts remain. Build/lint/test/APK assembly passed after that cleanup;
device/provider limitations below remain unchanged and are not promoted to passes.

## Verification

`lintDebug`, Kotlin/Java compilation, debug APK assembly, unit tests, and focused API36
instrumentation for lyric/settings/about passed after this slice. The required API37/API23 matrix,
process recreation, real share recipient behavior, provider audit and final clean-revision review
remain unexecuted or blocked; no task checkbox is promoted by this partial review.

## Post-migration review remediation

- `AndroidManifest.xml` assigns `HolySongsComposeNoActionBarTheme` to every Compose host
  (Main, DisplayLyric, Settings, and About). Each migrated screen owns its single Compose
  top bar; no migrated host uses a platform action bar.
- About links retain the exact `ABOUT_CONTENT` and destinations but now replace implicit
  auto-link activation with explicit labeled `SafeAboutLinkSpan` activation. The activation
  seam catches `ActivityNotFoundException` and other runtime launch failures, leaving About
  visible. Instrumentation covers success, no-handler, and launch-failure branches.
- Display lyric sharing/video entry has Activity-owned transient guards. Guards clear on return
  (`onResume`), launch failure, and mobile-confirmation cancellation; no-recipient and invalid
  network paths remain on the lyric screen.
- Settings displays `effectiveFontSize`; invalid raw values therefore render 20 without changing
  the raw preference. Boundary/storage tests and a Compose display assertion cover this contract.
- `VideoSearch.searchUrlFor` UTF-8 encodes only the query value with `Uri.encode`; provider
  prefixes and target identities remain unchanged.
- `HTML5WebView` keeps only HTTP(S) navigation in-WebView and consumes other schemes in both
  callback overloads. Progress-dialog cancellation routes through `cancelLoading` with an
  idempotent guard. WebView callback APIs provide no navigation generation token, so a same-URL
  stale callback cannot be distinguished from the current navigation; URL matching is retained
  as the reliable available guard and this limitation remains documented rather than guessed.

These source/test corrections do not promote T024, T032, T040, T041, or final acceptance: API23,
the complete API37 matrix, IDE/clean revision, process death, real share delivery, provider approval,
and the same-URL WebView callback guarantee remain blocked or unexecuted. Existing historical
feature001 statuses are unchanged.

A later parent-authorized run used the wiped disposable API37 `Pixel_10_Pro_XL` emulator
(`emulator-5554`, fingerprint `google/sdk_gphone16k_x86_64/emu64xa16k:17/CP31.260623.012/16064790:user/dev-keys`).
Against APK `318e8b9be474e30af30d7e42053549108eab2c4f73037b32d006aeba88f1eb7a`
and test APK `71bbf8ebe6682420ed365bd4eea7d71b547e9fd8d665c82e3aabfbaae82cd5ab`,
focused About (2), Settings (4), VideoSearch (8), Lyric (3), ReaderLifecycle (1), opted-in
ReaderPreferences (4), and the applicable SongList default/fit/preference cases passed. Compact
and resize opt-ins remained blocked because this emulator measured the fit geometry. Raw logs are
outside tracked source under `/tmp/holysongs-api37-final`; the emulator was stopped after testing.
This focused result does not substitute for the remaining manual/provider/final-artifact gates.

## Kotlin-only cleanup verification — 2026-09-17

The owner-authorized cleanup converted the retained WebView/video boundary, catalog helpers, and
project-owned Java tests to Kotlin, replaced the two video layout resources with programmatic Views,
and removed the superseded list/video layouts and menu. Repository inspection found no project-owned
`.java`, `res/layout`, or `res/menu` files. AndroidManifest.xml, values resources, `assets/songs.xml`,
and the bundled pinyin JAR remain because they are active platform/resource/data dependencies rather
than legacy screen implementations. Application bytecode remains at the owner-approved JVM11 target
recorded in `execution-decisions.md`; JDK17 remains the build toolchain and minSdk remains23.

A clean local gate passed:
`clean testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest`. The exact generated APKs
were then exercised on the approved wiped API37 `Pixel_10_Pro_XL` emulator (fingerprint
`google/sdk_gphone16k_x86_64/emu64xa16k:17/CP31.260623.012/16064790:user/dev-keys`). The connected
suite completed successfully at the Gradle task level: 27 tests executed without failure, including
all 9 `VideoSearchTest` cases; 9 opt-in cases reported JUnit assumption violations because their
runner flags were not supplied (4 preference-mutation and 5 sidebar mode/resize cases). APK SHA-256:
`b57ea868ec30c07351a21f700b1cea65226ae3acd1807d8268bc6d26d718c105`; test APK SHA-256:
`13369871e74530ee541e384dc6827b72e3d431720e60962c5c3b0148f5bab892`.

This run verifies the Kotlin replacement on API37 but does not claim API23 execution, real-provider
reachability, or a platform-impossible identity guarantee for delayed same-URL WebView callbacks.
The callback limitation and live-provider reachability remain explicitly recorded rather than
being hidden by the successful automated runs.
