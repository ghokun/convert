package dev.gokhun.convert.lib;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class ConverterTest {
  @TempDir
  File outputDirectory;

  @DisplayName("Should convert through public interface without touching internals")
  @Test
  void convertThroughInterface() throws Exception {
    File input = new File(outputDirectory, "input.json");
    File output = new File(outputDirectory, "output.json");
    Files.writeString(input.toPath(), "{\"key\":\"value\"}", UTF_8);

    Converter.create()
        .convert(
            input,
            output,
            ConverterOptions.builder()
                .csvSeparator(',')
                .pretty(false)
                .indentYaml(true)
                .minimizeYamlQuotes(true)
                .deduplicateKeys(false)
                .build());

    assertThat(output).exists().isFile();
    assertThat(output.toPath()).content(UTF_8).isEqualTo("{\"key\":\"value\"}");
  }

  @DisplayName("Should convert through streams without touching the file system")
  @Test
  void convertThroughStreams() throws Exception {
    var input = new ByteArrayInputStream("{\"key\":\"value\"}".getBytes(UTF_8));
    var output = new ByteArrayOutputStream();

    Converter.create()
        .convert(
            input,
            "json",
            output,
            "json",
            ConverterOptions.builder()
                .csvSeparator(',')
                .pretty(false)
                .indentYaml(true)
                .minimizeYamlQuotes(true)
                .deduplicateKeys(false)
                .build());

    assertThat(output.toString(UTF_8)).isEqualTo("{\"key\":\"value\"}");
  }

  @DisplayName("Should convert across formats through streams")
  @Test
  void convertAcrossFormatsThroughStreams() throws Exception {
    var input = new ByteArrayInputStream("name,age\nAda,36\n".getBytes(UTF_8));
    var output = new ByteArrayOutputStream();

    Converter.create()
        .convert(
            input,
            "csv",
            output,
            "json",
            ConverterOptions.builder()
                .csvSeparator(',')
                .pretty(false)
                .indentYaml(true)
                .minimizeYamlQuotes(true)
                .deduplicateKeys(false)
                .build());

    assertThat(output.toString(UTF_8)).isEqualTo("[{\"name\":\"Ada\",\"age\":\"36\"}]");
  }
}
