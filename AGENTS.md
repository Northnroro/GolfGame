# AGENTS.md

## Branch workflow

- Treat `master` as the stable branch. Do not develop directly on `master`.
- Use `develop` as the single default working branch for all requested changes, even when it contains unrelated unfinished or unmerged work.
- Do not create `develop-<feature_name>` or any other feature branch unless the user explicitly asks for a separate branch.
- Before starting work, compare `develop` with `master` and keep the user aware of commits/features that are not yet in `master` when relevant.
- It is acceptable for multiple in-progress features to accumulate together on `develop`.

## Checkpoint commits

- Commit working progress frequently so changes are recoverable.
- As a practical default, make about one useful checkpoint commit per user prompt when code or repository files were changed, unless the prompt results in no meaningful change.
- Keep checkpoint commit messages specific enough that the user can understand what changed from the commit history.
- Unrelated features may coexist on `develop`; commits should still be reasonably descriptive checkpoints.

## Build and verification

- After making a playable/build-related change, run the relevant CI/build and provide the resulting artifact when practical.
- Keep platform build pipelines working on `develop` when possible.
- Do not treat a successful compile alone as confirmation that gameplay works; user testing is the final verification for interactive/game behavior.

## Merge policy

- Do not merge development work into `master` merely because CI passes unless the user explicitly asks to merge it.
- When the user confirms a build/change works, or explicitly asks to merge, `develop` may be merged into `master`.
- Before merging, summarize the commits/features that will enter `master`, especially when `develop` contains several accumulated changes.
- Never silently drop unrelated work from `develop`; if the user asks to merge the branch, assume the accumulated `develop` state is intended unless they say otherwise.
