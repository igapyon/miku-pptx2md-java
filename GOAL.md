---
purpose: ai-agent-goal
read_when:
  - before_starting_work
  - before_finishing_work
  - when_scope_is_unclear
update_when:
  - goal_changes
  - done_conditions_change
  - stop_conditions_change
---

# Goal

This file defines what the AI agent is trying to accomplish.
Read this before starting work, before deciding that work is complete, and whenever scope becomes unclear.

## Objective

Maintain `miku-pptx2md-java` as the Java companion of the upstream TypeScript /
Node.js `miku-pptx2md` project. The current Java runtime version is `0.4.0`,
aligned with the checked upstream Node package version.

The first version should be good enough for local Java CLI use, basic
automation, and continued upstream-following maintenance. It does not need to
claim complete Node / Java parity for every PowerPoint feature.

The first usable Java `0.2.0` milestone is complete. The current goal is to keep
the Java runtime as close as practical to the current Node version's CLI/core
behavior.

## Done

- A single-module Maven Java runtime exists and follows the sibling
  `miku-xlsx2md-java` / `miku-docx2md-java` shape.
- The Java CLI supports the first-version contract for one local `.pptx` input:
  Markdown output, summary text, summary JSON, notes inclusion/exclusion,
  debug diagnostics, verbose diagnostics, `--help`, and `--version`.
- Core conversion covers the first-version PPTX feature set by referring to the
  checked upstream Node version: metadata, slide order, title/text blocks,
  basic lists, basic tables, external hyperlinks, image references, speaker
  notes, and structured diagnostics for known unsupported content.
- Focused Java tests cover core conversion, CLI behavior, summaries, and
  representative fixtures following the Node version's fixture and test intent.
- Node / Java parity is checked for representative upstream fixtures or
  documented as a focused comparison command with current known differences.
- README and migration documents record the supported first-version scope,
  upstream snapshot, sister references, mappings, remaining work, and
  verification commands.
- `mvn test`, `mvn package`, and runtime jar smoke commands pass.
- A release workflow is present for CLI runtime release assets.
- Java `0.4.0` follows the checked upstream Node `0.4.0` representative
  fixture and CLI behavior.
- Remaining parity work is explicitly listed in `TODO.md` or
  `docs/remaining-migration-items.md`.

## Next Goal

- Bring Java behavior as close as practical to the upstream Node version.
- Expand Node / Java parity fixtures and comparison scripts.
- Resolve or explicitly document remaining differences in Markdown output,
  summary JSON, diagnostics, asset manifest output, and supported PPTX feature
  handling.
- Keep the Java runtime version aligned when the checked upstream Node package
  version is intentionally adopted.

## Stop

- Implementing a feature would require guessing upstream semantics not present
  in the checked `miku-pptx2md` repository.
- A requested first-version requirement conflicts with the upstream Node CLI
  contract or the sibling Java repository shape.
- `TODO.md` の `Retry Log` に同じ原因の失敗が3回記録された
