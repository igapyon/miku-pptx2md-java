package jp.igapyon.mikupptx2md;

import jp.igapyon.mikupptx2md.cli.MikuPptx2mdCli;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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
    assertTrue(out.toString().contains("miku-pptx2md 0.2.0"));
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
  void printsHelpWithoutInput() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    int exitCode = new MikuPptx2mdCli().run(new String[] {"--help"}, new PrintStream(out), new PrintStream(err));

    assertEquals(0, exitCode);
    assertTrue(out.toString().contains("miku-pptx2md - local-first PPTX to Markdown converter"));
    assertTrue(out.toString().contains("USAGE"));
    assertTrue(out.toString().contains("OPTIONS"));
    assertTrue(out.toString().contains("EXIT CODES"));
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
    assertTrue(readUtf8(output).contains("## Slide 1: Overview"));
    assertTrue(readUtf8(summary).contains("slides: 1"));
    assertTrue(readUtf8(summary).contains("textBlocks: 2"));
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

  private static String readUtf8(Path path) throws Exception {
    return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
  }
}
