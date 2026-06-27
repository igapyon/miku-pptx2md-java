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

## Pending Parity Work

- Expand the Node / Java parity script beyond the representative fixture slice.
- Compare summary JSON field order and escaping against upstream output.
- Phase 2: port asset path safety and implement optional `--assets-dir`.

## Out Of Initial Scope

- Maven plugin support. A future plugin should live in
  `miku-pptx2md-java-maven`.
- Web App support.
