package dev.gokhun.convert.lib;

import java.io.File;
import java.io.IOException;

final class DefaultConverter implements Converter {
  @Override
  public void convert(File input, File output, ConverterOptions options) throws IOException {
    ConversionUtil.convert(
        input,
        output,
        ConversionUtil.ConversionOptions.builder()
            .setCsvSeparator(options.csvSeparator())
            .setPretty(options.pretty())
            .setIndentYaml(options.indentYaml())
            .setMinimizeYamlQuotes(options.minimizeYamlQuotes())
            .setDeduplicateKeys(options.deduplicateKeys())
            .build());
  }
}
