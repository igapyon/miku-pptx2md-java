# miku-pptx2md-java

`miku-pptx2md-java` is the Java runtime and CLI companion for
[`igapyon/miku-pptx2md`](https://github.com/igapyon/miku-pptx2md).

This repository is a new Java straight-conversion project. It follows the same
Java companion shape as `miku-xlsx2md-java` and `miku-docx2md-java`: a
single-module Maven runtime jar, a thin CLI adapter, reusable core APIs, focused
tests, and mapping documents under `docs/`.

## What is this?

`miku-pptx2md-java` converts PowerPoint (`.pptx`) presentations into
Markdown-oriented artifacts from Java.

Initial Java runtime support includes:

- reading local `.pptx` ZIP/XML packages
- extracting core metadata
- preserving slide order from `ppt/presentation.xml`
- extracting title placeholders and basic slide text
- basic list, table, hyperlink, image reference, speaker notes, and diagnostics
  handling based on representative Node test intent
- writing Markdown, summary text, and summary JSON from the CLI

The upstream TypeScript / Node.js implementation remains the semantic source of
truth. The Java implementation is intentionally small at this stage and records
remaining parity work in `TODO.md` and `docs/remaining-migration-items.md`.

## First-Version Scope

The `0.2.0` first usable Java version targets representative behavior from the
checked Node version:

- Markdown sections for ordered slides
- metadata title fallback and metadata summary output
- plain text, bullets, nested bullets, numbered items, and simple inline
  formatting
- external text hyperlinks
- simple Markdown tables and merged-table warning diagnostics
- speaker notes by default, with `--no-notes` support
- image relationship discovery and image summary metadata
- unsupported chart, SmartArt, comments, video, audio, and OLE diagnostics
- summary text and summary JSON outputs
- agent-readable CLI help, version, verbose output, and debug comments

Known first-version differences:

- `--assets-dir` is optional in the Node CLI and is planned for phase 2.
- Direct byte-level Node / Java output parity is not yet claimed.
- The current parity tests follow representative Node fixture intent rather
  than running the Node and Java CLIs side by side.

## Requirements

- Java 8 or later
- Maven, when building from source

## Build

```bash
mvn test
mvn package
```

The executable CLI jar is produced under `target/`.

## Java CLI

```bash
java -jar target/miku-pptx2md-0.2.0.jar ./sample.pptx --out ./sample.md
```

When `--out` is omitted, Markdown is written to stdout.

```bash
java -jar target/miku-pptx2md-0.2.0.jar ./sample.pptx > ./sample.md
```

Summary outputs:

```bash
java -jar target/miku-pptx2md-0.2.0.jar \
  ./sample.pptx \
  --out ./sample.md \
  --summary-out ./sample.summary.txt \
  --summary-json-out ./sample.summary.json
```

CLI options:

- `--out <file>`: Write Markdown to this file
- `--summary`: Print summary text to stdout
- `--summary-out <file>`: Write summary text to this file
- `--summary-json-out <file>`: Write structured summary JSON to this file
- `--no-notes`: Omit speaker notes from Markdown output
- `--debug`: Include diagnostic HTML comment traces in Markdown
- `--verbose`: Write progress diagnostics to stderr
- `--version`: Show product name and version
- `--help`: Show help

`--assets-dir` is part of the upstream Node CLI contract but is optional and is
planned for the phase 2 parity pass.

## Maven Plugin

Maven plugin support is out of scope for the initial Java runtime. If added
later, it should live in a separated `miku-pptx2md-java-maven` repository.

## Development

Primary verification:

```bash
mvn test
```

The local `workplace/` directory is reserved for upstream checkouts, local
comparison outputs, and temporary verification files. Only `workplace/.gitkeep`
is tracked.

## Documents

- [docs/miku-soft-reference.md](docs/miku-soft-reference.md)
- [docs/development-status.md](docs/development-status.md)
- [docs/upstream-snapshot.md](docs/upstream-snapshot.md)
- [docs/upstream-class-mapping.md](docs/upstream-class-mapping.md)
- [docs/upstream-test-mapping.md](docs/upstream-test-mapping.md)
- [docs/upstream-cli-mapping.md](docs/upstream-cli-mapping.md)
- [docs/remaining-migration-items.md](docs/remaining-migration-items.md)
- [docs/upstream-followup-log.md](docs/upstream-followup-log.md)
- [TODO.md](TODO.md)

## License

Apache License 2.0.

See [LICENSE](LICENSE).
