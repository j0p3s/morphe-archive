# Contributing Guidelines

## How we work

This repository operates on a direct-to-main model with a strict No-PR policy:

- No Pull Requests: work is committed directly to main.
- Branches other than main are short-lived local task branches and are deleted after integration.

## Commit rules

Every commit must follow these rules:

- Format: Conventional Commits (feat, fix, docs, style, refactor, perf, test, build, ci, chore, revert).
- Language and mood: English, imperative mood (e.g., feat(capcut): bypass watermark gate).
- Title length: 72 characters or fewer.
- Atomicity: one logical unit of work per commit.
- Vertical feature slicing: patch implementation and its corresponding documentation must be committed in the same atomic commit.

## Mandatory gate before declaring any patch done

Every patch change must pass the patch test verification gate before being declared done:

```bash
./gradlew runPatchTest -Papp=<target>
```

Acceptance criteria:
- 100% patches pass.
- Zero fingerprint mismatches.
- Zero smali compile errors.

Quiet invocation for background or logfile runs:

```bash
./gradlew runPatchTest -Papp=<target> --console=plain > build/patchtest-<target>.log 2>&1
```

## Style

- Zero emojis: no emojis in code, scripts, configs, logs, or commit messages (plain ASCII only).
- Named constants: use named constants instead of magic literals.
- Shared constants: consume app.morphe.patches.shared.Constants, never inline Compatibility blocks.

## Local hooks

Configure Git to execute the repository commit-validation hooks:

```bash
git config core.hooksPath .githooks
```
