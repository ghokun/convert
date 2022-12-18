# Convert

This is a data file converter that will support following data file formats eventually:

- [ ] [CSV](https://www.rfc-editor.org/rfc/rfc4180)
- [ ] [JSON](https://www.rfc-editor.org/rfc/rfc8259)
- [ ] [YAML](https://yaml.org/spec/history/2001-05-26.html)
- [ ] [TOML](https://toml.io/en/)
- [ ] [properties](https://en.wikipedia.org/wiki/.properties)

## Usage

```bash
convert --from abc.csv --to abc.json
```

## Purpose of another converter

I am aware that there are many file converters, even online ones, available for free. I wanted to practice/learn the following technologies:

- [Gradle](https://gradle.org)
- Native compilation with [GraalVM](https://www.graalvm.org)
- [JReleaser](https://jreleaser.org)
- [Releasing a homebrew formula](https://docs.brew.sh/Adding-Software-to-Homebrew)
- Visualizing native image size with [GraalVM Dashboard](https://www.graalvm.org/dashboard/?ojr=help%3Btopic%3Dgetting-started.md)
- Even smaller binaries with [UPX](https://upx.github.io)
