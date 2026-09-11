package dev.gokhun.convert.lib;

import static com.google.common.base.Preconditions.checkArgument;
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
    checkArgument(
        !isWhitespace(csvSeparator) && !isSpaceChar(csvSeparator),
        "CSV separator can not be blank or whitespace!");
  }
}
