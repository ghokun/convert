# desktop (experiment)

Minimal JVM desktop UI for `convert`, built with Compose Multiplatform and styled with the
Jewel standalone (Int UI) theme. Reuses `:lib` (`Converter.create().convert(...)` file overload).
This is a spike: desktop-only, no signing, no GraalVM native-image.

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
