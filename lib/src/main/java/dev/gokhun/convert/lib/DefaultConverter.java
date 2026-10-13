package dev.gokhun.convert.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

final class DefaultConverter implements Converter {
  @Override
  public void convert(File input, File output, ConverterOptions options) throws IOException {
    try (var in = new FileInputStream(input);
        var out = new FileOutputStream(output)) {
      convert(
          in,
          ConversionUtil.getFileExtension(input.getName()),
          out,
          ConversionUtil.getFileExtension(output.getName()),
          options);
    }
  }

  @Override
  public void convert(
      InputStream input,
      String inputExtension,
      OutputStream output,
      String outputExtension,
      ConverterOptions options)
      throws IOException {
    ConversionUtil.convert(
        input,
        inputExtension,
        output,
        outputExtension,
        ConversionUtil.ConversionOptions.builder()
            .setCsvSeparator(options.csvSeparator())
            .setPretty(options.pretty())
            .setIndentYaml(options.indentYaml())
            .setMinimizeYamlQuotes(options.minimizeYamlQuotes())
            .setDeduplicateKeys(options.deduplicateKeys())
            .build());
  }
}
