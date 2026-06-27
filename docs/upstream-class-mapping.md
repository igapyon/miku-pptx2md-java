# Upstream Class Mapping

| Upstream file | Java class / package | Status |
| --- | --- | --- |
| `src/ts/core.ts` | `jp.igapyon.mikupptx2md.core.MikuPptx2mdCore` and model classes | Initial partial port |
| `src/ts/xml-utils.ts` | `jp.igapyon.mikupptx2md.xml.XmlUtils` | Initial partial port |
| `src/ts/zip-io.ts` | `MikuPptx2mdCore.readZipEntries` | Java standard-library implementation |
| `src/ts/asset-path.ts` | Not yet ported | Needed for `--assets-dir` |
| `scripts/miku-pptx2md-cli.mjs` | `jp.igapyon.mikupptx2md.cli.MikuPptx2mdCli` and `CliOptions` | Initial partial port |

The current Java implementation keeps the upstream public vocabulary where
practical but does not yet claim full source-unit parity.
