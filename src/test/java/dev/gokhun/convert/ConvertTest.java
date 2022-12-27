package dev.gokhun.convert;

import static com.google.common.io.Files.getFileExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static picocli.CommandLine.ExitCode.OK;

import com.google.common.collect.ImmutableSet;
import dev.gokhun.convert.Convert.ExecutionExceptionHandler;
import dev.gokhun.convert.Convert.SystemManager;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import picocli.CommandLine;

final class ConvertTest {

    @Test
    @DisplayName("Should show help when no argument is provided")
    void invalid1() {
        var systemManager = new MockSystemManager();
        systemManager.exit(
                new CommandLine(new Convert())
                        .setOut(systemManager.getOut())
                        .setErr(systemManager.getErr())
                        .setExecutionExceptionHandler(new ExecutionExceptionHandler())
                        .execute());

        assertThat(systemManager.getExitStatus()).isEqualTo(OK);
        assertThat(systemManager.getOutput()).isEmpty();
        assertThat(systemManager.getError())
                .isEqualTo(
                        """
Missing required options: '--input=<input>', '--output=<output>'
Usage: convert [-hV] [--pretty] -i=<input> -o=<output> [-s=<separator>]
Converts one file type to another.
  -h, --help              Show this help message and exit.
  -V, --version           Print version information and exit.
  -i, --input=<input>     File to convert from.
  -o, --output=<output>   File to convert into.
  -s, --separator=<separator>
                          Character(s) to separate CSV columns. Default value is ','.
      --pretty            Prettify output if possible. Default is false and output is minimized.
""");
    }

    @Nested
    class ConversionTests {
        @TempDir File outputDirectory;

        @ParameterizedTest
        @MethodSource("inputOutputProvider")
        @DisplayName("Should convert from input to output correctly")
        void conversion1(String input, String output, String expected) {
            var outputPath = outputDirectory.getAbsolutePath() + output;
            var systemManager = new MockSystemManager();
            systemManager.exit(
                    new CommandLine(new Convert())
                            .setOut(systemManager.getOut())
                            .setErr(systemManager.getErr())
                            .setExecutionExceptionHandler(new ExecutionExceptionHandler())
                            .execute("-i", getTestResourcePath(input), "-o", outputPath));

            assertThat(new File(outputPath))
                    .hasSameTextualContentAs(new File(getTestResourcePath(expected)));
        }

        private static Stream<Arguments> inputOutputProvider() {
            var inputs =
                    ImmutableSet.of(
                            "json/mini1.json",
                            "properties/mini1.properties",
                            "toml/mini1.toml",
                            "yaml/mini1.yaml");
            return inputs.stream()
                    .flatMap(input -> inputs.stream().map(output -> toArguments(input, output)));
        }

        private static Arguments toArguments(String input, String output) {
            return arguments(input, "/actual." + getFileExtension(output), output);
        }
    }

    @Nested
    class JsonTests {
        @TempDir File outputDirectory;

        @Test
        @DisplayName("Should prettify mini JSON")
        void fromJson1() {
            String input = getTestResourcePath("json/mini1.json");
            String output = outputDirectory.getAbsolutePath() + "/actual.json";
            String expected = getTestResourcePath("json/pretty1.json");

            var systemManager = new MockSystemManager();
            systemManager.exit(
                    new CommandLine(new Convert())
                            .setOut(systemManager.getOut())
                            .setErr(systemManager.getErr())
                            .setExecutionExceptionHandler(new ExecutionExceptionHandler())
                            .execute("-i", input, "-o", output, "--pretty"));

            assertThat(new File(output)).hasSameTextualContentAs(new File(expected));
        }

        @Test
        @DisplayName("Should minimize pretty JSON")
        void fromJson2() {
            String input = getTestResourcePath("json/pretty1.json");
            String output = outputDirectory.getAbsolutePath() + "/actual.json";
            String expected = getTestResourcePath("json/mini1.json");

            var systemManager = new MockSystemManager();
            systemManager.exit(
                    new CommandLine(new Convert())
                            .setOut(systemManager.getOut())
                            .setErr(systemManager.getErr())
                            .setExecutionExceptionHandler(new ExecutionExceptionHandler())
                            .execute("-i", input, "-o", output));

            assertThat(new File(output)).hasSameTextualContentAs(new File(expected));
        }
    }

    static final class MockSystemManager implements SystemManager {
        private final ByteArrayOutputStream out = new ByteArrayOutputStream();
        private final ByteArrayOutputStream err = new ByteArrayOutputStream();
        private int exitStatus;

        @Override
        public PrintWriter getOut() {
            return new PrintWriter(out);
        }

        @Override
        public PrintWriter getErr() {
            return new PrintWriter(err);
        }

        @Override
        public void exit(int status) {
            exitStatus = status;
        }

        public int getExitStatus() {
            return exitStatus;
        }

        public String getOutput() {
            return out.toString();
        }

        public String getError() {
            return err.toString();
        }
    }

    private static String getTestResourcePath(String file) {
        return "src/test/resources/" + file;
    }
}
