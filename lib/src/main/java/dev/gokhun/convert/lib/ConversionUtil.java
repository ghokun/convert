package dev.gokhun.convert.lib;

import static com.fasterxml.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY;
import static com.fasterxml.jackson.dataformat.csv.CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.INDENT_ARRAYS;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.INDENT_ARRAYS_WITH_INDICATOR;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.MINIMIZE_QUOTES;
import static dev.gokhun.convert.lib.ConversionUtil.FileType.fromFileExtension;
import static java.lang.Character.isSpaceChar;
import static java.lang.Character.isWhitespace;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Set;
import org.yaml.snakeyaml.Yaml;

final class ConversionUtil {
  // Shared CSV mapper to keep a single instance on the image heap.
  private static final CsvMapper CSV_MAPPER = new CsvMapper().enable(ALWAYS_QUOTE_STRINGS);
  private static final CsvSchema CSV_BASE_SCHEMA = CsvSchema.emptySchema().withHeader();
  private static final char HORIZONTAL_TABULATION = '\t';

  private ConversionUtil() {}

  interface Reader {
    JsonNode read(File file) throws IOException;
  }

  interface Writer {
    void write(File file, JsonNode jsonNode) throws IOException;
  }

  static String getFileExtension(String fileName) {
    var lastDot = fileName.lastIndexOf('.');
    if (lastDot == -1 || lastDot == fileName.length() - 1) {
      return "";
    }
    return fileName.substring(lastDot + 1);
  }

  @SuppressWarnings("ImmutableEnumChecker")
  enum FileType {
    CSV(Set.of("csv")) {
      @Override
      Reader reader(ConversionOptions options) {
        return file -> readDelimited(file, options.csvSeparator());
      }

      @Override
      Writer writer(ConversionOptions options) {
        return (file, jsonNode) -> writeDelimited(file, jsonNode, options.csvSeparator());
      }
    },
    TSV(Set.of("tsv")) {
      @Override
      Reader reader(ConversionOptions options) {
        return file -> readDelimited(file, HORIZONTAL_TABULATION);
      }

      @Override
      Writer writer(ConversionOptions options) {
        return (file, jsonNode) -> writeDelimited(file, jsonNode, HORIZONTAL_TABULATION);
      }
    },
    JSON(Set.of("json")) {
      private static final JsonMapper MAPPER = new JsonMapper();

      @Override
      Reader reader(ConversionOptions options) {
        return MAPPER::readTree;
      }

      @Override
      Writer writer(ConversionOptions options) {
        return (file, jsonNode) -> (options.pretty()
                ? MAPPER.writerWithDefaultPrettyPrinter()
                : MAPPER.writer())
            .writeValue(file, jsonNode);
      }
    },
    PROPERTIES(Set.of("properties")) {
      private static final JavaPropsMapper MAPPER = JavaPropsMapper.builder()
          .configure(SORT_PROPERTIES_ALPHABETICALLY, true)
          .build();

      @Override
      Reader reader(ConversionOptions options) {
        return MAPPER::readTree;
      }

      @Override
      Writer writer(ConversionOptions options) {
        return MAPPER::writeValue;
      }
    },
    TOML(Set.of("toml")) {
      private static final TomlMapper MAPPER = new TomlMapper();

      @Override
      Reader reader(ConversionOptions options) {
        return MAPPER::readTree;
      }

      @Override
      Writer writer(ConversionOptions options) {
        return MAPPER::writeValue;
      }
    },
    YAML(Set.of("yaml", "yml")) {
      private static final YAMLMapper MAPPER = new YAMLMapper();

      @Override
      Reader reader(ConversionOptions options) {
        // Use SnakeYAML directly so anchors/aliases resolve to their values.
        // YAMLMapper.readTree keeps alias names (e.g. "*foo") instead.
        return file -> MAPPER.valueToTree(new Yaml().load(Files.newInputStream(file.toPath())));
      }

      @Override
      Writer writer(ConversionOptions options) {
        return (file, jsonNode) -> MAPPER
            .configure(INDENT_ARRAYS, options.indentYaml())
            .configure(INDENT_ARRAYS_WITH_INDICATOR, options.indentYaml())
            .configure(MINIMIZE_QUOTES, options.minimizeYamlQuotes())
            .writeValue(file, jsonNode);
      }
    };

