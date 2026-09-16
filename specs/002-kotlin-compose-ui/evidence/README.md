# Feature 002 verification records

**T002 completed:** 2026-09-15. This defines the recording format, not successful execution
of any build or runtime scenario. Feature001's dated results and unfinished statuses remain intact.

## Required fields

Quote from data-model.md:

> requirement/scenario ID; date; source revision; APK SHA-256; toolchain; device/emulator identifier; Android API/version; WebView version where applicable; prerequisites; steps; expected result; observed result; status (`passed`, `failed`, `blocked`); evidence path; blocker/limitation; maintainer approval reference for accepted provider changes or limitations

> Unexecuted checks have no success result

Use one dated record per scenario/artifact/endpoint. Append retests; never overwrite prior failures
or combine distinct APKs into a claimed final pass. `blocked` covers required scenarios which
cannot execute; explain the missing prerequisite. A defined but not attempted future scenario has
no observed success, and must not be marked passed.

## Record template

| Field | Value |
|---|---|
| Requirement/scenario ID | Stable B/U/N contract ID and task reference |
| Date | ISO date/time with timezone |
| Source revision | Full commit plus dirty patch/checkpoint identity if applicable |
| APK SHA-256 | Exact tested APK; not applicable only for source/setup checks |
| Toolchain | Launcher/daemon/build JDK, Gradle, AGP, compiler/plugin, SDK/Build Tools, IDE as relevant |
| Device/emulator identifier | Explicitly selected and ownership-verified target; N/A for host-only checks |
| Android API/version | Observed runtime values, not inferred from AVD name |
| WebView version | Actual package/version where applicable, otherwise N/A |
| Prerequisites | Fixture/isolation/network/navigation/window/font state and predecessor gates |
| Steps | Reproducible commands or interaction sequence |
| Expected result | Contract behavior; distinguish required safety from actual legacy defects |
| Observed result | Actual outcome, or unexecuted with reason |
| Status | passed / failed / blocked |
| Evidence path | Curated summary plus private raw-artifact reference |
| Blocker/limitation | Missing evidence, failures, scope limits; none only if verified |
| Maintainer approval reference | Owner/date/scope for provider changes/limitations, otherwise N/A |

## Evidence boundaries

- Recovery/setup checkpoints do not establish compiler, IDE, device, pilot or final acceptance.
- A successful `gradlew help` is configuration evidence only; it is not application compilation.
- Host XML/pinyin/Collator tests cannot establish Android ordering, rendering or lifecycle parity.
- Runtime captures must identify the changed-toolchain legacy APK separately from the original
  merged checkpoint APK. Never manufacture API23 fixtures from API37/host output.
- Raw logs, APKs, screenshots, preference snapshots and keys stay outside tracked source.
  Only sanitized summaries and deliberate test-only golden fixtures belong in the repository.
- Test preference writes require disposable-device authorization and snapshot/restore. Do not
  clear production data, uninstall a user's app, or disable unrelated apps to create missing recipients.
- API23 and API37 are separate required endpoints. A missing endpoint blocks its gate; any
  accepted provider limitation is separate from non-waivable crash/security/offline-return safety.
- Owner's current-session Java25 exception is recorded in environment.md. Report actual JDKs;
  do not relabel a Java25 daemon run as passing the original JDK17-only contract.
- Checkbox changes belong to the master and require task evidence. All final acceptance checks
  remain open until the complete final artifact has the required results and approvals.
