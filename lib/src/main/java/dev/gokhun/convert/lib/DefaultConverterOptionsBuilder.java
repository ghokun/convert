package dev.gokhun.convert.lib;

final class DefaultConverterOptionsBuilder implements ConverterOptions.Builder {
  private char csvSeparator;
  private boolean pretty;
  private boolean indentYaml;
  private boolean minimizeYamlQuotes;
  private boolean deduplicateKeys;

  @Override
  public ConverterOptions.Builder csvSeparator(char value) {
    this.csvSeparator = value;
    return this;
  }

  @Override
  public ConverterOptions.Builder pretty(boolean value) {
    this.pretty = value;
    return this;
  }

  @Override
  public ConverterOptions.Builder indentYaml(boolean value) {
    this.indentYaml = value;
    return this;
  }

  @Override
  public ConverterOptions.Builder minimizeYamlQuotes(boolean value) {
    this.minimizeYamlQuotes = value;
    return this;
  }

  @Override
  public ConverterOptions.Builder deduplicateKeys(boolean value) {
    this.deduplicateKeys = value;
    return this;
  }

  @Override
  public ConverterOptions build() {
    return new DefaultConverterOptions(
        csvSeparator, pretty, indentYaml, minimizeYamlQuotes, deduplicateKeys);
  }
}
