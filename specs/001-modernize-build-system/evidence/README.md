# Verification evidence rules

This directory stores only sanitized evidence, not raw APKs, signing keys, credentials, screenshots, or private device state. Raw runtime logs, APKs, and installed artifacts remain outside tracked source.

## Verification record fields (verbatim)

Every future verification file must preserve these fields in the exact order and wording below:

```text
requirement/scenario ID; date; source revision; APK SHA-256; toolchain; device/emulator identifier; Android API/version; WebView version where applicable; prerequisites; steps; expected result; observed result; status (`passed`, `failed`, `blocked`); evidence path; blocker/limitation; maintainer approval reference for accepted provider changes or limitations
```

## Stable requirement/scenario ID convention

Use a stable ID format that is independent from the filename and consistently identifies the requirement, user story, and endpoint.

```text
<CATEGORY>-<ENTRYPOINT>-<NNN> [FR-###|USx|endpoint]
```

Examples:

- `C1-CLI-001 [FR-001|US1|CLI|clean-checkout]`
- `C1-IDE-001 [FR-001|US1|IDE|Android Studio Quail 4]`
- `C2-APK-001 [FR-006|US1|APK|app-debug]`
- `C3-RDR-001 [FR-008|US2|API14|offline-first-launch]`
- `C3-RDR-002 [FR-008|US2|API37|offline-first-launch]`
- `C4-SHR-001 [FR-010|US2|share|recipient-available]`
- `C4-VID-001 [FR-011|US3|API14|provider-audit]`

Rules:

- `C1` = developer build interface
- `C2` = APK and installation identity
- `C3` = offline reader/settings behavior
- `C4` = sharing and video-provider behavior
- `C5` = evidence and completion
- The `ENTRYPOINT` identifies the access path or runtime endpoint, such as CLI, IDE, APK, API14, API37, or share.
- Include the requirement ID and the user-story label in every record to preserve traceability.
- Keep the requirement/scenario ID stable across reruns; do not replace a blocked or failed result with a renamed record that hides the prior outcome.

## Naming rules

- Use descriptive but stable file names under `specs/001-modernize-build-system/evidence/`.
- Prefer scenario slugs such as `us1-build.md`, `us2-reader.md`, `providers.md`, or `verification.md`.
- When validation is rerun, keep the original dated record and add a new dated record instead of overwriting earlier findings.
- A new evidence record must not silently replace a failed or blocked result with a later success claim.
- Every record must identify the exact artifact under review, the test environment, the source revision, and the relevant requirement/scenario ID.

## Status rules

- `passed`, `failed`, and `blocked` are the only valid status values.
- Lower-case formatting is required.
- Unexecuted checks have no success result.
- A result is not `passed` unless it is based on an actual command, device observation, or recorded artifact inspection.
- Missing device access, missing toolchain components, or missing provider availability remain a `blocked` result rather than a passing result.
- "Unexecuted checks have no success result" is an explicit requirement and applies to every evidence file.

## Evidence expectations

- Link each check to the relevant requirement or scenario ID.
- Capture the prerequisites and exact steps used.
- Record expected results before comparing against the observed result.
- Separate environment problems from app failures; do not mark an environment blocker as a product pass.
- Keep provider approval references separate from general technical validation.
- Retain dated prior failures when rechecking later artifacts or environments.

## Scope boundary

This directory supports the build-modernization feature only. No release keystore material, SDK credentials, or generated local build outputs are checked in here.
