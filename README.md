# Repertoire

The catalogue of role templates and skills for [Partitur](https://github.com/VirtueMe/partitur), a tree-structured, gated agent swarm.

The catalogue lives in its own repository so that merging here is the approval. A project pins the catalogue at an approved commit; a merged change reaches a project only when a PR in that project moves the pin.

## Layout

```
schema/role.edn        Malli schema every role template must satisfy
roles/<name>/role.edn  mandate, gate, mounts, tokens, default model
roles/<name>/instructions.md   agent-neutral instructions
skills/<name>/SKILL.md
```

Starting roles: architect, coder, tester, reviewer, merger, writer. Starting skills: worktree, commit, review.

Models are named by class (`:strong`, `:standard`, `:fast`); each project maps the classes to concrete models.

## Changing the catalogue

`v0.1.0` was written by the human and merged by the human. Every later change is a proposal raised as an escalation, opened as a pull request, and merged only by the human. Branch protection enforces this.

## Check

```
bb check
```

CI runs this on every pull request: each `role.edn` must satisfy `schema/role.edn`, match its directory, and have `instructions.md`; each skill needs a `SKILL.md` whose frontmatter name matches its directory.

## Licence

MIT.

## Releases

Versions are managed by [Release Please](https://github.com/googleapis/release-please) from Conventional Commits. It keeps a release PR open with the changelog and version bump; merging that PR tags and publishes the release. Pre-1.0, `feat` bumps the minor version and `fix` the patch.
