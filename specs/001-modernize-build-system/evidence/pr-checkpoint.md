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

Pending at initial checkpoint commit. Record the exact tested commit, commands, isolated
Gradle-home setup, artifact hash and observed results in a subsequent evidence-only update.
No copied local configuration or generated application outputs are permitted. IDE/runtime
checks remain separate even if host checks pass.
