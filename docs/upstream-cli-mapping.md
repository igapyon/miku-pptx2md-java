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
| `--no-notes` | Implemented | Omits speaker notes from Markdown |
| `--debug` | Implemented | Adds diagnostic HTML comments |
| `--include-unsupported-comments` | Implemented | Alias for `--debug` |
| `--verbose` | Implemented | Writes `verbose:` lines to stderr |
| `--help` | Implemented | Metadata command |
| `--version` | Implemented | Metadata command |
| `--assets-dir <dir>` | Not implemented | Pending asset-path safety port and file export |

Exit codes:

- `0`: success, `--help`, or `--version`
- `1`: usage, file I/O, parse, or runtime error
