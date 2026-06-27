# Upstream Test Mapping

| Upstream test intent | Java test | Status |
| --- | --- | --- |
| Core converts a basic PPTX into Markdown | `MikuPptx2mdCoreTest.convertsBasicPresentationText` | Initial Java fixture |
| Core summary text exposes metadata and counts | `MikuPptx2mdCoreTest.createsSummaryText` | Initial Java fixture |
| CLI `--version` contract | `MikuPptx2mdCliTest.printsVersion` | Covered |
| CLI metadata command exclusivity | `MikuPptx2mdCliTest.rejectsMixedVersion` | Covered |
| Node fixture parity from `tests/pptx2md-core.test.mjs` | `MikuPptx2mdNodeIntentTest` | Representative `0.2.0` slice covered |
| Node CLI output parity from `tests/pptx2md-cli.test.mjs` | `MikuPptx2mdCliTest` | Representative `0.2.0` slice covered |

## Representative `0.2.0` Node Intent Slice

The first Java parity slice follows these upstream Node test intents:

- minimal slide Markdown section, title, body paragraphs, and summary counts
- core metadata extraction and metadata title fallback
- bullets, nested bullets, numbered paragraphs, and plain paragraph after list
- bold, italic, bold+italic, and underline run formatting
- external text hyperlink rendering and hyperlink count
- simple table rendering and table cell text block count
- merged table warning diagnostic
- unsupported chart and SmartArt diagnostics
- speaker notes inclusion and `includeNotes=false`
- image relationship discovery, placeholder rendering, asset metadata, and byte retention
- missing image diagnostic and debug comment rendering
- unsupported slide comments diagnostic
- unsupported video, audio, and OLE picture diagnostics

The first Java CLI parity slice follows these upstream Node CLI test intents:

- `--help` works without an input file
- `--version` prints product name and version
- metadata commands cannot be mixed with normal conversion arguments
- Markdown and summary files are written to requested paths
- verbose diagnostics are written to stderr
- structured summary JSON is written to a requested path
- debug mode includes diagnostic comments in Markdown
- `--assets-dir` writes image assets, `manifest.json`, and relative Markdown
  image links

The comparison script generates the checked upstream fixture set and compares
Node / Java Markdown plus summary JSON. It also compares `--assets-dir`
Markdown, `manifest.json`, and written image bytes for the image fixture.

Focused command:

```bash
mvn test
sh scripts/compare-node-java-cli.sh
```
