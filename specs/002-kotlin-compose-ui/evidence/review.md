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

`res/layout/song_item.xml`, `src/com/goodtrendltd/HolySongs/SongTitleAdapter.java`, and
`src/com/goodtrendltd/HolySongs/Sidebar.java` remain because source references or the formal
post-story cleanup gate still require them. `res/layout/main.xml` was not edited or removed; the
owner's modified history is preserved. Required assets, strings, Holo styles, Java WebView/video
helpers and manifest identity remain.

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
