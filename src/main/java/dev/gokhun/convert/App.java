package dev.gokhun.convert;

import static picocli.CommandLine.Help.Ansi.ON;
import static picocli.CommandLine.Help.defaultColorScheme;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.Option;

public class App {

    private static ColorScheme colorScheme = defaultColorScheme(ON);

    @Command(name = "convert", helpCommand = true)
    static class Convert implements Runnable {

        @Option(names = {"-i", "--input"})
        File input;

        @Option(names = {"-o", "--output"})
        File output;

        @Override
        public void run() {
            try {
                var inputMapper = getMapper(input);
                var outputMapper = getMapper(output);
                var tree = inputMapper.readTree(input);
                outputMapper.writeValue(output, tree);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private static ObjectMapper getMapper(File file) {
            return switch (Files.getFileExtension(file.getName())) {
                case "json" -> new JsonMapper();
                case "yaml" -> new YAMLMapper();
                case "toml" -> new TomlMapper();
                case "csv" -> new CsvMapper();
                case "properties" -> new JavaPropsMapper();
                default -> throw new IllegalArgumentException("Unsupported file type!");
            };
        }
    }

    public static void main(String... args) {
        var cmd = new CommandLine(new Convert());
        if (args.length == 0) {
            cmd.usage(System.out, colorScheme);
        } else {
            cmd.setColorScheme(colorScheme).execute(args);
        }
    }
}
