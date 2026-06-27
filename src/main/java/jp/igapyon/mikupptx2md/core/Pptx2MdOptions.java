package jp.igapyon.mikupptx2md.core;

import java.util.function.Function;

public class Pptx2MdOptions {
  public String title;
  public String fallbackTitle;
  public boolean includeNotes = true;
  public boolean includeUnsupportedComments = false;
  public Function<Pptx2MdAsset, String> imagePathResolver;
}
