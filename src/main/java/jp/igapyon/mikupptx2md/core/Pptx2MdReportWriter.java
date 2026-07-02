package jp.igapyon.mikupptx2md.core;

import java.util.List;
import java.util.Map;

final class Pptx2MdReportWriter {
  private static final String[] SUMMARY_FIELDS = new String[] {
      "slides", "slidesWithTitles", "textBlocks", "listItems", "tables", "hyperlinks",
      "imageAssets", "notesSlides", "comments", "warnings", "errors", "diagnostics"
  };

  private Pptx2MdReportWriter() {
  }

  static String createSummaryText(Pptx2MdResult result) {
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

  static String createSummaryJsonText(Pptx2MdResult result) {
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

  static String createAssetsManifestJsonText(List<Pptx2MdAsset> assets) {
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
    if ("comments".equals(field)) return summary.comments;
    if ("warnings".equals(field)) return summary.warnings;
    if ("errors".equals(field)) return summary.errors;
    if ("diagnostics".equals(field)) return summary.diagnostics;
    return 0;
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
}
