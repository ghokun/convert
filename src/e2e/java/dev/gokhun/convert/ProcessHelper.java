package dev.gokhun.convert;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.ImmutableList;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Objects;

final class ProcessHelper {
  static final String RESOURCES_DIR = "./src/e2e/resources/dev/gokhun/convert";

  private ProcessHelper() {}

  static ProcessResult runCommand(String command, String... args) {
    ProcessBuilder processBuilder = new ProcessBuilder()
        .redirectErrorStream(true)
        .command(ImmutableList.<String>builder()
            .add(command)
            .addAll(Arrays.stream(args)
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(toImmutableList()))
            .build())
        .directory(new File(RESOURCES_DIR));
    try {
      Process process = processBuilder.start();
      return new ProcessResult(
          process.waitFor(), clearAnsiFormatting(readOutput(process.getInputStream())));
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  record ProcessResult(int exitCode, String output) {}

  private static String readOutput(InputStream inputStream) {
    StringBuilder sb = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, UTF_8))) {
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line).append(System.lineSeparator());
      }
      return sb.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static String clearAnsiFormatting(String coloredText) {
    return coloredText.replaceAll("\\e\\[[\\d;]*[^\\d;]", "");
  }
}
