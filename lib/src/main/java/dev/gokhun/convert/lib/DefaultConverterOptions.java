package dev.gokhun.convert.lib;

import static java.lang.Character.isSpaceChar;
import static java.lang.Character.isWhitespace;

record DefaultConverterOptions(
    char csvSeparator,
    boolean pretty,
    boolean indentYaml,
    boolean minimizeYamlQuotes,
    boolean deduplicateKeys)
    implements ConverterOptions {
  DefaultConverterOptions {
    if (isWhitespace(csvSeparator) || isSpaceChar(csvSeparator)) {
      throw new IllegalArgumentException("CSV separator can not be blank or whitespace!");
    }
  }
}
