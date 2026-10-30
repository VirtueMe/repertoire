# Repertoire

The catalogue of role templates and skills for [Partitur](https://github.com/VirtueMe/partitur), a tree-structured, gated agent swarm.

The catalogue lives in its own repository so that merging here is the approval. A project pins the catalogue at an approved commit; a merged change reaches a project only when a PR in that project moves the pin.

## Layout

```
schema/role.edn        Malli schema every role template must satisfy
roles/<name>/<version>/role.edn   mandate, gate, mounts, tokens, default model
roles/<name>/<version>/instructions.md   agent-neutral instructions
roles/<name>/role.edn, instructions.md   links to the latest version
skills/<name>/<version>/SKILL.md
skills/<name>/SKILL.md   link to the latest version
```

Starting roles: architect, coder, tester, reviewer, merger, writer. Starting skills: worktree, commit, review.

Models are named by class (`:strong`, `:standard`, `:fast`); each project maps the classes to concrete models.

## Changing the catalogue

`v0.1.0` was written by the human and merged by the human. Every later change is a proposal raised as an escalation, opened as a pull request, and merged only by the human. Branch protection enforces this.

## Check

```
bb check
```

CI runs this on every pull request: in each version folder, `role.edn` must satisfy `schema/role.edn`, match its role directory, and sit beside `instructions.md`; each skill needs a `SKILL.md` whose frontmatter name matches its directory; and the links at the root of each role and skill must point to the highest version.

Roles and skills are versioned by folder (`roles/coder/1.0.0/`). A released version is never edited: `bb check` fails if any file in a version folder that exists in the previous `v*` release tag has changed, so a change is a new folder. Removing a whole deprecated version is allowed. Patch is for wording, minor for added guidance, and major for any change to a role's mandate, writable paths, mounts, tokens or tmux access. The checks cannot judge which bump a change deserves, so review does. If no release tag exists, the immutability check is skipped.

## Licence

MIT.

## Releases

Versions are managed by [Release Please](https://github.com/googleapis/release-please) from Conventional Commits. It keeps a release PR open with the changelog and version bump; merging that PR tags and publishes the release. Pre-1.0, `feat` bumps the minor version and `fix` the patch.
