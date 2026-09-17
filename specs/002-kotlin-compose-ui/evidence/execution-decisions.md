# Owner execution decisions — 2026-09-15, 22:38 America/New_York

The owner instructed: "it builds fine, and we can ignore android 6. let's keep going , feel free
to use multiple subagents in parallel with mai or luna model".

## Accepted continuation scope

1. **IDE build:** accept the owner's current-checkout Studio build report for T009 continuation.
   This is owner-attested IDE evidence, not an agent-observed IDE log or independently hashed IDE
   artifact. The separately hashed CLI app/test APKs and lock replay remain the selected T010 inputs.
   The earlier Java25 daemon exception continues; no local Java/IDE configuration is changed.
2. **Runtime endpoint waiver:** API23/Android6 execution is waived for feature002 continuation.
   This explicitly supersedes API23 runtime-gate/fixture obligations in the original tasks, plan,
   data model, contracts and quickstart. Required execution is now on the authorized API37 Pixel,
   including the still-applicable navigation, window, font, offline and safety scenarios.
   Do not create legacy-api23.json, relabel API37 observations, or mark Android6 tests passed.
   Keep the original API23 plan/history visible and report the runtime coverage limitation.
3. **Support floor unchanged:** retain minSdk23 and dependency/API compatibility checks. This
   waiver authorizes neither a higher minimum nor a claim of validated Android6 behavior.
4. **Other gates retained:** real API37 legacy capture before Main/index replacement, exact data/
   preferences/route preservation, regression tests, reviewer/master acceptance, application safety,
   and provider-change/accepted-unavailability approvals are not waived.
5. **Parallel delegation:** owner explicitly authorizes multiple bounded children using exact
   github-copilot/mai-code-1.1-flash or github-copilot/gpt-5.6-luna. Keep one writer per checkout
   and exclusive ownership of the selected device. Read-only review may run beside device-only
   execution on an unchanged source snapshot. No commit/publication or global model changes.

## Attributable continuation inputs

- HEAD: f62519b4bff420ab3f78b1d544732c25683f4772 plus current uncommitted feature002 work.
- App APK: 3661c49c61e377548129e098215ac6e3c2d96c946b8170a3572f8c4ebfd8c39e.
- Test APK: 39716a31f7cbfbaf98cf0afe9ae2f058cbe02a6ecf3f6546f4c0320ca1174df2.
- LegacyBaselineTest.kt: 0a0cc913a603846c917d11769bc4040c37d3fb79c06d01f45e9cbba2fc16f94d.
- Selected device currently emulator-5554; reverify Pixel_10_Pro_XL and runtime API37 before use.
- Owner previously explicitly confirmed this Pixel AVD is disposable and approved debug install/
  preference-changing tests. No production or unrelated device is authorized.

T009 is accepted under these recorded owner decisions and the prior host audit. T010 remains
unchecked until actual API37 capture and master review; this decision is not runtime evidence.

## JVM compatibility approval — 2026-09-16, 09:31 local

After the demonstrated Compose inline-bytecode blocker, the owner said: "yes, you can change
target to higer jvm, you can also change min sdk to higher if needed".

- Align Java source/target and Kotlin JVM target to **11**, the minimum required by the actually
  resolved Compose1.12.1 inline APIs. This supersedes original JVM8 clauses throughout feature002's
  plan/tasks/contracts. Earlier major52 build evidence remains historical, not rewritten.
- Retain the existing Java17 compilation toolchain and approved Java25 Gradle daemon; these are
  distinct from application bytecode target11.
- A higher minSdk is now conditionally authorized only if an actual compatibility requirement
  demands it. Current dependency metadata supports23, so keep minSdk23 unless new evidence proves
  otherwise. Record any necessary increase and resulting support loss before acceptance.
- No dependency substitution, compiler validation bypass, permission expansion, feature waiver,
  release signing or publication is authorized. Re-run mixed compilation, tests/lint, bytecode,
  APK/D8/API/signing inspection and the still-required API37 pilot checks.

This approves the bounded compatibility change, not the partial Compose pilot or runtime results.
