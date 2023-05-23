package dev.gokhun.convert;

import static dev.gokhun.convert.ConversionUtil.ConversionOptions;
import static dev.gokhun.convert.ConversionUtil.FileType;
import static dev.gokhun.convert.ConversionUtil.FileType.CSV;
import static dev.gokhun.convert.ConversionUtil.FileType.JSON;
import static dev.gokhun.convert.ConversionUtil.FileType.PROPERTIES;
import static dev.gokhun.convert.ConversionUtil.FileType.TOML;
import static dev.gokhun.convert.ConversionUtil.FileType.YAML;
import static java.lang.Character.LINE_SEPARATOR;
import static java.lang.Character.SPACE_SEPARATOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.common.collect.ImmutableSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

final class ConversionUtilTest {

  @DisplayName("Should return correct type for file extension")
  @MethodSource("validFileExtensionProvider")
  @ParameterizedTest
  void mapperForFileType1(FileType fileType, ImmutableSet<String> fileExtensions) {
    for (String fileExtension : fileExtensions) {
      assertThat(FileType.fromFileExtension(fileExtension)).isEqualTo(fileType);
    }
  }

  @DisplayName("Should throw exception on invalid file extensions")
  @MethodSource("invalidFileExtensionProvider")
  @ParameterizedTest
  void fromFileExtension1(
      String fileExtension, Class<? extends Throwable> throwable, String message) {
    assertThatThrownBy(() -> FileType.fromFileExtension(fileExtension))
        .isInstanceOf(throwable)
        .satisfies(ex -> assertThat(ex.getMessage()).isEqualTo(message));
  }

  @DisplayName("Should throw exception on invalid csv separator")
  @ParameterizedTest
  @ValueSource(chars = {LINE_SEPARATOR, SPACE_SEPARATOR})
  void invalidCsvSeparator1(char csvSeparator) {
    assertThatThrownBy(
            () -> ConversionOptions.builder().setCsvSeparator(csvSeparator).build())
        .isInstanceOfSatisfying(IllegalArgumentException.class, ex -> assertThat(ex.getMessage())
            .isEqualTo("CSV separator can not be blank or whitespace!"));
  }

  private static Stream<Arguments> validFileExtensionProvider() {
    return Stream.of(
        arguments(CSV, ImmutableSet.of("CSV", "csv", "cSV", "csV")),
        arguments(JSON, ImmutableSet.of("json", "JSON", "jSoN", "JsOn")),
        arguments(PROPERTIES, ImmutableSet.of("properties", "PROPERTIES", "propertIes")),
        arguments(TOML, ImmutableSet.of("toml", "TOML", "toMl", "tomL")),
        arguments(YAML, ImmutableSet.of("yml", "YAML", "yaml", "YML")));
  }

  private static Stream<Arguments> invalidFileExtensionProvider() {
    var fileTypeMessage = "File type could not be determined!";
    return Stream.of(
        arguments(null, IllegalArgumentException.class, fileTypeMessage),
        arguments("", IllegalArgumentException.class, fileTypeMessage),
        arguments("   ", IllegalArgumentException.class, fileTypeMessage),
        arguments("cs", IllegalArgumentException.class, "Unsupported file type! [cs]"),
        arguments(".csv", IllegalArgumentException.class, "Unsupported file type! [.csv]"),
        arguments("jsonp", IllegalArgumentException.class, "Unsupported file type! [jsonp]"),
        arguments(
            "PROPERTİES", IllegalArgumentException.class, "Unsupported file type! [PROPERTİES]"),
        arguments("tomI", IllegalArgumentException.class, "Unsupported file type! [tomI]"),
        arguments("yamI", IllegalArgumentException.class, "Unsupported file type! [yamI]"));
  }
}
