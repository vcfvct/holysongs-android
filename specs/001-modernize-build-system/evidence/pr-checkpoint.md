# Draft PR checkpoint — 2026-09-15

This is an intermediate review checkpoint, **not merge or feature acceptance**.
The owner requested a PR after discussing a draft checkpoint. That authorizes this
checkpoint's branch/commit/push/draft PR, not a merge, license acceptance, release,
supported-range change or provider replacement/removal.

## Preservation and scope

- Base: `634f98624b654fe9926c7b1c8186fbad56df6673` on `master`.
- Before staging this PR, the only cached delta was exactly `D local.properties`.
  The scoped staging-policy contract was checked by the parent before publication staging.
  Explicit PR authorization permits staging the reviewed checkpoint; after committing
  the untracking, the one-file exception is no longer needed.
- Current local.properties SHA-256:
  `4f79b6fcd66f9921a892fd6023754652b8154e0b5df615128943508f4356bb25`.
  Preserve these current bytes, not the older recovery snapshot.
- Private pre-PR backup: `/tmp/holysongs-pr-checkpoint.jpsr5mgz/worktree.tar.gz`;
  SHA-256 `87258ee01bbf5ca1c840e0b05ff72c076b8881c5920cd7b778c89df2b56fe24f`.
  Separate staged/unstaged patches are in the same private directory. This is a recovery
  archive, not clean-checkout evidence. Local editor configuration was also backed up.
- Include the Gradle build/wrapper, narrowly scoped manifest/menu/lint changes, legacy
  retirement, README, constitution, feature documents/evidence and partial JVM tests.
- Preserve but do not publish the local Spec Kit scaffolding (apart from the constitution),
  ignored IDE state/local.properties, or `gradle/gradle-daemon-jvm.properties`.
  The latter selects JDK25 and includes provisioning URLs; it is not part of the selected
  JDK17 contract. Its existing local contents remain untouched.
- The owner explicitly approved removing machine-specific Java paths from project
  `gradle.properties`: those paths now live in the owner's user-local Gradle settings.
  This does not authorize changing those global settings. Retain the owner's portable
  `org.gradle.tooling.parallel=true` setting.
- Source song and JAR hashes still match the baseline; existing working-tree APK hash is
  `df6c6d66d395c23e284091e15c2f0dc378e5076a48168ee30ac2fa909a02f174`.
  This hash has not been tied to the owner's simulator execution.

## Task reconciliation

Exactly **15/40** tasks remain checked: T001 and T003–T016. T018–T020 were checked
prematurely in the previous session; the parent has returned them to unchecked.
See `us2-reader.md` for concrete coverage gaps. The new tests are partial preparation,
not completed US2 scenarios or device acceptance.

T002 remains blocked: SDK command-line tools are still absent and no SDK-license
acceptance was performed or newly authorized. JDK/SDK inventories do not prove IDE execution.
T017 remains incomplete pending its clean-source host replay plus independently attributable
IDE sync/build and dedicated supported-target install/launch evidence. Any host-only replay
below is only one part of T017. T027/T034/T039 still require both endpoint gates.

## IDE and owner-reported simulator evidence

- requirement/scenario ID: C1-IDE-001 [FR-001|US1|IDE]; C2-APK-002 [FR-001|US1|install-launch]
- date: 2026-09-15 (report collection; actual run time not supplied)
- source revision: owner did not provide an exact revision/source-state snapshot for the run
- APK SHA-256: unknown for the owner's run; existing local APK hash above is not proof of association
- toolchain: installed Studio build `AI-261.26222.65.2614.16204760` (build.txt and product-info.json); selected IDE Quail 4/2026.1.4, but owner-run About/version and Gradle-JDK evidence still needed
- device/emulator identifier: local AVD `Pixel_10_Pro_XL` exists; owner has not explicitly identified it as the tested target; explicit SDK adb inventory currently has no attached targets
- Android API/version: AVD config says `target=android-37.1`; actual running-device getprop/API/release not collected; API14 not tested
- WebView version where applicable: not collected
- prerequisites: owner reported Android Studio build and simulator execution succeeded
- steps: inspected installed IDE metadata, local AVD configuration, Git source state and existing APK hash; no install, launch, clearing, or simulator mutation performed in this review
- expected result: associate IDE sync/build and launch with exact source, artifact, IDE/JDK and runtime; record tested flows
- observed result: owner reports successful build/simulator execution. No additional tested flows were supplied (offline first launch, browsing, lyrics, settings, sharing, lifecycle and video are unverified). Separate sync outcome and artifact association remain unknown
- status: blocked
- evidence path: this file; `environment.md`, `devices.md`, `us1-build.md`
- blocker/limitation: incomplete attribution, not evidence of an app failure; launch success does not prove API14/API37 core-flow acceptance
- maintainer approval reference for accepted provider changes or limitations: none; draft PR authorization is not a provider limitation waiver

