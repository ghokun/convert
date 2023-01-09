# Convert

This is a data file converter that will support following data file formats eventually:

- [ ] [CSV](https://www.rfc-editor.org/rfc/rfc4180)
- [ ] [JSON](https://www.rfc-editor.org/rfc/rfc8259)
- [ ] [YAML](https://yaml.org/spec/history/2001-05-26.html)
- [ ] [TOML](https://toml.io/en/)
- [ ] [properties](https://en.wikipedia.org/wiki/.properties)

## Usage

```bash
convert --input abc.csv --output abc.json
```

## Purpose of another converter

I am aware that there are many file converters, even online ones, available for free. Consider this as a playground since I wanted to practice/learn the
following technologies:

- [Gradle](https://gradle.org)
- Native compilation with [GraalVM](https://www.graalvm.org)
- [JReleaser](https://jreleaser.org)
- [Releasing a homebrew formula](https://docs.brew.sh/Adding-Software-to-Homebrew)
- Visualizing native image size
  with [GraalVM Dashboard](https://www.graalvm.org/dashboard/?ojr=help%3Btopic%3Dgetting-started.md)
- Even smaller binaries with [UPX](https://upx.github.io)

## Development

```bash
# Switch java version (If you are using SDKMAN!)
sdk env

# Format code
./gradlew spotlessApply

# Do static analysis, compile and test
./gradlew check

# Compile the project and build a native executable
./gradlew nativeRun

# Run the native executable
./build/native/nativeCompile/convert

# Run the application with the agent on JVM
./gradlew -Pagent run

# Copy metadata into /META-INF/native-image directory
./gradlew metadataCopy --task run --dir src/main/resources/META-INF/native-image

# Build a native executable using metadata
./gradlew nativeCompile

# Run the native executable
./build/native/nativeCompile/convert

# Run JUnit tests
./gradlew nativeTest

# Run tests on JVM with the agent
./gradlew -Pagent test

# Test building a native executable using metadata
./gradlew -Pagent nativeTest
```

## TODO list

- [x] Basic setup (project structure, formatting, static analysis, testing)
- [ ] Implement all possible file conversions
  - [ ] CSV needs special implementation, others are trivial
- [ ] GraalVM setup
  - [x] Native compilation
  - [x] Generate/Analyze/Validate reflection configs
  - [ ] Visualize image size with GraalVM Dashboard
- [ ] Set up JReleaser to release on:
  - [ ] GitHub
  - [ ] Homebrew
- [ ] Build for OS / Arch combinations:
  - [ ] Windows - amd64
  - [ ] Darwin - amd64
  - [ ] Darwin - aarch64
  - [ ] Linux - amd64
  - [ ] Linux - aarch64
- [ ] Streaming support
  - [ ] Read data from `STDIN`
  - [ ] Handle huge files
  - [ ] Handle partial inputs (streaming json logs ?)
- [ ] Minify binary with UPX on supported platforms
- [ ] Have fun
