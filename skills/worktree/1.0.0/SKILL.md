---
name: worktree
description: Start work on an issue in its own git worktree. Derives the branch name and type from the issue, creates the worktree off the parent branch, and seeds a local checklist. Use when a task is assigned and no worktree exists yet.
---

# Worktree: start work on an issue

Each task works in its own worktree, so parallel tasks never share a working directory.

## Inputs

- The issue: its id, title, labels and body. The conductor gives you these; do not look them up in a tracker yourself.
- The parent branch to branch from. In a tree this is the parent's branch, not necessarily `main`.

## Derive the branch type

| Issue label | Type |
| --- | --- |
| feature, enhancement | `feat` |
| bug, fix | `fix` |
| docs, documentation | `docs` |
| refactor | `refactor` |
| test | `test` |
| chore, ci, tooling, config | `chore` |

Anything under `.github/` is `chore` with scope `ci`. If labels conflict, take the most specific. If still unclear, `feat` for new things, `fix` for corrections.

## Derive the slug

Lowercase the title, replace spaces and special characters with hyphens, drop a leading type prefix such as `feat:`, and cut to 35 characters.

## Create the worktree

```bash
git worktree add .worktrees/{type}-{id}-{slug} -b {type}/{id}_{slug} {parent-branch}
```

Make sure `.worktrees/` is ignored (add it to `.git/info/exclude` if the repository does not already ignore it).

If the project has a lockfile and installed dependencies, make them available in the worktree (symlink or clean install) so hooks and scripts run. Skip when not applicable.

## Seed the checklist

Create `CHECKLIST.md` in the worktree root and exclude it locally with `git rev-parse --git-path info/exclude`.

Take items in this order: the issue's checklist block (`<!-- checklist:start -->` to `<!-- checklist:end -->`) verbatim; else the `- [ ]` items in the issue body; else a one-line skeleton. The checklist is the working copy. Progress reaches the issue through events, not by editing a tracker.

## Report

State the branch, the worktree path and the first checklist item. Then start work.

## Never

- Put a secret, token or private data in a branch name, checklist or commit.
- Branch from `main` when you have a parent branch.
