package dev.gokhun.convert;

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

    FileUtil() {}

    enum FileType {
        CSV(CsvMapper::new, ImmutableSet.of("csv")),
        JSON(JsonMapper::new, ImmutableSet.of("json")),
        PROPERTIES(JavaPropsMapper::new, ImmutableSet.of("properties")),
        TOML(TomlMapper::new, ImmutableSet.of("toml")),
        YAML(FileType::yamlMapper, ImmutableSet.of("yaml", "yml"));

        private final Supplier<ObjectMapper> mapperSupplier;
        private final ImmutableSet<String> extensions;

        FileType(Supplier<ObjectMapper> mapperSupplier, ImmutableSet<String> extensions) {
            this.mapperSupplier = mapperSupplier;
            this.extensions = extensions;
        }

        static ObjectMapper mapperForFileType(String fileExtension) {
            if (fileExtension == null || fileExtension.isBlank()) {
                throw new IllegalArgumentException("File type could not be determined!");
            }
            return Arrays.stream(values())
                    .filter(f -> f.extensions.contains(fileExtension.toLowerCase(Locale.ENGLISH)))
                    .findAny()
                    .orElseThrow(
                            () ->
                                    new IllegalArgumentException(
                                            String.format(
                                                    "Unsupported file type! [%s]", fileExtension)))
                    .mapperSupplier
                    .get();
        }

        private static ObjectMapper yamlMapper() {
            return new YAMLMapper()
                    .configure(YAMLGenerator.Feature.INDENT_ARRAYS, true)
                    .configure(YAMLGenerator.Feature.INDENT_ARRAYS_WITH_INDICATOR, true)
                    .configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true);
        }
    }

    record ConversionOptions(String separator, boolean pretty) {
        ConversionOptions {
            requireNonNull(separator);
        }
    }

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
