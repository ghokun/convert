package dev.gokhun.convert;

import static dev.gokhun.convert.FileUtil.convert;
import static picocli.CommandLine.ExitCode.OK;
import static picocli.CommandLine.Help.Ansi.ON;
import static picocli.CommandLine.Help.defaultColorScheme;

import dev.gokhun.convert.FileUtil.ConversionOptions;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.Option;

@Command(
        exitCodeOnInvalidInput = OK,
        mixinStandardHelpOptions = true,
        name = "convert",
        sortOptions = false,
        usageHelpWidth = 120)
public class Convert implements Callable<Integer> {
    private static final ColorScheme colorScheme = defaultColorScheme(ON);

    @Option(
            names = {"-i", "--input"},
            required = true,
            order = 1,
            description = "File to convert from")
    File input;

    @Option(
            names = {"-o", "--output"},
            required = true,
            order = 2,
            description = "File to convert into")
    File output;

    @Option(
            names = {"-s", "--separator"},
            order = 3,
            defaultValue = ",",
            description = "Character(s) to separate CSV columns. Default value is ','.")
    String separator;

    @Option(
            names = {"--pretty"},
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
        private final PrintWriter out = new PrintWriter(System.out);
        private final PrintWriter err = new PrintWriter(System.err);

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
