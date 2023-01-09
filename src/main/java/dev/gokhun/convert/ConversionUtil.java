package dev.gokhun.convert;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.io.Files.getFileExtension;
import static dev.gokhun.convert.ConversionUtil.FileType.fromFileExtension;
import static java.lang.Character.isSpaceChar;
import static java.lang.Character.isWhitespace;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import org.yaml.snakeyaml.Yaml;

final class ConversionUtil {
    private ConversionUtil() {}

    private interface Reader {
        JsonNode read(File file) throws IOException;
    }

    private interface Writer {
        void write(File file, JsonNode jsonNode) throws IOException;
    }

    enum FileType {
        CSV(ImmutableSet.of("csv")) {
            private static final CsvMapper mapper = new CsvMapper();

            @Override
            Reader reader(ConversionOptions options) {
                return file ->
                        mapper.readerFor(JsonNode.class)
                                .with(
                                        CsvSchema.emptySchema()
                                                .withColumnSeparator(options.csvSeparator())
                                                .withHeader())
                                .readValue(file);
            }

            @Override
            Writer writer(ConversionOptions options) {
                return (file, jsonNode) -> {
                    var csvSchemaBuilder = CsvSchema.builder();
                    var firstObject =
                            jsonNode instanceof ArrayNode ? jsonNode.elements().next() : jsonNode;
                    firstObject.fieldNames().forEachRemaining(csvSchemaBuilder::addColumn);
                    mapper.writerFor(JsonNode.class)
                            .with(
                                    csvSchemaBuilder
                                            .build()
                                            .withColumnSeparator(options.csvSeparator())
                                            .withHeader())
                            .writeValue(file, jsonNode);
                };
            }
        },
        JSON(ImmutableSet.of("json")) {
            private static final JsonMapper mapper = new JsonMapper();

            @Override
            Reader reader(ConversionOptions options) {
                return mapper::readTree;
            }

            @Override
            Writer writer(ConversionOptions options) {
                return (file, jsonNode) ->
                        (options.pretty()
                                        ? mapper.writerWithDefaultPrettyPrinter()
                                        : mapper.writer())
                                .writeValue(file, jsonNode);
            }
        },
        PROPERTIES(ImmutableSet.of("properties")) {
            private static final JavaPropsMapper mapper = new JavaPropsMapper();

            @Override
            Reader reader(ConversionOptions options) {
                return mapper::readTree;
            }

            @Override
            Writer writer(ConversionOptions options) {
                return mapper::writeValue;
            }
        },
        TOML(ImmutableSet.of("toml")) {

            private static final TomlMapper mapper = new TomlMapper();

            @Override
            Reader reader(ConversionOptions options) {
                return mapper::readTree;
            }

            @Override
            Writer writer(ConversionOptions options) {
                return mapper::writeValue;
            }
        },
        YAML(ImmutableSet.of("yaml", "yml")) {
            private static final YAMLMapper mapper = new YAMLMapper();

            @Override
            Reader reader(ConversionOptions options) {
                return file -> mapper.valueToTree(new Yaml().load(new FileInputStream(file)));
            }

            @Override
            Writer writer(ConversionOptions options) {
                return (file, jsonNode) ->
                        mapper.configure(YAMLGenerator.Feature.INDENT_ARRAYS, options.indentYaml())
                                .configure(
                                        YAMLGenerator.Feature.INDENT_ARRAYS_WITH_INDICATOR,
                                        options.indentYaml())
                                .configure(
                                        YAMLGenerator.Feature.MINIMIZE_QUOTES,
                                        options.minimizeYamlQuotes())
                                .writeValue(file, jsonNode);
            }
        };

        private final ImmutableSet<String> extensions;

        FileType(ImmutableSet<String> extensions) {
            this.extensions = extensions;
        }

        abstract Reader reader(ConversionOptions options);

        abstract Writer writer(ConversionOptions options);

        static FileType fromFileExtension(String fileExtension) {
            checkArgument(
                    fileExtension != null && !fileExtension.isBlank(),
                    "File type could not be determined!");
            return Arrays.stream(values())
                    .filter(f -> f.extensions.contains(fileExtension.toLowerCase(Locale.ENGLISH)))
                    .findAny()
                    .orElseThrow(
                            () ->
                                    new IllegalArgumentException(
                                            String.format(
                                                    "Unsupported file type! [%s]", fileExtension)));
        }
    }

    record ConversionOptions(
            char csvSeparator, boolean pretty, boolean indentYaml, boolean minimizeYamlQuotes) {
        ConversionOptions {
            checkArgument(
                    !isWhitespace(csvSeparator) && !isSpaceChar(csvSeparator),
                    "CSV separator can not be blank or whitespace!");
        }

        static Builder builder() {
            return new Builder();
        }

        static final class Builder {
            private char csvSeparator;
            private boolean pretty;
            private boolean indentYaml;
            private boolean minimizeYamlQuotes;

            private Builder() {}

            Builder setCsvSeparator(char csvSeparator) {
                this.csvSeparator = csvSeparator;
                return this;
            }

            Builder setPretty(boolean pretty) {
                this.pretty = pretty;
                return this;
            }

            Builder setIndentYaml(boolean indentYaml) {
                this.indentYaml = indentYaml;
                return this;
            }

            Builder setMinimizeYamlQuotes(boolean minimizeYamlQuotes) {
                this.minimizeYamlQuotes = minimizeYamlQuotes;
                return this;
            }

            ConversionOptions build() {
                return new ConversionOptions(
                        this.csvSeparator, this.pretty, this.indentYaml, this.minimizeYamlQuotes);
            }
        }
    }

    // TODO Just a dummy implementation for now. Consider using java.nio.
    static void convert(File input, File output, ConversionOptions options) throws IOException {
        requireNonNull(input);
        requireNonNull(output);

        var reader = fromFileExtension(getFileExtension(input.getName())).reader(options);
        var writer = fromFileExtension(getFileExtension(output.getName())).writer(options);

        var data = reader.read(input);
        writer.write(output, data);
    }
}
