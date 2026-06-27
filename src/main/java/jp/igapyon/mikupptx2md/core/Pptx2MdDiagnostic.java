package jp.igapyon.mikupptx2md.core;

public class Pptx2MdDiagnostic {
  public final String severity;
  public final String code;
  public final String message;
  public final String source;

  public Pptx2MdDiagnostic(String severity, String code, String message, String source) {
    this.severity = severity;
    this.code = code;
    this.message = message;
    this.source = source;
  }
}
