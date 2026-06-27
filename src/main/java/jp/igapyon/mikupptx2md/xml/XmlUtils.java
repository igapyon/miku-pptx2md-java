package jp.igapyon.mikupptx2md.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class XmlUtils {
  private XmlUtils() {
  }

  public static String decodeXmlEntities(String text) {
    return text.replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
        .replace("&amp;", "&");
  }

  public static String stripXmlTags(String text) {
    return text.replaceAll("<[^>]+>", "");
  }

  public static List<String> collectTagBlocks(String xml, String localName) {
    Pattern pattern = Pattern.compile("<[^<\\s:]*:?" + Pattern.quote(localName)
        + "\\b[\\s\\S]*?</[^<\\s:]*:?" + Pattern.quote(localName) + ">");
    Matcher matcher = pattern.matcher(xml);
    List<String> blocks = new ArrayList<String>();
    while (matcher.find()) {
      blocks.add(matcher.group());
    }
    return blocks;
  }

  public static List<String> collectTextValues(String xml) {
    Pattern pattern = Pattern.compile("<[^<\\s:]*:?t\\b[^>]*>([\\s\\S]*?)</[^<\\s:]*:?t>");
    Matcher matcher = pattern.matcher(xml);
    List<String> values = new ArrayList<String>();
    while (matcher.find()) {
      values.add(decodeXmlEntities(stripXmlTags(matcher.group(1))));
    }
    return values;
  }

  public static String getAttribute(String tag, String localName) {
    Pattern pattern = Pattern.compile("(?:^|\\s)(?:[^\\s:=]+:)?" + Pattern.quote(localName) + "=\"([^\"]*)\"");
    Matcher matcher = pattern.matcher(tag);
    return matcher.find() ? decodeXmlEntities(matcher.group(1)) : null;
  }

  public static String firstTag(String xml, String localName) {
    Pattern pattern = Pattern.compile("<[^<\\s:]*:?" + Pattern.quote(localName) + "\\b[^>]*>");
    Matcher matcher = pattern.matcher(xml);
    return matcher.find() ? matcher.group() : null;
  }

  public static String normalizePackagePath(String baseDir, String target) {
    String[] parts = (baseDir + "/" + target).split("/");
    List<String> normalized = new ArrayList<String>();
    for (String part : parts) {
      if (part.length() == 0 || ".".equals(part)) {
        continue;
      }
      if ("..".equals(part)) {
        if (!normalized.isEmpty()) {
          normalized.remove(normalized.size() - 1);
        }
        continue;
      }
      normalized.add(part);
    }
    return join("/", normalized);
  }

  public static String join(String delimiter, List<String> values) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < values.size(); i++) {
      if (i > 0) {
        builder.append(delimiter);
      }
      builder.append(values.get(i));
    }
    return builder.toString();
  }
}
