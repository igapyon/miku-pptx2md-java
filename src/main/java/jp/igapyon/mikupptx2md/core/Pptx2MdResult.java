package jp.igapyon.mikupptx2md.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Pptx2MdResult {
  public String markdown;
  public final Map<String, String> metadata = new LinkedHashMap<String, String>();
  public final Pptx2MdSummary summary = new Pptx2MdSummary();
  public final List<Pptx2MdDiagnostic> diagnostics = new ArrayList<Pptx2MdDiagnostic>();
  public final List<Pptx2MdAsset> assets = new ArrayList<Pptx2MdAsset>();
}
