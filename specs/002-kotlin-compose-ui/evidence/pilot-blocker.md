# Compose pilot prerequisite blocker — 2026-09-15, 23:42 local

**Status: blocked awaiting owner decision; T020–T024 remain unchecked.**
Worker f6b95f15-bc0b-46ea-8ba4-8bd9b69f2e05 (github-copilot/gpt-5.6-luna) finished with partial
source changes, not a passing implementation. Output: compose-list-pilot.md in its managed
artifact directory. No device execution or new accepted APK was produced.

## Exact failure

`JAVA_HOME=<actual Homebrew JDK17 libexec> ./gradlew :app:compileDebugKotlin --console=plain`
failed with `Cannot inline bytecode built with JVM target 11 into bytecode that is being built
with JVM target 1.8`. Raw initial log: `/tmp/holysongs-t020-compile.log`. Initial unrelated import/
API mistakes were corrected by the worker, but source still needs compile review after unblock.

Master independently inspected classes.jar from the actually resolved Compose1.12.1
foundation-layout-android and runtime-android AARs: ColumnKt, RowKt and ComposablesKt have class
major55 (Java11). The project still targets Java/Kotlin8. Earlier theme-only compilation at major52
passed because it did not exercise the inline APIs now used by the list/sidebar; earlier success
was insufficient evidence for this new use. Do not downgrade/force dependencies, disable inline
checks or silently change bytecode targets to manufacture acceptance.

The supervisor rejected an unapproved target change. Proposed owner decision: align both Java
and Kotlin compilation to JVM11, retaining minSdk23, selected dependencies, Java17 compilation
toolchain and owner-approved Java25 daemon. This is a proposal, **not yet approved**. Rebuild,
inspect bytecode/D8/APK/API compatibility, tests and lint after any authorized change. No Android6
runtime pass is implied; its prior owner waiver still applies.

## Preserved partial work

New Kotlin MainActivity, LetterSidebar, SongListScreen; Java Main removed to avoid duplicate FQCN;
Main manifest theme updated; four state strings added; minimal Settings CLEAR_TOP workaround
removal is already present. None of these changes is accepted as T020–T023 completion. Legacy
capture test remains and must be retired after transferring any remaining assertions at T024;
never add fake listView compatibility to the replacement Main merely to compile that retired test.

The working checkout currently does **not compile** under its still-approved JVM8 configuration.
Prior successful build/test/runtime results remain evidence for their earlier artifacts, not for
this incomplete pilot. No commits/staging/publication or build-target change was made.

Recovery checkpoint: `$HOME/.local/state/holysongs-android/checkpoints/
jvm11-blocked-20260915T234257/`, containing132 current files in workspace.tar.gz, file hashes,
tracked binary patch and full status. Archive SHA-256:
32395c77939c8e1ed41acc7fd897460d4aad242af9edf427e4036d9a99202ccc.
Private directory0700/files0600. Branch master, HEADf62519b4bff420ab3f78b1d544732c25683f4772.
Recover by extracting separately and comparing/selectively restoring; do not overwrite owner
changes or reset the dirty checkout. No extension registry exists, so no post-hooks to dispatch.

## Owner decision — 2026-09-16, 09:31 local

The owner approved a higher JVM target and a higher minSdk only if necessary. Master selects
aligned Java/Kotlin11 from the observed requirement, retaining minSdk23 because the resolved
metadata supports it. See execution-decisions.md. The approval removes the decision blocker;
compilation and pilot acceptance remain pending revalidation. Earlier failed outcomes above
are preserved. No target change is described as a passing build before it executes.

## JVM11 validation / new Espresso blocker — 2026-09-16

Luna worker7576c35d-9085-4ea8-8505-7933119cdd64 aligned Java/Kotlin11 in app/build.gradle,
retaining min23 and the existing pinned dependency lock. Normal assembleDebug/testDebugUnitTest/
lintDebug/assembleDebugAndroidTest passed. Parent independently confirmed MainActivity,
SongListScreenKt and retained JavaSettings class major55; current JVM XML reports25 tests,
0 failures/errors/skips. App APK1c1b3e1a9dbdcb8e0693188d446b168b3e156fc0d506e3dc2a92f44ac7ce7227;
test APK67f590fd70df8400422ccf928a11c9a6375efffaa3688d7cb5a1caeca802b97f;
lock remains ffb46252f9f5cc1140318e46270b9c23ead4da7880421955e2a838e18271f9ef.

The T024-start legacy capture retirement occurred: LegacyBaselineTest was removed, frozen API37
fixtures retained. Catalog/order/letter/winner/route/share/About assertions are now in the actual
Main UI tests; preference storage/types/defaults/actions/listener/restore assertions are in
ReaderPreferencesTest. This is assertion transfer/preparation, not completed T024 acceptance.

On the verified disposable PixelAPI37, all4 preference tests passed. The real Compose UI class
returned11 failures before product assertions: `java.lang.NoSuchMethodException:
android.hardware.input.InputManager.getInstance []` in Espresso's
InputManagerEventInjectionStrategy. Explicit fit/Settings mutation tests failed at the same
framework initialization boundary. No dependency override or reflection bypass was applied.
Compact/resize were not advanced after that failure. The proposed v2 Compose rule did not fix
it and was not retained. These are failed executions, not skipped/passing UI results.

Direct selected-device launch/Settings/Back smoke worked with visible Compose list/actions and
same Main task; it is not a substitute for blocked assertions or launch-to-Ready timing. Original
preference file content hash was restored; no executed device-setting mutation remained.
Private evidence: `$HOME/.local/state/holysongs-android/verification/
api37-compose-jvm11-20260916T093508-0400/`. No provider/video probe or final acceptance occurred.

T020–T024 remain unchecked. Current blocker is the Espresso/API37 test-framework mismatch,
not JVM target or minSdk. Parallel read-only investigation and production review are in workflow
b6936f7b-58af-433e-b365-68aa8bcccf5e; any test-library correction requires actual compatibility
evidence and revalidation, not suppressing failures. Earlier JVM8 failures remain historical.
