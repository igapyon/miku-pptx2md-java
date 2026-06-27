package jp.igapyon.mikupptx2md;

import jp.igapyon.mikupptx2md.core.MikuPptx2mdCore;
import jp.igapyon.mikupptx2md.core.Pptx2MdOptions;
import jp.igapyon.mikupptx2md.core.Pptx2MdResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MikuPptx2mdNodeIntentTest {
  private final MikuPptx2mdCore core = new MikuPptx2mdCore();

  @Test
  void followsMinimalFixtureIntent() throws Exception {
    Pptx2MdResult result = convert(PptxFixtures.minimal(), "sample");

    assertTrue(result.markdown.startsWith("# sample\n\n## Slide 1: Overview"));
    assertTrue(result.markdown.contains("First paragraph"));
    assertTrue(result.markdown.contains("Second paragraph"));
    assertEquals(1, result.summary.slides);
    assertEquals(1, result.summary.slidesWithTitles);
    assertEquals(2, result.summary.textBlocks);
  }

  @Test
  void followsMetadataFixtureIntent() throws Exception {
    Pptx2MdOptions options = new Pptx2MdOptions();
    options.fallbackTitle = "file-stem";
    Pptx2MdResult result = core.convertPptxToMarkdown(PptxFixtures.metadata(), options);

    assertTrue(result.markdown.startsWith("# Roadmap & Review\n\n## Slide 1: Metadata Slide"));
    assertEquals("Roadmap & Review", result.metadata.get("title"));
    assertEquals("Alice", result.metadata.get("creator"));
    assertEquals("2026-06-25T11:30:00Z", result.metadata.get("modified"));
  }

  @Test
  void followsListAndFormattingIntent() throws Exception {
    Pptx2MdResult list = convert(PptxFixtures.list(), "list-sample");
    assertTrue(list.markdown.contains("\n- Bullet item\n"));
    assertTrue(list.markdown.contains("\n  - Nested bullet\n"));
    assertTrue(list.markdown.contains("\n1. Numbered item\n"));
    assertEquals(4, list.summary.textBlocks);
    assertEquals(3, list.summary.listItems);

    Pptx2MdResult formatted = convert(PptxFixtures.formatted(), "format-sample");
    assertTrue(formatted.markdown.contains("Use **bold** and *italic* plus ***both*** and <u>underlined</u>"));
    assertEquals(1, formatted.summary.textBlocks);
  }

  @Test
  void followsHyperlinkIntent() throws Exception {
    Pptx2MdResult result = convert(PptxFixtures.hyperlink(), "link-sample");

    assertTrue(result.markdown.contains("See [**project site**](https://example.com/project?from=pptx) for details"));
    assertEquals(1, result.summary.hyperlinks);
    assertEquals(0, result.summary.warnings);
  }

  @Test
  void followsTableAndMergedTableIntent() throws Exception {
    Pptx2MdResult table = convert(PptxFixtures.table(), "table-sample");
    assertTrue(table.markdown.contains("| Name | Status |"));
    assertTrue(table.markdown.contains("| Parser | In progress |"));
    assertTrue(table.markdown.contains("\nAfter table\n"));
    assertEquals(5, table.summary.textBlocks);
    assertEquals(1, table.summary.tables);

    Pptx2MdResult merged = convert(PptxFixtures.mergedTable(), "merged-table-sample");
    assertTrue(merged.markdown.contains("| Combined Header |"));
    assertEquals(1, merged.summary.tables);
    assertEquals(1, merged.summary.warnings);
    assertEquals("limited-table-merged-cells", merged.diagnostics.get(0).code);
  }

  @Test
  void followsUnsupportedGraphicFrameIntent() throws Exception {
    Pptx2MdResult chart = convert(PptxFixtures.chart(), "chart-sample");
    assertEquals("unsupported-chart", chart.diagnostics.get(0).code);
    assertEquals(1, chart.summary.warnings);

    Pptx2MdResult smartArt = convert(PptxFixtures.smartArt(), "smartart-sample");
    assertEquals("unsupported-smartart", smartArt.diagnostics.get(0).code);
    assertEquals(1, smartArt.summary.warnings);
  }

  @Test
  void followsNotesIntent() throws Exception {
    Pptx2MdResult result = convert(PptxFixtures.notes(), "notes-sample");

    assertTrue(result.markdown.contains("\n### Speaker Notes\n"));
    assertTrue(result.markdown.contains("Speaker note line one"));
    assertFalse(result.markdown.contains("Slide thumbnail placeholder"));
    assertEquals(3, result.summary.textBlocks);
    assertEquals(1, result.summary.notesSlides);

    Pptx2MdOptions options = new Pptx2MdOptions();
    options.title = "notes-sample";
    options.includeNotes = false;
    Pptx2MdResult withoutNotes = core.convertPptxToMarkdown(PptxFixtures.notes(), options);
    assertFalse(withoutNotes.markdown.contains("Speaker Notes"));
  }

  @Test
  void followsImageSummaryIntent() throws Exception {
    Pptx2MdResult result = convert(PptxFixtures.image(), "image-sample");

    assertTrue(result.markdown.contains("[Image: Diagram alt]"));
    assertEquals(1, result.assets.size());
    assertEquals("ppt/media/image1.png", result.assets.get(0).sourcePath);
    assertEquals("image/png", result.assets.get(0).mediaType);
    assertEquals("Diagram alt", result.assets.get(0).altText);
    assertEquals("picture:image(ppt/media/image1.png):alt(Diagram alt)", result.assets.get(0).sourceTrace);
    assertArrayEquals(new byte[] {(byte) 137, 80, 78, 71}, result.assets.get(0).bytes);
    assertEquals(1, result.summary.imageAssets);

    Pptx2MdOptions options = new Pptx2MdOptions();
    options.title = "image-sample";
    options.imagePathResolver = asset -> "assets/" + asset.sourcePath;
    Pptx2MdResult linked = core.convertPptxToMarkdown(PptxFixtures.image(), options);
    assertTrue(linked.markdown.contains("![Diagram alt](assets/ppt/media/image1.png)"));

    String manifest = core.createPptx2MdAssetsManifestJsonText(result.assets);
    assertTrue(manifest.contains("\"version\": 1"));
    assertTrue(manifest.contains("\"sourcePath\": \"ppt/media/image1.png\""));
    assertTrue(manifest.contains("\"documentPosition\": {"));
    assertTrue(manifest.contains("\"blockKind\": \"image\""));
  }

  @Test
  void followsDiagnosticsDebugIntent() throws Exception {
    Pptx2MdResult result = convert(PptxFixtures.missingImage(), "diagnostic-sample");
    assertEquals("missing-image-part", result.diagnostics.get(0).code);
    assertFalse(result.markdown.contains("<!-- warning:"));

    Pptx2MdOptions options = new Pptx2MdOptions();
    options.title = "diagnostic-sample";
    options.includeUnsupportedComments = true;
    Pptx2MdResult debug = core.convertPptxToMarkdown(PptxFixtures.missingImage(), options);
    assertTrue(debug.markdown.contains("## Diagnostics"));
    assertTrue(debug.markdown.contains("<!-- warning: missing-image-part source=ppt/slides/slide1.xml:"));

    Pptx2MdResult comments = convert(PptxFixtures.comments(), "comment-sample");
    assertEquals("unsupported-comments", comments.diagnostics.get(0).code);
  }

  @Test
  void followsUnsupportedPictureIntent() throws Exception {
    assertEquals("unsupported-video", convert(PptxFixtures.unsupportedPicture("video"), "video-sample").diagnostics.get(0).code);
    assertEquals("unsupported-audio", convert(PptxFixtures.unsupportedPicture("audio"), "audio-sample").diagnostics.get(0).code);
    assertEquals("unsupported-ole-object", convert(PptxFixtures.unsupportedPicture("ole"), "ole-sample").diagnostics.get(0).code);
  }

  private Pptx2MdResult convert(byte[] pptx, String title) {
    Pptx2MdOptions options = new Pptx2MdOptions();
    options.title = title;
    return core.convertPptxToMarkdown(pptx, options);
  }
}
