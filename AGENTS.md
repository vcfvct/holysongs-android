# Project Agent Instructions

## Model Routing and Delegation

Owner preference recorded on 2026-09-15:

- The main conversation model is the master/coordinator. At the time of this decision it is
  `github-copilot/gpt-6-astra`; keep the owner's selected main model unless instructed otherwise.
- The owner authorizes bounded subagent delegation for requested project work. Use
  **`github-copilot/mai-code-1.1-flash`**  or **`github-copilot/gpt-5.6-luna`** for all child subagents, including research,
  implementation and review. Pass the exact model explicitly rather than inheriting the parent.
- The master owns planning, task boundaries, synthesis, integration, final review and acceptance.
  Subagent outputs are evidence to assess, not automatic approval.
- If the requested child model is unavailable or a delegated run fails, report the blocker;
  do not silently substitute another model or execution protocol. Follow the harness's recovery rules.
- Keep one writer per working directory. Isolate concurrent writers, and preserve existing user
  edits. Delegation does not authorize unrelated changes, commits, publication or release actions.
- This is project-local guidance, not a change to global model/agent configuration. It applies
  to future delegated work; earlier research runs are not retroactively attributed to this model.

## Active Modernization Scope

Use `specs/002-kotlin-compose-ui/spec.md`, `plan.md`, and `transition.md` for the current
Kotlin/Compose migration. Follow `.specify/memory/constitution.md`. Preserve feature 001's
historical evidence and unfinished statuses; consult the transition mapping before resuming
its legacy Java-specific task sequence.
