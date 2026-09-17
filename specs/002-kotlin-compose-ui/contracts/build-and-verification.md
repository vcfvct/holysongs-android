# Build and Verification Contract

## B1. Build Interface (FR-001, FR-002, FR-014)

| Entry point | Required outcome after implementation |
|---|---|
| `./gradlew --version` | Gradle9.6.0; both launcher and daemon JDK17 |
| `./gradlew :app:buildEnvironment` | AGP9.4.0 and KGP/Compose compiler2.2.10 without conflicting Kotlin Android plugin |
| `./gradlew clean :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` | Successful regenerated mixed Java/Kotlin build, passing actual unit tests, reviewed lint |
| `./gradlew :app:assembleDebugAndroidTest` | Instrumented APK built with AndroidJUnitRunner and matching Compose test BOM |
| `./gradlew :app:connectedDebugAndroidTest` | Required device tests pass on explicitly selected dedicated endpoint |
| Android Studio Quail4 / 2026.1.4 | Sync/build with local JDK17 and wrapper, no tracked machine configuration changes |

Keep the application JVM target at 11, compile/target37, Build Tools36.0.0, and minSdk23. JVM11
is the owner-approved minimum required by the resolved Compose 1.12.1 inline APIs; this supersedes
the original JVM8 planning constraint without changing the JDK17 build toolchain or Android support
floor. The completed source mapping is Kotlin-only under the module while retaining the root
manifest/resources/assets. Preserve the JVM-test `repositoryRoot` input and do not introduce
duplicate fully qualified Activity definitions during replacement.

Root declaration and applied app Compose compiler plugin use version2.2.10; remove the Java-only
built-in-Kotlin opt-out. Do not apply `org.jetbrains.kotlin.android` or legacy compiler-extension
DSL. Exact dependency pins and source evidence are in [research.md](../research.md).

Resolve and review debug compile/runtime plus androidTest compile/runtime dependency graphs.
Record selected compiler, stdlib and coroutines versions separately; library stdlib resolution
must not be mistaken for a compiler upgrade. Capture dependency locks for the app's resolved
configurations once verified. A changed graph requires deliberate lock refresh/review, never
blind force/downgrade. Review all resolved Android metadata/merged manifests for API23/compile37
compatibility; no overrideLibrary, global lint bypass, or silent minimum-version increase.

## B2. Artifact Integrity

- Application ID/namespace `com.goodtrendltd.HolySongs`, version8/2.5, launcher `.MainActivity`.
- minSdk23 / targetSdk37. The app manifest permission allowlist is exactly
  `android.permission.INTERNET`, `android.permission.ACCESS_NETWORK_STATE`, and
  `com.goodtrendltd.HolySongs.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`.
  The last entry is the narrowly approved AndroidX Core protection: inspect its merged
  `<permission>` declaration with `android:protectionLevel="signature"` and matching
  `<uses-permission>`. Record originating dependency/manifest-merger attribution and exact
  applicationId substitution. Reject changed names/protection levels or any additional permissions
  pending owner review; this is not an exception for arbitrary dependency-added permissions,
  exported components, dangerous/runtime grants, or broader application capabilities.
  Do not remove this protective declaration/use merely to obtain an artificially smaller manifest.
- Internal Activities remain non-exported; no debug applicationId suffix or new release signing.
- Inspect APK packaged song hash and retained JAR/source hashes against [data-model.md](../data-model.md).
- Verify signature on API23, including v1 support needed below API24. Debug signing is not proof
  of an update over a production-signed installation.
- No tracked local.properties, daemon JVM machine paths, generated builds, credentials or keys.

## B3. Tests and Evidence

Tests precede relevant replacements. Keep JVM assertions against real production loading/indexing,
not an independent duplicate pipeline. Baseline tests cover full raw order, both positions of all
duplicates, exact lyric/whitespace expectations, polyphonic helper behavior and comparator properties.
Actual native ordering/rendering, settings, insets and lifecycle remain device checks.

Instrumentation must use real declared Activities initially and explicitly wait for Ready UI.
Do not replace Activity content twice with the test rule. Tests must not assume all 414 lazy rows
are simultaneously in the semantics tree. Verify accessible control labels rather than test tags.
State restoration tests do not replace Activity/process recreation checks.

Required matrix and scenario IDs are in [ui.md](ui.md) and
[navigation-and-video.md](navigation-and-video.md). On each endpoint record startup timing/method,
scroll stalls and app-specific crash/ANR observations. Missing devices block their checks, not
produce test passes. No fresh runtime results exist at planning completion.

## B4. Slice and Final Acceptance

- A toolchain checkpoint proves only enablement, not migrated UI behavior.
- The first user-visible checkpoint follows [ui.md's applicability matrix](ui.md#checkpoint-applicability):
  full Main/list/sidebar U1/U2/U4 behavior, correct insets, and explicitly defined legacy
  reader/settings/about/share/video safety/interoperability smoke on API23/API37. Full later-screen
  restoration/reflow is not a pilot gate; migration-introduced regressions and safety failures are.
- Reader/settings/about migration must preserve storage and state at each subsequent checkpoint.
- Final feature acceptance requires U1–U4 and N1–N4 on the final artifact, provider decisions,
  clean CLI/IDE replay, signature/data inspection, and reviewed documentation.
- Preserve feature001's incomplete historical API14 outcomes; they are not feature002 runtime gates.
- Required failures/blockers prevent completion. An approved external-site limitation cannot waive
  app safety or return to offline reading. Raw test logs/screenshots/APKs remain private; commit
  sanitized summaries with revision, APK hash, device/API/WebView and scenario status.
