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
Node.js `miku-pptx2md` project. The current Java runtime version is `0.5.1`,
following the checked upstream Node `0.5.1` package version.

The first version should be good enough for local Java CLI use, basic
automation, and continued upstream-following maintenance. It does not need to
claim complete Node / Java parity for every PowerPoint feature.

The first usable Java `0.2.0` milestone is complete. Java `0.4.0` aligned the
runtime with the checked upstream Node `0.4.0` representative behavior. Java
`0.5.1` follows the checked upstream Node `0.5.1` representative behavior,
including YAML front matter and slide comment rendering. The current goal is
to continue low-risk refactoring of the Java core so future Node-parity work
can be added without growing `MikuPptx2mdCore` into a hard-to-maintain
monolith.

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
- Java `0.4.1` preserves the `0.4.0` behavior while starting focused
  refactoring; report generation is extracted from the core converter.
- Java `0.5.1` follows the checked upstream Node `0.5.1` representative
  fixture and CLI behavior for front matter, slide comments, summaries, and
  artifact projection shape.
- Remaining parity work is explicitly listed in `TODO.md` or
  `docs/remaining-migration-items.md`.

## Next Goal

- Continue staged refactoring without changing public CLI/core behavior.
- Keep `MikuPptx2mdCore` as the orchestration entry point while extracting
  cohesive responsibilities into package-private collaborators.
- Prefer the next refactoring cuts in this order:
  1. PPTX package and relationship reading.
  2. slide/text/table/image extraction.
  3. Markdown rendering.
  4. internal slide/block models when extraction makes them reusable.
- After each refactoring step, run `mvn test` and, when versioned output or
  packaging is affected, `mvn package` plus a runtime jar smoke command.
- Continue Node-parity work only after preserving current tests and behavior.

## Stop

- Implementing a feature would require guessing upstream semantics not present
  in the checked `miku-pptx2md` repository.
- A requested first-version requirement conflicts with the upstream Node CLI
  contract or the sibling Java repository shape.
- `TODO.md` の `Retry Log` に同じ原因の失敗が3回記録された
