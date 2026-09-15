<!--
Sync Impact Report
- Version change: template placeholder -> 1.0.0
- Modified principles: new constitution established for a legacy Android app modernization initiative
- Added sections: Core Principles, Application Constraints, Development Workflow, Governance
- Removed sections: none
- Follow-up TODOs: RATIFICATION_DATE (original adoption date unknown)
-->

# HolySongs Constitution

## Core Principles

### I. Preserve the App's Core Mission
The app MUST continue to provide a reliable, offline-first catalog of Chinese hymn lyrics and related worship content. Modernization work MUST NOT remove the ability to browse songs, view lyrics, adjust display settings, share content, or rely on the app's lightweight local data model.

### II. Modernize Incrementally
The project MUST favor incremental modernization over redesign or wholesale rewrites. Legacy Java may remain in service until intentionally changed, and Kotlin adoption MUST be introduced gradually in a way that preserves existing behavior. Large refactors MUST be broken into reviewable stages with working checkpoints.

### III. Compatibility and Offline Reliability
The app MUST remain stable on supported Android devices and modern tooling without requiring a backend or cloud dependency for core features. The song library and user settings MUST remain available offline. Migration work MUST preserve user-facing functionality and local data integrity.

### IV. Keep the Architecture Simple and Maintainable
The project MUST favor straightforward app structure over premature abstraction. Features and screens MUST be implemented in a way that is easy to read, test, and maintain. Complexity MUST be justified by explicit product or maintenance needs.

### V. Quality Through Verification
The app MUST compile against supported Android SDKs and pass validation for critical user flows before merge. Any change affecting song loading, lyric display, sharing, settings, or external video search MUST be checked for regressions. Stability and compatibility take precedence over speculative cleanup.

## Application Constraints

The HolySongs project operates under the following constraints:

- The app MUST remain lightweight, local-first, and focused on lyric browsing and worship content.
- The project MUST use supported Android build tooling and modern SDK practices as soon as practical.
- Java and Kotlin MAY coexist during migration, but the project MUST avoid unnecessary language churn without a clear maintenance benefit.
- Data files, lyric assets, and user preferences MUST remain compatible with the app's core workflows.
- Deprecated Android APIs MUST be modernized when touched, with compatibility checks for critical screens and flows.
- The app MUST preserve readability and usability for Chinese-language content, including lyric formatting and accessibility needs.
- New scope MUST be justified by user value; the project MUST avoid over-engineering or feature creep beyond the app's original purpose.

## Development Workflow

The project MUST follow a disciplined modernization workflow:

- Changes MUST be broken into small, reviewable steps with working checkpoints.
- Legacy code MUST be stabilized before large refactors are attempted.
- New code and migrated code MUST preserve current behavior for song browsing, lyric viewing, settings, and sharing.
- UI and lifecycle changes MUST be validated against modern Android behavior, especially screen rotation, background/foreground transitions, and network state handling.
- Code review MUST confirm that modernization work does not introduce regressions or unnecessary complexity.
- Kotlin adoption MUST be gradual and incremental rather than a forced rewrite of the entire application.

## Governance

This Constitution supersedes ad hoc decision-making on project direction. All changes to the app's architecture, tooling, and feature scope MUST align with the principles above and be documented in the project record.

Amendments MUST include a rationale for the change, a migration or compatibility impact note, and a version bump using semantic versioning. A major version bump is required for backward-incompatible principle removal or governance changes; a minor bump is required for new principles or materially expanded guidance; a patch bump is used for clarifications or non-semantic wording fixes.

Compliance review MUST confirm that modernization changes preserve the app's mission, do not break critical user flows, and remain maintainable on current Android tooling. Any intentional deviation from this Constitution MUST be documented and explicitly justified.

**Version**: 1.0.0 | **Ratified**: TODO(RATIFICATION_DATE): original adoption date not recorded | **Last Amended**: 2026-09-15
