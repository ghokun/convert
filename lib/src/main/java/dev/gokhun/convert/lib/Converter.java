package dev.gokhun.convert.lib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Converter {
  void convert(File input, File output, ConverterOptions options) throws IOException;

  void convert(
      InputStream input,
      String inputExtension,
      OutputStream output,
      String outputExtension,
      ConverterOptions options)
      throws IOException;

  static Converter create() {
    return new DefaultConverter();
  }
}
