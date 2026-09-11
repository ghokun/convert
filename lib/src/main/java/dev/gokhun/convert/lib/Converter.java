package dev.gokhun.convert.lib;

import java.io.File;
import java.io.IOException;

public interface Converter {
  void convert(File input, File output, ConverterOptions options) throws IOException;

  static Converter create() {
    return new DefaultConverter();
  }
}
