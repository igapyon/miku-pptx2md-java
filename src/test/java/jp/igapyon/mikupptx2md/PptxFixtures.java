package jp.igapyon.mikupptx2md;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

final class PptxFixtures {
  private PptxFixtures() {
  }

  static byte[] minimal() throws Exception {
    Map<String, Object> files = base("Overview",
        "<p:sp><p:txBody>"
            + "<a:p><a:r><a:t>First paragraph</a:t></a:r></a:p>"
            + "<a:p><a:r><a:t>Second paragraph</a:t></a:r></a:p>"
            + "</p:txBody></p:sp>",
        null);
    return zip(files);
  }

  static byte[] metadata() throws Exception {
    Map<String, Object> files = base("Metadata Slide", "", null);
    files.put("docProps/core.xml",
        "<cp:coreProperties xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\" "
            + "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\">"
            + "<dc:title>Roadmap &amp; Review</dc:title>"
            + "<dc:subject>Planning</dc:subject>"
            + "<dc:creator>Alice</dc:creator>"
            + "<dc:description>Quarterly deck</dc:description>"
            + "<cp:keywords>pptx, markdown</cp:keywords>"
            + "<cp:lastModifiedBy>Bob</cp:lastModifiedBy>"
            + "<cp:revision>7</cp:revision>"
            + "<cp:category>Engineering</cp:category>"
            + "<dcterms:created>2026-06-24T10:00:00Z</dcterms:created>"
            + "<dcterms:modified>2026-06-25T11:30:00Z</dcterms:modified>"
            + "</cp:coreProperties>");
    return zip(files);
  }

  static byte[] list() throws Exception {
    return zip(base("List Slide",
        "<p:sp><p:nvSpPr><p:nvPr><p:ph type=\"body\"/></p:nvPr></p:nvSpPr><p:txBody>"
            + "<a:p><a:pPr><a:buChar char=\"&#8226;\"/></a:pPr><a:r><a:t>Bullet item</a:t></a:r></a:p>"
            + "<a:p><a:pPr lvl=\"1\"><a:buChar char=\"&#8226;\"/></a:pPr><a:r><a:t>Nested bullet</a:t></a:r></a:p>"
            + "<a:p><a:pPr><a:buAutoNum type=\"arabicPeriod\"/></a:pPr><a:r><a:t>Numbered item</a:t></a:r></a:p>"
            + "<a:p><a:r><a:t>Plain after list</a:t></a:r></a:p>"
            + "</p:txBody></p:sp>",
        null));
  }

  static byte[] formatted() throws Exception {
    return zip(base("Formatted Slide",
        "<p:sp><p:nvSpPr><p:nvPr><p:ph type=\"body\"/></p:nvPr></p:nvSpPr><p:txBody><a:p>"
            + "<a:r><a:t>Use </a:t></a:r>"
            + "<a:r><a:rPr b=\"1\"/><a:t>bold</a:t></a:r>"
            + "<a:r><a:t> and </a:t></a:r>"
            + "<a:r><a:rPr i=\"1\"/><a:t>italic</a:t></a:r>"
            + "<a:r><a:t> plus </a:t></a:r>"
            + "<a:r><a:rPr b=\"1\" i=\"1\"/><a:t>both</a:t></a:r>"
            + "<a:r><a:t> and </a:t></a:r>"
            + "<a:r><a:rPr u=\"sng\"/><a:t>underlined</a:t></a:r>"
            + "</a:p></p:txBody></p:sp>",
        null));
  }

  static byte[] hyperlink() throws Exception {
    return zip(base("Hyperlink Slide",
        "<p:sp><p:nvSpPr><p:nvPr><p:ph type=\"body\"/></p:nvPr></p:nvSpPr><p:txBody><a:p>"
            + "<a:r><a:t>See </a:t></a:r>"
            + "<a:r><a:rPr b=\"1\"><a:hlinkClick r:id=\"rIdLink1\"/></a:rPr><a:t>project site</a:t></a:r>"
            + "<a:r><a:t> for details</a:t></a:r>"
            + "</a:p></p:txBody></p:sp>",
        "<Relationship Id=\"rIdLink1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink\" "
            + "Target=\"https://example.com/project?from=pptx\" TargetMode=\"External\"/>"));
  }

