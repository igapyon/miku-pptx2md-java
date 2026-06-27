package jp.igapyon.mikupptx2md;

import jp.igapyon.mikupptx2md.core.MikuPptx2mdCore;
import jp.igapyon.mikupptx2md.core.Pptx2MdOptions;
import jp.igapyon.mikupptx2md.core.Pptx2MdResult;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MikuPptx2mdCoreTest {
  @Test
  void convertsBasicPresentationText() throws Exception {
    Pptx2MdOptions options = new Pptx2MdOptions();
    options.fallbackTitle = "sample";
    Pptx2MdResult result = new MikuPptx2mdCore().convertPptxToMarkdown(basicPptx(), options);

    assertEquals(1, result.summary.slides);
    assertEquals(1, result.summary.slidesWithTitles);
    assertEquals(1, result.summary.textBlocks);
    assertTrue(result.markdown.contains("# Sample Deck"));
    assertTrue(result.markdown.contains("## Slide 1: Opening"));
    assertTrue(result.markdown.contains("Hello from PPTX"));
  }

  @Test
  void createsSummaryText() throws Exception {
    Pptx2MdResult result = new MikuPptx2mdCore().convertPptxToMarkdown(basicPptx(), new Pptx2MdOptions());
    String summary = new MikuPptx2mdCore().createPptx2MdSummaryText(result);

    assertTrue(summary.contains("metadata.title: Sample Deck"));
    assertTrue(summary.contains("slides: 1"));
    assertTrue(summary.contains("textBlocks: 1"));
  }

  private static byte[] basicPptx() throws Exception {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    ZipOutputStream zip = new ZipOutputStream(bytes);
    add(zip, "docProps/core.xml",
        "<cp:coreProperties xmlns:cp=\"x\" xmlns:dc=\"x\"><dc:title>Sample Deck</dc:title></cp:coreProperties>");
    add(zip, "ppt/presentation.xml",
        "<p:presentation xmlns:p=\"p\" xmlns:r=\"r\"><p:sldIdLst><p:sldId r:id=\"rId1\"/></p:sldIdLst></p:presentation>");
    add(zip, "ppt/_rels/presentation.xml.rels",
        "<Relationships><Relationship Id=\"rId1\" Type=\"x/slide\" Target=\"slides/slide1.xml\"/></Relationships>");
    add(zip, "ppt/slides/slide1.xml",
        "<p:sld xmlns:p=\"p\" xmlns:a=\"a\"><p:cSld><p:spTree>"
            + "<p:sp><p:nvSpPr><p:nvPr><p:ph type=\"title\"/></p:nvPr></p:nvSpPr>"
            + "<p:txBody><a:p><a:r><a:t>Opening</a:t></a:r></a:p></p:txBody></p:sp>"
            + "<p:sp><p:txBody><a:p><a:r><a:t>Hello from PPTX</a:t></a:r></a:p></p:txBody></p:sp>"
            + "</p:spTree></p:cSld></p:sld>");
    zip.close();
    return bytes.toByteArray();
  }

  private static void add(ZipOutputStream zip, String path, String content) throws Exception {
    zip.putNextEntry(new ZipEntry(path));
    zip.write(content.getBytes(StandardCharsets.UTF_8));
    zip.closeEntry();
  }
}
