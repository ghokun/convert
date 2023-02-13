# Convert

This is a data file converter that supports following files:

- [CSV](https://www.rfc-editor.org/rfc/rfc4180)
- [JSON](https://www.rfc-editor.org/rfc/rfc8259)
- [YAML](https://yaml.org/spec/history/2001-05-26.html)
- [TOML](https://toml.io/en/)
- [properties](https://en.wikipedia.org/wiki/.properties)

## Install

```shell
brew install ghokun/tap/convert

# or
brew tap ghokun/tap
brew install convert

# Windows users can download from releases page
```

## Usage

```bash
# Simple conversions
convert --input abc.csv --output abc.json
convert --input abc.yaml --output abc.properties
convert --input abc.toml --output abc.csv

# Prettify json
convert --input abc.json --output pretty-abc.json --pretty

# Minify json
convert --input pretty-abc.json --output abc.json

# Indent yaml
#
# abc:       abc:
# - item1 ->   - item1
# - item2      - item2
#
convert --input abc.yaml --output indented-abc.yaml --indent-yaml

# Minimize yaml quotes
#
# abc: "Hello world!" -> abc: Hello world!
convert --input abc.yaml --output abc-without-quotes.yaml --minimize-yaml-quotes

# Deduplicate keys
# This:
# [
#   {
#     "key1": "value1",
#     "key2": "value2"
#   },
#   {
#     "key1": "value3",
#     "key2": "value4"
#   }
# ]
#
# Becomes this:
# {
#   "keys" : [ "key1", "key2" ],
#   "values" : [ [ "value1", "value2" ], [ "value3", "value4" ] ]
# }
convert --input abc.json --output dedup-abc.json --deduplicate-keys
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

# Generate a distributable package with VERSION
./gradlew -Pversion=${VERSION} nativeCompile generatePackage
```

## TODO list

- [x] Basic setup (project structure, formatting, static analysis, testing)
- [x] Implement all possible file conversions
  - [x] CSV needs special implementation, others are trivial
- [ ] GraalVM setup
  - [x] Native compilation
  - [x] Generate/Analyze/Validate reflection configs
  - [ ] Visualize image size with GraalVM Dashboard
- [x] Set up JReleaser to release on:
  - [x] GitHub
  - [x] Homebrew
- [x] Build for OS / Arch combinations:
  - [x] Windows - x86_64
  - [x] Darwin - x86_64
  - [x] Darwin - aarch64 (self hosted runner)
  - [x] Linux - x86_64
  - [x] Linux - aarch64 (self hosted docker runner)
- [ ] Streaming support
  - [ ] Read data from `STDIN`
  - [ ] Handle huge files
  - [ ] Handle partial inputs (streaming json logs ?)
- [x] Minify binary with UPX on supported platforms
- [ ] Have fun