  static byte[] table() throws Exception {
    return zip(base("Table Slide",
        tableXml("<a:tr><a:tc><a:txBody><a:p><a:r><a:t>Name</a:t></a:r></a:p></a:txBody></a:tc>"
            + "<a:tc><a:txBody><a:p><a:r><a:t>Status</a:t></a:r></a:p></a:txBody></a:tc></a:tr>"
            + "<a:tr><a:tc><a:txBody><a:p><a:r><a:t>Parser</a:t></a:r></a:p></a:txBody></a:tc>"
            + "<a:tc><a:txBody><a:p><a:r><a:t>In progress</a:t></a:r></a:p></a:txBody></a:tc></a:tr>")
            + "<p:sp><p:txBody><a:p><a:r><a:t>After table</a:t></a:r></a:p></p:txBody></p:sp>",
        null));
  }

  static byte[] mergedTable() throws Exception {
    return zip(base("Merged Table Slide",
        tableXml("<a:tr><a:tc gridSpan=\"2\"><a:txBody><a:p><a:r><a:t>Combined Header</a:t></a:r></a:p></a:txBody></a:tc></a:tr>"
            + "<a:tr><a:tc><a:txBody><a:p><a:r><a:t>Left</a:t></a:r></a:p></a:txBody></a:tc>"
            + "<a:tc><a:txBody><a:p><a:r><a:t>Right</a:t></a:r></a:p></a:txBody></a:tc></a:tr>"),
        null));
  }

  static byte[] chart() throws Exception {
    return zip(base("Chart Slide",
        "<p:graphicFrame><a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/chart\">"
            + "<c:chart r:id=\"rIdChart1\"/></a:graphicData></a:graphic></p:graphicFrame>",
        "<Relationship Id=\"rIdChart1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/chart\" Target=\"../charts/chart1.xml\"/>"));
  }

  static byte[] smartArt() throws Exception {
    return zip(base("SmartArt Slide",
        "<p:graphicFrame><a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/diagram\">"
            + "<dgm:relIds r:dm=\"rIdDm1\"/></a:graphicData></a:graphic></p:graphicFrame>",
        "<Relationship Id=\"rIdDm1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/diagramData\" Target=\"../diagrams/data1.xml\"/>"));
  }

  static byte[] notes() throws Exception {
    Map<String, Object> files = base("Notes Slide",
        "<p:sp><p:txBody><a:p><a:r><a:t>Slide body</a:t></a:r></a:p></p:txBody></p:sp>",
        "<Relationship Id=\"rIdNotes\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/notesSlide\" Target=\"../notesSlides/notesSlide1.xml\"/>");
    files.put("ppt/notesSlides/notesSlide1.xml",
        "<p:notes xmlns:p=\"p\" xmlns:a=\"a\"><p:cSld><p:spTree>"
            + "<p:sp><p:nvSpPr><p:nvPr><p:ph type=\"sldImg\"/></p:nvPr></p:nvSpPr><p:txBody><a:p><a:r><a:t>Slide thumbnail placeholder</a:t></a:r></a:p></p:txBody></p:sp>"
            + "<p:sp><p:nvSpPr><p:nvPr><p:ph type=\"body\"/></p:nvPr></p:nvSpPr><p:txBody>"
            + "<a:p><a:r><a:t>Speaker note line one</a:t></a:r></a:p>"
            + "<a:p><a:r><a:t>Speaker note line two</a:t></a:r></a:p>"
            + "</p:txBody></p:sp>"
            + "</p:spTree></p:cSld></p:notes>");
    return zip(files);
  }

  static byte[] image() throws Exception {
    Map<String, Object> files = base("Image Slide",
        "<p:pic><p:nvPicPr><p:cNvPr id=\"2\" name=\"Picture 1\" descr=\"Diagram alt\"/></p:nvPicPr>"
            + "<p:blipFill><a:blip r:embed=\"rIdImage1\"/></p:blipFill></p:pic>",
        "<Relationship Id=\"rIdImage1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image\" Target=\"../media/image1.png\"/>");
    files.put("[Content_Types].xml", "<Types><Default Extension=\"png\" ContentType=\"image/png\"/></Types>");
    files.put("ppt/media/image1.png", new byte[] {(byte) 137, 80, 78, 71});
    return zip(files);
  }

