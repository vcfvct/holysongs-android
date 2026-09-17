# Navigation, Sharing and Video Contracts

## N1. Installed Identity and Activity Boundaries (FR-005, FR-009)

Application ID/namespace: `com.goodtrendltd.HolySongs`. Keep `.MainActivity` as exported launcher;
`.DisplayLyricActivity`, `.SettingsActivity`, `.AboutActivity` and `.VideoSearch` stay non-exported.
Keep the current 8 / 2.5 version for this debug-only feature; a genuine later release requires
reviewed version progression and compatible signing. No public deep links or permissions beyond
[build contract B2's explicit allowlist](build-and-verification.md#b2-artifact-integrity); its sole
new entry is AndroidX Core's app-private signature protection, not a new runtime capability.

| Scenario | Caller → destination | Inputs / expected result |
|---|---|---|
| N1-LYRIC | Main → DisplayLyric | String `com.goodtrendltd.SONG_NAME` and `com.goodtrendltd.LYRIC`; selected effective title/lyric |
| N1-SETTINGS | Main → Settings | Existing entry remains; normal Back returns to same Main instance when alive |
| N1-ABOUT | Main → About | Existing content and clickable website/email behavior preserved |
| N1-VIDEO | DisplayLyric → VideoSearch | String `com.goodtrendltd.searchTarget` and `com.goodtrendltd.SONG_NAME` |
| N1-INVALID | Any retained boundary | Missing/wrong-type/invalid argument yields safe error/back; no crash or arbitrary fallback song |

Keep constants Java-callable when their declaring Activities move to Kotlin. The pilot must work
with legacy Java DisplayLyricActivity reading MainActivity constants. The later reader must work
with retained Java VideoSearch reading DisplayLyricActivity.SEARCH_TARGET. This compile-time and
runtime interop is required, not just an internal renaming convenience.

Navigation is user-event-driven, never a composable-body side effect. Do not save/replay pending
navigation commands. Back normally pops the Activity stack; no new navigation graph or stack-clearing
theme restart. Guard custom Back handling only where needed so normal system Back remains available.

## N2. Sharing (FR-009)

- **N2-LYRIC**: `ACTION_SEND`, `text/plain`, title as `EXTRA_SUBJECT`, effective lyric as `EXTRA_TEXT`.
  Preserve all intended newlines/Chinese text; never share the wrong duplicate's lyric.
- **N2-APP**: Preserve the original MainActivity app-sharing subject/text, Android/iOS link resource
  values and whitespace. Snapshot the current payload independently before replacing its builder.
- **N2-NONE**: No available recipient cannot crash the app; show a safe unavailable outcome or
  return to the reader. Catch actual launch failure; do not infer availability solely from a
  package-visibility-sensitive query. No new broad package visibility permission is authorized.
- **N2-DELIVER**: Verify with an installed receiving app on each runtime. Test fakes can assert
  payloads but do not prove the real system chooser/recipient interaction works.

## N3. Video Providers (US3; FR-011)

| Target String | Baseline prefix |
|---|---|
| `youtube` | `http://m.youtube.com/results?q=` |
| `youku` | `http://www.soku.com/m/y/video?q=` |
| `tudou` | `http://www.soku.com/m/t/video?q=` |

- **N3-QUERY**: Validate exact target and nonempty title. URL construction preserves search intent
  and encodes the Chinese query using UTF-8. Audit baseline/redirect/final URL, status and actual
  WebView behavior before selecting any same-provider HTTPS replacement.
- **N3-NETWORK**: API23 active-network/capabilities checks are null-safe and retain mobile-data
  confirmation on non-Wi-Fi paths. Lack of connectivity is not a crash; cancellation returns
  immediately to offline reading. Network presence does not prove site reachability.
- **N3-PROVIDER**: Record one audit per provider per required runtime, with WebView version and
  a representative Chinese query. Failure, untested access, and successful intent/query delivery
  are distinct results. Never invent working playback from an HTTP status alone.
- **N3-APPROVAL**: Replacement/removal, external-browser UX, or accepted provider unavailability
  needs owner/date/scope approval. No blanket approval is inferred from Kotlin/Compose adoption.
- **N3-SECURITY**: Deny unsolicited geolocation; cancel SSL errors normally. No TLS bypass,
  mixed-content/cleartext global allowance, permission outside build contract B2's allowlist, or
  unapproved external launch behavior. Video work adds no permissions beyond that existing build allowance.

## N4. WebView Ownership and Back (FR-010, FR-011)

Retain Java/View-based video. It may use ComponentActivity for OnBackPressedDispatcher without
changing its language or embedding it in Compose.

| Scenario | Expected behavior |
|---|---|
| N4-PAUSE | Temporary pause/resume retains a live WebView; verify media does not continue unexpectedly |
| N4-SAVE | Save/restore while live; no methods invoked after destroy |
| N4-DESTROY | Dismiss loading, release custom fullscreen view/callbacks, detach and finally destroy once |
| N4-ERROR | Main-frame network/HTTP failures and cancellation settle loading safely; no trapped popup |
| N4-LATE | Late callbacks cannot reopen dismissed UI, act on destroyed hosts, or settle a newer navigation |
| N4-BACK | Cancel foreground loading; otherwise exit fullscreen; otherwise navigate WebView history; otherwise finish to the existing lyric |
| N4-RESIZE | Resizing/rotation and background return preserve usable navigation; do not rely on portrait lock on large screens |

When loading and fullscreen coexist, cancel/dismiss loading without skipping later fullscreen
cleanup. Avoid a blanket always-enabled Back callback at the root. Do not use global WebView
pauseTimers as a per-screen cleanup shortcut. A provider outage may be accepted, but application
crashes, unsafe transport workarounds, trapped loading and failed offline return may not.

## Evidence

Use the record fields in [data-model.md](../data-model.md). Provider audit and fixture tooling are
implementation/testing tasks; no provider outcome is claimed by this contract. Keep raw evidence
outside tracked source. Test-device selection and signing precautions are in
[quickstart.md](../quickstart.md).
