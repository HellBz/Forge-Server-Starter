# Contributing

Thank you for your interest in improving Forge Server Starter!

## How to Contribute

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/my-improvement`)
3. Make your changes
4. Build the project with Maven: `mvn package`
5. Test the generated `target/minecraft_server.jar`
6. Commit your changes with clear messages
7. Open a pull request

## Build Requirements

- Java 8+ runtime (the output JAR is Java 8-compatible)
- Maven 3.8+ **or** the provided `build.bat` / `build.sh` scripts

## Building Locally

Without Maven installed, run the manual build scripts:

- Windows: `build.bat`
- Linux/macOS: `./build.sh`

This builds `out/compiled/minecraft_server.jar`.

With Maven installed:

```bash
mvn package
```

The shaded JAR is then available at `target/minecraft_server.jar`.

## Project Structure

- `src/de/hellbz/forge/` — Java source code
- `res/` — bundled resources (config templates, modInfo.json)
- `src/test/` — manual smoke tests
- `pom.xml` — Maven build configuration
- `build.bat` / `build.sh` — manual build without Maven

## Releasing

Releases are created on GitHub. The `maven-release.yml` workflow:
1. Bumps the version in `pom.xml` and `res/modInfo.json` from the release tag
2. Builds the shaded JAR
3. Attaches the JAR and a ZIP bundle with start scripts to the release