  static byte[] missingImage() throws Exception {
    return zip(base("Missing Image Slide",
        "<p:pic><p:nvPicPr><p:cNvPr id=\"2\" name=\"Picture 1\" descr=\"Missing diagram\"/></p:nvPicPr>"
            + "<p:blipFill><a:blip r:embed=\"rIdImage1\"/></p:blipFill></p:pic>",
        "<Relationship Id=\"rIdImage1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image\" Target=\"../media/missing.png\"/>"));
  }

  static byte[] comments() throws Exception {
    return zip(base("Comment Slide",
        "<p:sp><p:txBody><a:p><a:r><a:t>Visible slide body</a:t></a:r></a:p></p:txBody></p:sp>",
        "<Relationship Id=\"rIdComments1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/comments\" Target=\"../comments/comment1.xml\"/>"));
  }

  static byte[] unsupportedPicture(String kind) throws Exception {
    String relType = "video".equals(kind) ? "video" : "audio".equals(kind) ? "audio" : "oleObject";
    String tag = "video".equals(kind) ? "a:videoFile" : "audio".equals(kind) ? "a:audioFile" : "p14:oleObj";
    String relId = "rId" + kind;
    return zip(base(capitalize(kind) + " Slide",
        "<p:pic><p:nvPicPr><p:cNvPr id=\"2\" name=\"" + kind + "\"/></p:nvPicPr><p:blipFill><" + tag + " r:link=\"" + relId + "\"/></p:blipFill></p:pic>",
        "<Relationship Id=\"" + relId + "\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/" + relType + "\" Target=\"../media/" + kind + ".bin\"/>"));
  }

  private static Map<String, Object> base(String title, String body, String slideRels) {
    Map<String, Object> files = new LinkedHashMap<String, Object>();
    files.put("ppt/presentation.xml",
        "<p:presentation xmlns:p=\"p\" xmlns:r=\"r\"><p:sldIdLst><p:sldId id=\"256\" r:id=\"rId1\"/></p:sldIdLst></p:presentation>");
    files.put("ppt/_rels/presentation.xml.rels",
        "<Relationships><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide\" Target=\"slides/slide1.xml\"/></Relationships>");
    files.put("ppt/slides/slide1.xml",
        "<p:sld xmlns:p=\"p\" xmlns:p14=\"p14\" xmlns:a=\"a\" xmlns:c=\"c\" xmlns:dgm=\"dgm\" xmlns:r=\"r\"><p:cSld><p:spTree>"
            + "<p:sp><p:nvSpPr><p:nvPr><p:ph type=\"title\"/></p:nvPr></p:nvSpPr><p:txBody><a:p><a:r><a:t>" + title + "</a:t></a:r></a:p></p:txBody></p:sp>"
            + body
            + "</p:spTree></p:cSld></p:sld>");
    if (slideRels != null) {
      files.put("ppt/slides/_rels/slide1.xml.rels", "<Relationships>" + slideRels + "</Relationships>");
    }
    return files;
  }

  private static String tableXml(String rows) {
    return "<p:graphicFrame><a:graphic><a:graphicData><a:tbl>" + rows + "</a:tbl></a:graphicData></a:graphic></p:graphicFrame>";
  }

  private static byte[] zip(Map<String, Object> files) throws Exception {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    ZipOutputStream zip = new ZipOutputStream(bytes);
    for (Map.Entry<String, Object> entry : files.entrySet()) {
      zip.putNextEntry(new ZipEntry(entry.getKey()));
      Object value = entry.getValue();
      if (value instanceof byte[]) {
        zip.write((byte[]) value);
      } else {
        zip.write(String.valueOf(value).getBytes(StandardCharsets.UTF_8));
      }
      zip.closeEntry();
    }
    zip.close();
    return bytes.toByteArray();
  }

  private static String capitalize(String value) {
    return value.substring(0, 1).toUpperCase() + value.substring(1);
  }
}
