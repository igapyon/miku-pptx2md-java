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

final class PptxPackage {
  private final Map<String, byte[]> entries;

  private PptxPackage(Map<String, byte[]> entries) {
    this.entries = entries;
  }

  static PptxPackage read(byte[] bytes) {
    return new PptxPackage(readZipEntries(bytes));
  }

  String readTextEntry(String path) {
    byte[] bytes = readBinaryEntry(path);
    return bytes == null ? null : new String(bytes, StandardCharsets.UTF_8);
  }

  byte[] readBinaryEntry(String path) {
    return entries.get(path);
  }

  Map<String, RelationshipEntry> readRelationshipMap(String relationshipsPath, String baseDir) {
    String xml = readTextEntry(relationshipsPath);
    Map<String, RelationshipEntry> map = new LinkedHashMap<String, RelationshipEntry>();
    if (xml == null) {
      return map;
    }
    for (RelationshipEntry entry : parseRelationshipEntries(xml, baseDir)) {
      map.put(entry.id, entry);
    }
    return map;
  }

  List<RelationshipEntry> readPartRelationships(String partPath) {
    String relsXml = readTextEntry(buildRelationshipsPath(partPath));
    return relsXml == null ? new ArrayList<RelationshipEntry>()
        : parseRelationshipEntries(relsXml, getPackageDir(partPath));
  }

  String buildRelationshipsPath(String partPath) {
    String dir = getPackageDir(partPath);
    String fileName = dir.length() > 0 ? partPath.substring(dir.length() + 1) : partPath;
    return dir.length() > 0 ? dir + "/_rels/" + fileName + ".rels" : "_rels/" + fileName + ".rels";
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

  private static String getPackageDir(String partPath) {
    int index = partPath.lastIndexOf('/');
    return index >= 0 ? partPath.substring(0, index) : "";
  }
}
