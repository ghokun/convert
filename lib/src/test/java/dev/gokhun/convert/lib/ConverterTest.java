package dev.gokhun.convert.lib;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

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
}
