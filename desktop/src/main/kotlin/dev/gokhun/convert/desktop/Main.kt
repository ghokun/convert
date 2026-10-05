package dev.gokhun.convert.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.gokhun.convert.lib.Converter
import dev.gokhun.convert.lib.ConverterOptions
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.ui.component.CheckboxRow
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField

fun main() = application {
  val windowState = rememberWindowState(width = 1024.dp, height = 720.dp)
  Window(onCloseRequest = ::exitApplication, state = windowState, title = "Convert") {
    var darkTheme by remember { mutableStateOf(false) }
    IntUiTheme(isDark = darkTheme) {
      App(darkTheme = darkTheme, onDarkThemeChange = { darkTheme = it })
    }
  }
}

private val FORMATS = listOf("json", "yaml", "csv", "tsv", "toml", "properties")

private const val SAMPLE_JSON =
    """{
  "name": "Ada Lovelace",
  "age": 36,
  "admin": false,
  "score": 98.6,
  "tags": ["dev", "ops"],
  "address": {
    "city": "Berlin",
    "zip": 10115
  },
  "nickname": null
}"""

@Composable
private fun App(darkTheme: Boolean, onDarkThemeChange: (Boolean) -> Unit) {
  var inputFormat by remember { mutableStateOf("json") }
  var outputFormat by remember { mutableStateOf("yaml") }
  var inputValue by remember { mutableStateOf(TextFieldValue(SAMPLE_JSON)) }
  var outputText by remember { mutableStateOf("") }
  val separatorState = rememberTextFieldState(",")
  var pretty by remember { mutableStateOf(false) }
  var indentYaml by remember { mutableStateOf(true) }
  var minimizeYamlQuotes by remember { mutableStateOf(true) }
  var deduplicateKeys by remember { mutableStateOf(false) }
  var status by remember { mutableStateOf("Edit the input text, pick formats, then press Convert.") }

  val editorBackground = if (darkTheme) Color(0xFF1E1F22) else Color(0xFFFFFFFF)
  val editorBorder = if (darkTheme) Color(0xFF4E5157) else Color(0xFFC9CCD1)
  val editorTextColor = if (darkTheme) Color(0xFFDFE1E5) else Color(0xFF191C20)
  val monoStyle =
      TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = editorTextColor)
  val inputScroll = rememberScrollState()
  val outputScroll = rememberScrollState()

  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text("Convert between CSV, JSON, properties, TOML and YAML.")
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically) {
      FormatDropdown(label = "Input", selected = inputFormat, onSelect = { inputFormat = it })
      Text("to")
      FormatDropdown(label = "Output", selected = outputFormat, onSelect = { outputFormat = it })
    }
    Row(
        modifier = Modifier.fillMaxWidth().weight(1f),
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      EditorColumn(
          label = "Input ($inputFormat)",
          background = editorBackground,
          border = editorBorder,
          modifier = Modifier.weight(1f)) {
        BasicTextField(
            value = inputValue.copy(annotatedString = highlightSyntax(inputValue.text, inputFormat)),
            onValueChange = { inputValue = it },
            textStyle = monoStyle,
            modifier = Modifier.fillMaxSize().verticalScroll(inputScroll).padding(8.dp))
      }
      EditorColumn(
          label = "Output ($outputFormat)",
          background = editorBackground,
          border = editorBorder,
          modifier = Modifier.weight(1f)) {
        SelectionContainer {
          if (outputText.isEmpty()) {
            BasicText(
                text = "Output appears here.",
                style = monoStyle.copy(color = Color(0xFF8A8F98)),
                modifier = Modifier.fillMaxSize().verticalScroll(outputScroll).padding(8.dp))
          } else {
            BasicText(
                text = highlightSyntax(outputText, outputFormat),
                style = monoStyle,
                modifier = Modifier.fillMaxSize().verticalScroll(outputScroll).padding(8.dp))
          }
        }
      }
    }
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
                val inputBytes = inputValue.text.toByteArray(Charsets.UTF_8)
                ByteArrayInputStream(inputBytes).use { input ->
                  ByteArrayOutputStream().use { output ->
                    Converter.create().convert(input, inputFormat, output, outputFormat, options)
                    outputText = output.toString(Charsets.UTF_8)
                  }
                }
                "Done: converted input ($inputFormat -> $outputFormat)."
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
private fun FormatDropdown(label: String, selected: String, onSelect: (String) -> Unit) {
  var expanded by remember { mutableStateOf(false) }
  Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically) {
    Text(label)
    Box {
      OutlinedButton(onClick = { expanded = true }) { Text(selected) }
      DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        FORMATS.forEach { format ->
          DropdownMenuItem(
              onClick = {
                expanded = false
                onSelect(format)
              }) {
            Text(format)
          }
        }
      }
    }
  }
}

@Composable
private fun EditorColumn(
    label: String,
    background: Color,
    border: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(label)
    Box(modifier = Modifier.fillMaxWidth().weight(1f).background(background).border(1.dp, border)) {
      content()
    }
  }
}

internal fun highlightSyntax(text: String, format: String): AnnotatedString {
  if (text.isEmpty()) return AnnotatedString("")
  val stringStyle = SpanStyle(color = Color(0xFF2FA84F))
  val keyStyle = SpanStyle(color = Color(0xFF4E94CE))
  val numberStyle = SpanStyle(color = Color(0xFF9B6ED3))
  val keywordStyle = SpanStyle(color = Color(0xFFD07A00))
  val commentStyle = SpanStyle(color = Color(0xFF8A8F98))

  val comment =
      if (format == "properties" || format == "toml") """#[^\n]*|//[^\n]*|;[^\n]*"""
      else """#[^\n]*|//[^\n]*"""
  val tokenRegex =
      Regex(
          "($comment)" +
              "|(\"(?:[^\"\\\\\\n]|\\\\.)*\"|'(?:[^'\\\\\\n]|\\\\.)*')" +
              "|\\b(true|false|null|nil|yes|no|on|off|True|False|None|TRUE|FALSE|NULL|NaN|Inf)\\b" +
              "|(?<![\\w\$.-])-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?\\b")
  val keyRegex = Regex("(\"(?:[^\"\\\\\\n]|\\\\.)*\")\\s*:")

  return buildAnnotatedString {
    append(text)
    for (match in tokenRegex.findAll(text)) {
      val style =
          when {
            match.groups[1] != null -> commentStyle
            match.groups[2] != null -> stringStyle
            match.groups[3] != null -> keywordStyle
            else -> numberStyle
          }
      addStyle(style, match.range.first, match.range.last + 1)
    }
    // JSON object keys get a distinct color (applied last so they win over plain strings).
    for (match in keyRegex.findAll(text)) {
      val key = match.groups[1] ?: continue
      addStyle(keyStyle, key.range.first, key.range.last + 1)
    }
  }
}
