package dev.gokhun.convert;

import static dev.gokhun.convert.ConversionUtil.convert;
import static java.nio.charset.StandardCharsets.UTF_8;
import static picocli.CommandLine.ExitCode.OK;
import static picocli.CommandLine.Help.Ansi.ON;
import static picocli.CommandLine.Help.defaultColorScheme;

import dev.gokhun.convert.ConversionUtil.ConversionOptions;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;

@Command(
    mixinStandardHelpOptions = true,
    name = "convert",
    description = "Converts one file type to another.",
    sortOptions = false,
    usageHelpWidth = 120,
    versionProvider = VersionProvider.class)
public final class Convert implements Callable<Integer> {
  private static final SystemManager systemManager = new DefaultSystemManager();
  private static final ColorScheme colorScheme = defaultColorScheme(ON);
  private static final IExecutionExceptionHandler exceptionHandler =
      new ExecutionExceptionHandler();
  private static final CommandLine cmd = new CommandLine(new Convert())
      .setOut(systemManager.getOut())
      .setErr(systemManager.getErr())
      .setExecutionExceptionHandler(exceptionHandler)
      .setColorScheme(colorScheme);

  @Option(
      names = {"--from", "--input", "-f", "-i"},
      required = true,
      order = 1,
      description = "File to convert from.")
  File input;

  @Option(
      names = {"--output", "--to", "-o", "-t"},
      required = true,
      order = 2,
      description = "File to convert into.")
  File output;

  @Option(
      names = {"--csv-separator", "-s"},
      order = 3,
      defaultValue = ",",
      description = "Character(s) to separate CSV columns. Default value is ','.")
  char csvSeparator;

  @Option(
      names = "--pretty",
      order = 4,
      defaultValue = "false",
      description = "Prettify output if possible. Default is false and output is minimized.")
  boolean pretty;

  @Option(
      names = "--indent-yaml",
      order = 5,
      defaultValue = "true",
      description = "Indents YAML array fields. Default is true.")
  boolean indentYaml;

  @Option(
      names = "--minimize-yaml-quotes",
      order = 6,
      defaultValue = "true",
      description = "Minimizes YAML quotes if possible. Default is true.")
  boolean minimizeYamlQuotes;

  @Option(
      names = "--deduplicate-keys",
      order = 7,
      defaultValue = "false",
      description = "Think csv but as json. Only available on conversions where the initial source"
          + " is array. Keys are arrays and values are arrays of arrays.")
  boolean deduplicateKeys;

  @Override
  public Integer call() {
    try {
      convert(
          input,
          output,
          ConversionOptions.builder()
              .setCsvSeparator(csvSeparator)
              .setPretty(pretty)
              .setIndentYaml(indentYaml)
              .setMinimizeYamlQuotes(minimizeYamlQuotes)
              .setDeduplicateKeys(deduplicateKeys)
              .build());
    } catch (IllegalArgumentException | IOException e) {
      throw new ConvertAppException(e);
    }
    return OK;
  }

  public static void main(String... args) {
    systemManager.exit(cmd.execute(args));
  }

  interface SystemManager {
    PrintWriter getOut();

    PrintWriter getErr();

    void exit(int status);
  }

  static final class DefaultSystemManager implements SystemManager {
    private final PrintWriter out =
        new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out, UTF_8)));

    private final PrintWriter err =
        new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.err, UTF_8)));

    @Override
    public PrintWriter getOut() {
      return out;
    }

    @Override
    public PrintWriter getErr() {
      return err;
    }

    @Override
    public void exit(int status) {
      System.exit(status);
    }
  }

  static final class ExecutionExceptionHandler implements IExecutionExceptionHandler {

    @Override
    public int handleExecutionException(
        Exception ex, CommandLine commandLine, ParseResult parseResult) {
      commandLine.getErr().println(commandLine.getColorScheme().errorText(ex.getMessage()));
      return commandLine.getExitCodeExceptionMapper() != null
          ? commandLine.getExitCodeExceptionMapper().getExitCode(ex)
          : commandLine.getCommandSpec().exitCodeOnExecutionException();
    }
  }

  static final class ConvertAppException extends RuntimeException {
    ConvertAppException(Throwable e) {
      super(e);
    }
  }
}
