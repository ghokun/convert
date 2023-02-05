package dev.gokhun.convert;

import static com.google.common.io.Files.getFileExtension;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static picocli.CommandLine.ExitCode.OK;
import static picocli.CommandLine.ExitCode.USAGE;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
import picocli.CommandLine.IExecutionExceptionHandler;

final class ConvertTest {
    private static final IExecutionExceptionHandler exceptionHandler =
            new ExecutionExceptionHandler();
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
                        .setExecutionExceptionHandler(exceptionHandler)
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
                        .setExecutionExceptionHandler(exceptionHandler)
                        .execute());

        assertThat(systemManager.getExitStatus()).isEqualTo(USAGE);
        assertThat(systemManager.getOutput()).isEmpty();
        assertThat(systemManager.getError())
                .isEqualToNormalizingNewlines(
                        """
Missing required options: '--input=<input>', '--output=<output>'
Usage: convert [-hV] [--deduplicate-keys] [--indent-yaml] [--minimize-yaml-quotes] [--pretty] -f=<input> -o=<output>
               [-s=<csvSeparator>]
Converts one file type to another.
  -h, --help               Show this help message and exit.
  -V, --version            Print version information and exit.
  -f, -i, --from, --input=<input>
                           File to convert from.
  -o, -t, --to, --output=<output>
                           File to convert into.
  -s, --csv-separator=<csvSeparator>
                           Character(s) to separate CSV columns. Default value is ','.
      --pretty             Prettify output if possible. Default is false and output is minimized.
      --indent-yaml        Indents YAML array fields. Default is true.
      --minimize-yaml-quotes
                           Minimizes YAML quotes if possible. Default is true.
      --deduplicate-keys   Think csv but as json. Only available on conversions where the initial source is array. Keys
                             are arrays and values are arrays of arrays.
""");
    }

    @SuppressWarnings("ClassCanBeStatic")
    @Nested
    final class ConversionTests {
        @TempDir File outputDirectory;

        @DisplayName("Should convert from input to output correctly")
        @MethodSource({
            "fromJsonProvider",
            "fromPropertiesProvider",
            "fromTomlProvider",
            "fromYamlProvider",
            "minimalInputProvider"
        })
        @ParameterizedTest
        void conversion1(String input, String output, String expected) {
            var outputPath = outputDirectory.getAbsolutePath() + output;
            var systemManager = new MockSystemManager();
            systemManager.exit(
                    new CommandLine(new Convert())
                            .setOut(systemManager.getOut())
                            .setErr(systemManager.getErr())
                            .setExecutionExceptionHandler(exceptionHandler)
                            .execute("-i", getTestResourcePath(input), "-o", outputPath));

            assertThat(systemManager.getOutput()).isEmpty();
            assertThat(systemManager.getError()).isEmpty();
            assertThat(systemManager.getExitStatus()).isEqualTo(OK);
            assertThat(new File(outputPath))
                    .hasSameTextualContentAs(new File(getTestResourcePath(expected)));
        }

        private static Stream<Arguments> minimalInputProvider() {
            var inputs =
                    ImmutableSet.of(
                            "json/mini1.json",
                            "properties/mini1.properties",
                            "toml/mini1.toml",
                            "yaml/mini1.yaml");
            return inputs.stream()
                    .flatMap(input -> inputs.stream().map(output -> toArguments(input, output)));
        }

        private static Stream<Arguments> fromJsonProvider() {
            var outputs =
                    ImmutableSet.of(
                            "properties/fromjson.properties",
                            "toml/fromjson.toml",
                            "yaml/fromjson.yaml");

            return outputs.stream().map(output -> toArguments("json/fromjson.json", output));
        }

        private static Stream<Arguments> fromPropertiesProvider() {
            var outputs =
                    ImmutableSet.of(
                            "json/fromproperties.json",
                            "toml/fromproperties.toml",
                            "yaml/fromproperties.yaml");

            return outputs.stream()
                    .map(output -> toArguments("properties/fromproperties.properties", output));
        }

        private static Stream<Arguments> fromTomlProvider() {
            var outputs =
                    ImmutableSet.of(
                            "json/fromtoml.json",
                            "properties/fromtoml.properties",
                            "yaml/fromtoml.yaml");

            return outputs.stream().map(output -> toArguments("toml/fromtoml.toml", output));
        }

        private static Stream<Arguments> fromYamlProvider() {
            var outputs =
                    ImmutableSet.of(
                            "json/fromyaml.json",
                            "properties/fromyaml.properties",
                            "toml/fromyaml.toml");

            return outputs.stream().map(output -> toArguments("yaml/fromyaml.yaml", output));
        }
    }

    @SuppressWarnings("ClassCanBeStatic")
    @Nested
    final class JsonTests {
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
                            .setExecutionExceptionHandler(exceptionHandler)
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
                            .setExecutionExceptionHandler(exceptionHandler)
                            .execute("-i", input, "-o", output));

            assertThat(systemManager.getExitStatus()).isEqualTo(OK);
            assertThat(new File(output)).hasSameTextualContentAs(new File(expected));
        }
    }

    @SuppressWarnings("ClassCanBeStatic")
    @Nested
    final class DeduplicationTests {
        @TempDir File outputDirectory;

        @DisplayName("Should deduplicate correctly")
        @MethodSource("deduplicateProvider")
        @ParameterizedTest
        void deduplicate1(String input, String output, String expected) {
            var outputPath = outputDirectory.getAbsolutePath() + output;
            var systemManager = new MockSystemManager();
            systemManager.exit(
                    new CommandLine(new Convert())
                            .setOut(systemManager.getOut())
                            .setErr(systemManager.getErr())
                            .setExecutionExceptionHandler(exceptionHandler)
                            .execute(
                                    "-i",
                                    getTestResourcePath(input),
                                    "-o",
                                    outputPath,
                                    "--pretty",
                                    "--deduplicate-keys"));

            assertThat(systemManager.getOutput()).isEmpty();
            assertThat(systemManager.getError()).isEmpty();
            assertThat(systemManager.getExitStatus()).isEqualTo(OK);
            assertThat(new File(outputPath))
                    .hasSameTextualContentAs(new File(getTestResourcePath(expected)));
        }

        private static Stream<Arguments> deduplicateProvider() {
            var inputs =
                    ImmutableSet.of(
                            "csv/deduplicatefrom.csv",
                            "json/deduplicatefrom.json",
                            "yaml/deduplicatefrom.yaml");

            return inputs.stream().map(input -> toArguments(input, "json/deduplicateto.json"));
        }
    }

    @SuppressWarnings("ClassCanBeStatic")
    @Nested
    final class CsvTests {
        @TempDir File outputDirectory;

        @DisplayName("Should convert csv correctly")
        @MethodSource("csvProvider")
        @ParameterizedTest
        void csv1(String input, String output, String expected, boolean dedup) {
            var outputPath = outputDirectory.getAbsolutePath() + output;
            var systemManager = new MockSystemManager();
            var args =
                    ImmutableList.<String>builder()
                            .add("-i", getTestResourcePath(input), "-o", outputPath, "--pretty");
            if (dedup) {
                args.add("--deduplicate-keys");
            }
            systemManager.exit(
                    new CommandLine(new Convert())
                            .setOut(systemManager.getOut())
                            .setErr(systemManager.getErr())
                            .setExecutionExceptionHandler(exceptionHandler)
                            .execute(args.build().toArray(String[]::new)));

            assertThat(systemManager.getOutput()).isEmpty();
            assertThat(systemManager.getError()).isEmpty();
            assertThat(systemManager.getExitStatus()).isEqualTo(OK);
            assertThat(new File(outputPath))
                    .hasSameTextualContentAs(new File(getTestResourcePath(expected)));
        }

        private static Stream<Arguments> csvProvider() {
            return ImmutableMap.of("json/oscars.json", false, "json/oscars-dedup.json", true)
                    .entrySet()
                    .stream()
                    .map(
                            output ->
                                    arguments(
                                            "csv/oscars.csv",
                                            "/actual." + getFileExtension(output.getKey()),
                                            output.getKey(),
                                            output.getValue()));
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

    private static Arguments toArguments(String input, String output) {
        return arguments(input, "/actual." + getFileExtension(output), output);
    }
}
