package dev.gokhun.convert;

import static dev.gokhun.convert.FileUtil.ConversionOptions;
import static dev.gokhun.convert.FileUtil.FileType;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.collect.ImmutableSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class FileUtilTest {

    @ParameterizedTest
    @MethodSource("validFileExtensionProvider")
    @DisplayName("Should return correct mapper for file extension")
    void mapperForFileType1(
            Class<? extends ObjectMapper> expectedMapper, ImmutableSet<String> fileExtensions) {
        for (String fileExtension : fileExtensions) {
            var actualMapper = FileType.mapperForFileType(fileExtension);
            assertInstanceOf(expectedMapper, actualMapper);
        }
    }

    @ParameterizedTest
    @MethodSource("invalidFileExtensionProvider")
    @DisplayName("Should throw exception on invalid file extensions")
    void mapperForFileType1(
            String fileExtension, Class<? extends Throwable> exception, String message) {
        assertThrows(exception, () -> FileType.mapperForFileType(fileExtension), message);
    }

    @Test
    @DisplayName("Should throw exception on empty separator")
    void conversionOptions1() {
        assertThrows(NullPointerException.class, () -> new ConversionOptions(null, false));
    }

    private static Stream<Arguments> validFileExtensionProvider() {
        return Stream.of(
                arguments(CsvMapper.class, ImmutableSet.of("CSV", "csv", "cSV", "csV")),
                arguments(JsonMapper.class, ImmutableSet.of("json", "JSON", "jSoN", "JsOn")),
                arguments(
                        JavaPropsMapper.class,
                        ImmutableSet.of("properties", "PROPERTIES", "propertIes")),
                arguments(TomlMapper.class, ImmutableSet.of("toml", "TOML", "toMl", "tomL")),
                arguments(YAMLMapper.class, ImmutableSet.of("yml", "YAML", "yaml", "YML")));
    }

    private static Stream<Arguments> invalidFileExtensionProvider() {
        var fileTypeMessage = "File type could not be determined!";
        return Stream.of(
                arguments(null, IllegalArgumentException.class, fileTypeMessage),
                arguments("", IllegalArgumentException.class, fileTypeMessage),
                arguments("   ", IllegalArgumentException.class, fileTypeMessage),
                arguments("cs", IllegalArgumentException.class, "Unsupported file type! [cs]"),
                arguments(".csv", IllegalArgumentException.class, "Unsupported file type! [.csv]"),
                arguments(
                        "jsonp", IllegalArgumentException.class, "Unsupported file type! [jsonp]"),
                arguments(
                        "PROPERTİES",
                        IllegalArgumentException.class,
                        "Unsupported file type! [PROPERTİES]"),
                arguments("tomI", IllegalArgumentException.class, "Unsupported file type! [tomI]"),
                arguments("yamI", IllegalArgumentException.class, "Unsupported file type! [yamI]"));
    }
}
