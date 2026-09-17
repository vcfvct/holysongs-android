# Transition from Build Modernization to Kotlin/Compose

**Recorded**: 2026-09-15
**Baseline**: merged Gradle checkpoint `f62519b` (PR #3)
**Successor**: [Incremental Kotlin and Jetpack Compose UI](spec.md)

## Decision and Boundaries

The owner requested Kotlin and Jetpack Compose after merging the intermediate Gradle PR.
Use a new feature spec rather than rewriting the original Java-preserving build scope as
though Compose had always been included. This follows the constitution's gradual-migration
principle: build checkpoint, bounded UI slices, and regression verification between slices.

The predecessor's Java-only, Holo-preservation, and no-Compose constraints describe feature
001; they are not restrictions on the explicitly requested feature 002. Feature 002 permits
Compose UI and corresponding styling changes while preserving data, settings, offline use,
and existing actions.

On 2026-09-17 the owner separately approved replacing the repository-hosted
`libs/pinyin4j-2.5.0.jar` with the pinned Maven Central coordinate
`com.belerweb:pinyin4j:2.5.0` under issue #19. This changes dependency delivery, not the
preserved pinyin semantics. Earlier bundled-JAR requirements, hashes, and evidence remain
historical records of the baselines to which they apply; current build and verification
instructions use the locked Maven dependency instead. On 2026-09-15 the owner separately approved dropping old Android support
in response to the API 23 (Android 6.0) minimum proposal: "yes, dropping old android is fine."
Feature 002 therefore adopts API 23 as its support floor, subject to dependency verification;
API 14–22 devices cannot install or update to the new build. A higher floor requires renewed
approval. The current Gradle configuration is unchanged by this documentation decision.

The original checkboxes and dated evidence remain historical records. Unfinished work is not
marked completed, and old failures/blockers are not erased. Do not execute the old Java-specific
implementation sequence blindly before starting Compose. Plan equivalent outcomes under 002,
using the mapping below. Read this transition before resuming feature 001's implementation plan.

## Disposition of Unfinished Tasks

| Original tasks | Disposition / continuing obligation |
|---|---|
| T002, T017 | Retain as unfinished build-checkpoint verification. Reuse exact-revision host evidence where applicable, without claiming missing IDE/device checks passed. Feature 002 FR-001/FR-013 independently require clean-build/IDE/install verification for the new artifact. |
| T018–T021 | Carry into 002 FR-005/FR-012 as regression-baseline work. Test files already exist but were recorded as partial; review coverage before extending them. |
| T022–T025 | Supersede legacy file-specific prescriptions with equivalent loading, navigation, settings, sharing, and lifecycle outcomes in 002 FR-005–FR-010. Fix retained legacy code only where needed for a working slice; do not maintain duplicate UI implementations indefinitely. |
| T026 | Prioritize in the Compose list pilot under 002 US1/FR-007, then validate all migrated screens. Reproduce the reported overlap; do not assume Compose alone fixes it. Preserve any owner's in-progress layout work until replacement is explicitly implemented and verified. |
| T027 | Carry reader runtime acceptance into 002 US1/US2 and FR-012/FR-013, including actual catalog rendering, duplicate winners, per-runtime sidebar expectations, settings, lifecycle, timing, and crash/ANR observations. |
| T028–T034 | Carry video audit/safety/return-flow obligations into 002 US3/FR-011–FR-013. Existing WebView code may remain Java/View-based. No automatic provider removal/replacement or transport-security relaxation. |
| T035–T036 | Carry signing guidance and artifact-specific verification reporting into 002 FR-013/FR-014. Preserve prior dated outcomes. |
| T037–T040 | Do not claim original full-feature acceptance. Apply final review and clean-artifact validation to 002's explicit scope/support matrix; retain 001's historical incomplete status and evidence. The old no-Kotlin/no-Compose check is not a gate for the authorized successor. |

Original completed tasks retain their recorded statuses; merge is not additional test evidence.
The predecessor's API 14/API 37 matrix remains the historical contract. The successor uses
approved API 23 as its minimum endpoint; planning must verify dependency compatibility and
select a dated current-runtime endpoint. API 14 checks are not gates for feature 002, but their
historical unexecuted status remains unchanged. This support decision does not convert them
into passes.

## Proposed Planning Sequence

1. Verify maintained Kotlin/Compose/AndroidX versions against the approved API 23 minimum
   and select a dated current-runtime endpoint. Document the loss of update eligibility for
   API 14–22 users; seek renewed approval if dependency research requires a higher minimum.
2. Review existing preservation tests and complete missing baseline coverage. Capture the
   overlap reproduction and critical runtime behavior; label unavailable observations blocked.
3. Enable Kotlin/Compose without a bulk Java conversion. Confirm source-set wiring, clean
   builds, lint, Java tests, and the chosen Compose testing setup.
4. Replace only the main list/sidebar UI first. Preserve the transition to the legacy lyric
   screen, fix insets, and verify navigation/restoration on the required runtimes.
5. Replace lyric, settings, and about UI in reviewable slices. Reuse existing helpers and
   preference storage, preserve shares/video entry points, and remove superseded code only
   after the replacement is checked. Separate mechanical conversions from behavior fixes.
6. Complete inherited video safety/audit work without requiring a Compose rewrite of WebView
   internals. Request approval if provider behavior must change.
7. Revalidate the final integrated artifact, update documentation, and reconcile coverage.
   Missing required evidence blocks acceptance; it is not waived by this scope transition.

This document remains the specification handoff and does not rewrite feature 001's historical
results. Feature 002 replaced the core reader screens with Kotlin/Compose and hardened the video
boundary, which subsequently moved from Java to same-identity Kotlin; see [tasks.md](tasks.md) and the feature evidence directory for attributable
results. On 2026-09-16 the owner reported testing the complete app on a phone, accepted the result,
and closed the phase. This product acceptance does not retroactively turn narrower automated,
provider, API23, IDE, process-death, compact-window, or WebView callback evidence into passes, and
it does not complete any historical feature 001 task or runtime check.
