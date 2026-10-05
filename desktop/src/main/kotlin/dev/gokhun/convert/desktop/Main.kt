package dev.gokhun.convert.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.gokhun.convert.lib.Converter
import dev.gokhun.convert.lib.ConverterOptions
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.ui.component.CheckboxRow
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField

fun main() = application {
  val windowState = rememberWindowState(width = 680.dp, height = 600.dp)
  Window(onCloseRequest = ::exitApplication, state = windowState, title = "Convert") {
    var darkTheme by remember { mutableStateOf(false) }
    IntUiTheme(isDark = darkTheme) {
      App(frame = window, darkTheme = darkTheme, onDarkThemeChange = { darkTheme = it })
    }
  }
}

@Composable
private fun App(frame: Frame, darkTheme: Boolean, onDarkThemeChange: (Boolean) -> Unit) {
  val inputState = rememberTextFieldState("")
  val outputState = rememberTextFieldState("")
  val separatorState = rememberTextFieldState(",")
  var pretty by remember { mutableStateOf(false) }
  var indentYaml by remember { mutableStateOf(true) }
  var minimizeYamlQuotes by remember { mutableStateOf(true) }
  var deduplicateKeys by remember { mutableStateOf(false) }
  var status by remember { mutableStateOf("Choose an input and an output file, then press Convert.") }

  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text("Convert between CSV, JSON, properties, TOML and YAML.")
    FileRow(
        label = "Input",
        state = inputState,
        placeholder = "Input file (csv, json, properties, toml, yaml, yml)",
        onBrowse = {
          browse(frame, "Choose input file", FileDialog.LOAD)?.let { setText(inputState, it) }
        })
    FileRow(
        label = "Output",
        state = outputState,
        placeholder = "Output file",
        onBrowse = {
          browse(frame, "Choose output file", FileDialog.SAVE)?.let { setText(outputState, it) }
        })
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically) {
      Text("CSV separator:")
      TextField(state = separatorState, modifier = Modifier.width(64.dp))
      CheckboxRow(text = "Dark theme", checked = darkTheme, onCheckedChange = onDarkThemeChange)
    }
    CheckboxRow(
        text = "Pretty-print output", checked = pretty, onCheckedChange = { pretty = it })
    CheckboxRow(
        text = "Indent YAML", checked = indentYaml, onCheckedChange = { indentYaml = it })
    CheckboxRow(
        text = "Minimize YAML quotes",
        checked = minimizeYamlQuotes,
        onCheckedChange = { minimizeYamlQuotes = it })
    CheckboxRow(
        text = "Deduplicate keys",
        checked = deduplicateKeys,
        onCheckedChange = { deduplicateKeys = it })
    DefaultButton(
        onClick = {
          val input = inputState.text.toString()
          val output = outputState.text.toString()
          if (input.isBlank() || output.isBlank()) {
            status = "Error: input and output files must both be set."
            return@DefaultButton
          }
          val options =
              ConverterOptions.builder()
                  .csvSeparator(separatorState.text.firstOrNull() ?: ',')
                  .pretty(pretty)
                  .indentYaml(indentYaml)
                  .minimizeYamlQuotes(minimizeYamlQuotes)
                  .deduplicateKeys(deduplicateKeys)
                  .build()
          status =
              try {
                Converter.create().convert(File(input), File(output), options)
                "Done: converted $input to $output."
              } catch (e: Exception) {
                "Error: ${e.message}"
              }
        }) {
      Text("Convert")
    }
    Text(status)
  }
}

@Composable
private fun FileRow(label: String, state: TextFieldState, placeholder: String, onBrowse: () -> Unit) {
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically) {
    Text(label, modifier = Modifier.width(56.dp))
    TextField(
        state = state, placeholder = { Text(placeholder) }, modifier = Modifier.weight(1f))
    OutlinedButton(onClick = onBrowse) { Text("Browse") }
  }
}

private fun setText(state: TextFieldState, text: String) {
  state.edit { replace(0, length, text) }
}

private fun browse(parent: Frame, title: String, mode: Int): String? {
  val dialog = FileDialog(parent, title, mode)
  dialog.isVisible = true
  val dir = dialog.directory
  val file = dialog.file
  dialog.dispose()
  return if (dir != null && file != null) File(dir, file).absolutePath else null
}
