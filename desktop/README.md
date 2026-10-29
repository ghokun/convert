# desktop (experiment)

Minimal JVM desktop UI for `convert`, built with Compose Multiplatform and styled with the
Jewel standalone (Int UI) theme. Conversion runs in-memory via `:lib`
(`Converter.create().convert(InputStream, String, OutputStream, String, ConverterOptions)`).
This is a spike: desktop-only, no signing, no GraalVM native-image.

## UI

Two side-by-side monospace panes with simple regex syntax highlighting
(`highlightSyntax` in `Main.kt`: strings green, JSON object keys blue, numbers purple,
booleans/null orange, `#`/`//` comments gray):

* LEFT: editable input (`BasicTextField` backed by `TextFieldValue`; the displayed
  `AnnotatedString` is re-highlighted on every change, so the cursor may jump — acceptable
  for a spike). Prefilled with a small sample JSON document.
* RIGHT: read-only output inside a `SelectionContainer`, so converted text can be selected
  and copied.

Input/output format dropdowns (`json`, `yaml`, `csv`, `tsv`, `toml`, `properties`;
defaults `json` -> `yaml`) select the extensions passed to the stream API: on Convert, the
input text is wrapped in a `ByteArrayInputStream`, converted into a `ByteArrayOutputStream`,
and the UTF-8 result is shown in the right pane. Conversion failures are reported in the
status label.

The options row is unchanged (CSV separator default `","`, pretty-print off, indent-YAML on,
minimize-YAML-quotes on, deduplicate-keys off), as are the Convert button, the status label,
and the Jewel `IntUiTheme` dark-theme toggle.

## Jewel's new home

The old `github.com/JetBrains/jewel` repo is a read-only mirror. Active development lives in
`github.com/JetBrains/intellij-community/tree/master/platform/jewel`, and artifacts are published
as `org.jetbrains.jewel:*` on Maven Central. For a standalone (non-IDE) desktop app, use the
`int-ui-standalone` artifact and wrap content in `IntUiTheme`.

## Versions used

| Piece                       | Version                |
| --------------------------- | ---------------------- |
| Kotlin (`jvm` + `compose`)  | 2.3.21                 |
| Compose Multiplatform       | 1.12.0                 |
| Jewel `jewel-int-ui-standalone` | 0.41.0-262.10968.63 |

Jewel 0.41 builds against Compose Multiplatform 1.12.0 (see Jewel `RELEASE NOTES.md`), so the
Compose plugin version matches it exactly. Kotlin 2.3.x matches what Jewel 0.38+ itself builds
with (2.3.20) plus the latest 2.3 patch. See `gradle.properties` (`kotlinVersion`,
`composeVersion`, `jewelVersion`).

## How to run

```sh
./gradlew :desktop:run
```

## How to package

Compose Desktop bundles its own JVM via `jpackage` (this is not a GraalVM native binary):

```sh
./gradlew :desktop:packageDistributionForCurrentOS  # .dmg on macOS, .msi on Windows, .deb on Linux
./gradlew :desktop:packageDmg       # macOS only
./gradlew :desktop:packageMsi       # Windows only
./gradlew :desktop:packageDeb       # Linux only
```

`compose.desktop.application.nativeDistributions` in `desktop/build.gradle` sets
`targetFormats(Dmg, Msi, Deb)`; no signing is configured (desktop-only publication).

## Caveats

* Jewel officially supports running on the JetBrains Runtime; this module uses the repo default
  toolchain (Java 25) like the other modules. Basic `IntUiTheme` usage should work, but if you
  hit window-chrome issues, switch `kotlin { jvmToolchain(...) }` to a JetBrains Runtime vendor.
* Jewel 0.41 release notes warn that packaged standalone apps can crash with
  `NoSuchMethodError: kotlinx.coroutines.BuildersKt.runBlockingK$default` because the Icons API
  modules pull in the IntelliJ Platform fork of `kotlinx-coroutines-core`. If that hits, add the
  `replacedBy("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm", ...)` module substitution from
  the Jewel release notes to `desktop/build.gradle`.
* UI is single-file (`src/main/kotlin/dev/gokhun/convert/desktop/Main.kt`); conversion runs on
  the UI thread, fine for small files, not for huge ones.
