# Remaining Migration Items

## Completed Preparation

- Created single-module Maven skeleton.
- Added Java 8 build configuration.
- Added CLI and core package structure.
- Added initial upstream and sister-reference documents.
- Added focused core and CLI tests.
- Added representative Node core test intent coverage for `0.2.0`.
- Added representative Node CLI test intent coverage for `0.2.0`.
- Added CLI runtime release workflow.
- Ported asset path safety and implemented optional `--assets-dir`.
- Expanded the Node / Java CLI comparison script across the checked upstream
  generated fixture set.
- Added summary JSON comparison for the checked upstream generated fixture set.

## Pending Parity Work

- Keep expanding Node / Java parity checks when upstream adds new fixtures or
  PPTX feature behavior.

## Out Of Initial Scope

- Maven plugin support. A future plugin should live in
  `miku-pptx2md-java-maven`.
- Web App support.
