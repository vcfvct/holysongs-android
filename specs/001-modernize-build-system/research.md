# Research: Modernize HolySongs Build System

**Decision date:** 2026-09-15
**Scope:** Build migration and required compatibility fixes; no implementation performed.

## 1. Toolchain and supported platforms

**Decision:** Pin Android Gradle Plugin (AGP) 9.4.0 and Gradle wrapper 9.6.0; use JDK 17
for the build and tests. Use Android Studio Quail 4 (2026.1.4), compileSdk 37, targetSdk 37,
and SDK Build Tools 36.0.0. Set Java source/target compatibility explicitly to Java 8.
Keep minSdk 14 (Android 4.0). Required runtime endpoints are API 14 and Android 17/API 37.

**Rationale:** Official AGP 9.4 notes specify Gradle 9.6.0, JDK 17, Build Tools 36.0.0,
and support through API 37. The Studio release page identifies Quail 4 as stable and
compatible with AGP through 9.4. Android 17 is released, not a preview target. Running the
build with JDK 17 does not require exposing Java 17 APIs to Android 4.0. Existing application
code can remain Java 8-compatible without adding core-library desugaring dependencies.

Keeping the existing installation floor avoids dropping users merely for a build migration.
The minimum is a project support choice, not a claim that old Android or its WebView is secure
or still maintained. External-provider success on old WebViews is not guaranteed; safe failure
and return to offline content remain required. No unavailable device may be counted as tested.

**Alternatives considered:**
- Raise minSdk to 23: reduces legacy support burden but drops API 14–22 users without a
  demonstrated build requirement. Rejected for this feature; request approval if evidence
  proves API 14 support infeasible.
- AGP 8.x / older target SDK: can reduce migration friction but is not the selected current
  baseline. Do not silently downgrade versions or suppress compatibility warnings.
- Kotlin/Compose/AppCompat conversion: unnecessary for this platform-Activity app and out of scope.

