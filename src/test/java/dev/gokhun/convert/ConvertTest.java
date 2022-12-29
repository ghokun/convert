package dev.gokhun.convert;

import static com.google.common.io.Files.getFileExtension;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static picocli.CommandLine.ExitCode.OK;
import static picocli.CommandLine.ExitCode.USAGE;

import com.google.common.collect.ImmutableSet;
import dev.gokhun.convert.Convert.ExecutionExceptionHandler;
import dev.gokhun.convert.Convert.SystemManager;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
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
    private static final String SEMVER_REGEX =
            "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$";

    @DisplayName("Should get version")
    @Test
    void version1() {
        var systemManager = new MockSystemManager();
        systemManager.exit(
                new CommandLine(new Convert())
                        .setOut(systemManager.getOut())
                        .setErr(systemManager.getErr())
                        .setExecutionExceptionHandler(new ExecutionExceptionHandler())
                        .execute("-V"));

        assertThat(systemManager.getExitStatus()).isEqualTo(OK);
        assertThat(systemManager.getOutput().trim()).matches(SEMVER_REGEX);
        assertThat(systemManager.getError()).isEmpty();
    }

    @DisplayName("Should show help when no argument is provided")
    @Test
    void help1() {
        var systemManager = new MockSystemManager();
        systemManager.exit(
                new CommandLine(new Convert())
                        .setOut(systemManager.getOut())
                        .setErr(systemManager.getErr())
                        .setExecutionExceptionHandler(new ExecutionExceptionHandler())
                        .execute());

        assertThat(systemManager.getExitStatus()).isEqualTo(USAGE);
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
    static final class ConversionTests {
        @TempDir File outputDirectory;

        @DisplayName("Should convert from input to output correctly")
        @MethodSource("inputOutputProvider")
        @ParameterizedTest
        void conversion1(String input, String output, String expected) {
            var outputPath = outputDirectory.getAbsolutePath() + output;
            var systemManager = new MockSystemManager();
            systemManager.exit(
                    new CommandLine(new Convert())
                            .setOut(systemManager.getOut())
                            .setErr(systemManager.getErr())
                            .setExecutionExceptionHandler(new ExecutionExceptionHandler())
                            .execute("-i", getTestResourcePath(input), "-o", outputPath));

            assertThat(systemManager.getExitStatus()).isEqualTo(OK);
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
    static final class JsonTests {
        @TempDir File outputDirectory;

        @DisplayName("Should prettify mini JSON")
        @Test
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

            assertThat(systemManager.getExitStatus()).isEqualTo(OK);
            assertThat(new File(output)).hasSameTextualContentAs(new File(expected));
        }

        @DisplayName("Should minimize pretty JSON")
        @Test
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

            assertThat(systemManager.getExitStatus()).isEqualTo(OK);
            assertThat(new File(output)).hasSameTextualContentAs(new File(expected));
        }
    }

    static final class MockSystemManager implements SystemManager {
        private final ByteArrayOutputStream out = new ByteArrayOutputStream();
        private final ByteArrayOutputStream err = new ByteArrayOutputStream();
        private int exitStatus;

        @Override
        public PrintWriter getOut() {
            return new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, UTF_8)));
        }

        @Override
        public PrintWriter getErr() {
            return new PrintWriter(new BufferedWriter(new OutputStreamWriter(err, UTF_8)));
        }

        @Override
        public void exit(int status) {
            exitStatus = status;
        }

        public int getExitStatus() {
            return exitStatus;
        }

        public String getOutput() {
            return out.toString(UTF_8);
        }

        public String getError() {
            return err.toString(UTF_8);
        }
    }

    private static String getTestResourcePath(String file) {
        return "src/test/resources/" + file;
    }
}
