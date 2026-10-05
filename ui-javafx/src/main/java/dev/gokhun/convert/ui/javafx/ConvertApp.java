package dev.gokhun.convert.ui.javafx;

import dev.gokhun.convert.lib.Converter;
import dev.gokhun.convert.lib.ConverterOptions;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

public final class ConvertApp extends Application {
  private static final String SAMPLE_JSON = "{\n"
      + "  \"name\": \"Ada\",\n"
      + "  \"age\": 36,\n"
      + "  \"active\": true,\n"
      + "  \"address\": null,\n"
      + "  \"tags\": [\"admin\", \"user\"],\n"
      + "  \"score\": 9.5\n"
      + "}";

  private static final Pattern HIGHLIGHT_PATTERN = Pattern.compile(
      "(?<STRING>\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*')"
          + "|(?<COMMENT>#[^\\n]*|//[^\\n]*|;[^\\n]*)"
          + "|(?<NUMBER>\\b-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?\\b)"
          + "|(?<BOOLEAN>\\b(true|false|yes|no|on|off)\\b)"
          + "|(?<NULL>\\b(null|nil|~)\\b)"
          + "|(?<KEY>^[ \\t]*[A-Za-z0-9_.\\-]+(?=[ \\t]*[:=]))",
      Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

  private CodeArea inputArea;
  private CodeArea outputArea;
  private ComboBox<String> inputFormatBox;
  private ComboBox<String> outputFormatBox;
  private TextField separatorField;
  private CheckBox prettyBox;
  private CheckBox indentYamlBox;
  private CheckBox minimizeQuotesBox;
  private CheckBox deduplicateKeysBox;
  private Button convertButton;
  private Label statusLabel;

  @Override
  public void start(Stage stage) {
    inputFormatBox = new ComboBox<>(
        FXCollections.observableArrayList("json", "yaml", "csv", "tsv", "toml", "properties"));
    inputFormatBox.setValue("json");

    outputFormatBox = new ComboBox<>(
        FXCollections.observableArrayList("json", "yaml", "csv", "tsv", "toml", "properties"));
    outputFormatBox.setValue("yaml");

    inputArea = new CodeArea();
    inputArea.setWrapText(true);
    inputArea.setStyle("-fx-font-family: monospace; -fx-font-size: 13px;");
    inputArea.replaceText(SAMPLE_JSON);
    inputArea.setStyleSpans(0, computeHighlighting(SAMPLE_JSON));
    inputArea
        .multiPlainChanges()
        .successionEnds(Duration.ofMillis(100))
        .subscribe(ignore -> inputArea.setStyleSpans(0, computeHighlighting(inputArea.getText())));

    outputArea = new CodeArea();
    outputArea.setWrapText(true);
    outputArea.setEditable(false);
    outputArea.setStyle("-fx-font-family: monospace; -fx-font-size: 13px;");
    outputArea.replaceText("");
    outputArea.setStyleSpans(0, computeHighlighting(""));

    inputFormatBox.setOnAction(
        event -> inputArea.setStyleSpans(0, computeHighlighting(inputArea.getText())));
    outputFormatBox.setOnAction(
        event -> outputArea.setStyleSpans(0, computeHighlighting(outputArea.getText())));

    separatorField = new TextField(",");
    separatorField.setPrefWidth(60);

    prettyBox = new CheckBox("pretty");
    prettyBox.setSelected(false);
    indentYamlBox = new CheckBox("indentYaml");
    indentYamlBox.setSelected(true);
    minimizeQuotesBox = new CheckBox("minimizeYamlQuotes");
    minimizeQuotesBox.setSelected(true);
    deduplicateKeysBox = new CheckBox("deduplicateKeys");
    deduplicateKeysBox.setSelected(false);

    convertButton = new Button("Convert");
    convertButton.setOnAction(event -> onConvert());

    statusLabel = new Label("Idle");

    HBox formatRow =
        new HBox(8, new Label("Input:"), inputFormatBox, new Label("Output:"), outputFormatBox);
    VBox inputBox = new VBox(4, new Label("Input"), inputArea);
    VBox outputBox = new VBox(4, new Label("Output"), outputArea);
    VBox.setVgrow(inputArea, Priority.ALWAYS);
    VBox.setVgrow(outputArea, Priority.ALWAYS);
    HBox.setHgrow(inputBox, Priority.ALWAYS);
    HBox.setHgrow(outputBox, Priority.ALWAYS);
    inputBox.setPrefWidth(440);
    outputBox.setPrefWidth(440);
    inputArea.setPrefHeight(420);
    outputArea.setPrefHeight(420);
    HBox editorsRow = new HBox(10, inputBox, outputBox);
    VBox.setVgrow(editorsRow, Priority.ALWAYS);

    HBox separatorRow = new HBox(8, new Label("csv-separator:"), separatorField);
    HBox optionsRow = new HBox(12, prettyBox, indentYamlBox, minimizeQuotesBox, deduplicateKeysBox);

    VBox root =
        new VBox(10, formatRow, editorsRow, separatorRow, optionsRow, convertButton, statusLabel);
    root.setPadding(new Insets(16));

    Scene scene = new Scene(root, 960, 640);
    scene.getStylesheets().add(ConvertApp.class.getResource("syntax.css").toExternalForm());
    stage.setTitle("Convert");
    stage.setScene(scene);
    stage.show();
  }

  private static StyleSpans<Collection<String>> computeHighlighting(String text) {
    Matcher matcher = HIGHLIGHT_PATTERN.matcher(text);
    int lastEnd = 0;
    StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
    while (matcher.find()) {
      String styleClass = null;
      if (matcher.group("STRING") != null) {
        styleClass = "string";
      } else if (matcher.group("COMMENT") != null) {
        styleClass = "comment";
      } else if (matcher.group("NUMBER") != null) {
        styleClass = "number";
      } else if (matcher.group("BOOLEAN") != null) {
        styleClass = "boolean";
      } else if (matcher.group("NULL") != null) {
        styleClass = "null";
      } else if (matcher.group("KEY") != null) {
        styleClass = "key";
      }
      if (styleClass == null) {
        continue;
      }
      spansBuilder.add(Collections.emptyList(), matcher.start() - lastEnd);
      spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
      lastEnd = matcher.end();
    }
    spansBuilder.add(Collections.emptyList(), text.length() - lastEnd);
    return spansBuilder.create();
  }

  private void onConvert() {
    String inputText = inputArea.getText();
    String inputExtension = inputFormatBox.getValue();
    String outputExtension = outputFormatBox.getValue();
    String separatorText = separatorField.getText();
    char separator = separatorText.isEmpty() ? ',' : separatorText.charAt(0);
    ConverterOptions options = ConverterOptions.builder()
        .csvSeparator(separator)
        .pretty(prettyBox.isSelected())
        .indentYaml(indentYamlBox.isSelected())
        .minimizeYamlQuotes(minimizeQuotesBox.isSelected())
        .deduplicateKeys(deduplicateKeysBox.isSelected())
        .build();
    statusLabel.setText("Converting...");
    convertButton.setDisable(true);
    Thread worker =
        new Thread(() -> runConversion(inputText, inputExtension, outputExtension, options));
    worker.setDaemon(true);
    worker.start();
  }

  private void runConversion(
      String inputText, String inputExtension, String outputExtension, ConverterOptions options) {
    try {
      byte[] inputBytes = inputText.getBytes(StandardCharsets.UTF_8);
      ByteArrayInputStream input = new ByteArrayInputStream(inputBytes);
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      Converter.create().convert(input, inputExtension, output, outputExtension, options);
      String result = output.toString(StandardCharsets.UTF_8);
      Platform.runLater(() -> {
        outputArea.replaceText(result);
        outputArea.setStyleSpans(0, computeHighlighting(result));
        outputArea.moveTo(0);
        outputArea.requestFollowCaret();
        statusLabel.setText("Done (" + outputExtension + ", " + result.length() + " chars)");
        convertButton.setDisable(false);
      });
    } catch (IOException | IllegalArgumentException e) {
      Platform.runLater(() -> {
        String message = e.getMessage();
        statusLabel.setText("Failed: " + (message == null ? e.toString() : message));
        convertButton.setDisable(false);
      });
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
