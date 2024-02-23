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

        // try-catch block to handle exceptions
        try {
            //LogInfo("Files are:");

            // Display the names of the files
            for (int i = 0; i < currentFiles.length; i++) {

                Matcher matcher_auto = pattern_auto.matcher(currentFiles[i].getName());
                Matcher matcher_forge = Config.Pattern_Forge.matcher(currentFiles[i].getName());

                //If found Auto-Installer-File, set to true and continue
                if (matcher_auto.find()) autoFile = true;

                //If found Forge-Installer, exit VersionChecker
                if (matcher_forge.find()) return false;

            }
        } catch (Exception e) {
            Config.startupError = true;
            LogWarning(e.getMessage());
        }

        java.io.File autoConfigFile = new java.io.File("forge-auto-install.txt");

        // get the new versions map
        Map<String, Map<String, Object>> forgeVersions = Forge.getVersions();

        // get the first version key
        String firstForgeVersionKey = forgeVersions.keySet().iterator().next();

        // get the new NEO versions map
        Map<String, Map<String, Object>> neoVersions = NeoForge.getVersions();

        // get the first version key
        String firstNeoVersionKey = neoVersions.keySet().iterator().next();


        //LogInfo( firstEntry.toString() );
        if (autoFile && autoConfigFile.exists()) {

            // Search MC-Version and Forge in Auto-Installer-File
            FileReader autoReader = new FileReader(autoConfigFile);
            Config.autoProps = new Properties();
            Config.autoProps.load(autoReader);
            autoReader.close();

            Config.minecraftVersion = Config.autoProps.getProperty("minecraftVersion");
            loaderType = Config.autoProps.getProperty("loaderType");
            Config.loaderVersion = Config.autoProps.getProperty("loaderVersion");

            if (Config.minecraftVersion == null || Config.minecraftVersion.trim().isEmpty() || loaderType == null || loaderType.trim().isEmpty() || Config.loaderVersion == null || Config.loaderVersion.trim().isEmpty() ) {

                FileOperation.downloadOrReadFile("/res/forge-auto-install.txt" , Config.rootFolder + File.separator + "forge-auto-install.txt" );
                LogWarning("Found Error in the \"forge-auto-install.txt\", saved the File correct, please check the File.");
                Config.startupError = true;
                return false;
            }

            if ( !Config.minecraftVersion.matches("(?i)^[0-9.]+$|^latest$" ) ) {
                LogWarning("The Setting minecraftVersion in \"forge-auto-install.txt\", must be \"1.20.4\" or \"latest\".");
                Config.startupError = true;
                return false;
            }

            if ( !Config.loaderVersion.matches("(?i)^[0-9.]+$|^latest$|^recommended$" ) ) {
                LogWarning("The Setting loaderVersion in \"forge-auto-install.txt\", must be \"1.20.4\" or \"latest\" or \"recommended\".");
                Config.startupError = true;
                return false;
            }

            if ( !loaderType.matches( "(?i)^forge$|^(neo)?forge$" ) ) {
                LogWarning("The Setting loaderType in \"forge-auto-install.txt\", must be \"forge\" or \"neoforge\".");
                Config.startupError = true;
                return false;
            }



            if ( loaderType.equalsIgnoreCase("forge") ){
                Config.isForge = true;
                if ( Config.minecraftVersion.equalsIgnoreCase("latest") ) {
                    Config.minecraftVersion = firstForgeVersionKey;
                } else Config.minecraftVersion = Config.minecraftVersion;

                if ( forgeVersions.containsKey(Config.minecraftVersion) && Config.loaderVersion.equalsIgnoreCase("latest") ){
                    Config.loaderVersion = forgeVersions.get(Config.minecraftVersion).get("latest").toString();
                }else if ( forgeVersions.containsKey(Config.minecraftVersion) && forgeVersions.get(Config.minecraftVersion).containsKey("recommended") && Config.loaderVersion.equalsIgnoreCase("recommended") ){
                    Config.loaderVersion = forgeVersions.get(Config.minecraftVersion).get("recommended").toString();
                } else Config.loaderVersion = Config.loaderVersion;

            }else if ( loaderType.equalsIgnoreCase("neoforge") ){
                Config.isForge = false;
                if ( Config.minecraftVersion.equalsIgnoreCase("latest") ){
                    Config.minecraftVersion = firstNeoVersionKey;
                } else Config.minecraftVersion = Config.minecraftVersion;

                if ( neoVersions.containsKey(Config.minecraftVersion) && Config.loaderVersion.equalsIgnoreCase("latest") ){
                    Config.loaderVersion = neoVersions.get(Config.minecraftVersion).get("latest").toString();
                }else Config.loaderVersion = Config.loaderVersion;

            }

            LogInfo("Found \"forge-auto-install.txt\" with Minecraft-Version " + Config.minecraftVersion + " and " + (Config.isForge ? "Forge" : "NeoForge") + " " + Config.loaderVersion );
            return true;

        } else {

            // GUIDED installation
            LogWarning("Not found the \"forge-auto-install.txt\", start manuell installation-Process.");
            LogInfo("FORGE is available in the following Versions:");

            //LogInfo( ForgeLatestVersions.toString() );

            // join all keys
            String ForgeVersionsAsString = String.join(", ", forgeVersions.keySet());
            LogInfo(ForgeVersionsAsString);


            LogInfo("NeoFORGED is available in the following Versions:");
            String NeoForgeVersionsAsString = String.join(", ", neoVersions.keySet());

            LogInfo(NeoForgeVersionsAsString);

            // Using Scanner for Getting Input from User
            Scanner in = new Scanner(System.in);

            LogInfo("Wich MINECRAFT-Version you like to install [ eg. " + (firstForgeVersionKey != null ? firstForgeVersionKey : "") + " ]:");
            String mcVersionInput = in.nextLine();

            StringBuilder mcVersionFiltered = new StringBuilder();

            // Überprüfen und nur Zahlen und Punkte akzeptieren
            /*
                        1.7.10_pre4for (char c : mcVersionInput.toCharArray()) {
                                if (Character.isDigit(c) || c == '.') {
                                        mcVersionFiltered.append(c);
                                }
                        }
            */

            Config.minecraftVersion = String.valueOf(mcVersionInput);

            if (forgeVersions.containsKey(Config.minecraftVersion)) {
                LogInfo("Wich FORGE-Version you like to install [ Latest:  " + (forgeVersions.containsKey(Config.minecraftVersion) ? (String) forgeVersions.get(Config.minecraftVersion).get("latest") : "") + ", Recommended:  " + (forgeVersions.containsKey(Config.minecraftVersion) ? (String) forgeVersions.get(Config.minecraftVersion).get("recommended") : "") + " ]:");
                LogInfo("You can also install all other Versions, listed on this Site: https://files.minecraftforge.net/net/minecraftforge/forge/index_" + Config.minecraftVersion + ".html");
            }
            if (neoVersions.containsKey(Config.minecraftVersion)) {
                LogInfo("Wich NeoFORGED-Version you like to install [ Latest:  " + (neoVersions.containsKey(Config.minecraftVersion) ? (String) neoVersions.get(Config.minecraftVersion).get("latest") : "") + " ]:");
                LogInfo("You can also install all other Versions, listed on this Site: https://projects.neoforged.net/neoforged/neoforge");
            }

            if (!forgeVersions.containsKey(Config.minecraftVersion) && !neoVersions.containsKey(Config.minecraftVersion)) {
                // Der Schlüssel existiert in der Map nicht
                LogError("The Minecraft-Version \"" + Config.minecraftVersion + "\" not exists, restart Downloader.");
                checkLoaderVersion();
                return false;
            }

            String LoaderVersionInput = in.nextLine();
            Config.loaderVersion = String.valueOf(LoaderVersionInput);

            // Regulären Ausdruck erstellen
            Pattern pattern = Pattern.compile(Config.loaderVersion);

            // Suche im String nach Übereinstimmungen mit dem Muster
            Matcher neoMatcher = pattern.matcher(neoVersions.toString());

            if (neoMatcher.find()) {
                Config.isForge = false;
                return true;
            } else {
                Config.isForge = true;
                return true;
            }

            // Durchsuchen der Ergebnisse mit Streams
                        /*
                        neoVersions.entrySet().stream()
                                .flatMap(entry -> ((List<String>) entry.getValue().getOrDefault("versions", new ArrayList<>())).stream())
                                .filter(version -> pattern.matcher(version).matches())
                                .forEach(match -> System.out.println("Eintrag gefunden: " + match));
                        */
        }
    }

    public static boolean downloadLoader() {

        // Check if minecraftVersion and loaderVersion are set
        if ( Config.minecraftVersion == null || Config.loaderVersion == null) {
            LogWarning("One of the variables (minecraftVersion or loaderVersion) is not set properly.");
            Config.startupError = true;
            return false; // Abbruch der Ausführung
        }

        FileOperation fileOperation = null;
        FileOperation fileDownload = null;
        Map<String, String> links = null;
        if ( !Config.isForge ) {
            links = NeoForge.getFileLinks( Config.loaderVersion );
            fileDownload = FileOperation.downloadOrReadFile(links.get("fileURL"), Config.rootFolder + links.get("localFilePath"));
        } else {
            links = Forge.getFileLinks( Config.minecraftVersion, Config.loaderVersion );
            // System.out.println ( Config.minecraftVersion + " " + Config.isForge +  " " + Config.loaderVersion );
            fileDownload = FileOperation.downloadOrReadFile(links.get("fileURL"), Config.rootFolder + links.get("localFilePath"));
        }

        if (fileDownload != null && fileDownload.getResponseCode() == 200) {
            Data.LogInfo("Loader downloaded: " + links.get("fileURL") + " to " + Config.rootFolder + links.get("localFilePath"));
            return true;
        } else {
            Data.LogError("Error reading remote file. Response code: " + fileDownload.getResponseCode());
            return false;
        }

    }


    public static boolean checkLocalInstaller() {
        // Standardvalue is false
        return checkLocalInstaller(false );
    }

    public static boolean checkLocalInstaller(boolean output) {

        java.io.File[] currentFiles = Config.rootFolder.listFiles();
        // try-catch block to handle exceptions
        try {

            // Display the names of the files
            for (int i = 0; i < currentFiles.length; i++) {

                // if(output) System.out.println("Test: " + currentFiles[i].getName());
                Matcher matcherForge = Config.Pattern_Forge.matcher(currentFiles[i].getName());
                Matcher matcherNeoForge = Config.Pattern_NeoForge.matcher(currentFiles[i].getName());

                if (matcherForge.find()) {
                    Config.minecraftVersion =  matcherForge.group(1);
                    Config.loaderVersion = matcherForge.group(2);
                    Config.installerFile = currentFiles[i].getName();
                    if(output) LogInfo("Match found INSTALLER with MC-Version " + Config.minecraftVersion + " and Forge " + Config.loaderVersion);
                    return true;
                }
                if (matcherNeoForge.find()) {
                    Config.minecraftVersion = "1." + matcherNeoForge.group(1);
                    Config.loaderVersion = matcherNeoForge.group(1);
                    Config.installerFile = currentFiles[i].getName();
                    if(output) LogInfo("Match found INSTALLER with MC-Version " + Config.minecraftVersion + " and NeoForge " + Config.loaderVersion);
                    return true;
                }
            }
        } catch (Exception e) {
            Config.startupError = true;
            LogWarning(e.getMessage());
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
                //LogInfo("Attempting to use installer from " + installPath);
                LogInfo("Starting installation of Loader, installer output incoming");
                LogInfo("Check log from installer for more information");
                final Process start;

                String javaStart = "java";
                if (Config.configProps.getProperty("java_path") != null && !Config.configProps.getProperty("java_path").equals("java")) {
                    javaStart = Config.configProps.getProperty("java_path");
                    LogDebug("Use for Installer Custom Java Path: " + Config.configProps.getProperty("java_path"));
                }

                final Process installer = start = new ProcessBuilder(new String[]{javaStart, "-jar", Config.installerFile, "nogui", "--installServer"}).directory(Config.rootFolder).start();
                final Scanner serverLog = new Scanner(start.getInputStream());
                while (serverLog.hasNextLine()) {
                    final String println = serverLog.nextLine();
                    LogCustom(println, "FORGE-Installer", TXT_PURPLE);
                }
                installer.waitFor();
                //Installer is done

                java.io.File libraries_dir = new java.io.File("libraries/");
                if (libraries_dir.exists()) {

                    LogInfo("Done installing loader...");
                    LogInfo("Deleting leftover Files, after installation!");

                    final java.io.File installerFile = new java.io.File(Config.rootFolder + java.io.File.separator + Config.installerFile);
                    if (installerFile.exists()) {
                        Files.delete(installerFile.toPath());
                    }

                    final java.io.File installerFileLog = new java.io.File(Config.rootFolder + java.io.File.separator + Config.installerFile + ".log");
                    if (installerFileLog.exists()) {
                        Files.delete(installerFileLog.toPath());
                    }

                    final java.io.File installerFileRunBat = new java.io.File(Config.rootFolder + java.io.File.separator + "run.bat");
                    if (installerFileRunBat.exists()) {
                        Files.delete(installerFileRunBat.toPath());
                    }

                    final java.io.File installerFileRunSh = new java.io.File(Config.rootFolder + java.io.File.separator + "run.sh");
                    if (installerFileRunSh.exists()) {
                        Files.delete(installerFileRunSh.toPath());
                    }

                    final java.io.File installerFileJavaArgs = new java.io.File(Config.rootFolder + java.io.File.separator + "user_jvm_args.txt");
                    if (installerFileJavaArgs.exists()) {
                        Files.delete(installerFileJavaArgs.toPath());
                    }

                    return false;

                } else {
                    LogWarning("Problem while installing FORGE, \"libraries\"-Folder not successfully created.");
                    Config.startupError = true;
                    return true;
                }

            } catch (IOException | InterruptedException e) {
                LogWarning("Problem while installing Loader from " + Config.rootFolder + File.separator + ' ' + e);
                Config.startupError = true;
                return true;
            }
        } else {
            LogWarning("No \"libraries\"-Folders and no Installer-File could be found!");
            Config.startupError = true;
            return true;
        }

    }

    public static void checkLoaderFolder() {

        Data.LogDebug("Current Path: " + Config.rootFolder);

        if (Config.librariesFolder.exists() && Config.librariesFolder.isDirectory()) {

            if ((Config.minecraftForgeFolder.exists() && Config.minecraftForgeFolder.isDirectory()) || (Config.neoForgeFolder.exists() && Config.neoForgeFolder.isDirectory())) {

                Config.isForge = Config.minecraftForgeFolder.exists() && Config.minecraftForgeFolder.isDirectory();
                File loaderFolder = Config.isForge ? Config.minecraftForgeFolder : (Config.neoForgeFolder.exists() && Config.neoForgeFolder.isDirectory() ? Config.neoForgeFolder : null);
                Pattern pattern = Config.isForge ? Pattern.compile("(?<minecraftVersion>[.0-9]+)-(?<loaderVersion>[.0-9]+)") : Pattern.compile("(?<minecraftVersion>\\d+\\.\\d+)\\.(?<loaderVersion>\\d+).*");


                String LoaderPath = loaderFolder.getPath().replace("\\", "/"); // Pfad normalisieren, um eine einheitliche Darstellung zu erhalten

                Data.LogDebug(LoaderPath + " exists");

                // Filter für Ordner, die sowohl unix_args.txt als auch win_args.txt enthalten
                FileFilter folderFilter = new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory() &&
                                new File(file, "unix_args.txt").exists() &&
                                new File(file, "win_args.txt").exists();
                    }
                };

                // Listet nur die Ordner auf, die beide erforderlichen Dateien enthalten
                File[] versionFolders = loaderFolder.listFiles(folderFilter);

                if (versionFolders != null && versionFolders.length > 0) {

                    Arrays.sort(versionFolders, Comparator.comparing(File::getName, new Data.VersionComparator()).reversed());
                    File latestVersionFolder = versionFolders[0];

                    if (latestVersionFolder != null) {

                        // Auswahl des Präfixes je nach Betriebssystem
                        String systemPrefix = Config.OS.contains("win") ? "win_" : "unix_";

                        String startFolder = "libraries" + File.separator;
                        int librariesIndex = latestVersionFolder.getAbsolutePath().indexOf(startFolder);
                        if (librariesIndex >= 0) {
                            startFolder = latestVersionFolder.getAbsolutePath().substring(librariesIndex);
                        }

                        Data.LogDebug("Using " + (Config.OS.contains("win") ? "WINDOWS" : "UNIX") + " System-Parameter for " + (Config.isForge ? "Forge" : "NeoForge") + " folder"); //, "Blah,blah...", TXT_CYAN);
                        Config.startupFile = startFolder + File.separator + systemPrefix + "args.txt";

                        Matcher matcher = pattern.matcher(latestVersionFolder.getName());
                        if (matcher.matches()) {
                            Config.minecraftVersion =  Config.isForge ? matcher.group("minecraftVersion") : "1." + matcher.group("minecraftVersion");
                            Config.loaderVersion = Config.isForge ? matcher.group("loaderVersion") : latestVersionFolder.getName();
                            LogInfo("Found Minecraft: " + Config.minecraftVersion + " with " + (Config.isForge ? "Forge" : "NeoForge") + "-Version: " + Config.loaderVersion);
                        }
                        Data.LogDebug("Found MC-Version: " + Config.minecraftVersion);
                        Data.LogDebug("Found Loader-Version: " + Config.loaderVersion);

                        Data.LogDebug("Startup-File: " + Config.startupFile);
                        Data.LogDebug("Required files exist in the latest version folder of " + loaderFolder.getPath());
                    } else {
                        //Config.startupError = true;
                        Data.LogDebug("Required files do not exist in the latest version folder of " + loaderFolder.getPath());
                    }

                }

            } else {
                //System.out.println("Not Found a valid Folder.");
            }

        } else {
            //System.out.println("libraries folder does not exist");
        }
    }

    public static void checkLocalFolder() {

        Comparator<String> versionComparator = new VersionComparator();

        if (
                versionComparator.compare( Config.minecraftVersion, "1.17.0") < 0
                ||
                ( Config.isForge && versionComparator.compare( Config.minecraftVersion , "1.20.4") >= 0 )

        ) {

            // FilenameFilter erstellen
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return Config.Pattern_Forge_startfile.matcher(name).matches();
                }
            };

            // Dateien im Ordner auflisten, die dem Pattern entsprechen
            File[] matchingFiles = Config.rootFolder.listFiles(filter);

            if ( matchingFiles != null && matchingFiles.length > 0) {

                java.io.File matchingFile = matchingFiles[0];

                Matcher matcher = Config.Pattern_Forge_startfile.matcher(matchingFile.getName());

                if ( matcher.matches() ) {
                    Config.minecraftVersion =  matcher.group(1);
                    Config.loaderVersion = matcher.group(2);
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
                //System.out.println("Keine passende Datei im Root-Verzeichnis gefunden.");
                LogWarning("No Forge-Version could be Found!");
                Config.startupError = true;
            }

        }
    }
}
