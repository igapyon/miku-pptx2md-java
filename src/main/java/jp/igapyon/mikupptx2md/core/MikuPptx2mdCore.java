package jp.igapyon.mikupptx2md.core;

import jp.igapyon.mikupptx2md.xml.XmlUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MikuPptx2mdCore {
  private static final String[] SUMMARY_FIELDS = new String[] {
      "slides", "slidesWithTitles", "textBlocks", "listItems", "tables", "hyperlinks",
      "imageAssets", "notesSlides", "warnings", "errors", "diagnostics"
  };

  public Pptx2MdResult convertPptxToMarkdown(byte[] bytes, Pptx2MdOptions options) {
    Pptx2MdOptions effectiveOptions = options == null ? new Pptx2MdOptions() : options;
    Map<String, byte[]> entries = readZipEntries(bytes);
    Pptx2MdResult result = new Pptx2MdResult();
    parseCoreProperties(entries, result.metadata);

    List<SlideModel> slides = parseSlides(entries, result.diagnostics);
    result.assets.addAll(collectAssets(slides));
    result.markdown = renderMarkdown(resolveTitle(result.metadata, effectiveOptions), slides, result.diagnostics, effectiveOptions);
    fillSummary(result.summary, slides, result.assets, result.diagnostics);
    return result;
  }

  public String createPptx2MdSummaryText(Pptx2MdResult result) {
    StringBuilder builder = new StringBuilder();
    for (Map.Entry<String, String> entry : result.metadata.entrySet()) {
      builder.append("metadata.").append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
    }
    if (!result.metadata.isEmpty()) {
      builder.append('\n');
    }
    for (String field : SUMMARY_FIELDS) {
      builder.append(field).append(": ").append(summaryValue(result.summary, field)).append('\n');
    }
    if (builder.length() > 0) {
      builder.setLength(builder.length() - 1);
    }
    return builder.toString();
  }

  public String createPptx2MdSummaryJsonText(Pptx2MdResult result) {
    StringBuilder builder = new StringBuilder();
    builder.append("{\n");
    builder.append("  \"version\": 1,\n");
    builder.append("  \"metadata\": ").append(toJsonObject(result.metadata, 2)).append(",\n");
    builder.append("  \"summary\": ").append(summaryToJson(result.summary, 2)).append(",\n");
    builder.append("  \"diagnostics\": ").append(diagnosticsToJson(result.diagnostics, 2)).append(",\n");
    builder.append("  \"assets\": ").append(assetsToJson(result.assets, 2)).append('\n');
    builder.append("}\n");
    return builder.toString();
  }

  public String createPptx2MdAssetsManifestJsonText(List<Pptx2MdAsset> assets) {
    StringBuilder builder = new StringBuilder();
    builder.append("{\n");
    builder.append("  \"version\": 1,\n");
    builder.append("  \"assets\": ").append(assetsManifestToJson(assets, 2)).append('\n');
    builder.append("}\n");
    return builder.toString();
  }

  private static int summaryValue(Pptx2MdSummary summary, String field) {
    if ("slides".equals(field)) return summary.slides;
    if ("slidesWithTitles".equals(field)) return summary.slidesWithTitles;
    if ("textBlocks".equals(field)) return summary.textBlocks;
    if ("listItems".equals(field)) return summary.listItems;
    if ("tables".equals(field)) return summary.tables;
    if ("hyperlinks".equals(field)) return summary.hyperlinks;
    if ("imageAssets".equals(field)) return summary.imageAssets;
    if ("notesSlides".equals(field)) return summary.notesSlides;
    if ("warnings".equals(field)) return summary.warnings;
    if ("errors".equals(field)) return summary.errors;
    if ("diagnostics".equals(field)) return summary.diagnostics;
    return 0;
  }

  private static Map<String, byte[]> readZipEntries(byte[] bytes) {
    Map<String, byte[]> entries = new LinkedHashMap<String, byte[]>();
    try {
      ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes));
      ZipEntry entry;
      while ((entry = zip.getNextEntry()) != null) {
        if (!entry.isDirectory()) {
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          byte[] buffer = new byte[8192];
          int length;
          while ((length = zip.read(buffer)) >= 0) {
            out.write(buffer, 0, length);
          }
          entries.put(entry.getName(), out.toByteArray());
        }
      }
      return entries;
    } catch (IOException e) {
      throw new IllegalArgumentException("ZIP end of central directory was not found.", e);
    }
  }

  private static void parseCoreProperties(Map<String, byte[]> entries, Map<String, String> metadata) {
    String xml = readTextEntry(entries, "docProps/core.xml");
    if (xml == null) {
      return;
    }
    putMetadata(metadata, "title", readElementText(xml, "title"));
    putMetadata(metadata, "subject", readElementText(xml, "subject"));
    putMetadata(metadata, "creator", readElementText(xml, "creator"));
    putMetadata(metadata, "description", readElementText(xml, "description"));
    putMetadata(metadata, "keywords", readElementText(xml, "keywords"));
    putMetadata(metadata, "lastModifiedBy", readElementText(xml, "lastModifiedBy"));
    putMetadata(metadata, "revision", readElementText(xml, "revision"));
    putMetadata(metadata, "category", readElementText(xml, "category"));
    putMetadata(metadata, "created", readElementText(xml, "created"));
    putMetadata(metadata, "modified", readElementText(xml, "modified"));
  }

  private static void putMetadata(Map<String, String> metadata, String key, String value) {
    if (value != null && value.length() > 0) {
      metadata.put(key, value);
    }
  }

  private static String readElementText(String xml, String localName) {
    List<String> blocks = XmlUtils.collectTagBlocks(xml, localName);
    if (blocks.isEmpty()) {
      return null;
    }
    String value = XmlUtils.decodeXmlEntities(XmlUtils.stripXmlTags(blocks.get(0))).replaceAll("\\s+", " ").trim();
    return value.length() > 0 ? value : null;
  }

  private static List<SlideModel> parseSlides(Map<String, byte[]> entries, List<Pptx2MdDiagnostic> diagnostics) {
    String presentationXml = readTextEntry(entries, "ppt/presentation.xml");
    String presentationRelsXml = readTextEntry(entries, "ppt/_rels/presentation.xml.rels");
    if (presentationXml == null || presentationRelsXml == null) {
      throw new IllegalArgumentException("Required PPTX presentation parts were not found.");
    }

    Map<String, RelationshipEntry> rels = parseRelationshipMap(presentationRelsXml, "ppt");
    List<String> slideRelIds = new ArrayList<String>();
    Matcher matcher = Pattern.compile("<[^<\\s:]*:?sldId\\b[^>]*>").matcher(presentationXml);
    while (matcher.find()) {
      String relId = getRelationshipId(matcher.group());
      if (relId != null) {
        slideRelIds.add(relId);
      }
    }

    List<SlideModel> slides = new ArrayList<SlideModel>();
    for (int i = 0; i < slideRelIds.size(); i++) {
      String relId = slideRelIds.get(i);
      RelationshipEntry slideRel = rels.get(relId);
      if (slideRel == null) {
        diagnostics.add(new Pptx2MdDiagnostic("warning", "missing-slide-relationship",
            "Slide relationship was not found: " + relId, "ppt/presentation.xml"));
        slides.add(new SlideModel(i + 1, ""));
        continue;
      }
      String slideXml = readTextEntry(entries, slideRel.target);
      if (slideXml == null) {
        diagnostics.add(new Pptx2MdDiagnostic("warning", "missing-slide-part",
            "Slide part was not found: " + slideRel.target, slideRel.target));
        slides.add(new SlideModel(i + 1, slideRel.target));
        continue;
      }
      List<RelationshipEntry> slideRelationships = parseSlideRelationships(entries, slideRel.target);
      addUnsupportedSlideCommentDiagnostics(slideRelationships, slideRel.target, diagnostics);
      List<TextParagraph> notes = parseSlideNotes(entries, slideRel.target, slideRelationships, diagnostics);
      slides.add(parseSlideXml(slideXml, slideRel.target, i + 1, notes, slideRelationships, entries, diagnostics));
    }
    return slides;
  }

  private static void addUnsupportedSlideCommentDiagnostics(List<RelationshipEntry> relationships, String slidePath,
      List<Pptx2MdDiagnostic> diagnostics) {
    for (RelationshipEntry relationship : relationships) {
      if (relationship.type.endsWith("/comments")) {
        diagnostics.add(new Pptx2MdDiagnostic("warning", "unsupported-comments",
            "PowerPoint slide comments were found but are not converted to Markdown: " + relationship.target, slidePath));
      }
    }
  }

  private static List<RelationshipEntry> parseSlideRelationships(Map<String, byte[]> entries, String slidePath) {
    String relsXml = readTextEntry(entries, buildRelationshipsPath(slidePath));
    return relsXml == null ? new ArrayList<RelationshipEntry>() : parseRelationshipEntries(relsXml, getPackageDir(slidePath));
  }

  private static List<TextParagraph> parseSlideNotes(Map<String, byte[]> entries, String slidePath,
      List<RelationshipEntry> relationships, List<Pptx2MdDiagnostic> diagnostics) {
    for (RelationshipEntry rel : relationships) {
      if (rel.type.endsWith("/notesSlide")) {
        String notesXml = readTextEntry(entries, rel.target);
        if (notesXml == null) {
          diagnostics.add(new Pptx2MdDiagnostic("warning", "missing-notes-part",
              "Notes slide part was not found: " + rel.target, buildRelationshipsPath(slidePath)));
          return new ArrayList<TextParagraph>();
        }
        List<TextParagraph> notes = new ArrayList<TextParagraph>();
        for (String shapeXml : XmlUtils.collectTagBlocks(notesXml, "sp")) {
          if (!isNotesSlideImageShape(shapeXml)) {
            notes.addAll(extractShapeParagraphs(shapeXml, relationships, diagnostics, rel.target));
          }
        }
        return notes;
      }
    }
    return new ArrayList<TextParagraph>();
  }

  private static boolean isNotesSlideImageShape(String shapeXml) {
    String placeholderTag = XmlUtils.firstTag(shapeXml, "ph");
    return placeholderTag != null && "sldImg".equals(XmlUtils.getAttribute(placeholderTag, "type"));
  }

  private static SlideModel parseSlideXml(String xml, String slidePath, int index, List<TextParagraph> notes,
      List<RelationshipEntry> relationships, Map<String, byte[]> entries, List<Pptx2MdDiagnostic> diagnostics) {
    SlideModel slide = new SlideModel(index, slidePath);
    slide.notes.addAll(notes);
    List<SlideElement> elements = collectSlideContentElements(xml);
    for (SlideElement element : elements) {
      if ("graphicFrame".equals(element.kind)) {
        TableBlock table = parseGraphicFrameTable(element.xml);
        if (table != null) {
          if (table.diagnostic != null) {
            diagnostics.add(new Pptx2MdDiagnostic(table.diagnostic.severity, table.diagnostic.code,
                table.diagnostic.message, slidePath));
          }
          slide.blocks.add(table);
        } else {
          String kind = detectUnsupportedGraphicFrameKind(element.xml);
          diagnostics.add(new Pptx2MdDiagnostic("warning", "unsupported-" + kind,
              "Unsupported PowerPoint " + kind + " graphic frame was omitted from Markdown output.", slidePath));
        }
        continue;
      }

      if ("pic".equals(element.kind)) {
        ImageBlock image = parsePictureImage(element.xml, relationships, entries, slidePath, index, slide.blocks.size(), diagnostics);
        if (image != null) {
          slide.blocks.add(image);
        } else {
          Pptx2MdDiagnostic diagnostic = createUnsupportedPictureDiagnostic(element.xml, slidePath);
          if (diagnostic != null) {
            diagnostics.add(diagnostic);
          }
        }
        continue;
      }

      List<TextParagraph> paragraphs = extractShapeParagraphs(element.xml, relationships, diagnostics, slidePath);
      if (paragraphs.isEmpty()) {
        continue;
      }
      if (slide.title == null && isTitleShape(element.xml)) {
        slide.title = joinParagraphText(paragraphs);
        continue;
      }
      String shapeType = getOrdinaryShapeType(element.xml);
      if (shapeType != null) {
        slide.blocks.add(new ShapeTextBlock(shapeType, paragraphs));
        continue;
      }
      for (TextParagraph paragraph : paragraphs) {
        slide.blocks.add(new ParagraphBlock(paragraph));
      }
    }
    return slide;
  }

  private static List<SlideElement> collectSlideContentElements(String xml) {
    List<SlideElement> elements = new ArrayList<SlideElement>();
    Matcher matcher = Pattern.compile("<[^<\\s:]*:?(sp|graphicFrame|pic)\\b[\\s\\S]*?</[^<\\s:]*:?\\1>").matcher(xml);
    while (matcher.find()) {
      elements.add(new SlideElement(matcher.group(1), matcher.group()));
    }
    return elements;
  }

  private static List<TextParagraph> extractShapeParagraphs(String shapeXml, List<RelationshipEntry> relationships,
      List<Pptx2MdDiagnostic> diagnostics, String source) {
    List<TextParagraph> paragraphs = new ArrayList<TextParagraph>();
    for (String paragraphXml : XmlUtils.collectTagBlocks(shapeXml, "p")) {
      TextParagraph paragraph = parseTextParagraph(paragraphXml, relationships, diagnostics, source);
      if (paragraph.text.length() > 0) {
        paragraphs.add(paragraph);
      }
    }
    return paragraphs;
  }

  private static TextParagraph parseTextParagraph(String paragraphXml, List<RelationshipEntry> relationships,
      List<Pptx2MdDiagnostic> diagnostics, String source) {
    List<TextRun> runs = collectTextRuns(paragraphXml, relationships, diagnostics, source);
    TextParagraph paragraph = new TextParagraph();
    paragraph.text = normalizeInlineText(joinRunText(runs, false));
    paragraph.markdown = normalizeInlineText(joinRunText(runs, true));
    paragraph.hyperlinkCount = 0;
    for (TextRun run : runs) {
      if (run.hyperlink) {
        paragraph.hyperlinkCount++;
      }
    }
    String pPr = XmlUtils.firstTag(paragraphXml, "pPr");
    paragraph.level = pPr == null ? 0 : parseListLevel(pPr);
    if (Pattern.compile("<[^<\\s:]*:?buAutoNum\\b[^>]*>").matcher(paragraphXml).find()) {
      paragraph.listKind = "ordered";
    } else if (Pattern.compile("<[^<\\s:]*:?buChar\\b[^>]*>").matcher(paragraphXml).find()) {
      paragraph.listKind = "bullet";
    }
    return paragraph;
  }

  private static List<TextRun> collectTextRuns(String paragraphXml, List<RelationshipEntry> relationships,
      List<Pptx2MdDiagnostic> diagnostics, String source) {
    List<String> runBlocks = XmlUtils.collectTagBlocks(paragraphXml, "r");
    List<TextRun> runs = new ArrayList<TextRun>();
    if (runBlocks.isEmpty()) {
      String text = joinStrings(XmlUtils.collectTextValues(paragraphXml), "");
      runs.add(new TextRun(text, text, false));
      return runs;
    }
    for (String runXml : runBlocks) {
      String text = joinStrings(XmlUtils.collectTextValues(runXml), "");
      String hyperlinkRelId = getHyperlinkRelationshipId(runXml);
      if (hyperlinkRelId == null || text.length() == 0) {
        runs.add(new TextRun(text, applyInlineFormatting(text, runXml), false));
        continue;
      }
      RelationshipEntry hyperlinkRel = findRelationship(relationships, hyperlinkRelId, "/hyperlink");
      if (hyperlinkRel == null) {
        diagnostics.add(new Pptx2MdDiagnostic("warning", "missing-hyperlink-relationship",
            "Hyperlink relationship was not found: " + hyperlinkRelId, source));
        runs.add(new TextRun(text, text, false));
        continue;
      }
      runs.add(new TextRun(text, "[" + escapeMarkdownLinkLabel(applyInlineFormatting(text, runXml)) + "]("
          + escapeMarkdownLinkDestination(hyperlinkRel.target) + ")", true));
    }
    return runs;
  }

  private static TableBlock parseGraphicFrameTable(String graphicFrameXml) {
    List<String> tables = XmlUtils.collectTagBlocks(graphicFrameXml, "tbl");
    if (tables.isEmpty()) {
      return null;
    }
    List<List<String>> rows = new ArrayList<List<String>>();
    for (String rowXml : XmlUtils.collectTagBlocks(tables.get(0), "tr")) {
      List<String> row = new ArrayList<String>();
      for (String cellXml : XmlUtils.collectTagBlocks(rowXml, "tc")) {
        row.add(joinStrings(XmlUtils.collectTextValues(cellXml), " ").replaceAll("\\s+", " ").trim());
      }
      rows.add(row);
    }
    if (rows.isEmpty()) {
      return null;
    }
    Pptx2MdDiagnostic diagnostic = hasMergedTableCells(tables.get(0))
        ? new Pptx2MdDiagnostic("warning", "limited-table-merged-cells",
            "PowerPoint table contains merged cells; Markdown table output is an approximate flattened representation.", null)
        : null;
    return new TableBlock(rows, diagnostic);
  }

  private static boolean hasMergedTableCells(String tableXml) {
    return Pattern.compile("<[^<\\s:]*:?tc\\b[^>]*(?:gridSpan|rowSpan|hMerge|vMerge)=\"[^\"]*\"").matcher(tableXml).find()
        || Pattern.compile("<[^<\\s:]*:?(?:hMerge|vMerge)\\b").matcher(tableXml).find();
  }

  private static ImageBlock parsePictureImage(String pictureXml, List<RelationshipEntry> relationships,
      Map<String, byte[]> entries, String slidePath, int slideIndex, int blockIndex, List<Pptx2MdDiagnostic> diagnostics) {
    String blipTag = XmlUtils.firstTag(pictureXml, "blip");
    if (blipTag == null) {
      return null;
    }
    String relId = getRelationshipId(blipTag);
    RelationshipEntry imageRel = relId == null ? null : findRelationship(relationships, relId, "/image");
    if (imageRel == null) {
      diagnostics.add(new Pptx2MdDiagnostic("warning", "missing-image-relationship",
          "Image relationship was not found: " + relId, slidePath));
      return null;
    }
    byte[] assetBytes = entries.get(imageRel.target);
    if (assetBytes == null) {
      diagnostics.add(new Pptx2MdDiagnostic("warning", "missing-image-part",
          "Image part was not found: " + imageRel.target, slidePath));
      return null;
    }
    Pptx2MdAsset asset = new Pptx2MdAsset();
    asset.sourcePath = imageRel.target;
    asset.mediaType = mediaTypeForPath(imageRel.target);
    asset.altText = readPictureAltText(pictureXml);
    asset.sourceTrace = "picture:image(" + imageRel.target + "):alt(" + asset.altText + ")";
    asset.slideIndex = slideIndex;
    asset.blockIndex = blockIndex;
    asset.relationshipId = relId;
    asset.bytes = assetBytes;
    return new ImageBlock(asset);
  }

  private static String renderMarkdown(String title, List<SlideModel> slides, List<Pptx2MdDiagnostic> diagnostics,
      Pptx2MdOptions options) {
    StringBuilder builder = new StringBuilder();
    builder.append("# ").append(title).append("\n\n");
    for (SlideModel slide : slides) {
      builder.append("## Slide ").append(slide.index);
      if (slide.title != null && slide.title.length() > 0) {
        builder.append(": ").append(slide.title);
      }
      builder.append("\n\n");
      for (SlideBlock block : slide.blocks) {
        block.appendMarkdown(builder, options);
      }
      if (options.includeNotes && !slide.notes.isEmpty()) {
        builder.append("### Speaker Notes\n\n");
        for (TextParagraph note : slide.notes) {
          builder.append(note.markdown).append("\n\n");
        }
      }
    }
    if (options.includeUnsupportedComments && !diagnostics.isEmpty()) {
      builder.append("## Diagnostics\n\n");
      for (Pptx2MdDiagnostic diagnostic : diagnostics) {
        builder.append("<!-- ").append(diagnostic.severity).append(": ").append(diagnostic.code);
        if (diagnostic.source != null) {
          builder.append(" source=").append(diagnostic.source);
        }
        builder.append(": ").append(diagnostic.message).append(" -->\n");
      }
    }
    while (builder.length() >= 2 && builder.charAt(builder.length() - 1) == '\n'
        && builder.charAt(builder.length() - 2) == '\n') {
      builder.setLength(builder.length() - 1);
    }
    return builder.toString();
  }

  private static void fillSummary(Pptx2MdSummary summary, List<SlideModel> slides, List<Pptx2MdAsset> assets,
      List<Pptx2MdDiagnostic> diagnostics) {
    summary.slides = slides.size();
    summary.imageAssets = assets.size();
    summary.diagnostics = diagnostics.size();
    for (SlideModel slide : slides) {
      if (slide.title != null && slide.title.length() > 0) summary.slidesWithTitles++;
      if (!slide.notes.isEmpty()) summary.notesSlides++;
      summary.textBlocks += slide.notes.size();
      for (SlideBlock block : slide.blocks) {
        summary.textBlocks += block.textBlockCount();
        summary.listItems += block.listItemCount();
        summary.tables += block.tableCount();
        summary.hyperlinks += block.hyperlinkCount();
      }
    }
    for (Pptx2MdDiagnostic diagnostic : diagnostics) {
      if ("warning".equals(diagnostic.severity)) summary.warnings++;
      if ("error".equals(diagnostic.severity)) summary.errors++;
    }
  }

  private static List<Pptx2MdAsset> collectAssets(List<SlideModel> slides) {
    List<Pptx2MdAsset> assets = new ArrayList<Pptx2MdAsset>();
    for (SlideModel slide : slides) {
      for (SlideBlock block : slide.blocks) {
        if (block instanceof ImageBlock) {
          assets.add(((ImageBlock) block).asset);
        }
      }
    }
    return assets;
  }

  private static String readTextEntry(Map<String, byte[]> entries, String path) {
    byte[] bytes = entries.get(path);
    return bytes == null ? null : new String(bytes, StandardCharsets.UTF_8);
  }

  private static String resolveTitle(Map<String, String> metadata, Pptx2MdOptions options) {
    if (options.title != null && options.title.length() > 0) return options.title;
    if (metadata.containsKey("title")) return metadata.get("title");
    if (options.fallbackTitle != null && options.fallbackTitle.length() > 0) return options.fallbackTitle;
    return "presentation";
  }

  private static String getRelationshipId(String tag) {
    Matcher matcher = Pattern.compile("\\sr:(?:id|embed)=\"([^\"]*)\"").matcher(tag);
    return matcher.find() ? matcher.group(1) : null;
  }

  private static Map<String, RelationshipEntry> parseRelationshipMap(String xml, String baseDir) {
    Map<String, RelationshipEntry> map = new LinkedHashMap<String, RelationshipEntry>();
    for (RelationshipEntry entry : parseRelationshipEntries(xml, baseDir)) {
      map.put(entry.id, entry);
    }
    return map;
  }

  private static List<RelationshipEntry> parseRelationshipEntries(String xml, String baseDir) {
    List<RelationshipEntry> rels = new ArrayList<RelationshipEntry>();
    Matcher matcher = Pattern.compile("<[^<\\s:]*:?Relationship\\b[^>]*>").matcher(xml);
    while (matcher.find()) {
      String tag = matcher.group();
      String id = XmlUtils.getAttribute(tag, "Id");
      String type = XmlUtils.getAttribute(tag, "Type");
      String target = XmlUtils.getAttribute(tag, "Target");
      String targetMode = XmlUtils.getAttribute(tag, "TargetMode");
      if (id != null && target != null) {
        rels.add(new RelationshipEntry(id, type == null ? "" : type,
            "External".equals(targetMode) ? target : XmlUtils.normalizePackagePath(baseDir, target), targetMode));
      }
    }
    return rels;
  }

  private static String buildRelationshipsPath(String partPath) {
    String dir = getPackageDir(partPath);
    String fileName = dir.length() > 0 ? partPath.substring(dir.length() + 1) : partPath;
    return dir.length() > 0 ? dir + "/_rels/" + fileName + ".rels" : "_rels/" + fileName + ".rels";
  }

  private static String getPackageDir(String partPath) {
    int index = partPath.lastIndexOf('/');
    return index >= 0 ? partPath.substring(0, index) : "";
  }

  private static boolean isTitleShape(String shapeXml) {
    String placeholderTag = XmlUtils.firstTag(shapeXml, "ph");
    if (placeholderTag == null) return false;
    String type = XmlUtils.getAttribute(placeholderTag, "type");
    return "title".equals(type) || "ctrTitle".equals(type);
  }

  private static String getOrdinaryShapeType(String shapeXml) {
    String cNvSpPr = XmlUtils.firstTag(shapeXml, "cNvSpPr");
    if (cNvSpPr != null && "1".equals(XmlUtils.getAttribute(cNvSpPr, "txBox"))) {
      return null;
    }
    String prstGeom = XmlUtils.firstTag(shapeXml, "prstGeom");
    return prstGeom == null ? null : XmlUtils.getAttribute(prstGeom, "prst");
  }

  private static String detectUnsupportedGraphicFrameKind(String graphicFrameXml) {
    String graphicDataTag = XmlUtils.firstTag(graphicFrameXml, "graphicData");
    String uri = graphicDataTag == null ? "" : valueOrEmpty(XmlUtils.getAttribute(graphicDataTag, "uri"));
    if (uri.contains("/chart") || Pattern.compile("<[^<\\s:]*:?chart\\b").matcher(graphicFrameXml).find()) {
      return "chart";
    }
    if (uri.contains("/diagram") || Pattern.compile("<[^<\\s:]*:?relIds\\b").matcher(graphicFrameXml).find()) {
      return "smartart";
    }
    return "graphic-frame";
  }

  private static Pptx2MdDiagnostic createUnsupportedPictureDiagnostic(String pictureXml, String slidePath) {
    if (Pattern.compile("<[^<\\s:]*:?videoFile\\b").matcher(pictureXml).find()) {
      return new Pptx2MdDiagnostic("warning", "unsupported-video",
          "Unsupported PowerPoint video picture object was omitted from Markdown output.", slidePath);
    }
    if (Pattern.compile("<[^<\\s:]*:?audioFile\\b").matcher(pictureXml).find()) {
      return new Pptx2MdDiagnostic("warning", "unsupported-audio",
          "Unsupported PowerPoint audio picture object was omitted from Markdown output.", slidePath);
    }
    if (Pattern.compile("<[^<\\s:]*:?oleObj\\b").matcher(pictureXml).find()) {
      return new Pptx2MdDiagnostic("warning", "unsupported-ole-object",
          "Unsupported PowerPoint ole-object picture object was omitted from Markdown output.", slidePath);
    }
    return null;
  }

  private static String valueOrEmpty(String value) {
    return value == null ? "" : value;
  }

  private static String joinParagraphText(List<TextParagraph> paragraphs) {
    List<String> values = new ArrayList<String>();
    for (TextParagraph paragraph : paragraphs) {
      values.add(paragraph.text);
    }
    return joinStrings(values, " ").trim();
  }

  private static String joinRunText(List<TextRun> runs, boolean markdown) {
    StringBuilder builder = new StringBuilder();
    for (TextRun run : runs) {
      builder.append(markdown ? run.markdown : run.text);
    }
    return builder.toString();
  }

  private static String normalizeInlineText(String text) {
    return text.replaceAll("\\s+", " ").trim();
  }

  private static int parseListLevel(String paragraphPropertiesTag) {
    String rawLevel = XmlUtils.getAttribute(paragraphPropertiesTag, "lvl");
    if (rawLevel == null) return 0;
    try {
      int level = Integer.parseInt(rawLevel);
      if (level < 0) return 0;
      return Math.min(level, 8);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private static String getHyperlinkRelationshipId(String runXml) {
    String hyperlinkTag = XmlUtils.firstTag(runXml, "hlinkClick");
    return hyperlinkTag == null ? null : getRelationshipId(hyperlinkTag);
  }

  private static String applyInlineFormatting(String text, String runXml) {
    String runPropertiesTag = XmlUtils.firstTag(runXml, "rPr");
    if (runPropertiesTag == null) return text;
    boolean bold = "1".equals(XmlUtils.getAttribute(runPropertiesTag, "b"));
    boolean italic = "1".equals(XmlUtils.getAttribute(runPropertiesTag, "i"));
    String formatted = text;
    if (bold && italic) formatted = "***" + formatted + "***";
    else if (bold) formatted = "**" + formatted + "**";
    else if (italic) formatted = "*" + formatted + "*";
    String underline = XmlUtils.getAttribute(runPropertiesTag, "u");
    if (underline != null && !"none".equals(underline)) {
      formatted = "<u>" + formatted + "</u>";
    }
    return formatted;
  }

  private static String escapeMarkdownLinkLabel(String text) {
    return text.replace("\\", "\\\\").replace("[", "\\[").replace("]", "\\]");
  }

  private static String escapeMarkdownLinkDestination(String text) {
    return text.replace(")", "%29").replace("(", "%28");
  }

  private static RelationshipEntry findRelationship(List<RelationshipEntry> relationships, String id, String typeSuffix) {
    for (RelationshipEntry relationship : relationships) {
      if (relationship.id.equals(id) && relationship.type.endsWith(typeSuffix)) {
        return relationship;
      }
    }
    return null;
  }

  private static String mediaTypeForPath(String path) {
    String lower = path.toLowerCase();
    if (lower.endsWith(".png")) return "image/png";
    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
    if (lower.endsWith(".gif")) return "image/gif";
    if (lower.endsWith(".svg")) return "image/svg+xml";
    return "application/octet-stream";
  }

  private static String readPictureAltText(String pictureXml) {
    String cNvPr = XmlUtils.firstTag(pictureXml, "cNvPr");
    String descr = cNvPr == null ? null : XmlUtils.getAttribute(cNvPr, "descr");
    if (descr != null) return descr;
    String name = cNvPr == null ? null : XmlUtils.getAttribute(cNvPr, "name");
    return name == null ? "" : name;
  }

  private static String joinStrings(List<String> values, String delimiter) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < values.size(); i++) {
      if (i > 0) builder.append(delimiter);
      builder.append(values.get(i));
    }
    return builder.toString();
  }

  private static String toJsonObject(Map<String, String> map, int indent) {
    if (map.isEmpty()) return "{}";
    String pad = spaces(indent);
    String childPad = spaces(indent + 2);
    StringBuilder builder = new StringBuilder();
    builder.append("{\n");
    int index = 0;
    for (Map.Entry<String, String> entry : map.entrySet()) {
      if (index++ > 0) builder.append(",\n");
      builder.append(childPad).append(json(entry.getKey())).append(": ").append(json(entry.getValue()));
    }
    builder.append('\n').append(pad).append('}');
    return builder.toString();
  }

  private static String summaryToJson(Pptx2MdSummary summary, int indent) {
    String pad = spaces(indent);
    String childPad = spaces(indent + 2);
    StringBuilder builder = new StringBuilder();
    builder.append("{\n");
    for (int i = 0; i < SUMMARY_FIELDS.length; i++) {
      if (i > 0) builder.append(",\n");
      builder.append(childPad).append(json(SUMMARY_FIELDS[i])).append(": ").append(summaryValue(summary, SUMMARY_FIELDS[i]));
    }
    builder.append('\n').append(pad).append('}');
    return builder.toString();
  }

  private static String diagnosticsToJson(List<Pptx2MdDiagnostic> diagnostics, int indent) {
    if (diagnostics.isEmpty()) return "[]";
    String pad = spaces(indent);
    String childPad = spaces(indent + 2);
    String grandChildPad = spaces(indent + 4);
    StringBuilder builder = new StringBuilder();
    builder.append("[\n");
    for (int i = 0; i < diagnostics.size(); i++) {
      Pptx2MdDiagnostic diagnostic = diagnostics.get(i);
      if (i > 0) builder.append(",\n");
      builder.append(childPad).append("{\n");
      builder.append(grandChildPad).append("\"severity\": ").append(json(diagnostic.severity)).append(",\n");
      builder.append(grandChildPad).append("\"code\": ").append(json(diagnostic.code)).append(",\n");
      builder.append(grandChildPad).append("\"message\": ").append(json(diagnostic.message));
      if (diagnostic.source != null) {
        builder.append(",\n").append(grandChildPad).append("\"source\": ").append(json(diagnostic.source));
      }
      builder.append('\n').append(childPad).append('}');
    }
    builder.append('\n').append(pad).append(']');
    return builder.toString();
  }

  private static String assetsToJson(List<Pptx2MdAsset> assets, int indent) {
    if (assets.isEmpty()) return "[]";
    String pad = spaces(indent);
    String childPad = spaces(indent + 2);
    String grandChildPad = spaces(indent + 4);
    StringBuilder builder = new StringBuilder();
    builder.append("[\n");
    for (int i = 0; i < assets.size(); i++) {
      Pptx2MdAsset asset = assets.get(i);
      if (i > 0) builder.append(",\n");
      builder.append(childPad).append("{\n");
      builder.append(grandChildPad).append("\"kind\": \"image\",\n");
      builder.append(grandChildPad).append("\"sourcePath\": ").append(json(asset.sourcePath)).append(",\n");
      builder.append(grandChildPad).append("\"mediaType\": ").append(json(asset.mediaType)).append(",\n");
      builder.append(grandChildPad).append("\"altText\": ").append(json(asset.altText)).append(",\n");
      builder.append(grandChildPad).append("\"sourceTrace\": ").append(json(asset.sourceTrace)).append(",\n");
      builder.append(grandChildPad).append("\"slideIndex\": ").append(asset.slideIndex).append(",\n");
      builder.append(grandChildPad).append("\"blockIndex\": ").append(asset.blockIndex).append(",\n");
      builder.append(grandChildPad).append("\"relationshipId\": ").append(json(asset.relationshipId)).append(",\n");
      builder.append(grandChildPad).append("\"size\": ").append(asset.bytes == null ? 0 : asset.bytes.length).append('\n');
      builder.append(childPad).append('}');
    }
    builder.append('\n').append(pad).append(']');
    return builder.toString();
  }

  private static String assetsManifestToJson(List<Pptx2MdAsset> assets, int indent) {
    if (assets.isEmpty()) return "[]";
    String pad = spaces(indent);
    String childPad = spaces(indent + 2);
    String grandChildPad = spaces(indent + 4);
    String positionPad = spaces(indent + 6);
    StringBuilder builder = new StringBuilder();
    builder.append("[\n");
    for (int i = 0; i < assets.size(); i++) {
      Pptx2MdAsset asset = assets.get(i);
      if (i > 0) builder.append(",\n");
      builder.append(childPad).append("{\n");
      builder.append(grandChildPad).append("\"kind\": \"image\",\n");
      builder.append(grandChildPad).append("\"sourcePath\": ").append(json(asset.sourcePath)).append(",\n");
      builder.append(grandChildPad).append("\"mediaType\": ").append(json(asset.mediaType)).append(",\n");
      builder.append(grandChildPad).append("\"altText\": ").append(json(asset.altText)).append(",\n");
      builder.append(grandChildPad).append("\"sourceTrace\": ").append(json(asset.sourceTrace)).append(",\n");
      builder.append(grandChildPad).append("\"slideIndex\": ").append(asset.slideIndex).append(",\n");
      builder.append(grandChildPad).append("\"blockIndex\": ").append(asset.blockIndex).append(",\n");
      builder.append(grandChildPad).append("\"relationshipId\": ").append(json(asset.relationshipId)).append(",\n");
      builder.append(grandChildPad).append("\"size\": ").append(asset.bytes == null ? 0 : asset.bytes.length).append(",\n");
      builder.append(grandChildPad).append("\"documentPosition\": {\n");
      builder.append(positionPad).append("\"slideIndex\": ").append(asset.slideIndex).append(",\n");
      builder.append(positionPad).append("\"blockIndex\": ").append(asset.blockIndex).append(",\n");
      builder.append(positionPad).append("\"blockKind\": \"image\"\n");
      builder.append(grandChildPad).append("}\n");
      builder.append(childPad).append('}');
    }
    builder.append('\n').append(pad).append(']');
    return builder.toString();
  }

  private static String json(String value) {
    StringBuilder builder = new StringBuilder();
    builder.append('"');
    for (int i = 0; i < value.length(); i++) {
      char ch = value.charAt(i);
      if (ch == '"' || ch == '\\') builder.append('\\').append(ch);
      else if (ch == '\n') builder.append("\\n");
      else if (ch == '\r') builder.append("\\r");
      else if (ch == '\t') builder.append("\\t");
      else builder.append(ch);
    }
    builder.append('"');
    return builder.toString();
  }

  private static String spaces(int count) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < count; i++) builder.append(' ');
    return builder.toString();
  }

  private static class RelationshipEntry {
    final String id;
    final String type;
    final String target;
    final String targetMode;

    RelationshipEntry(String id, String type, String target, String targetMode) {
      this.id = id;
      this.type = type;
      this.target = target;
      this.targetMode = targetMode;
    }
  }

  private static class SlideModel {
    final int index;
    final String path;
    String title;
    final List<SlideBlock> blocks = new ArrayList<SlideBlock>();
    final List<TextParagraph> notes = new ArrayList<TextParagraph>();

    SlideModel(int index, String path) {
      this.index = index;
      this.path = path;
    }
  }

  private static class SlideElement {
    final String kind;
    final String xml;

    SlideElement(String kind, String xml) {
      this.kind = kind;
      this.xml = xml;
    }
  }

  private interface SlideBlock {
    void appendMarkdown(StringBuilder builder, Pptx2MdOptions options);
    int textBlockCount();
    int listItemCount();
    int tableCount();
    int hyperlinkCount();
  }

  private static class ParagraphBlock implements SlideBlock {
    final TextParagraph paragraph;

    ParagraphBlock(TextParagraph paragraph) {
      this.paragraph = paragraph;
    }

    public void appendMarkdown(StringBuilder builder, Pptx2MdOptions options) {
      if ("ordered".equals(paragraph.listKind)) {
        builder.append(repeat("  ", paragraph.level)).append("1. ").append(paragraph.markdown).append("\n");
      } else if ("bullet".equals(paragraph.listKind)) {
        builder.append(repeat("  ", paragraph.level)).append("- ").append(paragraph.markdown).append("\n");
      } else {
        builder.append(paragraph.markdown).append("\n");
      }
      builder.append('\n');
    }

    public int textBlockCount() {
      return 1;
    }

    public int listItemCount() {
      return paragraph.listKind == null ? 0 : 1;
    }

    public int tableCount() {
      return 0;
    }

    public int hyperlinkCount() {
      return paragraph.hyperlinkCount;
    }
  }

  private static class ShapeTextBlock implements SlideBlock {
    final String shapeType;
    final List<TextParagraph> paragraphs;

    ShapeTextBlock(String shapeType, List<TextParagraph> paragraphs) {
      this.shapeType = shapeType;
      this.paragraphs = paragraphs;
    }

    public void appendMarkdown(StringBuilder builder, Pptx2MdOptions options) {
      for (int i = 0; i < paragraphs.size(); i++) {
        TextParagraph paragraph = paragraphs.get(i);
        builder.append("> ");
        if (i == 0) {
          builder.append("[Shape: ").append(shapeType).append("] ");
        }
        builder.append(paragraph.markdown).append("\n");
      }
      builder.append('\n');
    }

    public int textBlockCount() {
      return paragraphs.size();
    }

    public int listItemCount() {
      int count = 0;
      for (TextParagraph paragraph : paragraphs) {
        if (paragraph.listKind != null) count++;
      }
      return count;
    }

    public int tableCount() {
      return 0;
    }

    public int hyperlinkCount() {
      int count = 0;
      for (TextParagraph paragraph : paragraphs) {
        count += paragraph.hyperlinkCount;
      }
      return count;
    }
  }

  private static class TableBlock implements SlideBlock {
    final List<List<String>> rows;
    final Pptx2MdDiagnostic diagnostic;

    TableBlock(List<List<String>> rows, Pptx2MdDiagnostic diagnostic) {
      this.rows = rows;
      this.diagnostic = diagnostic;
    }

    public void appendMarkdown(StringBuilder builder, Pptx2MdOptions options) {
      int width = 0;
      for (List<String> row : rows) {
        width = Math.max(width, row.size());
      }
      if (width == 0) return;
      appendTableRow(builder, rows.get(0), width);
      List<String> separator = new ArrayList<String>();
      for (int i = 0; i < width; i++) separator.add("---");
      appendTableRow(builder, separator, width);
      for (int i = 1; i < rows.size(); i++) appendTableRow(builder, rows.get(i), width);
      builder.append('\n');
    }

    public int textBlockCount() {
      int count = 0;
      for (List<String> row : rows) {
        for (String cell : row) {
          if (cell != null && cell.length() > 0) {
            count++;
          }
        }
      }
      return count;
    }

    public int listItemCount() {
      return 0;
    }

    public int tableCount() {
      return 1;
    }

    public int hyperlinkCount() {
      return 0;
    }
  }

  private static class ImageBlock implements SlideBlock {
    final Pptx2MdAsset asset;

    ImageBlock(Pptx2MdAsset asset) {
      this.asset = asset;
    }

    public void appendMarkdown(StringBuilder builder, Pptx2MdOptions options) {
      String altText = asset.altText == null || asset.altText.length() == 0 ? asset.sourcePath : asset.altText;
      String imagePath = options.imagePathResolver == null ? "" : options.imagePathResolver.apply(asset);
      if (imagePath == null || imagePath.length() == 0) {
        builder.append("[Image: ").append(altText).append("]\n\n");
      } else {
        builder.append("![").append(escapeMarkdownLinkLabel(altText)).append("](")
            .append(escapeMarkdownLinkDestination(imagePath)).append(")\n\n");
      }
    }

    public int textBlockCount() {
      return 0;
    }

    public int listItemCount() {
      return 0;
    }

    public int tableCount() {
      return 0;
    }

    public int hyperlinkCount() {
      return 0;
    }
  }

  private static class TextParagraph {
    String text;
    String markdown;
    int hyperlinkCount;
    String listKind;
    int level;
  }

  private static class TextRun {
    final String text;
    final String markdown;
    final boolean hyperlink;

    TextRun(String text, String markdown, boolean hyperlink) {
      this.text = text;
      this.markdown = markdown;
      this.hyperlink = hyperlink;
    }
  }

  private static void appendTableRow(StringBuilder builder, List<String> row, int width) {
    builder.append('|');
    for (int i = 0; i < width; i++) {
      String value = i < row.size() ? row.get(i) : "";
      builder.append(' ').append(value.replace("|", "\\|")).append(" |");
    }
    builder.append('\n');
  }

  private static String repeat(String value, int count) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < count; i++) builder.append(value);
    return builder.toString();
  }
}
