# Local properties staging policy

Date: 2026-09-15. **Status: verified by the native runner and reviewed by the parent.**

Cause: normal async-writer policy inference raises a requested `attested` level to `checked` and adds `no-staged-files`. Thus the earlier attempt did not remove the conflict. The supported explicit `agentContract: { version: 1 }` mode uses the supplied evidence list without that inferred requirement. Explicit `verified` acceptance is retained, with a host-run exact whitelist replacing only the incompatible staging assertion.

The repository is intentionally untracking `local.properties` while preserving its current user-local contents. This exception allows the existing staged deletion (`D local.properties`) only; no application change or additional feature-task completion is claimed.

## Explicit request-scoped contract

Supply these fields on the native child launch. `verifyCommand` is the complete Bash command in the Runtime verification section below, not a command name or invented policy field:

```javascript
{
  agentContract: { version: 1 },
  acceptance: {
    level: "verified",
    criteria: [
      "Only the authorized staged deletion of local.properties exists, and its current user contents remain intact and ignored"
    ],
    evidence: [
      "changed-files", "commands-run", "validation-output", "residual-risks"
    ],
    verify: [{
      id: "authorized-local-properties-untracking",
      command: verifyCommand,
      timeoutMs: 10000
    }]
  }
}
```

This was tested on the native `worker` with `github-copilot/mai-code-1.1-flash`. Keep all other task-specific tests/review requirements when reusing the staging exception; this focused run verifies Git policy only. Do not add the generic `no-staged-files` evidence item to this one-file exception, and do not disable acceptance globally.

The acceptance evidence intentionally omits `no-staged-files` because the authorized state is one staged delete for `local.properties`. The normal policy resumes after the staged untracking is committed.

## Exact protections in force

The runtime verification asserts all of the following:

- `git diff --cached --name-status -z` equals `b'D\0local.properties\0'`
- `git ls-files -- local.properties` is empty
- `git check-ignore -q -- local.properties` exits 0
- `sha256(local.properties)` equals `4f79b6fcd66f9921a892fd6023754652b8154e0b5df615128943508f4356bb25`

These checks protect the authorized deletion record, ensure the file remains ignored and absent from the index, and preserve the current user-local file contents instead of restoring an older snapshot.

## Preflight observed status

- Staged delta: exactly `D local.properties`
- `local.properties` is absent from the git index
- `local.properties` is ignored by git
- Current file hash matches `4f79b6fcd66f9921a892fd6023754652b8154e0b5df615128943508f4356bb25`
- Historical snapshot hash `39ecd...` is not the current user config and must not be restored

The current local file changed since the earlier build snapshot; its author was not established. It was already changed when this policy task checked it and was preserved rather than overwritten. Its private backup is `/tmp/holysongs-policy-preflight.TR83xp/local.properties.current`. Reconfirm the current authorized contents before later runs; this hash is a per-run preservation check, not a permanent ban on local configuration edits.

## Runtime verification

Native acceptance ledger: **status `verified`, evidenceStatus `verified`**. The `authorized-local-properties-untracking` host command ran in 34ms, exited0, and was **not memoized**. Output: `PASS: authorized untracking only; ignored current local file preserved`.

- Workflow: `7f58d170-910e-4948-a642-5ecb07fbe2ab` — completed
- Child: `af2cdf98-53ec-4cd4-90e5-e3ed0a716a9d` — completed
- Receipt: `/tmp/pi-subagents-uid-1000/async-subagent-runs/7f58d170-910e-4948-a642-5ecb07fbe2ab/workflow-receipt.json`
- Verified ledger: `/tmp/pi-subagents-uid-1000/async-subagent-runs/af2cdf98-53ec-4cd4-90e5-e3ed0a716a9d/status.json`

Parent inspected the effective policy and host verifyRuns, not just the child's success prose. The previous failed workflows remain failed historical records; this new successful run resolves the staging-policy blocker for the scoped exception.

```bash
python3 - <<'PY'
from pathlib import Path
import subprocess,hashlib
assert subprocess.check_output(['git','diff','--cached','--name-status','-z'])==b'D\0local.properties\0', 'Unexpected staged delta'
assert subprocess.check_output(['git','ls-files','--','local.properties'])==b'', 'local.properties still in index'
assert subprocess.run(['git','check-ignore','-q','--','local.properties']).returncode==0, 'local.properties not ignored'
assert hashlib.sha256(Path('local.properties').read_bytes()).hexdigest()=='4f79b6fcd66f9921a892fd6023754652b8154e0b5df615128943508f4356bb25', 'Current user local.properties changed'
print('PASS: authorized untracking only; ignored current local file preserved')
PY
```

## Scope limits

- No commit has been created by this action.
- No global Pi/agent/package policy changes are made.
- No app code or build configuration changes are introduced.
- No US1/application task completion is claimed.
- This record is policy verification only.

Once the untracking is committed, ordinary no-staged-files policy resumes.