## Clean committed-source host replay

Initially pending at the source checkpoint commit; the following later record is an
evidence-only update and does not change application/build/test inputs.

- requirement/scenario ID: C1-CLI-001 [FR-001|US1|CLI|clean-checkout]; C2-APK-001 [FR-006, FR-007|US1|identity-and-signing]
- date: 2026-09-15; final test timestamps 19:13:53 UTC
- source revision: `2a498f3a0371e40bc6b1a44c07fe55138eaa06bb`, cloned from the committed branch with `git clone --no-hardlinks --branch build/modernize-build-checkpoint`; clean Git status before and after both builds
- APK SHA-256: `df6c6d66d395c23e284091e15c2f0dc378e5076a48168ee30ac2fa909a02f174`
- toolchain: Gradle9.6.0, launcher and daemon Temurin17.0.14+7, AGP9.4.0, platform37, Build Tools36.0.0, Java8 application source/target
- device/emulator identifier: n/a; Linux host only
- Android API/version: APK min14/target37; no runtime execution
- WebView version where applicable: n/a
- prerequisites: existing installed JDK17 and SDK; README session-local environment exports; an initially empty private GRADLE_USER_HOME; no copied user Gradle settings, local.properties, daemon criteria, build outputs or caches. Wrapper/dependencies downloaded normally. Existing host debug signing material is used by standard Android debug signing; no release key or license acceptance
- steps: run `./gradlew --version`; run `./gradlew clean :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --no-build-cache --rerun-tasks` twice; inspect test/lint XML, aapt badging, `apksigner verify --verbose --print-certs --min-sdk-version 14`, APK/packaged-song hashes and Git state
- expected result: pinned runtime, regenerated outputs and passing host checks without user-global Gradle configuration; unchanged identity/data and v1-compatible signature
- observed result: all commands exit0. Initial build: 37s, 45 tasks executed / 1 up-to-date (initial clean); regeneration replay: 3s, 46 tasks executed. **7 tests, 0 failures/errors/skips** across four classes; **0 lint errors, 25 warnings**, same warning dispositions as review.md. Expected package, version8/2.5, min14/target37 and only the two existing permissions. v1/v2 signatures verify. Packaged songs SHA-256 remains `88eb0db602e018b49a327947dd8607f04e6159e58f39ec38ed59f20c39af9d89`. No local.properties/daemon criteria or generated inputs tracked; checkout remains clean
- status: passed
- evidence path: this file; private replay `/tmp/holysongs-pr-clean.yoKfOO/{version.log,build.log,rebuild.log,badging.txt,signing.txt}`; reports in its `repo/app/build/` directory
- blocker/limitation: host-only pass for this exact commit, not complete T017 or final feature acceptance. The partial tests still lack the T018/T019 coverage described above. No missing-prerequisite negative test, IDE sync/build or dedicated-target install/launch performed here. No claim of a pristine SDK installation or production upgrade. apksigner retains the build-metadata v1 protection warning recorded in earlier evidence
- maintainer approval reference for accepted provider changes or limitations: none; no provider changes or accepted provider limitations

Subsequent documentation-only commits may record this result but are not themselves represented
as separately rebuilt revisions. Application/build/test input equivalence must be checked before
attributing this checkpoint artifact to a later documentation-only PR head.
