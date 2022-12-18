package dev.gokhun.convert;

import static com.google.common.io.Files.getFileExtension;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

final class FileUtil {

    FileUtil() {}

    enum FileType {
        // CSV(CsvMapper::new), TODO add CSV support
        JSON(JsonMapper::new),
        PROPERTIES(JavaPropsMapper::new),
        TOML(TomlMapper::new),
        YAML(YAMLMapper::new);

        private final Supplier<ObjectMapper> mapperSupplier;

        FileType(Supplier<ObjectMapper> mapperSupplier) {
            this.mapperSupplier = mapperSupplier;
        }

        static ObjectMapper fromFileExtension(String fileExtension) {
            return stream(FileType.values())
                    .filter(f -> f.name().equals(fileExtension.toUpperCase()))
                    .findAny()
                    .orElseThrow(
                            () ->
                                    new IllegalArgumentException(
                                            String.format(
                                                    "Unsupported file type! [%s]", fileExtension)))
                    .mapperSupplier
                    .get();
        }
    }

    static void convert(File input, File output) throws IOException {
        requireNonNull(input);
        requireNonNull(output);

        var inputMapper = FileType.fromFileExtension(getFileExtension(input.getName()));
        var outputMapper = FileType.fromFileExtension(getFileExtension(output.getName()));

        var data = inputMapper.readTree(input);
        outputMapper.writeValue(output, data);
    }
}
