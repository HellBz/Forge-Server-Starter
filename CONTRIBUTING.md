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

- Java 17+ (for building)
- Maven 3.8+
- The output JAR is compatible with Java 8+

## Project Structure

- `src/de/hellbz/forge/` — Java source code
- `res/` — bundled resources (config templates, modInfo.json)
- `src/test/` — manual smoke tests
- `pom.xml` — Maven build configuration

## Releasing

Releases are created on GitHub. The `maven-release.yml` workflow:
1. Bumps the version in `pom.xml` and `res/modInfo.json` from the release tag
2. Builds the shaded JAR
3. Attaches the JAR and a ZIP bundle with start scripts to the release