package jp.igapyon.mikupptx2md;

import jp.igapyon.mikupptx2md.cli.MikuPptx2mdCli;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MikuPptx2mdCliTest {
  @TempDir
  Path tempDir;

  @Test
  void printsVersion() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    int exitCode = new MikuPptx2mdCli().run(new String[] {"--version"}, new PrintStream(out), new PrintStream(err));

    assertEquals(0, exitCode);
    assertTrue(out.toString().contains("miku-pptx2md 0.5.1"));
    assertEquals("", err.toString());
  }

  @Test
  void rejectsMixedVersion() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    int exitCode = new MikuPptx2mdCli().run(new String[] {"--version", "sample.pptx"}, new PrintStream(out), new PrintStream(err));

    assertEquals(1, exitCode);
    assertTrue(err.toString().contains("Use --help or --version without other arguments."));
  }

  @Test
  void reportsReadFailureWithInputNameAndStage() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    int exitCode = new MikuPptx2mdCli().run(new String[] {"does-not-exist.pptx"}, new PrintStream(out), new PrintStream(err));

    assertEquals(1, exitCode);
    assertEquals("", out.toString());
    assertTrue(err.toString().contains("[does-not-exist.pptx] read failed:"));
  }

  @Test
  void printsHelpWithoutInput() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    int exitCode = new MikuPptx2mdCli().run(new String[] {"--help"}, new PrintStream(out), new PrintStream(err));

    assertEquals(0, exitCode);
    assertTrue(out.toString().contains("miku-pptx2md - local-first PPTX to Markdown converter"));
    assertTrue(out.toString().contains("USAGE"));
    assertTrue(out.toString().contains("CONTRACT"));
    assertTrue(out.toString().contains("OPTIONS"));
    assertTrue(out.toString().contains("OUTPUTS"));
    assertTrue(out.toString().contains("EXAMPLES"));
    assertTrue(out.toString().contains("EXIT CODES"));
    assertTrue(out.toString().contains("Input is exactly one local .pptx file path."));
    assertTrue(out.toString().contains("If --out is omitted, Markdown is written to stdout."));
    assertTrue(out.toString().contains("--verbose writes progress and timing diagnostics to stderr."));
    assertTrue(out.toString().contains("--help and --version are metadata commands and must be used without other arguments."));
    assertTrue(out.toString().contains("Core metadata plus text, list, table, hyperlink, image, notes, and diagnostics counts."));
    assertTrue(out.toString().contains("--summary-json-out <file>"));
    assertTrue(out.toString().contains("--front-matter <mode>"));
    assertTrue(out.toString().contains("use --front-matter exclude to omit it"));
    assertTrue(out.toString().contains("Omit speaker notes from Markdown output."));
    assertTrue(out.toString().contains("--include-unsupported-comments"));
    assertTrue(out.toString().contains("manifest.json"));
    assertTrue(out.toString().contains("Asset manifest:"));
    assertTrue(out.toString().contains("JSON with asset path, media type, alt text,"));
    assertTrue(out.toString().contains("slide index, block index, relationship id, and"));
    assertTrue(out.toString().contains("Export resolved embedded image assets into this directory."));
    assertEquals("", err.toString());
  }

  @Test
  void writesMarkdownSummaryAndVerboseDiagnostics() throws Exception {
    Path input = tempDir.resolve("sample.pptx");
    Path output = tempDir.resolve("sample.md");
    Path summary = tempDir.resolve("sample.summary.txt");
    Files.write(input, PptxFixtures.minimal());
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    int exitCode = new MikuPptx2mdCli().run(new String[] {
        input.toString(),
        "--out", output.toString(),
        "--summary-out", summary.toString(),
        "--verbose"
    }, new PrintStream(out), new PrintStream(err));

    assertEquals(0, exitCode);
    assertEquals("", out.toString());
    assertTrue(err.toString().contains("verbose:"));
    assertTrue(err.toString().contains("input=" + input));
    assertTrue(err.toString().contains("converted slides=1 textBlocks=2"));
    assertTrue(readUtf8(output).contains("---\ntitle: \"sample\"\ntype: converted\nconversion:\n  tool: miku-pptx2md\n  version: \"0.5.1\"\n  notes: include\n  unsupported_comments: exclude\n---\n\n# sample\n\n## Slide 1: Overview"));
    assertTrue(readUtf8(summary).contains("slides: 1"));
    assertTrue(readUtf8(summary).contains("textBlocks: 2"));
    assertTrue(readUtf8(summary).contains("comments: 0"));
  }

  @Test
  void omitsFrontMatterWhenRequested() throws Exception {
    Path input = tempDir.resolve("sample.pptx");
    Path output = tempDir.resolve("sample.md");
    Files.write(input, PptxFixtures.minimal());
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    int exitCode = new MikuPptx2mdCli().run(new String[] {
        input.toString(),
        "--out", output.toString(),
        "--front-matter", "exclude"
    }, new PrintStream(out), new PrintStream(err));

    assertEquals(0, exitCode);
    String markdown = readUtf8(output);
    assertEquals(false, markdown.startsWith("---\n"));
    assertTrue(markdown.startsWith("# sample\n\n## Slide 1: Overview"));
  }

  @Test
  void rejectsInvalidFrontMatterMode() throws Exception {
    Path input = tempDir.resolve("sample.pptx");
    Files.write(input, PptxFixtures.minimal());
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    int exitCode = new MikuPptx2mdCli().run(new String[] {
        input.toString(),
        "--front-matter", "invalid"
    }, new PrintStream(out), new PrintStream(err));

    assertEquals(1, exitCode);
    assertTrue(err.toString().contains("Invalid front matter mode: invalid"));
  }

  @Test
  void writesStructuredSummaryJson() throws Exception {
    Path input = tempDir.resolve("metadata.pptx");
    Path output = tempDir.resolve("metadata.md");
    Path summaryJson = tempDir.resolve("metadata.summary.json");
    Files.write(input, PptxFixtures.metadata());
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    int exitCode = new MikuPptx2mdCli().run(new String[] {
        input.toString(),
        "--out", output.toString(),
        "--summary-json-out", summaryJson.toString()
    }, new PrintStream(out), new PrintStream(err));

    assertEquals(0, exitCode);
    String json = readUtf8(summaryJson);
    assertTrue(json.contains("\"version\": 1"));
    assertTrue(json.contains("\"title\": \"Roadmap & Review\""));
    assertTrue(json.contains("\"slides\": 1"));
    assertTrue(json.contains("\"diagnostics\": []"));
  }

  @Test
  void includesDiagnosticCommentsInDebugMarkdown() throws Exception {
    Path input = tempDir.resolve("sample.pptx");
    Path output = tempDir.resolve("sample.md");
    Files.write(input, PptxFixtures.missingImage());
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    int exitCode = new MikuPptx2mdCli().run(new String[] {
        input.toString(),
        "--out", output.toString(),
        "--debug"
    }, new PrintStream(out), new PrintStream(err));

    assertEquals(0, exitCode);
    String markdown = readUtf8(output);
    assertTrue(markdown.contains("## Diagnostics"));
    assertTrue(markdown.contains("<!-- warning: missing-image-part source=ppt/slides/slide1.xml:"));
  }

  @Test
  void writesImageAssetsAndManifestWhenAssetsDirIsSet() throws Exception {
    Path input = tempDir.resolve("sample.pptx");
    Path output = tempDir.resolve("sample.md");
    Path assetsDir = tempDir.resolve("sample.assets");
    Files.write(input, PptxFixtures.image());
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    int exitCode = new MikuPptx2mdCli().run(new String[] {
        input.toString(),
        "--out", output.toString(),
        "--assets-dir", assetsDir.toString()
    }, new PrintStream(out), new PrintStream(err));

    assertEquals(0, exitCode);
    assertEquals("", out.toString());
    assertEquals("", err.toString());
    assertTrue(readUtf8(output).contains("![Diagram alt](sample.assets/ppt/media/image1.png)"));
    assertArrayEquals(new byte[] {(byte) 137, 80, 78, 71}, Files.readAllBytes(assetsDir.resolve("ppt/media/image1.png")));
    String manifest = readUtf8(assetsDir.resolve("manifest.json"));
    assertTrue(manifest.contains("\"version\": 1"));
    assertTrue(manifest.contains("\"sourcePath\": \"ppt/media/image1.png\""));
    assertTrue(manifest.contains("\"documentPosition\": {"));
    assertTrue(manifest.contains("\"blockKind\": \"image\""));
    assertTrue(manifest.contains("\"size\": 4"));
  }

  private static String readUtf8(Path path) throws Exception {
    return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
  }
}
