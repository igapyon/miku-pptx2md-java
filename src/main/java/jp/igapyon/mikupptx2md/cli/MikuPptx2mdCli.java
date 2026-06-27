package jp.igapyon.mikupptx2md.cli;

import jp.igapyon.mikupptx2md.core.MikuPptx2mdCore;
import jp.igapyon.mikupptx2md.core.Pptx2MdOptions;
import jp.igapyon.mikupptx2md.core.Pptx2MdResult;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MikuPptx2mdCli {
  public static final String VERSION = "0.2.0";

  public static void main(String[] args) {
    int exitCode = new MikuPptx2mdCli().run(args, System.out, System.err);
    if (exitCode != 0) {
      System.exit(exitCode);
    }
  }

  public int run(String[] args, PrintStream out, PrintStream err) {
    try {
      CliOptions options = CliOptions.parse(args);
      if (options.version) {
        out.println("miku-pptx2md " + VERSION);
        return 0;
      }
      if (options.help || options.inputPath == null) {
        out.print(helpText());
        return options.help ? 0 : 1;
      }

      long startedAt = System.currentTimeMillis();
      verbose(err, options, startedAt, "input=" + options.inputPath);
      byte[] inputBytes = Files.readAllBytes(Paths.get(options.inputPath));
      Pptx2MdOptions coreOptions = new Pptx2MdOptions();
      coreOptions.fallbackTitle = inputStem(options.inputPath);
      coreOptions.includeNotes = options.includeNotes;
      coreOptions.includeUnsupportedComments = options.includeUnsupportedComments;
      Pptx2MdResult result = new MikuPptx2mdCore().convertPptxToMarkdown(inputBytes, coreOptions);
      verbose(err, options, startedAt, "converted slides=" + result.summary.slides + " textBlocks=" + result.summary.textBlocks);

      if (options.summary) {
        out.println(new MikuPptx2mdCore().createPptx2MdSummaryText(result));
      }
      if (options.summaryOutPath != null) {
        writeText(options.summaryOutPath, new MikuPptx2mdCore().createPptx2MdSummaryText(result) + "\n");
      }
      if (options.summaryJsonOutPath != null) {
        writeText(options.summaryJsonOutPath, new MikuPptx2mdCore().createPptx2MdSummaryJsonText(result));
      }
      if (options.outPath != null) {
        writeText(options.outPath, result.markdown);
      } else {
        out.print(result.markdown);
      }
      verbose(err, options, startedAt, "done total-ms=" + (System.currentTimeMillis() - startedAt));
      return 0;
    } catch (Exception e) {
      err.println(e.getMessage());
      return 1;
    }
  }

  private static void writeText(String outputPath, String content) throws IOException {
    Path path = Paths.get(outputPath);
    Path parent = path.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
    Files.write(path, content.getBytes(StandardCharsets.UTF_8));
  }

  private static void verbose(PrintStream err, CliOptions options, long startedAt, String message) {
    if (options.verbose) {
      err.println("verbose: +" + (System.currentTimeMillis() - startedAt) + "ms " + message);
    }
  }

  private static String inputStem(String inputPath) {
    String fileName = Paths.get(inputPath).getFileName().toString();
    int index = fileName.lastIndexOf('.');
    return index > 0 ? fileName.substring(0, index) : fileName;
  }

  private static String helpText() {
    return "miku-pptx2md - local-first PPTX to Markdown converter\n\n"
        + "USAGE\n"
        + "  java -jar target/miku-pptx2md-" + VERSION + ".jar <input.pptx> [options]\n"
        + "  java -jar target/miku-pptx2md-" + VERSION + ".jar --version\n"
        + "  java -jar target/miku-pptx2md-" + VERSION + ".jar --help\n\n"
        + "OPTIONS\n"
        + "  --out <file>              Write Markdown to this file. Defaults to stdout.\n"
        + "  --summary                 Print summary text to stdout.\n"
        + "  --summary-out <file>      Write summary text to a file.\n"
        + "  --summary-json-out <file> Write structured summary JSON to a file.\n"
        + "  --no-notes                Omit speaker notes from Markdown output.\n"
        + "  --debug                   Include diagnostic HTML comment traces in Markdown.\n"
        + "  --verbose                 Write progress diagnostics to stderr.\n"
        + "  --version                 Show product name and version.\n"
        + "  --help                    Show this help.\n\n"
        + "EXIT CODES\n"
        + "  0  Success, --version, or --help.\n"
        + "  1  Usage, file I/O, parse, or runtime error.\n";
  }
}
