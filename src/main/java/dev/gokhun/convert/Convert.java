package dev.gokhun.convert;

import static dev.gokhun.convert.FileUtil.convert;
import static java.nio.charset.StandardCharsets.UTF_8;
import static picocli.CommandLine.ExitCode.OK;
import static picocli.CommandLine.Help.Ansi.ON;
import static picocli.CommandLine.Help.defaultColorScheme;

import dev.gokhun.convert.FileUtil.ConversionOptions;
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

@Command(
        mixinStandardHelpOptions = true,
        name = "convert",
        description = "Converts one file type to another.",
        sortOptions = false,
        usageHelpWidth = 120,
        versionProvider = VersionProvider.class)
public final class Convert implements Callable<Integer> {
    private static final ColorScheme colorScheme = defaultColorScheme(ON);

    @Option(
            names = {"--input", "-i"},
            required = true,
            order = 1,
            description = "File to convert from.")
    File input;

    @Option(
            names = {"--output", "-o"},
            required = true,
            order = 2,
            description = "File to convert into.")
    File output;

    @Option(
            names = {"--separator", "-s"},
            order = 3,
            defaultValue = ",",
            description = "Character(s) to separate CSV columns. Default value is ','.")
    String separator;

    @Option(
            names = "--pretty",
            order = 4,
            defaultValue = "false",
            description = "Prettify output if possible. Default is false and output is minimized.")
    boolean pretty;

    @Override
    public Integer call() {
        try {
            convert(input, output, new ConversionOptions(separator, pretty));
        } catch (IllegalArgumentException | IOException e) {
            throw new ConvertAppException(e);
        }
        return OK;
    }

    public static void main(String... args) {
        SystemManager systemManager = new DefaultSystemManager();
        systemManager.exit(
                new CommandLine(new Convert())
                        .setOut(systemManager.getOut())
                        .setErr(systemManager.getErr())
                        .setExecutionExceptionHandler(new ExecutionExceptionHandler())
                        .setColorScheme(colorScheme)
                        .execute(args));
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
                Exception ex, CommandLine commandLine, CommandLine.ParseResult parseResult) {
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
