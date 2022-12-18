package dev.gokhun.convert;

import static dev.gokhun.convert.FileUtil.convert;
import static picocli.CommandLine.ExitCode.OK;
import static picocli.CommandLine.ExitCode.SOFTWARE;
import static picocli.CommandLine.Help.Ansi.ON;
import static picocli.CommandLine.Help.defaultColorScheme;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.Option;

public class App {

    private static final ColorScheme colorScheme = defaultColorScheme(ON);
    private static final PrintStream out = System.out;
    private static final PrintStream err = System.err;

    @Command(name = "convert", helpCommand = true, sortOptions = false)
    static class Convert implements Callable<Integer> {

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

        /*@Option(
                names = {"-s", "--separator"},
                required = false,
                order = 3,
                defaultValue = ",",
                description = "Character or text to separate CSV columns")
        String separator;*/

        @Option(
                names = {"--keep-order"},
                required = false,
                order = 4,
                defaultValue = "false",
                description = "Whether to keep order or not")
        boolean keepOrder;

        @Option(
                names = {"--pretty"},
                required = false,
                order = 5,
                defaultValue = "false",
                description = "Write pretty JSON")
        boolean pretty;

        @Override
        public Integer call() {
            try {
                convert(input, output);
            } catch (IllegalArgumentException | IOException e) {
                err.println(e.getMessage());
                return SOFTWARE;
            }
            return OK;
        }
    }

    public static void main(String... args) {
        var cmd = new CommandLine(new Convert()).setColorScheme(colorScheme);
        System.exit(args.length > 0 ? cmd.execute(args) : printUsage(cmd));
    }

    private static int printUsage(CommandLine cmd) {
        cmd.usage(out);
        return OK;
    }
}
