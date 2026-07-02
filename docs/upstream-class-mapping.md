# Upstream Class Mapping

| Upstream file | Java class / package | Status |
| --- | --- | --- |
| `src/ts/core.ts` | `jp.igapyon.mikupptx2md.core.MikuPptx2mdCore` and model classes | Initial partial port; orchestration retained while helpers are being extracted |
| `src/ts/artifacts.ts` | `jp.igapyon.mikupptx2md.core.Pptx2MdResult`, `Pptx2MdSummary`, `Pptx2MdDiagnostic`, `Pptx2MdAsset`, and `Pptx2MdReportWriter` | Java artifact models and report projection |
| `src/ts/xml-utils.ts` | `jp.igapyon.mikupptx2md.xml.XmlUtils` | Initial partial port |
| `src/ts/zip-io.ts` | `jp.igapyon.mikupptx2md.core.PptxPackage` | Java standard-library implementation |
| `src/ts/core.ts` relationship handling | `jp.igapyon.mikupptx2md.core.PptxPackage` and `RelationshipEntry` | Extracted package-private helper |
| `src/ts/asset-path.ts` | `MikuPptx2mdCli.safePptxPackagePathParts` | Ported for `--assets-dir` asset writes |
| `scripts/miku-pptx2md-cli.mjs` | `jp.igapyon.mikupptx2md.cli.MikuPptx2mdCli` and `CliOptions` | Initial partial port |

The current Java implementation keeps the upstream public vocabulary where
practical but does not yet claim full source-unit parity.
