# ui-javafx (experiment)

Minimal JavaFX desktop UI for `convert`, reusing the `:lib` module.

## Versions

- JavaFX SDK: `25` (supports JDK 25)
- Gradle plugin `org.openjfx.javafxplugin`: `0.1.0` (latest)
- Gradle plugin `com.gluonhq.gluonfx-gradle-plugin`: `1.0.29` (latest, June 2026)

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
- JavaFX needs a display; headless CI cannot launch the UI without extra setup
  (e.g. Xvfb/Monocle).
