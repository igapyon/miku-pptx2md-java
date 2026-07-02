# Upstream CLI Mapping

The Java CLI follows the upstream Node CLI where implemented.

| Node CLI option | Java CLI status | Notes |
| --- | --- | --- |
| `<input.pptx>` | Implemented | Exactly one local input path |
| `--out <file>` | Implemented | Writes Markdown to UTF-8 file |
| no `--out` | Implemented | Writes Markdown to stdout |
| `--summary` | Implemented | Prints summary text to stdout |
| `--summary-out <file>` | Implemented | Writes summary text |
| `--summary-json-out <file>` | Implemented | Writes structured summary JSON |
| `--front-matter <mode>` | Implemented | `include` or `exclude`; CLI default is `include` |
| `--no-notes` | Implemented | Omits speaker notes from Markdown |
| `--debug` | Implemented | Adds diagnostic HTML comments |
| `--include-unsupported-comments` | Implemented | Alias for `--debug` |
| `--verbose` | Implemented | Writes `verbose:` lines to stderr |
| `--help` | Implemented | Metadata command |
| `--version` | Implemented | Metadata command |
| `--assets-dir <dir>` | Implemented | Exports image assets and `manifest.json` |

Exit codes:

- `0`: success, `--help`, or `--version`
- `1`: usage, file I/O, parse, or runtime error

## Help Text Parity Status

The Java `--help` output follows the upstream Node agent-readable help shape:

- `USAGE`: adapted for `java -jar target/miku-pptx2md-<version>.jar`
- `CONTRACT`: aligned with Node
- `OPTIONS`: aligned for implemented options, including `--assets-dir` and
  `--front-matter`
- `OUTPUTS`: aligned with Node concepts, including asset directory and asset
  manifest
- `EXAMPLES`: adapted for Java jar execution, including asset export
- `EXIT CODES`: aligned with Node wording

Known intentional differences:

- Version output uses the Java runtime version, currently `0.5.1`. The checked
  upstream Node package version for this mapping is `0.5.1`.
- Help examples use Java jar invocation rather than
  `node scripts/miku-pptx2md-cli.mjs`.
