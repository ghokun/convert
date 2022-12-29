package dev.gokhun.convert;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.io.Files.getFileExtension;
import static dev.gokhun.convert.FileUtil.FileType.mapperForFileType;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Supplier;

final class FileUtil {

    private FileUtil() {}

    enum FileType {
        CSV(ImmutableSet.of("csv")) {
            @Override
            Supplier<ObjectMapper> supplyMapper() {
                return CsvMapper::new;
            }
        },
        JSON(ImmutableSet.of("json")) {
            @Override
            Supplier<ObjectMapper> supplyMapper() {
                return JsonMapper::new;
            }
        },
        PROPERTIES(ImmutableSet.of("properties")) {
            @Override
            Supplier<ObjectMapper> supplyMapper() {
                return JavaPropsMapper::new;
            }
        },
        TOML(ImmutableSet.of("toml")) {
            @Override
            Supplier<ObjectMapper> supplyMapper() {
                return TomlMapper::new;
            }
        },
        YAML(ImmutableSet.of("yaml", "yml")) {
            @Override
            Supplier<ObjectMapper> supplyMapper() {
                return () ->
                        // TODO get these from command line
                        new YAMLMapper()
                                .configure(YAMLGenerator.Feature.INDENT_ARRAYS, true)
                                .configure(YAMLGenerator.Feature.INDENT_ARRAYS_WITH_INDICATOR, true)
                                .configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true);
            }
        };

        private final ImmutableSet<String> extensions;

        FileType(ImmutableSet<String> extensions) {
            this.extensions = extensions;
        }

        abstract Supplier<ObjectMapper> supplyMapper();

        static ObjectMapper mapperForFileType(String fileExtension) {
            checkArgument(
                    fileExtension != null && !fileExtension.isBlank(),
                    "File type could not be determined!");
            return Arrays.stream(values())
                    .filter(f -> f.extensions.contains(fileExtension.toLowerCase(Locale.ENGLISH)))
                    .findAny()
                    .orElseThrow(
                            () ->
                                    new IllegalArgumentException(
                                            String.format(
                                                    "Unsupported file type! [%s]", fileExtension)))
                    .supplyMapper()
                    .get();
        }
    }

    record ConversionOptions(String separator, boolean pretty) {
        ConversionOptions {
            requireNonNull(separator);
        }
    }

    // TODO Just a dummy implementation for now. Consider using java.nio.
    static void convert(File input, File output, ConversionOptions options) throws IOException {
        requireNonNull(input);
        requireNonNull(output);

        var inputMapper = mapperForFileType(getFileExtension(input.getName()));
        var outputMapper = mapperForFileType(getFileExtension(output.getName()));

        var data = inputMapper.readTree(input);

        var writer =
                options.pretty()
                        ? outputMapper.writerWithDefaultPrettyPrinter()
                        : outputMapper.writer();
        writer.writeValue(output, data);
    }
}
