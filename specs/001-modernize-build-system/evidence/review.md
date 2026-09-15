# Parent review — US1 build checkpoint

Date: 2026-09-15. Scope: setup/foundation and T007–T014 only. This is **not** T037/T040 final acceptance. Base revision: `634f98624b654fe9926c7b1c8186fbad56df6673` plus uncommitted implementation. Main agent reviewed the actual files, reports and child transcripts, then reran the full build without task/output caching. See `us1-build.md` for exact artifact and results.

## File-specific reasons and review corrections

| Files | Reason / review result |
|---|---|
| `.gitignore` | Preserve original `.pi/`; add local SDK/build/signing/editor exclusions; no blanket JAR exclusion. Existing tracked legacy outputs/local.properties await T015. |
| `gradlew`, `gradlew.bat`, `gradle/wrapper/*` | Standard wrapper bootstraps pinned Gradle9.6.0; official distribution and wrapper-JAR SHA256 independently verified. |
| `settings.gradle`, `build.gradle` | One `:app`, pinned AGP9.4.0, Google/MavenCentral repositories. Removed unnecessary plugin portal. |
| `gradle.properties` | Portable JVM/caching settings, explicit Java-only Kotlin opt-out; SDK/JDK automatic provisioning disabled. Parent removed worker-added `android.sync.suppressAgpWarnings`; deprecation warning is visible. |
| `app/build.gradle` | Preserve app identity, version8/2.5 and API14–37 range; explicit legacy manifest/java/res/assets mapping, JDK17/Java8 and test-root property. Removed redundant Java-resource/AIDL/RenderScript/native-library mappings. No `gen/` input, release signing or app ID suffix. |
| `AndroidManifest.xml` | SDK/version/namespace owned by Gradle, launcher explicitly exported, internal Activities non-exported; component identity and two existing permissions retained. |
| `MainActivity.java`, `DisplayLyricActivity.java` | Six menu action branches converted from constant-ID switches to equivalent ID comparisons; actions and default return values preserved. No algorithm/settings/provider edits. |
| `res/layout/video_html5_screen.xml` | Lint's Orientation error fixed with explicit horizontal orientation on the empty error-console container, matching the previous default. Initial vertical value rejected in review. |
| `BuildInputsTest.java` | Two JVM input checks cover immutable asset/JAR hashes and actual XML preference identifiers; explicit root required. No simulated Activity pipeline or runtime claim. |
| `specs/001-modernize-build-system/evidence/*` | Curated source-derived fixtures, setup and verification history. Parent mechanically verified 11 full lyric fixtures and all eight winner metadata rows, corrected one mistyped hash. No shipped song data changed. |

## Legacy compile/startup inspection

`HTML5WebView.java` compiles against the selected SDK without API-removal corrections; deprecated `setSavePassword`/form settings are not assumed removed. Its unconditional geolocation grant, loading-dialog null/lifecycle risks and `VideoSearch` ownership/back issues remain US3 work, not waived. `MainActivity` launcher compiles and remains the packaged launcher, but posted overlay attachment/removal, empty-list callbacks and asset-stream handling need US2 validation/fixes. No device was available to demonstrate successful startup; compile success is not a startup pass.

## Lint warning dispositions

Parent read the generated lint XML; 0 errors and the following 25 warnings remain visible. No baseline, severity downgrade, blanket suppression or security bypass is used. Retaining warnings at the developer build checkpoint does not waive required later runtime checks.

| Issue (count) | Location / disposition |
|---|---|
| ApplySharedPref (3) | SettingsActivity synchronous commits; preserve current persistence behavior for now, responsiveness/lifecycle checks in US2. |
| DefaultLocale (1) | MainActivity sidebar uppercase; collation/navigation baseline must precede any semantics change in US2. |
| UnusedAttribute (3) | Manifest parentActivityName is ignored on API14–15; pre-existing attribute, explicit navigation/back still requires endpoint testing. |
| InflateParams (3) | HTML5WebView and SongTitleAdapter null-root inflation; layout/fullscreen/runtime checks in US2/US3, not speculative build cleanup. |
| RedundantLabel (1) | Manifest label redundancy; harmless, retained to minimize churn. |
| AndroidGradlePluginVersion (1) | Suggests Gradle9.7.1; plan intentionally pins9.6.0. No automatic version substitution. |
| LockedOrientationActivity (1), DiscouragedApi (1) | VideoSearch portrait request may be ignored on modern large screens; mandatory resize/navigation checks in US3 remain blocked. |
| SetJavaScriptEnabled (1) | Existing video WebView requires audit; no JavaScript bridge/TLS/cleartext bypass added. US3 security/behavior work remains outstanding. |
| DrawAllocation (1) | Sidebar onDraw allocation; retain algorithm; observe stalls in US2 before targeted changes. |
| RedundantNamespace (2), UnusedNamespace (2) | Existing settings XML namespace declarations; no behavior impact, deferred cleanup. |
| IconDensities (2), IconMissingDensityFolder (1) | Existing icon/poster density coverage; no asset redesign authorized. |
| ClickableViewAccessibility (1) | Sidebar touch implementation; accessibility/navigation validation remains required in US2; not a final acceptance waiver. |
| RtlHardcoded (1) | Existing main-layout right alignment in Chinese UI; preserve layout pending compatibility checks, no redesign. |

## Current decision

Host-only build and APK inspection passed; input hashes, identity and API14-compatible v1 signing verified. Original local.properties bytes restored after temporary host setup. Legacy CRLF files pass `git -c core.whitespace=cr-at-eol diff --check`; ordinary diff check flags their existing line-ending convention.

T015 cleanup and README can proceed after this checkpoint. T017 clean exact-revision/IDE/install remains unexecuted or blocked. No release, supported-range change, provider removal/replacement, SDK-license acceptance or final feature approval is authorized.

## Subsequent draft-PR review — 2026-09-15

The above decision is historical. T015/T016 were subsequently accepted independently;
see `recovery.md`. The owner now authorizes a draft PR, not merge/final acceptance.
See `pr-checkpoint.md` for publication scope, preservation backup, owner Java-path
approval, exact-source host replay and outstanding IDE/runtime evidence.

The parent reviewed the manifest, menu comparisons, horizontal layout fix and build
source mapping again. No additional production Java/resource changes are included.
The partial SongCatalog/HanziHelper/ChineseCharComp tests pass the earlier host harness
but do not yet cover all T018/T019 requirements. T020 is a scenario stub, not a finished
runtime test plan. T018–T020 have therefore been returned to unchecked; total 15/40.

Local Studio-generated daemon criteria select JDK25. That file is preserved locally
and excluded from this PR, rather than overwriting owner configuration or changing
the agreed JDK17 contract. A separate clean checkout must verify the published files
without global machine-specific Gradle settings. Prior automation failures and prior
blocked/failed checks remain in history; the draft PR does not retroactively pass them.
