package dev.gokhun.convert.lib;

public interface ConverterOptions {
  char csvSeparator();

  boolean pretty();

  boolean indentYaml();

  boolean minimizeYamlQuotes();

  boolean deduplicateKeys();

  static Builder builder() {
    return new DefaultConverterOptionsBuilder();
  }

  interface Builder {
    Builder csvSeparator(char csvSeparator);

    Builder pretty(boolean pretty);

    Builder indentYaml(boolean indentYaml);

    Builder minimizeYamlQuotes(boolean minimizeYamlQuotes);

    Builder deduplicateKeys(boolean deduplicateKeys);

    ConverterOptions build();
  }
}
