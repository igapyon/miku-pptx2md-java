---
purpose: ai-agent-decisions
read_when:
  - before_starting_work
  - when_making_decision
  - when_looping_or_repeating_work
update_when:
  - important_decision_is_made
  - option_is_rejected
  - work_is_deferred
---

# Decisions

This file records important decisions for the AI agent.
Read this before making or revisiting decisions, especially when the work seems to loop.

## 2026-06-27: Use Single-Module Java Runtime Shape

理由:
`miku-xlsx2md-java` and `miku-docx2md-java` are the closest same-layer sister
repositories. Both use a single Maven runtime repository with Java 8
compatibility, a thin CLI, core APIs, JUnit tests, and `docs/` mapping files.

影響:
`miku-pptx2md-java` starts as a single-module runtime jar. Maven plugin support
is not part of the initial repository and should live in a separated
`miku-pptx2md-java-maven` repository if needed later.

## 2026-06-27: Treat Upstream Node Project As Semantic Source

理由:
The miku-soft Java straight-conversion workflow requires preserving upstream
meaning and traceability rather than redesigning the product for Java first.
The checked upstream is local `miku-pptx2md` branch `devel-tiga0626acc` at
commit `53067f5bf3bb95018737e6631c02ebc81a643865`.

影響:
The current Java implementation is an initial partial port. Remaining parity
work is recorded in `TODO.md` and `docs/remaining-migration-items.md`.

## 2026-06-27: Keep `--assets-dir` Optional For First Version

理由:
The upstream Node CLI supports asset export, but `--assets-dir` is optional in
the Node version. The first usable Java version should not require this option
unless it is needed for practical first-version workflows.

影響:
The Java CLI may reject `--assets-dir` for now. Asset export remains a parity
candidate for the next Node-parity-focused phase.

## 2026-06-27: Split First Version And Node-Parity Work

理由:
The first milestone should finish a usable Java runtime without requiring
complete Node parity for every PowerPoint feature. After that first version,
the next milestone should move the Java implementation as close as practical to
the upstream Node version.

影響:
First-version work remains focused on usable CLI/core behavior, representative
tests, docs, and smoke verification. Node-parity work remains a strong follow-up
direction rather than being discarded.

## 2026-06-27: Target Version 0.2.0 For Step 1

理由:
The first usable Java version should be tracked as step 1 and target version
`0.2.0`.

影響:
Development used `0.2.0-SNAPSHOT` while the first-version work was in progress.
The project version and CLI version are finalized to `0.2.0`.

## 2026-06-27: Use Node Fixtures And Feature Behavior As Reference

理由:
The Java first version should not invent its own PPTX feature set. The checked
upstream Node version at commit `53067f5bf3bb95018737e6631c02ebc81a643865` is
the reference for fixture selection, test intent, CLI behavior, and supported
feature behavior.

影響:
When expanding first-version coverage, inspect the Node implementation and
tests first. Java tests should follow representative Node fixtures or their
test intent. Any Java differences should be documented as current known
differences.

## 2026-06-27: Include Release Workflow Before Final 0.2.0

理由:
The first usable Java version should eventually include the runtime release
asset workflow, even if local implementation work can proceed before that file
is added.

影響:
Before finalizing `0.2.0`, add the CLI runtime release workflow and verify it
matches the single-module runtime jar shape.

## 2026-06-27: Align Java Runtime Version To Upstream Node 0.4.0

理由:
After phase 2 parity tightening, the Java CLI/core behavior is checked against
the upstream Node `0.4.0` generated fixture set and representative CLI behavior.
The Java runtime should communicate that it follows the checked upstream package
version instead of remaining on the earlier first-usable Java milestone number.

影響:
The Maven project version, Java CLI `--version`, jar examples, and current
status documents move from `0.2.0` to `0.4.0`. The existing `v0.2.0` tag remains
the first usable Java milestone. A future publication should use `v0.4.0`.
