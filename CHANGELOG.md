# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [3.6.2] - 2026-09-12

### Fixed
- Network check now uses a 2-second timeout per host to avoid long DNS hangs
- `java_path` is validated before use, falls back to `java` if not found
- Download retry on HTTP 5xx errors
- CI workflow only updates the project version, not dependency versions

## [3.6.1] - 2026-09-12

### Added
- `network_check` option in `server_starter.conf` to skip the internet connection check
- `auto_update_loader` option for automatic Forge/NeoForge updates on every start
- `/restart` command support, both from server console and the Server Starter terminal
- Java 21/25 support with updated class version checks
- 1.7.10 legacy Forge support (named regex groups)
- Manual smoke tests for `FileOperation`, JSON parsing and loader patterns
- Maven build (`pom.xml`) and GitHub Actions Maven workflow
- Download progress logging for large installer JARs/ZIPs
- Log rotation for `logs/server-starter.log` when it exceeds 5 MB
- Typed configuration helpers in `Config.java`

### Changed
- Improved error messages across `Loader.java`, especially for installers and missing files
- Java version is now checked **before** downloading a Forge/NeoForge installer
- `ServerStarter.main()` split into smaller methods (`buildCommandLine()`, `startServer()`)
- README updated with quick start guide and configuration reference

### Fixed
- Binary file downloads (JAR/ZIP) no longer corrupted by text-mode reading
- NeoForge pattern now matches 4-segment versions like `26.1.2.2-beta`
- NeoForge 26.x Minecraft version mapping
- Console input broken with Java 17+ (replaced `inheritIO()` with explicit stdin/stdout threads)
- `latest`/`recommended` keywords in guided installation now resolve correctly
- Installer stderr output no longer silently discarded
- Various `NullPointerException` crashes in `Data.getJsonValue()` and `Remote.checkForUpdate()`

## [3.5.0] - 2024

### Added
- Forge and NeoForged support with `loaderType` in `forge-auto-install.txt`
- `minecraftVersion` and `loaderVersion` variables
- Auto-installation file generation (`generate_auto_installation_file.bat`/`.sh`)
- Version checker based on GitHub

## [3.0.0] - 2024

### Added
- Guided installation of Forge via console
- Automatic installation with `forge-auto-install.txt`

## [CE22] - 2023

### Added
- Simplified upload: only the Forge-Installer-JAR is needed
- Automatic setup by renaming the JAR to `minecraft_server.jar`