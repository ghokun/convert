package dev.gokhun.convert.ui.javafx;

import dev.gokhun.convert.lib.Converter;
import dev.gokhun.convert.lib.ConverterOptions;
import java.io.File;
import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public final class ConvertApp extends Application {
  private TextField inputField;
  private TextField outputField;
  private TextField separatorField;
  private CheckBox prettyBox;
  private CheckBox indentYamlBox;
  private CheckBox minimizeQuotesBox;
  private CheckBox deduplicateKeysBox;
  private Button convertButton;
  private Label statusLabel;

  @Override
  public void start(Stage stage) {
    inputField = new TextField();
    inputField.setPromptText("Input file");
    inputField.setPrefWidth(380);

    outputField = new TextField();
    outputField.setPromptText("Output file");
    outputField.setPrefWidth(380);

    Button browseInputButton = new Button("Browse...");
    browseInputButton.setOnAction(event -> chooseInputFile(stage));

    Button browseOutputButton = new Button("Browse...");
    browseOutputButton.setOnAction(event -> chooseOutputFile(stage));

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

    HBox inputRow = new HBox(8, inputField, browseInputButton);
    HBox outputRow = new HBox(8, outputField, browseOutputButton);
    HBox separatorRow = new HBox(8, new Label("csv-separator:"), separatorField);
    HBox optionsRow = new HBox(12, prettyBox, indentYamlBox, minimizeQuotesBox, deduplicateKeysBox);

    VBox root =
        new VBox(10, inputRow, outputRow, separatorRow, optionsRow, convertButton, statusLabel);
    root.setPadding(new Insets(16));

    stage.setTitle("Convert");
    stage.setScene(new Scene(root, 560, 320));
    stage.show();
  }

  private void chooseInputFile(Stage stage) {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Select input file");
    File chosen = chooser.showOpenDialog(stage);
    if (chosen != null) {
      inputField.setText(chosen.getAbsolutePath());
    }
  }

  private void chooseOutputFile(Stage stage) {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Select output file");
    File chosen = chooser.showSaveDialog(stage);
    if (chosen != null) {
      outputField.setText(chosen.getAbsolutePath());
    }
  }

  private void onConvert() {
    String inputPath = inputField.getText().trim();
    String outputPath = outputField.getText().trim();
    if (inputPath.isEmpty() || outputPath.isEmpty()) {
      statusLabel.setText("Select input and output files.");
      return;
    }
    String separatorText = separatorField.getText();
    char separator = separatorText.isEmpty() ? ',' : separatorText.charAt(0);
    ConverterOptions options = ConverterOptions.builder()
        .csvSeparator(separator)
        .pretty(prettyBox.isSelected())
        .indentYaml(indentYamlBox.isSelected())
        .minimizeYamlQuotes(minimizeQuotesBox.isSelected())
        .deduplicateKeys(deduplicateKeysBox.isSelected())
        .build();
    File input = new File(inputPath);
    File output = new File(outputPath);
    statusLabel.setText("Converting...");
    convertButton.setDisable(true);
    Thread worker = new Thread(() -> runConversion(input, output, options));
    worker.setDaemon(true);
    worker.start();
  }

  private void runConversion(File input, File output, ConverterOptions options) {
    try {
      Converter.create().convert(input, output, options);
      Platform.runLater(() -> {
        statusLabel.setText("Done: " + output.getName());
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
