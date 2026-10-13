# ui-javafx (experiment)

Minimal JavaFX desktop UI for `convert`, reusing the `:lib` module.

## UI

- Two side-by-side editors: LEFT = editable input, RIGHT = read-only output.
- Both editors are RichTextFX `CodeArea`s with a shared monospace font
  (`-fx-font-family: monospace; -fx-font-size: 13px;`).
- Input-format and output-format `ComboBox`es with values
  `json, yaml, csv, tsv, toml, properties`; defaults `json -> yaml`.
- Options preserved: `csv-separator` (default `,`), `pretty=false`,
  `indentYaml=true`, `minimizeYamlQuotes=true`, `deduplicateKeys=false`.
- `Convert` button + status `Label` preserved.
- Conversion runs on a background daemon thread via the `:lib` stream API
  (`convert(InputStream, String, OutputStream, String, ConverterOptions)`,
  UTF-8 bytes of the input-area text); UI updates go through
  `Platform.runLater`. Failures (`IOException | IllegalArgumentException`)
  are shown in the status label.
- Input area is prefilled with a small sample JSON document.

## Syntax highlighting

- RichTextFX `org.fxmisc.richtext:richtextfx:0.11.7` (latest on Maven Central,
  resolves and compiles against JavaFX 25 / JDK 25).
- Generic regex `StyleSpans` highlighter covering strings, numbers, booleans,
  null, comments (`#`, `//`, `;`), and `key:`/`key=` at line starts.
- Styles live in
  `src/main/resources/dev/gokhun/convert/ui/javafx/syntax.css`
  (`.string`, `.number`, `.boolean`, `.null`, `.comment`, `.key`).
- Input re-highlights debounced (~100 ms); output re-highlights after each
  conversion.

## Versions

- JavaFX SDK: `25` (supports JDK 25)
- Gradle plugin `org.openjfx.javafxplugin`: `0.1.0` (latest)
- Gradle plugin `com.gluonhq.gluonfx-gradle-plugin`: `1.0.29` (latest, June 2026)
- RichTextFX: `0.11.7` (Maven Central, latest Nov 2025)

## How to run (JVM, verified)

```sh
./gradlew :ui-javafx:run
```

## How a native build would run (NOT verified in this experiment)

```sh
./gradlew :ui-javafx:nativeBuild
# or: ./gradlew :ui-javafx:build :ui-javafx:nativeBuild :ui-javafx:nativeRun
```

A `gluonfx { ... }` block is present in `ui-javafx/build.gradle` with
`target = "host"`, `appIdentifier = "dev.gokhun.convert.uijavafx"`, a small
Jackson `reflectionList`, and a `resourcesList` stub. There is also a stub
`src/main/resources/META-INF/gluonfx/reflectionconfig.json`.

## Caveats

- Only the JVM `build`/`run` path is verified in this spike. `nativeBuild` was
  intentionally NOT attempted (too heavy for a spike).
- A real native image needs GraalVM plus platform SDKs installed; there is no
  cross-compilation (build on the OS/arch you want to ship).
- Full native-image metadata still needs GraalVM agent runs:
  `./gradlew :ui-javafx:nativeRunAgent`, then review/copy the generated config
  into the Gluon/reflection config before `nativeBuild`.
- The Gluon `reflectionList` does not yet include RichTextFX classes; a native
  image would likely need extra RichTextFX/ReactFX/Flowless reflection entries
  plus the `syntax.css` resource pattern.
- JavaFX needs a display; headless CI cannot launch the UI without extra setup
  (e.g. Xvfb/Monocle).
