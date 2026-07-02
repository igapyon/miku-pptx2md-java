---
purpose: ai-agent-handoff
read_when:
  - before_resuming_work
  - before_handing_off_work
  - when_context_is_missing
update_when:
  - work_is_paused
  - handoff_summary_changes
  - verification_status_changes
---

# Handoff

This file summarizes the current working state for the next human or AI agent.
Keep it concise. Do not use this as a full work log or a replacement for `TODO.md` and `DECISIONS.md`.

## Current State

- Initial `miku-pptx2md-java` repository scaffold has been created.
- Maven build, Java core, CLI, tests, README, miku-soft reference, upstream
  mapping docs, and migration notes are committed.
- The runtime currently supports minimal PPTX ZIP/XML conversion, metadata,
  slide text, basic tables, hyperlinks, image references, `--assets-dir` asset
  export, notes, slide comments, YAML front matter, summary text, and summary
  JSON.
- Representative Node core test intent coverage has been added for the checked
  upstream Node `0.5.1`.
- Representative Node CLI test intent coverage has been added for the checked
  upstream Node `0.5.1`.
- The CLI runtime release workflow has been added.
- The active goal is now first-version completion, not just scaffold
  completion.
- First-version `0.2.0` work is complete. Java `0.4.0` tracked the checked
  upstream Node `0.4.0` package version. Java `0.5.1` now tracks the checked
  upstream Node `0.5.1` package version.
- Report generation has been extracted from `MikuPptx2mdCore` into
  `Pptx2MdReportWriter`.
- Local tag `v0.2.0` has been created.
- The intended next release tag is `v0.5.1` if this version bump is published.
- PPTX ZIP entry loading and relationship parsing have been extracted from
  `MikuPptx2mdCore` into package-private `PptxPackage` and
  `RelationshipEntry` collaborators.
- Upstream Node `0.5.1` front matter, slide comments, and summary `comments`
  behavior has been ported to Java.

## Next Action

- Continue staged refactoring from `GOAL.md`.
- Extract slide/text/table/image extraction next, preserving the public
  `MikuPptx2mdCore` API and current CLI behavior.
- Run `mvn test` after each refactoring step.

## Relevant Files

- `GOAL.md`: current objective and completion / stop conditions.
- `TODO.md`: active follow-up tasks and AI Agent current tasks.
- `DECISIONS.md`: important design decisions and deferred work.
- `README.md`: current user-facing Java runtime usage.
- `docs/remaining-migration-items.md`: parity and migration backlog.
- `src/main/java/jp/igapyon/mikupptx2md/core/MikuPptx2mdCore.java`: current core orchestration and remaining slide/content extraction implementation.
- `src/main/java/jp/igapyon/mikupptx2md/core/PptxPackage.java`: package-private PPTX ZIP entry and relationship reading helper.
- `src/main/java/jp/igapyon/mikupptx2md/core/RelationshipEntry.java`: package-private relationship value object.
- `src/main/java/jp/igapyon/mikupptx2md/core/Pptx2MdReportWriter.java`: summary, summary JSON, and asset manifest writer.
- `src/main/java/jp/igapyon/mikupptx2md/cli/MikuPptx2mdCli.java`: current CLI implementation.
- `src/main/java/jp/igapyon/mikupptx2md/cli/CliOptions.java`: current CLI option parser, including `--front-matter`.

## Watch Outs

- `--assets-dir` is now implemented for sidecar image export and manifest
  output. The comparison script covers the checked upstream generated fixture
  set, but not every possible real-world PPTX behavior.
- The current tests use generated minimal PPTX fixtures, not upstream fixture
  parity.
- Feature scope should be checked against the Node implementation and tests
  before adding Java-only behavior.
- Keep refactoring commits behavior-preserving; use existing tests before
  widening PPTX feature scope.
- `git status` should be clean before tagging or release publication.

## Last Verification

- `mvn test`: passed, 20 tests on 2026-07-02 after extracting PPTX package /
  relationship reading.
- `mvn test`: passed, 23 tests on 2026-07-02 after aligning to upstream Node
  `0.5.1`.
- `mvn package`: passed, 23 tests on 2026-07-02; runtime jar and sources jar
  generated for `0.5.1`.
- `java -jar target/miku-pptx2md-0.5.1.jar --version`: printed
  `miku-pptx2md 0.5.1`.
- `sh scripts/compare-node-java-cli.sh`: passed for the checked upstream
  generated fixture set, summary text, summary JSON, stdout Markdown,
  `--no-notes`, `--debug`, metadata rejection, read failures, and
  `--assets-dir` image asset output on 2026-07-02.
