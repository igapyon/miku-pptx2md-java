# TODO

- Decide later whether a separated `miku-pptx2md-java-maven` adapter is useful.

## Phase 2 Direction

After the first usable Java version is complete, make the Java version as close
as practical to the upstream Node version. Track remaining differences with
fixture comparisons, focused parity tests, and explicit known-difference notes.

CLI parity has a focused tracking baseline before claiming Node-equivalent
behavior. Keep comparing Java `--help`, metadata command handling, option names,
stderr diagnostics, output artifact descriptions, and exit-code behavior against
the upstream Node CLI tests and help text.

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
- [x] Pull optional `--assets-dir` forward from phase 2 after CLI parity review.
- [x] Implement optional `--assets-dir` with safe PPTX package path handling,
  sidecar asset writes, Markdown image links, and `manifest.json`.
- [x] Align Java `--help` with the upstream Node agent-readable help shape:
  include `CONTRACT`, `OUTPUTS`, `EXAMPLES`, `--include-unsupported-comments`,
  and current asset / manifest wording.
- [x] Add Java CLI tests that mirror the upstream Node CLI metadata-command
  assertions for `--help`, `--version`, mixed metadata command rejection, and
  expected stderr / stdout separation.
- [x] Extend `docs/upstream-cli-mapping.md` with a help-text parity status
  section that records which Node help sections and options are identical,
  adapted for Java, or intentionally different.
- [x] Add or update a local comparison command that captures Node and Java
  `--help` / `--version` outputs and reports known differences.
- [x] Expand the Node / Java CLI comparison command across the checked upstream
  generated fixture set, including Markdown and summary JSON.
- [x] Compare `--assets-dir` Markdown, manifest JSON, and written image bytes
  against the upstream Node CLI.
- [x] Start phase 2 parity tightening by expanding the Node / Java CLI
  comparison command to cover summary text, stdout Markdown, `--no-notes`,
  `--debug`, metadata command rejection, and read-failure behavior.
- [x] Align Java CLI read-failure diagnostics with the upstream Node
  `[input] read failed:` message shape.
- [x] Bump the Java runtime version to `0.4.0` to align with the checked
  upstream Node package version.

### Blockers

- None.

### Retry Log

Use this section only when the same task or error is repeated.
If the same failure appears 3 times, stop and ask the user.

- None.
