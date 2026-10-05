# SPIKE: SwiftUI frontend for `convert`

## Verdict

Direct Java-lib linking from Swift is **not viable** — Swift cannot consume
JVM bytecode, so crossing the JVM boundary would require either embedding a
JVM via JNI (fragile, ships a JDK) or rebuilding `:lib` as a GraalVM native
shared library with JNI config for all of Jackson (heavy). Recommended path:
**sidecar binary** (this spike) or a local HTTP wrapper around `:lib`.

## Why direct linking was stopped early

- Swift has C/ObjC interop only; there is no import of `.class`/`.jar`
  bytecode. Any direct use of `dev.gokhun.convert.lib.Converter` from Swift
  must cross a native boundary.
- Option (a) — JNI + embedded JVM: the app would bundle and launch a JDK,
  manage `JNI_CreateJVM` lifecycle, and hand-marshal every call. Fragile to
  ship and update, and it has no match with the existing GraalVM **static**
  `convert` binary (linux musl static + mac arm), which cannot act as an
  embeddable library.
- Option (b) — GraalVM native shared library + Swift C-interop: requires a
  separate `shared` native-image build plus JNI/reflection config covering
  all of Jackson databind/format/… used by the converters. Heavy one-off and
  ongoing maintenance cost. The recent `InputStream`-based `convert` overload
  helps only marginally here (fewer temp files over a boundary) and does not
  remove the boundary itself.
- Per the spike's stop rule, neither (a) nor (b) was implemented; no heavy
  builds were attempted.

## What was actually built (sidecar spike)

- `swift-ui/ConvertBridge.swift` — resolves the `convert` binary (explicit
  path > bundle resources > `PATH`), builds `--from/--to` + options argv
  matching `cli/.../Convert.java` flags, runs it via Foundation `Process`,
  captures exit code/stderr.
- `swift-ui/ContentView.swift` — input/output pickers via
  `NSOpenPanel`/`NSSavePanel`, toggles matching `ConverterOptions` defaults
  (csvSeparator `","`, pretty `false`, indentYaml `true`,
  minimizeYamlQuotes `true`, deduplicateKeys `false`), Convert button,
  status text. Clearly marked EXPERIMENT; no `.xcodeproj`.
- `swift-ui/README.md` — how the bridge works and how to point it at a
  `convert` binary built from `:cli`.

Neither `lib/` nor `cli/` was modified.

## Environment

- `xcodebuild -version`: Xcode 27.0, Build version 27A266a
- `swift --version`: swift-driver 1.168.6, Apple Swift 6.4
  (swiftlang-6.4.0.34.1 clang-2100.3.34.1), Target arm64-apple-macosx27.0.0
- `xcrun --show-sdk-path`:
  `/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk`
  (macOS SDK available, so AppKit/SwiftUI typecheck is possible).

## Verification

- `swiftc --typecheck swift-ui/ConvertBridge.swift swift-ui/ContentView.swift`
  (with `-sdk` pointing at the path above): result recorded in the final
  spike report.
- `./gradlew :lib:test --no-daemon`: confirms the untouched base still
  passes (no native involved).
