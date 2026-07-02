package jp.igapyon.mikupptx2md.cli;

import jp.igapyon.mikupptx2md.core.MikuPptx2mdCore;
import jp.igapyon.mikupptx2md.core.Pptx2MdAsset;
import jp.igapyon.mikupptx2md.core.Pptx2MdOptions;
import jp.igapyon.mikupptx2md.core.Pptx2MdResult;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MikuPptx2mdCli {
  public static final String VERSION = "0.5.1";

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
      verbose(err, options, startedAt, "output=" + (options.outPath == null ? "stdout" : options.outPath));
      verbose(err, options, startedAt, "summary=" + (options.summaryOutPath == null ? (options.summary ? "stdout" : "disabled") : options.summaryOutPath));
      verbose(err, options, startedAt, "summary-json=" + (options.summaryJsonOutPath == null ? "disabled" : options.summaryJsonOutPath));
      verbose(err, options, startedAt, "assets=" + (options.assetsDirPath == null ? "disabled" : options.assetsDirPath));
      byte[] inputBytes = readInputBytes(options.inputPath);
      verbose(err, options, startedAt, "input-bytes=" + inputBytes.length);
      Pptx2MdOptions coreOptions = new Pptx2MdOptions();
      coreOptions.fallbackTitle = inputStem(options.inputPath);
      coreOptions.frontMatter = options.frontMatter;
      coreOptions.toolVersion = VERSION;
      coreOptions.includeNotes = options.includeNotes;
      coreOptions.includeUnsupportedComments = options.includeUnsupportedComments;
      final Path resolvedAssetsDir = options.assetsDirPath == null ? null : Paths.get(options.assetsDirPath).toAbsolutePath().normalize();
      final Path resolvedOutputPath = options.outPath == null ? null : Paths.get(options.outPath).toAbsolutePath().normalize();
      if (resolvedAssetsDir != null) {
        coreOptions.imagePathResolver = asset -> toPosixPath(relativeImagePath(asset, resolvedAssetsDir, resolvedOutputPath));
      }
      Pptx2MdResult result = new MikuPptx2mdCore().convertPptxToMarkdown(inputBytes, coreOptions);
      verbose(err, options, startedAt, "converted slides=" + result.summary.slides + " textBlocks=" + result.summary.textBlocks
          + " imageAssets=" + result.summary.imageAssets);

      MikuPptx2mdCore core = new MikuPptx2mdCore();
      if (resolvedAssetsDir != null) {
        writeAssets(resolvedAssetsDir, result.assets, core.createPptx2MdAssetsManifestJsonText(result.assets));
        verbose(err, options, startedAt, "assets-written count=" + result.assets.size());
      }
      if (options.summary) {
        out.println(core.createPptx2MdSummaryText(result));
      }
      if (options.summaryOutPath != null) {
        writeText(options.summaryOutPath, core.createPptx2MdSummaryText(result) + "\n");
        verbose(err, options, startedAt, "summary-written " + options.summaryOutPath);
      }
      if (options.summaryJsonOutPath != null) {
        writeText(options.summaryJsonOutPath, core.createPptx2MdSummaryJsonText(result));
        verbose(err, options, startedAt, "summary-json-written " + options.summaryJsonOutPath);
      }
      if (options.outPath != null) {
        writeText(options.outPath, result.markdown);
        verbose(err, options, startedAt, "markdown-written " + options.outPath);
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

  private static void writeAssets(Path assetsDir, List<Pptx2MdAsset> assets, String manifestJson) throws IOException {
    writeText(assetsDir.resolve("manifest.json").toString(), manifestJson);
    for (Pptx2MdAsset asset : assets) {
      Path outputPath = resolveAssetOutputPath(assetsDir, asset.sourcePath);
      Path parent = outputPath.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      Files.write(outputPath, asset.bytes == null ? new byte[0] : asset.bytes);
    }
  }

  private static byte[] readInputBytes(String inputPath) throws IOException {
    try {
      return Files.readAllBytes(Paths.get(inputPath));
    } catch (NoSuchFileException e) {
      throw new IOException("[" + inputPath + "] read failed: " + e.getMessage(), e);
    } catch (IOException e) {
      throw new IOException("[" + inputPath + "] read failed: " + e.getMessage(), e);
    }
  }

  private static Path resolveAssetOutputPath(Path assetsDir, String packagePath) {
    String[] parts = safePptxPackagePathParts(packagePath);
    Path outputPath = assetsDir;
    for (String part : parts) {
      outputPath = outputPath.resolve(part);
    }
    outputPath = outputPath.normalize();
    if (!outputPath.startsWith(assetsDir)) {
      throw new IllegalArgumentException("PPTX asset path escapes assets directory: " + packagePath);
    }
    return outputPath;
  }

  private static String[] safePptxPackagePathParts(String packagePath) {
    if (packagePath == null || packagePath.length() == 0 || packagePath.startsWith("/") || packagePath.indexOf('\\') >= 0) {
      throw new IllegalArgumentException("Unsafe PPTX asset path: " + packagePath);
    }
    String[] parts = packagePath.split("/");
    for (String part : parts) {
      if (part.length() == 0 || ".".equals(part) || "..".equals(part)) {
        throw new IllegalArgumentException("Unsafe PPTX asset path: " + packagePath);
      }
    }
    return parts;
  }

  private static String relativeImagePath(Pptx2MdAsset asset, Path assetsDir, Path outputPath) {
    Path assetOutputPath = resolveAssetOutputPath(assetsDir, asset.sourcePath);
    Path base = outputPath == null ? Paths.get("").toAbsolutePath().normalize() : outputPath.getParent();
    if (base == null) {
      base = Paths.get("").toAbsolutePath().normalize();
    }
    return base.relativize(assetOutputPath).toString();
  }

  private static String toPosixPath(String path) {
    return path.replace('\\', '/');
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
        + "CONTRACT\n"
        + "  Input is exactly one local .pptx file path.\n"
        + "  Primary output is Markdown.\n"
        + "  If --out is set, Markdown is written to that file.\n"
        + "  If --out is omitted, Markdown is written to stdout.\n"
        + "  --summary prints conversion summary text to stdout.\n"
        + "  --summary-out writes conversion summary text to a file.\n"
        + "  --summary-json-out writes structured summary JSON to a file.\n"
        + "  If --out is omitted, avoid --summary unless mixed stdout output is acceptable.\n"
        + "  --verbose writes progress and timing diagnostics to stderr.\n"
        + "  --help and --version are metadata commands and must be used without other arguments.\n\n"
        + "OPTIONS\n"
        + "  --out <file>\n"
        + "      Write Markdown to this file. Parent directories are created.\n\n"
        + "  --assets-dir <dir>\n"
        + "      Export resolved embedded image assets into this directory.\n"
        + "      Also writes <dir>/manifest.json.\n"
        + "      Markdown image links are made relative to --out, or to the current directory\n"
        + "      when --out is omitted.\n\n"
        + "  --summary\n"
        + "      Print summary text to stdout.\n\n"
        + "  --summary-out <file>\n"
        + "      Write summary text to this file. Parent directories are created.\n\n"
        + "  --summary-json-out <file>\n"
        + "      Write structured summary JSON to this file. Parent directories are created.\n\n"
        + "  --front-matter <mode>\n"
        + "      include or exclude. Default: include.\n\n"
        + "  --no-notes\n"
        + "      Omit speaker notes from Markdown output.\n\n"
        + "  --debug\n"
        + "      Include diagnostic HTML comment traces in Markdown.\n\n"
        + "  --include-unsupported-comments\n"
        + "      Alias for --debug.\n\n"
        + "  --verbose\n"
        + "      Write progress and timing diagnostics to stderr with a \"verbose:\" prefix.\n"
        + "      Primary Markdown and summary outputs are unchanged.\n\n"
        + "  --version\n"
        + "      Show product name and package version, then exit.\n\n"
        + "  --help\n"
        + "      Show this help, then exit.\n\n"
        + "OUTPUTS\n"
        + "  Markdown:\n"
        + "      Main converted presentation structure. Starts with YAML front matter by\n"
        + "      default; use --front-matter exclude to omit it. Each slide is emitted as\n"
        + "      a section.\n\n"
        + "  Summary:\n"
        + "      Core metadata plus text, list, table, hyperlink, image, notes, and diagnostics counts.\n\n"
        + "  Asset directory:\n"
        + "      Contains resolved embedded image files at package-relative paths such as\n"
        + "      ppt/media/example.png, plus manifest.json.\n\n"
        + "  Asset manifest:\n"
        + "      JSON with asset path, media type, alt text, byte size, source trace,\n"
        + "      slide index, block index, relationship id, and document position.\n\n"
        + "EXAMPLES\n"
        + "  Write Markdown to a file:\n"
        + "    java -jar target/miku-pptx2md-" + VERSION + ".jar ./sample.pptx --out ./sample.md\n\n"
        + "  Print Markdown to stdout:\n"
        + "    java -jar target/miku-pptx2md-" + VERSION + ".jar ./sample.pptx\n\n"
        + "  Write Markdown and summary files:\n"
        + "    java -jar target/miku-pptx2md-" + VERSION + ".jar ./sample.pptx --out ./sample.md --summary-out ./sample.summary.txt\n\n"
        + "  Write structured summary JSON:\n"
        + "    java -jar target/miku-pptx2md-" + VERSION + ".jar ./sample.pptx --out ./sample.md --summary-json-out ./sample.summary.json\n\n"
        + "  Omit YAML front matter:\n"
        + "    java -jar target/miku-pptx2md-" + VERSION + ".jar ./sample.pptx --out ./sample.md --front-matter exclude\n\n"
        + "  Print a summary:\n"
        + "    java -jar target/miku-pptx2md-" + VERSION + ".jar ./sample.pptx --out ./sample.md --summary\n\n"
        + "  Write Markdown and export image assets:\n"
        + "    java -jar target/miku-pptx2md-" + VERSION + ".jar ./sample.pptx --out ./sample.md --assets-dir ./sample.assets\n\n"
        + "  Include diagnostic debug comments:\n"
        + "    java -jar target/miku-pptx2md-" + VERSION + ".jar ./sample.pptx --out ./sample.md --debug\n\n"
        + "  Show progress diagnostics on stderr:\n"
        + "    java -jar target/miku-pptx2md-" + VERSION + ".jar ./sample.pptx --out ./sample.md --verbose\n\n"
        + "  Show version:\n"
        + "    java -jar target/miku-pptx2md-" + VERSION + ".jar --version\n\n"
        + "EXIT CODES\n"
        + "  0  Success, or explicit metadata command such as --version / --help.\n"
        + "  1  CLI usage error, file I/O error, parse error, or unexpected runtime error.\n";
  }
}
