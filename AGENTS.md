# AGENTS.md

## Branch workflow

- Treat `master` as the stable branch. Do not develop directly on `master`.
- Use `develop` as the default working branch for requested changes.
- Before starting work, compare the chosen development branch with `master`.
- If `develop` already contains unrelated unfinished/unmerged work, it is acceptable to create a separate branch named `develop-<feature_name>` for the new task instead of mixing unrelated work.
- When using a `develop-<feature_name>` branch, clearly warn the user which commits/messages on that branch (or on `develop`) are not yet present in `master`, so the user knows what remains unmerged.

## Checkpoint commits

- Commit working progress frequently so changes are recoverable.
- As a practical default, make at least one useful checkpoint commit per user prompt when code or repository files were changed, unless the prompt results in no meaningful change.
- Keep checkpoint commit messages specific enough that the user can understand what was changed from the commit history.
- Avoid bundling unrelated features into the same commit when they can reasonably be separated.

## Build and verification

- After making a playable/build-related change, run the relevant CI/build and provide the resulting artifact when practical.
- Prefer keeping the Windows x64 build pipeline working on the active development branch.
- Do not treat a successful compile alone as confirmation that gameplay works; user testing is the final verification for interactive/game behavior.

## Merge policy

- Do not merge development work into `master` merely because CI passes.
- Wait for the user to explicitly confirm that the built game/change works as expected.
- Once the user confirms it works, the development branch may be merged into `master`.
- Before merging, summarize the commits/features that will enter `master`, especially if the branch contains more than the feature most recently discussed.
- If the branch includes unrelated unfinished work, do not merge all of it silently; either use a clean feature branch or clearly warn the user and keep unfinished work out of `master`.
