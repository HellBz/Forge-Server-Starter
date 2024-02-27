# FORGE Server Starter - Simplify Your Server Launch

In light of the difficulties many are currently facing when launching their FORGE servers with server hosts, I have developed a small JAR file. This file takes on the roles of the traditional .sh or .bat files and offers a suite of useful features to simplify and automate the server launch process.

![](https://media.forgecdn.net/attachments/725/476/screenshot-2023-09-07-012517.png)

## Key Features

- **Library Check:** Ensures the `libraries` folder exists
- **FORGE Version Detection:** Checks for a FORGE version in the `libraries` folder
- **System-Specific Arguments:** Opens argument files for Windows or Linux, depending on the operating system
- **Server Launch:** Starts the server with the pre-defined Java arguments as well as those specific to FORGE
- **Runtime Monitoring:** Continuously checks if the server is still online
- **Console Output:** Outputs the complete console of the started server
- **Console Commands:** Allows sending commands to the server via the console
- **Automatic EULA Creation:** Automatically creates `eula.txt` for faster startup
- **Time Zone Setting:** Allows setting up a custom timezone, defined in `server_starter.conf`
- **Custom Java Path:** Allows running the server with a custom Java path, defined in `server_starter.conf`
- **Debugging:** Logs all events from the Server Starter, defined in `server_starter.conf`.
- **Automatic Start:** Enables automatic server start with just two files since the CE22 update.
- **FORGE Installation:** Installs FORGE with guided installation in the console or with `forge-auto-installer.txt`
- **Forge and NeoForged Support:** Now supports both Forge and NeoForged. The `forge-auto-installer.txt` includes a `loaderType` variable for distinguishing between FORGE and NeoForged during installation.
- **Detailed Configurations:** Added detailed explanations of settings in `forge-auto-installer.txt` and `server_starter.conf`.
- **Auto-Installation File Generation:** Introduces `generate_auto_installation_file.bat`/`.sh` for the automatic creation of `forge-auto-installer.txt`
- **Version Checker:** Features a version checker that uses `version.xml` to check for updates on GitHub and logs changes along with the download URL in the starter's log.


## What's New?

### 3.5 Update
- **Forge and NeoForged Support:** Enhanced compatibility with `forge-auto-installer.txt` including `loaderType` for easy selection.
- **Updated Variables:** Streamlined to `minecraftVersion` and `loaderVersion`, ensuring forward compatibility.
- **Detailed Configurations:** Comprehensive settings explanations added to documentation for clarity.
- **Auto-Installation File Generation:** Simplified setup with `generate_auto_installation_file.bat` / `sh`.
- **Version Checker:** Automated updates and download notifications via `version.xml`.

### 3.0 Update
- **Guided Installation:** Installation of Forge via the console.
- **Automatic Forge Installation:** Option for automatic installation of Forge with `forge-auto-installer.txt`.

### CE22 Update
- **Simplified Upload:** Only upload the Forge-Installer-JAR-File (no more uploading the `libraries` folder).
- **Automatic Setup:** Rename the Forge-Server-Starter, e.g., to `minecraft_server.jar`. Everything else is done automatically upon server start.

## Planned Features

- **Faster Restart:** Restart the server with the `/restart` command, directly from the Forge server launcher.
- **Forge-Version-Update:** automatic update the Forge-Version to the newest at every Server-Start

## Feedback and Suggestions

We welcome any feedback or suggestions for improvement. Feel free to open an issue or send a bug report.

-> [Report Issue here](https://legacy.curseforge.com/minecraft/mc-mods/forge-server-starter/issues)

Optimize your FORGE server launch with our Server Starter and enjoy a seamless gaming experience.
