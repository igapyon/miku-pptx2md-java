package jp.igapyon.mikupptx2md.cli;

class CliOptions {
  String inputPath;
  String outPath;
  String summaryOutPath;
  String summaryJsonOutPath;
  boolean summary;
  boolean includeNotes = true;
  boolean includeUnsupportedComments;
  boolean verbose;
  boolean help;
  boolean version;

  static CliOptions parse(String[] args) {
    CliOptions options = new CliOptions();
    if (args.length == 1 && "--help".equals(args[0])) {
      options.help = true;
      return options;
    }
    if (args.length == 1 && "--version".equals(args[0])) {
      options.version = true;
      return options;
    }
    for (String arg : args) {
      if ("--help".equals(arg) || "--version".equals(arg)) {
        throw new IllegalArgumentException("Use --help or --version without other arguments.");
      }
    }

    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      if (!arg.startsWith("--")) {
        if (options.inputPath != null) {
          throw new IllegalArgumentException("Specify exactly one input .pptx file.");
        }
        options.inputPath = arg;
        continue;
      }
      if ("--summary".equals(arg)) {
        options.summary = true;
      } else if ("--no-notes".equals(arg)) {
        options.includeNotes = false;
      } else if ("--debug".equals(arg) || "--include-unsupported-comments".equals(arg)) {
        options.includeUnsupportedComments = true;
      } else if ("--verbose".equals(arg)) {
        options.verbose = true;
      } else if ("--out".equals(arg)) {
        options.outPath = requireValue(args, ++i, arg);
      } else if ("--summary-out".equals(arg)) {
        options.summaryOutPath = requireValue(args, ++i, arg);
      } else if ("--summary-json-out".equals(arg)) {
        options.summaryJsonOutPath = requireValue(args, ++i, arg);
      } else if ("--assets-dir".equals(arg)) {
        throw new IllegalArgumentException("--assets-dir is planned for the phase 2 parity pass.");
      } else {
        throw new IllegalArgumentException("Unknown option: " + arg);
      }
    }
    return options;
  }

  private static String requireValue(String[] args, int index, String option) {
    if (index >= args.length || args[index].startsWith("--")) {
      throw new IllegalArgumentException("Missing value for " + option);
    }
    return args[index];
  }
}
