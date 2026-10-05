// EXPERIMENT (spike): sidecar bridge to the `convert` CLI binary.
// This file intentionally has no dependency on the Java :lib — Swift cannot
// consume JVM bytecode directly, so we shell out to the GraalVM native binary.

import Foundation

/// EXPERIMENT: options mirroring `ConverterOptions` defaults in :lib.
/// csvSeparator ",", pretty false, indentYaml true, minimizeYamlQuotes true,
/// deduplicateKeys false.
struct ConvertOptions {
  var csvSeparator: String = ","
  var pretty: Bool = false
  var indentYaml: Bool = true
  var minimizeYamlQuotes: Bool = true
  var deduplicateKeys: Bool = false
}

/// EXPERIMENT: result of one sidecar invocation.
struct ConvertResult {
  var exitCode: Int32
  var stderr: String
  var succeeded: Bool { exitCode == 0 }
}

/// EXPERIMENT: builds `Process` arguments for the `convert` CLI and runs it.
///
/// Flag mapping (see `cli/.../Convert.java`):
/// input -> --from, output -> --to, plus --csv-separator/--pretty/
/// --indent-yaml/--minimize-yaml-quotes/--deduplicate-keys. Picocli boolean
/// flags accept explicit `true`/`false` values, which is what we pass so the
/// Swift defaults stay visible even when they match the CLI defaults.
struct ConvertBridge {
  /// Resolve the sidecar binary: explicit path wins, then bundle resources,
  /// then PATH lookup ("/usr/bin/which convert").
  static func resolveBinary(explicitPath: String? = nil) -> String {
    if let explicitPath, !explicitPath.isEmpty { return explicitPath }
    if let bundled = Bundle.main.path(forResource: "convert", ofType: nil) {
      return bundled
    }
    let probe = Process()
    probe.executableURL = URL(fileURLWithPath: "/usr/bin/which")
    probe.arguments = ["convert"]
    let pipe = Pipe()
    probe.standardOutput = pipe
    probe.standardError = Pipe()
    do {
      try probe.run()
      probe.waitUntilExit()
      if probe.terminationStatus == 0 {
        let found =
          String(data: pipe.fileHandleForReading.readDataToEndOfFile(), encoding: .utf8)?
          .trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        if !found.isEmpty { return found }
      }
    } catch {
      // Fall through to the bare name and let Process report the error.
    }
    return "convert"
  }

  /// Build the argv array for the sidecar binary.
  static func arguments(input: String, output: String, options: ConvertOptions) -> [String] {
    [
      "--from", input,
      "--to", output,
      "--csv-separator", options.csvSeparator,
      "--pretty", String(options.pretty),
      "--indent-yaml", String(options.indentYaml),
      "--minimize-yaml-quotes", String(options.minimizeYamlQuotes),
      "--deduplicate-keys", String(options.deduplicateKeys),
    ]
  }

  /// Run the sidecar binary synchronously. Call from a background task and
  /// marshal the result back to the main actor for UI updates.
  static func run(
    binaryPath: String? = nil, input: String, output: String, options: ConvertOptions
  ) -> ConvertResult {
    let binary = resolveBinary(explicitPath: binaryPath)
    let process = Process()
    process.executableURL = URL(fileURLWithPath: binary)
    // Bare command name (PATH fallback): use /usr/bin/env for lookup.
    if binary == "convert" {
      process.executableURL = URL(fileURLWithPath: "/usr/bin/env")
      process.arguments = ["convert"] + arguments(input: input, output: output, options: options)
    } else {
      process.arguments = arguments(input: input, output: output, options: options)
    }
    let errPipe = Pipe()
    process.standardOutput = Pipe()  // CLI writes files, not stdout; discard.
    process.standardError = errPipe
    do {
      try process.run()
    } catch {
      return ConvertResult(exitCode: -1, stderr: "Failed to launch '\(binary)': \(error)")
    }
    process.waitUntilExit()
    let stderr =
      String(data: errPipe.fileHandleForReading.readDataToEndOfFile(), encoding: .utf8) ?? ""
    return ConvertResult(exitCode: process.terminationStatus, stderr: stderr)
  }
}
