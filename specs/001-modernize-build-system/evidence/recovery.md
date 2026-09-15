# Recovery cleanup for T015/T016

Date: 2026-09-15
Base revision: `634f98624b654fe9926c7b1c8186fbad56df6673` plus existing uncommitted implementation/spec changes
Device/emulator: `n/a` (no dedicated API 14/API 37 target was used or claimed)
Approval scope: parent-reviewed T015/T016 cleanup and README recovery only; no T017 execution, no US2/US3 acceptance, no release-signing approval

## Scope

This file records the limited cleanup performed to complete the approved recovery scope only:

- T015: retire legacy Ant/generated files from the worktree only, while leaving the index to the authorized `local.properties` staged deletion only
- T016: update `README.md` to describe the verified working-tree build and the blocked clean-checkout/IDE/device gates without claiming completion
- no T017 execution, no checklist status changes, and no final feature claim

This recovery preserves the private failure history in `specs/001-modernize-build-system/evidence/handoff-blocker.md` and does not overwrite other evidence files.

## Exact legacy deletion set (worktree only)

The following files were deleted from disk without `git rm`, index writes, or any tracked-file restoration:

- `ant.properties`
- `build.xml`
- `project.properties`
- `proguard-project.txt`
- `gen/com/goodtrendltd/HolySongs/BuildConfig.java`
- `gen/com/goodtrendltd/HolySongs/Manifest.java`
- `gen/com/goodtrendltd/HolySongs/R.java`

`local.properties` was restored to the original HEAD bytes and left on disk; the authorized staged deletion remains the only cached index delta.

## Preservation check

Original HEAD `local.properties` SHA-256:

```text
39ecd56b58739d9ea4acbdb3c229666f68b86853ba889ab569d11c827bf92bbf
```

Current worktree value after preservation:

```text
39ecd56b58739d9ea4acbdb3c229666f68b86853ba889ab569d11c827bf92bbf  local.properties
```

Authorized staged index state after the cleanup:

```text
D	local.properties
```

This is the only `git diff --cached --name-status` delta after the recovery work. No additional index entries were created or modified.

## Validation and artifact results

Commands executed in-session (host-only, no global config):

```bash
cd /home/hanli3/GIT/holysongs-android
export JAVA_HOME="$HOME/.local/tools/jdk-17"
export ANDROID_HOME="$HOME/Android/Sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"

git diff --cached --name-status
sha256sum local.properties
./gradlew --version
./gradlew clean :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --no-build-cache --rerun-tasks
sha256sum app/build/outputs/apk/debug/app-debug.apk
```

Observed result summary:

```text
D	local.properties
39ecd56b58739d9ea4acbdb3c229666f68b86853ba889ab569d11c827bf92bbf  local.properties
Gradle 9.6.0
BUILD SUCCESSFUL in 11s
46 actionable tasks: 46 executed
df6c6d66d395c23e284091e15c2f0dc378e5076a48168ee30ac2fa909a02f174  app/build/outputs/apk/debug/app-debug.apk
```

Unit-test evidence:

```text
app/build/test-results/testDebugUnitTest/TEST-com.goodtrendltd.HolySongs.BuildInputsTest.xml
<testsuite name="com.goodtrendltd.HolySongs.BuildInputsTest" tests="2" skipped="0" failures="0" errors="0" timestamp="2026-09-15T17:08:42.663Z" ...>
```

Lint evidence:

```text
app/build/reports/lint-results-debug.xml
0 errors, 25 warnings
```

Artifact inspection record (host-only, no device install claim):

```bash
"$ANDROID_HOME/build-tools/36.0.0/aapt" dump badging app/build/outputs/apk/debug/app-debug.apk
"$ANDROID_HOME/build-tools/36.0.0/apksigner" verify --verbose --print-certs --min-sdk-version 14 app/build/outputs/apk/debug/app-debug.apk
```

The APK remained `com.goodtrendltd.HolySongs`, minSdk 14, targetSdk 37, version code 8, version name 2.5, and preserved the app identity in the verified working-tree slice. This is not a clean-checkout or device-install approval.

## Raw log and report reference

Raw console output from this session is captured by the active terminal environment; the generated reports remain under:

- `app/build/reports/lint-results-debug.xml`
- `app/build/reports/lint-results-debug.html`
- `app/build/test-results/testDebugUnitTest/TEST-com.goodtrendltd.HolySongs.BuildInputsTest.xml`

## CRLF and diff gate

CRLF-aware diff check executed:

```bash
git -c core.whitespace=cr-at-eol diff --check -- README.md ':(exclude)local.properties'
```

Observed output: no output (clean).

## Remaining blockers and honest status

These gates remain unexecuted and are intentionally not marked as passed:

- T017 clean exact-revision checkout replay
- Android Studio Quail 4 sync/build verification
- dedicated API 14 and API 37 install/launch checks
- provider audit and video-path validation

Production signing, release-key generation and publishing are excluded from scope, not required acceptance checks awaiting execution.

The repository remains in an implementation state with uncommitted work. The valid recovery result is the limited cleanup plus honest README guidance, not a final feature acceptance or a release approval.

