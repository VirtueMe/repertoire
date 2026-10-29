---
name: commit
description: Finish a batch of work with a well-formed commit. Determines the commit type, drafts the message, flags version-bump impact, and records progress on the checklist. Use at the end of a batch, before handing to the next role.
---

# Commit: end of a batch

A batch is a coherent piece of work that can be checked on its own. Commit at each one.

## See what changed

```bash
git status
git diff --staged
git diff
```

If nothing is staged, decide which files belong in this batch. Stage only those. Never stage files outside your role's write paths.

## Review first

If the batch is non-trivial (more than about 20 lines, more than one file that is not docs, or any new abstraction, state, dependency or public interface), run the `review` skill before committing. Skip it for trivial batches and say so in your report.

## Choose the type

| Type | Use for |
| --- | --- |
| `feat` | A new capability |
| `fix` | Correcting something broken |
| `refactor` | Restructuring with no behaviour change |
| `test` | Tests only |
| `docs` | Documentation and ADR text |
| `chore` | Tooling, dependencies, configuration, CI |

`feat` adds something new; `fix` corrects something wrong. Add a scope when the change sits clearly in one area (`feat(core):`); leave it out when it is broad.

If the project releases from commit types (for example `feat` makes a minor release, `fix` a patch), say which bump the commit causes.

## Write the message

```
{type}({scope}): {imperative description, under 72 characters}

- {what changed, essentials only}

Part of #{id}
```

Add the project's attribution trailer if its conventions ask for one.

## Commit

Commit when your role is allowed to and the checks pass. If the project requires approval first, show the message and wait.

```bash
git add {files}
git commit -m "{message}"
```

## Update the checklist

Tick the items this batch resolved in `CHECKLIST.md`. Strike through (`~~item~~`) any item you descoped and record why in an event on the issue. New items go at the end.

## Never

- Put a secret, token or private data in a message or file.
- Merge to `main`, or push to a protected branch.
- Commit work that fails the project's checks.