    private final Set<String> extensions;

    FileType(Set<String> extensions) {
      this.extensions = extensions;
    }

    abstract Reader reader(ConversionOptions options);

    abstract Writer writer(ConversionOptions options);

    static FileType fromFileExtension(String fileExtension) {
      if (fileExtension == null || fileExtension.isBlank()) {
        throw new IllegalArgumentException("File type could not be determined!");
      }
      return Arrays.stream(values())
          .filter(f -> f.extensions.contains(fileExtension.toLowerCase(Locale.ENGLISH)))
          .findAny()
          .orElseThrow(() ->
              new IllegalArgumentException("Unsupported file type! [%s]".formatted(fileExtension)));
    }
  }

  record ConversionOptions(
      char csvSeparator,
      boolean pretty,
      boolean indentYaml,
      boolean minimizeYamlQuotes,
      boolean deduplicateKeys) {
    ConversionOptions {
      if (isWhitespace(csvSeparator) || isSpaceChar(csvSeparator)) {
        throw new IllegalArgumentException("CSV separator can not be blank or whitespace!");
      }
    }

    static Builder builder() {
      return new Builder();
    }

    static final class Builder {
      private char csvSeparator;
      private boolean pretty;
      private boolean indentYaml;
      private boolean minimizeYamlQuotes;
      private boolean deduplicateKeys;

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

      Builder setDeduplicateKeys(boolean deduplicateKeys) {
        this.deduplicateKeys = deduplicateKeys;
        return this;
      }

      ConversionOptions build() {
        return new ConversionOptions(
            this.csvSeparator,
            this.pretty,
            this.indentYaml,
            this.minimizeYamlQuotes,
            this.deduplicateKeys);
      }
    }
  }

  static JsonNode deduplicateKeys(JsonNode original) {
    if (original.isArray()) {
      var factory = JsonNodeFactory.instance;
      var deduplicated = factory.objectNode();

      var it = original.elements();
      var keys = deduplicated.putArray("keys");
      var values = deduplicated.putArray("values");
      while (it.hasNext()) {
        var next = it.next();
        if (keys.isEmpty()) {
          var names = new ArrayList<String>();
          next.fieldNames().forEachRemaining(names::add);
          keys.addAll(names.stream().map(TextNode::valueOf).toList());
        }
        var value = values.addArray();
        keys.forEach(key -> value.add(next.get(key.asText())));
      }
      return deduplicated;
    }

    return original;
  }

  private static JsonNode readDelimited(File file, char separator) throws IOException {
    var it = CSV_MAPPER
        .readerFor(new TypeReference<LinkedHashMap<String, String>>() {})
        .with(CSV_BASE_SCHEMA.withColumnSeparator(separator))
        .readValues(file);
    var factory = JsonNodeFactory.instance;
    var result = factory.arrayNode();
    while (it.hasNextValue()) {
      result.add(CSV_MAPPER.convertValue(it.next(), JsonNode.class));
    }
    return result;
  }

  private static void writeDelimited(File file, JsonNode jsonNode, char separator)
      throws IOException {
    var csvSchemaBuilder = CsvSchema.builder();
    var firstObject = jsonNode instanceof ArrayNode ? jsonNode.elements().next() : jsonNode;
    firstObject.fieldNames().forEachRemaining(csvSchemaBuilder::addColumn);
    CSV_MAPPER
        .writerFor(JsonNode.class)
        .with(csvSchemaBuilder.build().withColumnSeparator(separator).withHeader())
        .writeValue(file, jsonNode);
  }

  // TODO Just a dummy implementation for now. Consider using java.nio.
  static void convert(File input, File output, ConversionOptions options) throws IOException {
    requireNonNull(input);
    requireNonNull(output);

    var reader = fromFileExtension(getFileExtension(input.getName())).reader(options);
    var writer = fromFileExtension(getFileExtension(output.getName())).writer(options);

    var data = reader.read(input);
    writer.write(output, options.deduplicateKeys() ? deduplicateKeys(data) : data);
  }
}