**Sources verified by the parent:**
- [AGP 9.4 compatibility](https://developer.android.com/build/releases/agp-9-4-0-release-notes)
- [Studio stable releases and AGP compatibility](https://developer.android.com/studio/releases)
- [Android 17 SDK setup](https://developer.android.com/about/versions/17/setup-sdk)
- [Android 17 release](https://developer.android.com/blog/posts/android-17-is-here)
- [Gradle 9.6 Java compatibility](https://docs.gradle.org/9.6.0/userguide/compatibility.html)
- [AGP 9 defaults and DSL changes](https://developer.android.com/build/releases/agp-9-0-0-release-notes)

Versions are design selections, not evidence of a successful local build. Verify published
artifacts and wrapper distribution checksums during implementation; unavailable artifacts block
that stage instead of authorizing an unrecorded version substitution.

## 2. Project structure and dependency policy

**Decision:** Introduce one `:app` Gradle module using Groovy build files. Keep existing
`src/`, `res/`, `assets/`, `libs/`, and root `AndroidManifest.xml` in place, mapped explicitly
through `app/build.gradle` source sets. Do not include `gen/`. Use the modern public DSL only.
Preserve namespace and applicationId as `com.goodtrendltd.HolySongs`, versionCode 8 and
versionName 2.5 for this debug-only migration. Move version and SDK declarations from the
manifest to the build configuration. No release publishing is planned.

Keep the existing pinyin4j 2.5.0 JAR byte-for-byte as a local runtime dependency. Add only
JUnit 4.13.2 for local tests. Use Google Maven and Maven Central, exact dependency versions,
and a checksum-verified wrapper. Disable built-in Kotlin for this Java-only module using the
AGP-supported configuration; do not introduce Kotlin sources or a separate Kotlin plugin.
BuildConfig generation is unnecessary unless implementation finds a genuine consumer.

**Rationale:** Explicit source sets are supported Gradle configuration, preserve file history,
and avoid moving every source/resource solely to change tooling. The vendored JAR controls
pinyin behavior; a simultaneous library upgrade would complicate regression attribution.

**Alternatives considered:** Mechanical migration to `app/src/main/` is valid but unnecessary
churn here. Resolving a newer pinyin4j version from Maven is deferred until equivalence and
licensing are reviewed. AndroidX instrumentation libraries are not selected because their
minimum-version requirements would complicate the API 14 verification path.

**Repository evidence:** No Gradle files, wrapper, or tests existed at the inspected baseline.
`project.properties` targets API 19. Generated Java and `local.properties` are tracked.
The JAR has `META-INF/MANIFEST.MF` but no embedded license file discovered by ZIP listing.
Retain it; record upstream provenance/license investigation as a distribution risk, not as
license clearance. Do not replace or delete it to hide that uncertainty.

## 3. Data and behavior preservation

**Decision:** Preserve the asset bytes, DOM-based loading behavior, existing title-keyed map,
Chinese Collator ordering, and pinyin-based sidebar behavior. Characterize them before edits.
No new persistent model or database is needed.

**Rationale and measured baseline:**
- `assets/songs.xml`: 233,820 bytes, 422 song elements, 414 unique titles.
- Eight titles occur twice; `MainActivity` currently retains the last lyric for each title.
- `getXml()` removes ASCII spaces from the XML string before parsing. Preserve this observable
  behavior rather than treating byte-preservation alone as display equivalence.
- `ChineseCharComp` uses `Collator.getInstance(Locale.CHINA)`, not pinyin sorting.
  Platform collation differences must be recorded across API endpoints; do not silently change
  the sorting algorithm to match the sidebar.
- `HanziHelper` uses the first pinyin reading, lowercase, no tones, Unicode ü.

Asset SHA-256:
`88eb0db602e018b49a327947dd8607f04e6159e58f39ec38ed59f20c39af9d89`

Vendored JAR SHA-256:
`6576dea7d351a0f5df1595b9c432ba7cf9246ca0ab6f7019b9ca4e6d500b0e68`

**Alternatives considered:** Deduplication, correcting whitespace, changing collation, or new
song IDs would change the product and are excluded. Defensive resource closing and explicit
UTF-8 decoding are acceptable only with preservation tests.

## 4. Compile and runtime compatibility work

**Decision:** Make targeted changes backed by compile/lint findings or required runtime tests:

| Location | Observed issue or risk | Minimal planned treatment |
|---|---|---|
| Manifest | Launcher filter lacks `android:exported` | Export launcher; keep internal activities non-exported; preserve component names |
| MainActivity, DisplayLyricActivity | `case R.id.*` assumes final generated IDs | Use equivalent `if` comparisons rather than changing AGP defaults globally |
| MainActivity | Posted window attachment, delayed callbacks, unconditional removal | Cancel callbacks and track attachment/lifecycle state; guard empty list access |
| DisplayLyricActivity | Dereferences possibly absent Wi-Fi network information | API-guarded modern connectivity query and safe legacy fallback; retain mobile-data confirmation |
| VideoSearch | Destroys WebView in `onPause`, then reuses it in save/stop | Pause/resume across temporary absence; detach/destroy only at final teardown |
| HTML5WebView | `pd.isShowing()` without null check; callbacks may outlive Activity | Null/lifecycle guards and consistent error/cancellation cleanup |
| SettingsActivity, VideoSearch | Back handling relies on `onKeyDown` | Adapt with API-guarded back callbacks where modern target requires them; preserve legacy behavior |
| Activities, Sidebar | Modern insets, resize and orientation behavior | Test both navigation modes and a large-screen API 37 configuration; minimal insets/bounds fixes |
| WebSettings calls | Old/deprecated settings | Remove/replace only calls shown unavailable or unsafe; deprecation alone is not proof of removal |

Preserve Holo themes and platform menus. The stock architecture is not itself a compilation
failure. For any touched deprecated API, use a maintained API on versions that provide it and
isolate a guarded legacy fallback where API 14 compatibility requires it.

**Alternatives considered:** Broad UI replacement or blanket lint suppressions would conceal
regressions and expand scope. An application-wide back-navigation opt-out is not the default fix.

## 5. Video integration and security boundary

**Decision:** Audit YouTube, Youku, and Tudou independently, including old endpoints, any
same-provider HTTPS equivalent, query encoding, redirects, modern WebView behavior, network
failure, and return navigation. Prefer verified same-provider HTTPS compatibility fixes.
Any provider replacement, removal, or switch to external-browser UX requires recorded approval.
Do not add global cleartext permission, mixed-content allowances, TLS-error bypasses, new
location permissions, or unconditional geolocation grants to make old pages appear to work.

**Rationale:** `strings.xml` uses HTTP URLs; modern cleartext defaults can block them. This does
not prove a provider is retired. `HTML5WebView` currently grants geolocation callbacks; deny
location requests because video search requires no location and the app declares no such need.
An unreachable provider can remain an approved limitation if it fails safely and leaves offline
content available. Unknown live-provider status is an audit task, not an unresolved design choice.

**Alternatives considered:** Rebuilding integrations or moving all searches to an external app
would change behavior and is excluded without approval. Domain-scoped cleartext exceptions
require a separate explicit security decision; none are authorized by this plan.

## 6. Validation and environment

**Decision:** Use local JUnit characterization tests, Android lint, APK/data inspection, and a
mandatory documented device/manual test matrix. No modern instrumentation runner is required
for this feature; adding one later must not raise the app's installation floor. Test the
minimum on an actual API 14 device or a working legacy emulator; do not substitute API 15/23.
Use an API 37 emulator/device for the current endpoint and large-screen checks.

**Rationale:** This small app can be verified without a testing-library-driven platform upgrade.
Every core-flow result must identify the exact APK, OS, environment, steps, and evidence. Manual
verification is valid; an unexecuted checklist is not runtime evidence.

**Known environment blockers:** Parent PATH checks found no `java`, `adb`, `sdkmanager`,
`emulator`, or `gradle`, and no ANDROID_HOME/ANDROID_SDK_ROOT value. This does not prove tools
are absent elsewhere; installation or local configuration is needed before execution.
No APK build, IDE import, or runtime checks were performed during planning. API 14 device
availability remains an execution dependency and blocks feature completion if unavailable.

**Alternatives considered:** Build-only sign-off contradicts FR-014. Claiming an actual upgrade
without historical signing keys would misrepresent compatibility. Use representative stored
preferences for compatibility tests and label genuine upgrade testing separately.

## Research provenance and resolution

The parent synthesized two bounded `github-copilot/mai-code-1.1-flash` research reports and
verified critical toolchain claims against official pages and data/code claims against files.
Workflow: `03e9217f-f95a-4f15-9edb-643af4d7ff74`; child runs:
`17493a68-a6e8-4ad4-bc80-c04ef44c50be` (toolchain),
`b3eaccb3-3b3c-4219-b023-730eef6143ca` (legacy).

The original reports are retained in session artifacts, not required to build this repository.
Parent corrections include distinguishing a Back override from a total navigation block,
checking actual duplicate counts/hashes, and treating deprecated APIs as risks rather than
proven removals. All design choices above are resolved. Provider availability, licensing
provenance, and device access remain explicitly recorded execution/distribution risks.
