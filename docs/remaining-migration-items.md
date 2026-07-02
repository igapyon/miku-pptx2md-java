# Remaining Migration Items

## Completed Preparation

- Created single-module Maven skeleton.
- Added Java 8 build configuration.
- Added CLI and core package structure.
- Added initial upstream and sister-reference documents.
- Added focused core and CLI tests.
- Added representative Node core test intent coverage for the checked upstream
  Node `0.5.1`.
- Added representative Node CLI test intent coverage for the checked upstream
  Node `0.5.1`.
- Added CLI runtime release workflow.
- Ported asset path safety and implemented optional `--assets-dir`.
- Expanded the Node / Java CLI comparison script across the checked upstream
  generated fixture set.
- Added summary JSON comparison for the checked upstream generated fixture set.
- Bumped the Java runtime version to `0.4.0` after phase 2 parity tightening
  against the checked upstream Node `0.4.0`.
- Bumped the Java runtime version to `0.5.1` after the checked upstream Node
  `0.5.1` added YAML front matter, slide comment rendering, and artifact
  projection separation.

## Pending Parity Work

- Keep expanding Node / Java parity checks when upstream adds new fixtures or
  PPTX feature behavior.

## Out Of Initial Scope

- Maven plugin support. A future plugin should live in
  `miku-pptx2md-java-maven`.
- Web App support.
