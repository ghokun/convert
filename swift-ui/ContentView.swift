// EXPERIMENT (spike): minimal SwiftUI frontend driving the `convert` sidecar.
// No .xcodeproj is provided; these sources + README are the deliverable.
// Wire `ConvertBridge.run` into this view; file pickers use AppKit panels
// because `fileImporter` needs entitlements/plist wiring in a real app target.

import SwiftUI
import AppKit
import Foundation

// EXPERIMENT: spike view — input/output pickers, ConverterOptions toggles,
// Convert button, status text.
struct ContentView: View {
  @State private var inputPath: String = ""
  @State private var outputPath: String = ""
  @State private var binaryPath: String = ""
  @State private var csvSeparator: String = ","
  @State private var pretty: Bool = false
  @State private var indentYaml: Bool = true
  @State private var minimizeYamlQuotes: Bool = true
  @State private var deduplicateKeys: Bool = false
  @State private var status: String = "Idle."
  @State private var isRunning: Bool = false

  var body: some View {
    Form {
      Section("Sidecar binary") {
        TextField("convert binary (empty = bundle resources, then PATH)", text: $binaryPath)
        Text(ConvertBridge.resolveBinary(explicitPath: binaryPath.isEmpty ? nil : binaryPath))
          .font(.caption)
          .foregroundStyle(.secondary)
      }
      Section("Files") {
        HStack {
          TextField("Input file", text: $inputPath)
          Button("Browse…") { pickInput() }
        }
        HStack {
          TextField("Output file", text: $outputPath)
          Button("Browse…") { pickOutput() }
        }
      }
      Section("Options (ConverterOptions defaults)") {
        TextField("CSV separator", text: $csvSeparator)
        Toggle("Pretty", isOn: $pretty)
        Toggle("Indent YAML", isOn: $indentYaml)
        Toggle("Minimize YAML quotes", isOn: $minimizeYamlQuotes)
        Toggle("Deduplicate keys", isOn: $deduplicateKeys)
      }
      Section {
        Button("Convert") { convert() }
          .disabled(isRunning || inputPath.isEmpty || outputPath.isEmpty)
        Text(status)
          .font(.caption)
          .textSelection(.enabled)
      }
    }
    .padding()
    .frame(minWidth: 480, minHeight: 420)
  }

  private func currentOptions() -> ConvertOptions {
    ConvertOptions(
      csvSeparator: csvSeparator.isEmpty ? "," : csvSeparator,
      pretty: pretty,
      indentYaml: indentYaml,
      minimizeYamlQuotes: minimizeYamlQuotes,
      deduplicateKeys: deduplicateKeys)
  }

  private func convert() {
    let input = inputPath
    let output = outputPath
    let binary = binaryPath.isEmpty ? nil : binaryPath
    let options = currentOptions()
    isRunning = true
    status = "Running…"
    Task.detached {
      let result = ConvertBridge.run(
        binaryPath: binary, input: input, output: output, options: options)
      await MainActor.run {
        isRunning = false
        if result.succeeded {
          status = "Done: \(output)"
        } else {
          let detail = result.stderr.trimmingCharacters(in: .whitespacesAndNewlines)
          status = "Failed (exit \(result.exitCode))\(detail.isEmpty ? "" : ": \(detail)")"
        }
      }
    }
  }

  private func pickInput() {
    let panel = NSOpenPanel()
    panel.canChooseFiles = true
    panel.canChooseDirectories = false
    if panel.runModal() == .OK, let url = panel.url {
      inputPath = url.path
    }
  }

  private func pickOutput() {
    let panel = NSSavePanel()
    if panel.runModal() == .OK, let url = panel.url {
      outputPath = url.path
    }
  }
}
