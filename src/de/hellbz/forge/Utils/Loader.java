package de.hellbz.forge.Utils;

import de.hellbz.forge.Utils.ModLoader.Forge;
import de.hellbz.forge.Utils.ModLoader.NeoForge;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.hellbz.forge.Utils.Data.*;

public class Loader {

    public static boolean checkLoaderVersion() throws IOException {

        java.io.File[] currentFiles = Config.rootFolder.listFiles();

        Pattern pattern_auto = Pattern.compile("forge-auto-install.txt", Pattern.CASE_INSENSITIVE);

        Boolean autoFile = false;
        String loaderType = null;

        try {
            for (int i = 0; i < currentFiles.length; i++) {

                Matcher matcher_auto = pattern_auto.matcher(currentFiles[i].getName());
                Matcher matcher_forge = Config.Pattern_Forge.matcher(currentFiles[i].getName());

                if (matcher_auto.find()) autoFile = true;
                if (matcher_forge.find()) return false;
            }
        } catch (Exception e) {
            Config.startupError = true;
            LogWarning("Could not read files in server folder: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }

        java.io.File autoConfigFile = new java.io.File("forge-auto-install.txt");

        // get the new versions map
        Config.forgeVersions = Forge.getVersions();

        // get the new NEO versions map
        Config.neoVersions = NeoForge.getVersions();

        if (autoFile && autoConfigFile.exists()) {

            FileReader autoReader = new FileReader(autoConfigFile);
            Config.autoProps = new Properties();
            Config.autoProps.load(autoReader);
            autoReader.close();

            Config.minecraftVersion = Config.autoProps.getProperty("minecraftVersion");
            loaderType = Config.autoProps.getProperty("loaderType");
            Config.loaderVersion = Config.autoProps.getProperty("loaderVersion");

            if (Config.minecraftVersion == null || Config.minecraftVersion.trim().isEmpty()
                    || loaderType == null || loaderType.trim().isEmpty()
                    || Config.loaderVersion == null || Config.loaderVersion.trim().isEmpty()) {

                FileOperation.downloadOrReadFile("/res/forge-auto-install.txt", Config.rootFolder + File.separator + "forge-auto-install.txt");
                LogWarning("\"forge-auto-install.txt\" is missing or incomplete (minecraftVersion/loaderType/loaderVersion). A default file has been created.");
                Config.startupError = true;
                return false;
            }

            if (!Config.minecraftVersion.matches("(?i)^[0-9.]+$|^latest$")) {
                LogWarning("\"minecraftVersion\" in \"forge-auto-install.txt\" must be a valid Minecraft version (e.g. \"1.20.4\") or \"latest\".");
                Config.startupError = true;
                return false;
            }

            if (!Config.loaderVersion.matches("(?i)^[0-9.]+$|^latest$|^recommended$")) {
                LogWarning("\"loaderVersion\" in \"forge-auto-install.txt\" must be a valid version number (e.g. \"1.20.4\"), \"latest\" or \"recommended\".");
                Config.startupError = true;
                return false;
            }

            if (!loaderType.matches("(?i)^forge$|^(neo)?forge$")) {
                LogWarning("\"loaderType\" in \"forge-auto-install.txt\" must be \"forge\" or \"neoforge\".");
                Config.startupError = true;
                return false;
            }

            if (loaderType.equalsIgnoreCase("forge")) {
                Config.isForge = true;
                if (Config.minecraftVersion.equalsIgnoreCase("latest")) {
                    Config.minecraftVersion = Config.forgeVersions.keySet().iterator().next();
                }

                if (Config.forgeVersions.containsKey(Config.minecraftVersion) && Config.loaderVersion.equalsIgnoreCase("latest")) {
                    Config.loaderVersion = Config.forgeVersions.get(Config.minecraftVersion).get("latest").toString();
                } else if (Config.forgeVersions.containsKey(Config.minecraftVersion)
                        && Config.forgeVersions.get(Config.minecraftVersion).containsKey("recommended")
                        && Config.loaderVersion.equalsIgnoreCase("recommended")) {
                    Config.loaderVersion = Config.forgeVersions.get(Config.minecraftVersion).get("recommended").toString();
                }

            } else if (loaderType.equalsIgnoreCase("neoforge")) {
                Config.isForge = false;
                if (Config.minecraftVersion.equalsIgnoreCase("latest")) {
                    Config.minecraftVersion = Config.neoVersions.keySet().iterator().next();
                }

                if (Config.neoVersions.containsKey(Config.minecraftVersion) && Config.loaderVersion.equalsIgnoreCase("latest")) {
                    Config.loaderVersion = Config.neoVersions.get(Config.minecraftVersion).get("latest").toString();
                }
            }

            LogInfo("Found \"forge-auto-install.txt\" with Minecraft-Version " + Config.minecraftVersion
                    + " and " + (Config.isForge ? "Forge" : "NeoForge") + " " + Config.loaderVersion);
            return true;

        } else {

            // GUIDED installation
            LogWarning("\"forge-auto-install.txt\" not found. Starting guided installation...");

            LogInfo("FORGE is available in the following Versions:");
            String ForgeVersionsAsString = String.join(", ", Config.forgeVersions.keySet());
            LogInfo(ForgeVersionsAsString);

            LogInfo("NeoFORGED is available for the following Minecraft-Versions:");
            String NeoForgeVersionsAsString = String.join(", ", Config.neoVersions.keySet());
            LogInfo(NeoForgeVersionsAsString);

            Scanner in = new Scanner(System.in);

            String latestMinecraftReleaseVersion = null;
            FileOperation fileDownload = FileOperation.downloadOrReadFile("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json");
            if (fileDownload != null && fileDownload.getResponseCode() == 200) {
                latestMinecraftReleaseVersion = getJsonValue(fileDownload.getContent().toString(), "latest/release");
            }
            LogInfo("Which MINECRAFT-Version you like to install [ eg. " + latestMinecraftReleaseVersion + " ]:");

            String mcVersionInput = in.nextLine().trim();
            Config.minecraftVersion = mcVersionInput;

            if (!Config.forgeVersions.containsKey(Config.minecraftVersion) && !Config.neoVersions.containsKey(Config.minecraftVersion)) {
                LogError("Minecraft version \"" + Config.minecraftVersion + "\" is not available for Forge or NeoForge.");
                checkLoaderVersion();
                return false;
            }

            boolean hasForge = Config.forgeVersions.containsKey(Config.minecraftVersion);
            boolean hasNeo = Config.neoVersions.containsKey(Config.minecraftVersion);

            // Ask user to choose loader type if both are available for this MC version
            if (hasForge && hasNeo) {
                LogInfo("Which MOD-LOADER you like to install? [ forge / neoforge ]:");
                String loaderTypeInput = in.nextLine().trim().toLowerCase();
                while (!loaderTypeInput.equals("forge") && !loaderTypeInput.equals("neoforge")) {
                    LogWarning("Invalid choice. Please type 'forge' or 'neoforge':");
                    loaderTypeInput = in.nextLine().trim().toLowerCase();
                }
                Config.isForge = loaderTypeInput.equals("forge");
            } else {
                Config.isForge = hasForge;
            }

            // Show available versions for the chosen loader
            String loaderVersionInput;
            if (Config.isForge) {
                String latestForge = (String) Config.forgeVersions.get(Config.minecraftVersion).get("latest");
                String recForge = Config.forgeVersions.get(Config.minecraftVersion).containsKey("recommended")
                        ? (String) Config.forgeVersions.get(Config.minecraftVersion).get("recommended") : null;
                LogInfo("Which FORGE-Version you like to install [ Latest: " + latestForge
                        + (recForge != null ? ", Recommended: " + recForge : "") + " ]:");
                LogInfo("You can type 'latest'" + (recForge != null ? ", 'recommended'" : "") + " or a specific version number.");
                LogInfo("You can also install all other Versions, listed on this Site: "
                        + "https://files.minecraftforge.net/net/minecraftforge/forge/index_" + Config.minecraftVersion + ".html");
            } else {
                String latestNeo = (String) Config.neoVersions.get(Config.minecraftVersion).get("latest");
                LogInfo("Which NeoFORGED-Version you like to install [ Latest: " + latestNeo + " ]:");
                LogInfo("You can type 'latest' or a specific version number.");
                LogInfo("You can also install all other Versions, listed on this Site: https://neoforged.net/");
            }

            loaderVersionInput = in.nextLine().trim();

            // Resolve 'latest' and 'recommended' keywords
            if (loaderVersionInput.equalsIgnoreCase("latest") || loaderVersionInput.equalsIgnoreCase("recommended")) {
                if (Config.isForge) {
                    if (loaderVersionInput.equalsIgnoreCase("recommended")
                            && Config.forgeVersions.get(Config.minecraftVersion).containsKey("recommended")) {
                        Config.loaderVersion = (String) Config.forgeVersions.get(Config.minecraftVersion).get("recommended");
                    } else {
                        if (loaderVersionInput.equalsIgnoreCase("recommended")) {
                            LogWarning("No recommended version found, using latest instead.");
                        }
                        Config.loaderVersion = (String) Config.forgeVersions.get(Config.minecraftVersion).get("latest");
                    }
                } else {
                    Config.loaderVersion = (String) Config.neoVersions.get(Config.minecraftVersion).get("latest");
                }
                LogInfo("Resolved '" + loaderVersionInput + "' to version: " + Config.loaderVersion);
            } else {
                Config.loaderVersion = loaderVersionInput;
            }

            return true;
        }
    }

    public static boolean downloadLoader() {

        if (Config.minecraftVersion == null || Config.loaderVersion == null) {
            LogWarning("Loader version or Minecraft version is missing. Please use \"forge-auto-install.txt\" or guided installation.");
            Config.startupError = true;
            return false;
        }

        if (!isJavaVersionSufficient()) {
            Config.startupError = true;
            return false;
        }

        Map<String, String> links = null;
        if (!Config.isForge) {
            links = NeoForge.getFileLinks(Config.loaderVersion);
        } else {
            links = Forge.getFileLinks(Config.minecraftVersion, Config.loaderVersion);
        }

        if (!Config.startupError) {
            FileOperation fileDownload = FileOperation.downloadOrReadFile(links.get("fileURL"), Config.rootFolder + links.get("localFilePath"));
            if (fileDownload != null && fileDownload.getResponseCode() == 200) {
                Data.LogInfo("Loader downloaded: " + links.get("fileURL") + " to " + Config.rootFolder + links.get("localFilePath"));
                return true;
            } else {
                Data.LogError("Error downloading loader. Response code: " + (fileDownload != null ? fileDownload.getResponseCode() : "null"));
                Data.LogError("URL attempted: " + links.get("fileURL"));
                return false;
            }
        } else return false;
    }

    private static boolean isJavaVersionSufficient() {
        Data.VersionComparator vc = new Data.VersionComparator();
        int requiredClassVersion;
        String requiredJava;

        if (!Config.isForge) {
            // NeoForge
            if (vc.compare(Config.minecraftVersion, "1.21.1") >= 0) {
                requiredClassVersion = 65; // Java 21
                requiredJava = "21";
            } else if (vc.compare(Config.minecraftVersion, "1.20.4") >= 0) {
                requiredClassVersion = 61; // Java 17
                requiredJava = "17";
            } else {
                requiredClassVersion = 52; // Java 8
                requiredJava = "8";
            }
        } else {
            // Forge
            if (vc.compare(Config.minecraftVersion, "1.18.0") >= 0) {
                requiredClassVersion = 61; // Java 17
                requiredJava = "17";
            } else if (vc.compare(Config.minecraftVersion, "1.17.0") >= 0) {
                requiredClassVersion = 60; // Java 16
                requiredJava = "16";
            } else {
                requiredClassVersion = 52; // Java 8
                requiredJava = "8";
            }
        }

        if (Config.javaVersion < requiredClassVersion) {
            Data.LogError("Java " + requiredJava + " or newer is required for "
                    + (Config.isForge ? "Forge" : "NeoForge") + " " + Config.loaderVersion
                    + " (Minecraft " + Config.minecraftVersion + "). Your Java class version: " + Config.javaVersion + ".");
            Data.LogError("Install a newer Java version and set it in server_starter.conf (java_path=...) or PATH.");
            return false;
        }

        return true;
    }

    public static boolean checkLocalInstaller() {
        return checkLocalInstaller(false);
    }

    public static boolean checkLocalInstaller(boolean output) {

        java.io.File[] currentFiles = Config.rootFolder.listFiles();
        try {
            for (int i = 0; i < currentFiles.length; i++) {
                Matcher matcherForge = Config.Pattern_Forge.matcher(currentFiles[i].getName());
                Matcher matcherNeoForge = Config.Pattern_NeoForge.matcher(currentFiles[i].getName());

                if (matcherForge.find()) {
                    Config.minecraftVersion = matcherForge.group(1);
                    Config.loaderVersion = matcherForge.group(2);
                    Config.installerFile = currentFiles[i].getName();
                    if (output) LogInfo("Match found INSTALLER with MC-Version " + Config.minecraftVersion + " and Forge " + Config.loaderVersion);
                    return true;
                }
                if (matcherNeoForge.find()) {
                    String neoVer = matcherNeoForge.group(1); // e.g. "26.1.2.2" or "21.1.3"
                    String[] parts = neoVer.split("\\.");
                    // NeoForge versioning:
                    // Old (major < 26): 3 segments, first two = MC minor+patch -> "1.major.minor"
                    //   e.g. 21.1.3 -> MC 1.21.1
                    // New (major >= 26): 4 segments, first three = full MC version -> "major.minor.patch"
                    //   e.g. 26.1.2.2-beta -> MC 26.1.2
                    int neoMajorInt = 0;
                    try { neoMajorInt = Integer.parseInt(parts[0]); } catch (NumberFormatException ignored) {}
                    if (neoMajorInt < 26) {
                        String neoMajor = parts.length > 0 ? parts[0] : "0";
                        String neoMinor = parts.length > 1 ? parts[1] : "0";
                        Config.minecraftVersion = "1." + neoMajor + "." + neoMinor;
                    } else {
                        String p0 = parts.length > 0 ? parts[0] : "0";
                        String p1 = parts.length > 1 ? parts[1] : "0";
                        String p2 = parts.length > 2 ? parts[2] : "0";
                        Config.minecraftVersion = p0 + "." + p1 + "." + p2;
                    }
                    Config.loaderVersion = neoVer;
                    Config.installerFile = currentFiles[i].getName();
                    if (output) LogInfo("Match found INSTALLER with MC-Version " + Config.minecraftVersion + " and NeoForge " + Config.loaderVersion);
                    return true;
                }
            }
        } catch (Exception e) {
            Config.startupError = true;
            LogWarning("Could not read files in server folder: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
        return false;
    }

    public static boolean installLoader() {

        if (Config.installerFile != null) {
            try {
                final String filename = new java.io.File(Config.installerFile).getName();

                LogInfo("Attempting to start Server " + Config.installerFile);
                LogDebug("Filename: " + filename);
                LogDebug("Directory: " + Config.rootFolder);
                LogInfo("Starting installation of Loader, installer output incoming");
                LogInfo("Check log from installer for more information");

                String javaStart = Config.getJavaPath();
                if (javaStart != null && !javaStart.equals("java")) {
                    LogDebug("Use for Installer Custom Java Path: " + javaStart);
                }

                final Process installer = new ProcessBuilder(new String[]{javaStart, "-jar", Config.installerFile, "nogui", "--installServer"})
                        .directory(Config.rootFolder)
                        .redirectErrorStream(true)
                        .start();
                final Scanner serverLog = new Scanner(installer.getInputStream());
                while (serverLog.hasNextLine()) {
                    final String println = serverLog.nextLine();
                    LogCustom(println, "FORGE-Installer", TXT_PURPLE);
                }
                installer.waitFor();

                java.io.File libraries_dir = new java.io.File("libraries/");
                if (libraries_dir.exists()) {

                    LogInfo("Done installing loader...");
                    LogInfo("Deleting leftover Files, after installation!");

                    final java.io.File installerFile = new java.io.File(Config.rootFolder + java.io.File.separator + Config.installerFile);
                    if (installerFile.exists()) Files.delete(installerFile.toPath());

                    final java.io.File installerFileLog = new java.io.File(Config.rootFolder + java.io.File.separator + Config.installerFile + ".log");
                    if (installerFileLog.exists()) Files.delete(installerFileLog.toPath());

                    final java.io.File installerFileRunBat = new java.io.File(Config.rootFolder + java.io.File.separator + "run.bat");
                    if (installerFileRunBat.exists()) Files.delete(installerFileRunBat.toPath());

                    final java.io.File installerFileRunSh = new java.io.File(Config.rootFolder + java.io.File.separator + "run.sh");
                    if (installerFileRunSh.exists()) Files.delete(installerFileRunSh.toPath());

                    return false;

                } else {
                    LogError("Forge/NeoForge installer finished, but the \"libraries\" folder was not created. The installer JAR may be corrupt or the Java version is incompatible.");
                    Config.startupError = true;
                    return true;
                }

            } catch (IOException | InterruptedException e) {
                LogWarning("Could not run the loader installer: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
                Config.startupError = true;
                return true;
            }
        } else {
            LogError("No installer file found and no \"libraries\" folder present. Place a Forge or NeoForge installer JAR next to the starter or use \"forge-auto-install.txt\".");
            Config.startupError = true;
            return true;
        }
    }

    public static void checkLoaderFolder() {

        Data.LogDebug("Current Path: " + Config.rootFolder);

        if (Config.librariesFolder.exists() && Config.librariesFolder.isDirectory()) {

            if ((Config.minecraftForgeFolder.exists() && Config.minecraftForgeFolder.isDirectory())
                    || (Config.neoForgeFolder.exists() && Config.neoForgeFolder.isDirectory())) {

                Config.isForge = Config.minecraftForgeFolder.exists() && Config.minecraftForgeFolder.isDirectory();
                File loaderFolder = Config.isForge ? Config.minecraftForgeFolder
                        : (Config.neoForgeFolder.exists() && Config.neoForgeFolder.isDirectory() ? Config.neoForgeFolder : null);
                Pattern pattern = Config.isForge
                        ? Pattern.compile("(?<minecraftVersion>[.0-9]+)-(?<loaderVersion>[.0-9]+)")
                        : Pattern.compile("(?<minecraftVersion>\\d+\\.\\d+)\\.(?<loaderVersion>\\d+).*");

                String LoaderPath = loaderFolder.getPath().replace("\\", "/");
                Data.LogDebug(LoaderPath + " exists");

                FileFilter folderFilter = file -> file.isDirectory()
                        && new File(file, "unix_args.txt").exists()
                        && new File(file, "win_args.txt").exists();

                File[] versionFolders = loaderFolder.listFiles(folderFilter);

                if (versionFolders != null && versionFolders.length > 0) {

                    Arrays.sort(versionFolders, Comparator.comparing(File::getName, new Data.VersionComparator()).reversed());
                    File latestVersionFolder = versionFolders[0];

                    if (latestVersionFolder != null) {

                        String systemPrefix = Config.OS.contains("win") ? "win_" : "unix_";

                        String startFolder = "libraries" + File.separator;
                        int librariesIndex = latestVersionFolder.getAbsolutePath().indexOf(startFolder);
                        if (librariesIndex >= 0) {
                            startFolder = latestVersionFolder.getAbsolutePath().substring(librariesIndex);
                        }

                        Data.LogDebug("Using " + (Config.OS.contains("win") ? "WINDOWS" : "UNIX")
                                + " System-Parameter for " + (Config.isForge ? "Forge" : "NeoForge") + " folder");
                        Config.startupFile = startFolder + File.separator + systemPrefix + "args.txt";

                        Matcher matcher = pattern.matcher(latestVersionFolder.getName());
                        if (matcher.matches()) {
                            if (Config.isForge) {
                                Config.minecraftVersion = matcher.group("minecraftVersion");
                                Config.loaderVersion = matcher.group("loaderVersion");
                            } else {
                                // NeoForge folder name = full NeoForge version e.g. "26.1.2.2-beta" or "21.1.3"
                                // Derive MC version using same logic as NeoForge.getVersions()
                                String folderName = latestVersionFolder.getName();
                                String[] fp = folderName.split("\\.");
                                int neoMaj = 0;
                                try { neoMaj = Integer.parseInt(fp[0]); } catch (NumberFormatException ignored) {}
                                if (neoMaj < 26) {
                                    // Old scheme: first two = MC minor+patch -> "1.major.minor"
                                    Config.minecraftVersion = "1." + (fp.length > 0 ? fp[0] : "0") + "." + (fp.length > 1 ? fp[1] : "0");
                                } else {
                                    // New scheme: first three = full MC version -> "major.minor.patch"
                                    Config.minecraftVersion = (fp.length > 0 ? fp[0] : "0") + "." + (fp.length > 1 ? fp[1] : "0") + "." + (fp.length > 2 ? fp[2] : "0");
                                }
                                Config.loaderVersion = folderName;
                            }
                            LogInfo("Found Minecraft: " + Config.minecraftVersion
                                    + " with " + (Config.isForge ? "Forge" : "NeoForge") + "-Version: " + Config.loaderVersion);
                        }
                        Data.LogDebug("Found MC-Version: " + Config.minecraftVersion);
                        Data.LogDebug("Found Loader-Version: " + Config.loaderVersion);
                        Data.LogDebug("Startup-File: " + Config.startupFile);
                    }
                }
            }
        }
    }

    public static void checkLocalFolder() {

        Comparator<String> versionComparator = new VersionComparator();

        if (versionComparator.compare(Config.minecraftVersion, "1.17.0") < 0
                || (Config.isForge && versionComparator.compare(Config.minecraftVersion, "1.20.4") >= 0)) {

            FilenameFilter filter = (dir, name) -> Config.Pattern_Forge_startfile.matcher(name).matches();

            File[] matchingFiles = Config.rootFolder.listFiles(filter);

            if (matchingFiles != null && matchingFiles.length > 0) {

                java.io.File matchingFile = matchingFiles[0];
                Matcher matcher = Config.Pattern_Forge_startfile.matcher(matchingFile.getName());

                if (matcher.matches()) {
                    // group(1) = prefix ("forge-" or "minecraftforge-universal-")
                    // group(2) = MC version, group(3) = Forge version
                    Config.minecraftVersion = matcher.group(2);
                    Config.loaderVersion = matcher.group(3);
                    Config.startupFile = matchingFile.getName();
                    LogInfo("Found Minecraft: " + Config.minecraftVersion + " with Forge " + Config.loaderVersion);
                    return;
                }

                Data.LogDebug("Found file in root directory:");
                Data.LogDebug("File: " + matchingFile.getName());
                Data.LogDebug("Absolute path: " + matchingFile.getAbsolutePath());
                Data.LogDebug("Size: " + matchingFile.length() + " bytes");
                Data.LogDebug("Last modified: " + matchingFile.lastModified());
            } else {
                LogError("No valid Forge server start file found in the server folder.");
                Config.startupError = true;
            }
        }
    }

    /**
     * Forge-Version-Update feature:
     * Checks if a newer version of Forge/NeoForge is available for the currently installed
     * Minecraft version and updates if the auto-update option is enabled in config.
     */
    public static void checkAndUpdateLoader() {
        if (Config.minecraftVersion == null || Config.loaderVersion == null) return;

        String loaderLabel = Config.isForge ? "Forge" : "NeoForge";
        if (!Config.isAutoUpdateLoaderEnabled()) {
            LogInfo("Forge-Version-Update: Disabled (set auto_update_loader=true in " + Config.PROPERTIES_FILE + " to enable).");
            return;
        }

        LogInfo("Forge-Version-Update: Checking for newer " + loaderLabel + " version for MC " + Config.minecraftVersion + "...");

        // Refresh version lists
        Map<String, Map<String, Object>> versions = Config.isForge ? Forge.getVersions() : NeoForge.getVersions();
        if (versions == null) {
            LogWarning("Forge-Version-Update: Could not fetch version list, skipping update.");
            return;
        }

        String mcKey = Config.minecraftVersion;
        if (!versions.containsKey(mcKey)) {
            LogInfo("Forge-Version-Update: No update information available for " + loaderLabel + " on MC " + mcKey + ". Skipping.");
            return;
        }

        String latestVersion = (String) versions.get(mcKey).get("latest");
        if (latestVersion == null) {
            LogDebug("Forge-Version-Update: Could not determine latest version.");
            return;
        }

        Data.VersionComparator vc = new Data.VersionComparator();
        if (vc.compare(Config.loaderVersion, latestVersion) < 0) {
            LogWarning("Forge-Version-Update: Newer " + loaderLabel + " version available: " + latestVersion + " (installed: " + Config.loaderVersion + ")");
            LogInfo("Forge-Version-Update: Downloading " + loaderLabel + " " + latestVersion + "...");

            String previousVersion = Config.loaderVersion;
            Config.loaderVersion = latestVersion;

            Map<String, String> links = Config.isForge
                    ? Forge.getFileLinks(Config.minecraftVersion, latestVersion)
                    : NeoForge.getFileLinks(latestVersion);

            FileOperation dl = FileOperation.downloadOrReadFile(links.get("fileURL"), Config.rootFolder + links.get("localFilePath"));
            if (dl != null && dl.getResponseCode() == 200) {
                LogInfo("Forge-Version-Update: Downloaded " + loaderLabel + " " + latestVersion);
                Config.installerFile = new File(Config.rootFolder + links.get("localFilePath")).getName();
                installLoader();
                if (!Config.startupError) {
                    // Refresh loader folder references after update
                    checkLoaderFolder();
                    checkLocalFolder();
                    LogInfo("Forge-Version-Update: Successfully updated to " + loaderLabel + " " + latestVersion);
                } else {
                    LogWarning("Forge-Version-Update: Update failed, reverting to previous version " + previousVersion);
                    Config.loaderVersion = previousVersion;
                    Config.startupError = false;
                }
            } else {
                LogWarning("Forge-Version-Update: Download failed (HTTP " + (dl != null ? dl.getResponseCode() : "null") + "), keeping current version " + previousVersion);
                Config.loaderVersion = previousVersion;
            }
        } else {
            LogInfo("Forge-Version-Update: Already on the latest " + loaderLabel + " version: " + Config.loaderVersion);
        }
    }
}