## Parent acceptance replay — T015/T016

- requirement/scenario ID: C1-CLI-003 [FR-004, FR-013|US1|post-retirement README replay]
- date: 2026-09-15; test report timestamp `2026-09-15T17:12:35.687Z`
- source revision: base `634f98624b654fe9926c7b1c8186fbad56df6673` plus uncommitted implementation and seven worktree legacy deletions; not a clean implementation-revision checkout
- APK SHA-256: `df6c6d66d395c23e284091e15c2f0dc378e5076a48168ee30ac2fa909a02f174`
- toolchain: Temurin17.0.14+7; Gradle9.6.0; AGP9.4.0; platform37; Build Tools36.0.0; Java8 source/target
- device/emulator identifier: n/a; Linux host only
- Android API/version: APK min14/target37; no device execution
- WebView version where applicable: n/a
- prerequisites: installed JDK/SDK; original stale local.properties preserved; exactly one authorized staged deletion, local.properties; all other edits unstaged
- steps: parent extracted and executed the README's complete temporary-override Bash block verbatim with the documented environment; inspected aapt badging and `apksigner verify --verbose --print-certs --min-sdk-version 14`; compared source/packaged hashes and original local.properties; asserted `git diff --cached --name-status -z` equals exactly `D\0local.properties\0`; ran full CRLF-aware diff check
- expected result: post-retirement build/tests/lint execute, original local file restored immediately, no other staging changes, unchanged app identity/data/signature compatibility
- observed result: exit0; **46 tasks executed**, **2 tests / 0 failures / 0 errors / 0 skipped**, **0 lint errors / 25 warnings**. Same APK hash, identity/version/min/target and v1/v2 verification as prior parent inspection. Packaged song SHA256 remains `88eb0db602e018b49a327947dd8607f04e6159e58f39ec38ed59f20c39af9d89`; JAR hash unchanged. Local properties byte-identical to original HEAD; exact staged whitelist and diff checks passed. Seven intended legacy files absent from working tree; no data/source deletion beyond the authorized list.
- status: passed for T015/T016 cleanup and host-tested documentation; T017 and full US1 acceptance remain incomplete
- evidence path: this file; private raw replay `/tmp/holysongs-readme-replay.akTBcG/{build.log,badging.txt,signing.txt}`; generated reports under app/build
- blocker/limitation: no exact committed-revision replay, no IDE sync/build or device proof. The safety snippet's trap was moved after successful backup so failed copying cannot delete an original local file. README clarifies Studio runtime vs GradleJDK, AGP10 opt-out removal, stale sdk.dir handling, and excluded release work. Warnings remain unsuppressed; see prior review dispositions.
- maintainer approval reference for accepted provider changes or limitations: n/a; user approved same-model cleanup recovery only. No license acceptance, provider change or release approval.

The recovery used native workflow `f7240d36-1228-4c5c-908a-00e3afc26d74`, child `1e42d4fb-303c-40e2-b5af-e928521ea18d`, model `github-copilot/mai-code-1.1-flash`. An attestation policy was requested to avoid the incompatible generic no-staged-files requirement, with independent parent enforcement of the exact authorized index delta. That automation reconciliation did **not** succeed: the final runtime gate still rejected `no-staged-files` evidence. The earlier and recovery workflows both remain failed historical records; neither is represented as a passing automation gate.

## Terminal automation failure, independently accepted task work

Workflow `f7240d36-1228-4c5c-908a-00e3afc26d74` and child `1e42d4fb-303c-40e2-b5af-e928521ea18d` ended **failed** with exact error `Acceptance rejected: no-staged-files evidence missing from child report.` Repository/cwd `/home/hanli3/GIT/holysongs-android`, branch master and base HEAD remain unchanged. Receipt: `/tmp/pi-subagents-uid-1000/async-subagent-runs/f7240d36-1228-4c5c-908a-00e3afc26d74/workflow-receipt.json`.

The parent did not retry or substitute execution methods/models. Final partial state and separate index/worktree patches were captured privately in `/tmp/holysongs-recovery-final.uX4ymw/`; archive SHA256 `d34b42c955f58a93cc7c65d7ae84a3167e61b764c8dbc2f39813e77e4a23065e`. Parent reconfirmed only D local.properties is staged and its original bytes remain on disk. Original .specify and feature-planning documents/checklists were compared with the pre-implementation archive and preserved, except authorized task checkbox updates. No post-implementation hooks file exists.

T015/T016 remain marked complete because their actual cleanup/documentation requirements were independently reviewed and executed by the parent before the child's final report. This does not pass the failed delegated automation gate, T017 or the overall feature. Fifteen of forty tasks are complete; T002 and T017 onward remain unfinished. At that handoff, the failed gate's policy resolution remained an orchestration blocker. **Subsequent resolution:** native workflow `7f58d170-910e-4948-a642-5ecb07fbe2ab` completed with independently host-verified explicit acceptance allowing exactly the authorized local.properties untracking. See [staging-policy.md](staging-policy.md) for the effective contract, current local-file preservation hash and verified receipt. This resolves the scoped staging-policy conflict without changing the earlier failed results or passing any outstanding app/IDE/device check.
