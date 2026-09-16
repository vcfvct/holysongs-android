# Compact rail failure and disposition — 2026-09-16

Workflow eeabc644-c768-4495-882a-3fd333c1e495 ran read-only source review alongside exclusive
API37 device validation. Runner42b64304-d55f-48a9-9467-83926a377f6b; reviewer
2cd25058-3757-48cd-adfa-b6faa03f2a0c; both github-copilot/gpt-5.6-luna.

App681099498b1ac14febabe009609722c5953c5dddfb3790b6458f3e12ea3cc07e;
test9313a476392f96cc7c011db591b8f18576584c2f54f65fb415db76f7bbe0bd65.
Verified Pixel_10_Pro_XL/emulator-5554/API37; compact1080×1200, density480, font1.3.
Focused compactModeTapSlopDragAndRailOnlyScrollingKeepSongListStable ran and failed1/1:
ComposeTimeoutException waiting3s for Z indicator after rail scroll-to-Z and click. It reached
positive-range, under-slop, past-slop/no-list-scroll and cancellation assertions first; these
substeps do not convert the failed test into a pass. No Espresso initialization failure recurred.
Resize and later window/font/navigation matrix stopped after this required failure.

Worker restored original1344×2992/density480/font1.0/navigation2 and private app-file contents,
leaving app stopped. No source changes, provider probe or unrelated device/app mutation occurred.
Private runner output names evidence directory compact-window-20260916-101335 with
compact-focused.txt, compact-failure-ui.xml/png, diagnostics and cleanup-verification-final.txt;
that post-teardown screenshot is launcher, not an after-overlap proof.

## Source review / master disposition

Accepted reviewer findings: pointerInput keys omit full rail size, so same-mode resize retains
stale gesture coordinates; semantic scrollBy always returns true even at a boundary; overflow
trigger label lacks expanded/collapsed state. These are concrete current behaviors, not failures
inferred just from unexecuted configurations.

Master additionally inspected current LetterSidebar: rememberScrollState is never attached to
scroll measurement, while a constrained Column.height plus graphicsLayer translation emulates
scrolling. A positive default maxValue is therefore not proof of actual content extent; children
can be constrained rather than measured at full26-row height. Use standard vertical-scroll
measurement with user scrolling disabled while the existing parent gesture arbiter owns input,
or an equivalently correct bounded measured layout; remove duplicated manual translation.
Test actual finite range and all26 nonzero row heights, not merely maxValue>0.

Fit pointer handling currently commits only in the Release branch; move events are consumed but
never select, and down never selects. A final A→D release test alone missed this. Preserve the
approved fit tap/drag rule by committing on down/movement, and add before-release assertions;
compact must retain no-selection-on-down and drag-only-rail behavior. Cancellation/size/stop must
end pending input without undoing already committed fit selection.

These corrections are pending; no T020/T024 acceptance is claimed. No sorting, index semantics,
provider, dependency, minSdk or persistence change is authorized by the rail defect.

## Correction / parent runtime verification — 2026-09-16, 10:27–10:29 local

Worker a217747e-8591-4eb8-b3ec-2b0e58acaf0f changed rail measurement to an attached
verticalScroll(enabled=false) child with parent gesture arbitration, full-size/lifecycle pointer
epoch, primary-pointer handling and fit down/move selection. It added finite-range/nonzero-row/
held-drag/same-mode-resize assertions and overflow state semantics. The worker built/tests/lint
successfully but did not perform requested device reruns; master did not count those as executed.

Master found one new source regression: de-duplication against a persistent committedLetter
blocked later taps on the same letter after manual song scrolling. Fixed de-duplication to be
per-gesture only, made selection semantics transient, and cleared it on size/lifecycle changes.
Added a repeated-A-after-manual-scroll regression. The held-D test now waits for D specifically
before UP, rather than racing against any still-visible earlier indicator.

Normal host assemble/JVM/lint/androidTest build passed. Parent direct selected-device execution
of the exact app/test artifacts then passed all4 required focused cases, each OK(1 test), no skip:

| Case | Configuration | Result |
|---|---|---|
| fitModeMeasuredTapAndDragDoNotScrollTheRail | 1344×2992, density480, font1.0 | passed, including held down/move and repeated same-letter tap |
| compactModeTapSlopDragAndRailOnlyScrollingKeepSongListStable | 1080×1200, density480, font1.3 | passed, including finite range,26 nonzero rows, Z, slop/cancel/no-list-scroll |
| sameModeResizeCancelsPendingCompactPointerWhenOptedIn | compact start, resize1080×1000 | passed |
| sidebarResizeAndModeTransitionCancelPendingInputWhenOptedIn | compact start, tall normal-font fit transition | passed |

App APK70c6269caaefbb5267db0cfbdc97beee79b5621b17048816248e4bae492e6482;
test APK0bffd36c7d1d4b91a36392ad75a06e5c98c53e69c2d2fa48ce27ceea189f3070.
Both installed with selected install-r after Pixel/API37 verification. Parent saved/reverified
all original shared_prefs/files content hashes and restored original window/density/font after
all tests, app stopped. No unrelated data/network/provider was touched.

Private evidence: `$HOME/.local/state/holysongs-android/verification/parent-rail-20260916T102735/`
with4 method logs, results.json, source APK copies, settings/private backups, main.xml and main.png.
Parent visually inspected after screenshot: Compose title/first row/sidebar are clear of the
system/app-bar overlap previously reproduced by T010. Current light stored theme differs from
T010 controlled dark theme; this is geometry evidence, not pixel-identical styling parity.
Host log `/tmp/holysongs-parent-rail-build.log`; reproducible private runner
`/tmp/holysongs-parent-rail-device.py`.

T020–T023 implementation is accepted based on reviewed source, earlier actual fit interactions
and these corrected-rail regressions. Full current-artifact T024 matrix, retained video/share
delivery safety and clean/IDE evidence remain separate unfinished gates.
