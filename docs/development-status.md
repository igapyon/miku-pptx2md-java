# Development Status

## Initial References

- Upstream main application: `git@github.com:igapyon/miku-pptx2md.git`
- Local upstream checkout: `/Users/igapyon/Documents/git/miku-pptx2md`
- Checked upstream branch: `devel-tiga0626acc`
- Checked upstream commit: `53067f5bf3bb95018737e6631c02ebc81a643865`
- Sister Java references:
  - `/Users/igapyon/Documents/git/miku-xlsx2md-java`
  - `/Users/igapyon/Documents/git/miku-docx2md-java`
- Main miku-soft workflow: `30-java-straight-conversion-workflow.md`
- Checked date: 2026-06-27

## Adopted Sister Repository Shape

The initial repository follows the single-module Java runtime shape used by
`miku-xlsx2md-java` and `miku-docx2md-java`.

- root `pom.xml`
- Java 8 source and target compatibility
- JUnit Jupiter tests
- executable shaded CLI jar
- base package `jp.igapyon.mikupptx2md`
- thin CLI class `jp.igapyon.mikupptx2md.cli.MikuPptx2mdCli`
- product behavior in `core`
- local scratch area in `workplace/`
- mapping and migration documents under `docs/`

Maven plugin support is intentionally not included in the initial runtime.

## Current Runtime Scope

Implemented:

- PPTX ZIP entry loading with Java standard library
- `docProps/core.xml` metadata extraction
- slide order from `ppt/presentation.xml`
- slide relationship resolution
- basic title and text extraction
- basic list detection
- basic table extraction
- merged table warning diagnostics
- basic hyperlink rendering
- image relationship discovery and summary metadata
- speaker notes extraction
- shape-text block rendering for ordinary preset shapes
- unsupported chart, SmartArt, comments, video, audio, and OLE diagnostics
- Markdown output
- summary text and summary JSON output
- CLI `--help`, `--version`, `--out`, `--summary`, `--summary-out`,
  `--summary-json-out`, `--no-notes`, `--debug`, and `--verbose`
- release workflow for CLI runtime assets
- representative Node core and CLI test intent coverage for `0.2.0`
- direct Node / Java CLI output comparison script for representative fixtures

Not yet implemented:

- byte-level Node / Java parity checks

Phase 2:

- asset file export through optional `--assets-dir`

## Verification

Primary command:

```bash
mvn test
mvn package
java -jar target/miku-pptx2md-0.2.0.jar --version
sh scripts/compare-node-java-cli.sh
```
