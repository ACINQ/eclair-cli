# Building

`eclair-cli` is a [Kotlin Native](https://kotlinlang.org/docs/native-overview.html) command-line application.
It runs on various desktop platforms, including linux, macOS and windows.

## Build

You should start by cloning the repository locally:

```sh
git clone git@github.com:ACINQ/eclair-cli.git
cd eclair-cli
```

To build the project library and execute tests, you can run:

```sh
./gradlew build
```

You can then execute the built application:

```sh
./build/bin/native/releaseExecutable/eclair-cli.kexe
```
