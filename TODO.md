# TODO

- Consider `--assets-dir` with safe PPTX package path handling as an optional
  phase 2 parity feature.
- Decide later whether a separated `miku-pptx2md-java-maven` adapter is useful.

## Phase 2 Direction

After the first usable Java version is complete, make the Java version as close
as practical to the upstream Node version. Track remaining differences with
fixture comparisons, focused parity tests, and explicit known-difference notes.

## AI Agent Current Tasks

This section tracks active work items for AI agents.
Update this section while working. Do not rewrite unrelated TODO items.

### Tasks

- [x] Add representative Node core test intent coverage against upstream
  `miku-pptx2md`.
- [x] Expand representative PPTX feature parity by referring to the Node
  version's current behavior and tests.
- [x] Add representative Node CLI output parity coverage.
- [x] Add documented Node / Java CLI comparison command for representative
  upstream fixtures.
- [x] Add release workflow before finalizing `0.2.0`.
- [x] Add packaged jar smoke verification as an automated or documented smoke
  command.
- [x] Finalize `0.2.0` by changing versions from `0.2.0-SNAPSHOT` to `0.2.0`.
- [x] Update README and docs to describe the first-version supported feature
  set and known differences.
- [x] Keep optional `--assets-dir` in phase 2 unless explicitly pulled forward.

### Blockers

- None.

### Retry Log

Use this section only when the same task or error is repeated.
If the same failure appears 3 times, stop and ask the user.

- None.
