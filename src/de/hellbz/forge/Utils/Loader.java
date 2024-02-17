package de.hellbz.forge.Utils;

import java.io.File;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.hellbz.forge.Utils.Data.*;

public class Loader {

    public static boolean downloadLoader() throws IOException {

        java.io.File[] currentFiles = Config.rootFolder.listFiles();

        Pattern pattern_auto = Pattern.compile("forge-auto-install.txt", Pattern.CASE_INSENSITIVE);

        Boolean installerFile = false;
        Boolean autoFile = false;
        String mcVersion = null;
        String loaderVersion = null;

        // try-catch block to handle exceptions
        try {
            //LogInfo("Files are:");

            // Display the names of the files
            for (int i = 0; i < currentFiles.length; i++) {

                Matcher matcher_auto = pattern_auto.matcher(currentFiles[i].getName());
                Matcher matcher_forge = Config.Pattern_Forge.matcher(currentFiles[i].getName());

                //If found Auto-Installer-File, set to true and continue
                if (matcher_auto.find()) autoFile = true;

                //If found Forge-Installer, exit downloader
                if (matcher_forge.find()) return false;

            }
        } catch (Exception e) {
            Config.startupError = true;
            LogWarning( e.getMessage() );
        }

        java.io.File autoConfigFile = new java.io.File("forge-auto-install.txt");

        // get the new versions map
        Map<String, Map<String, Object>> forgeVersions = Forge.getVersions();

        // get the first version key
        String firstForgeVersionKey = forgeVersions.keySet().iterator().next();

        // get the information for the first version
        Map<String, Object> firstForgeVersionInfo = forgeVersions.get(firstForgeVersionKey);

        // get the new NEO versions map
        Map<String, Map<String, Object>> neoVersions = NeoForge.getVersions();

        // get the first version key
        String firstNeoVersionKey = neoVersions.keySet().iterator().next();

        // get the information for the first version
        Map<String, Object> firstNeoVersionInfo = neoVersions.get(firstNeoVersionKey);

        //LogInfo( firstEntry.toString() );
        if ( autoFile && autoConfigFile.exists()  ) {

            // Search MC-Version and Forge in Auto-Installer-File
            FileReader autoReader = new FileReader(autoConfigFile);
            Config.autoProps = new Properties();
            Config.autoProps.load(autoReader);
            autoReader.close();

            mcVersion = Config.autoProps.getProperty("mc-version");
            loaderVersion = Config.autoProps.getProperty("forge-version");

            if ( mcVersion == null || mcVersion.trim().isEmpty() || loaderVersion == null || loaderVersion.trim().isEmpty() ) {

                //Write clean Config to File
                FileWriter writerConfig = new FileWriter(autoConfigFile);
                Config.autoProps = new Properties();
                Config.autoProps.setProperty("mc-version", "latest" /* firstEntry.getKey() */ );
                Config.autoProps.setProperty("mc-version-info", "like " + ( firstForgeVersionKey != null ? firstForgeVersionKey : "" ) + " or latest" /* firstEntry.getValue() */ );
                Config.autoProps.setProperty("forge-version", "latest" /* firstEntry.getValue() */ );
                Config.autoProps.setProperty("forge-version-info", "like " + ( firstForgeVersionInfo != null ? (String) firstForgeVersionInfo.get("recommended") : "" ) + " , recommended or latest" /* firstEntry.getValue() */ );
                Config.autoProps.store(writerConfig, "Forge Auto-Install Configuration");
                LogWarning("Found Error in the \"forge-auto-install.txt\", saved the File correct, please check the File.");
                Config.startupError = true;
                return false;
            }

            //LogInfo( mcVersion + forgeVersion );
            String regexmcVersion = "^[0-9.]+$|^latest$";
            String regexforgeVersion = "^[0-9.]+$|^latest$|^recommended$";

            if (mcVersion.matches(regexmcVersion)) {
                if (mcVersion.equals("latest")) {
                    mcVersion = ( firstForgeVersionKey != null ? firstForgeVersionKey : "" );
                }
            } else {
                LogWarning("The Minecraft-Version \"" + mcVersion + "\" does not expect like [\"" + ( firstForgeVersionKey != null ? firstForgeVersionKey : "" ) + "\"or\"latest\"");
                Config.startupError = true;
                return false;
            }
            if (mcVersion.matches(regexmcVersion)) {

                if ( forgeVersions.containsKey( mcVersion ) )  {
                    Map<String, Object> versionInfo = forgeVersions.get( mcVersion );
                    if( versionInfo != null ){
                        if ( loaderVersion.equals("latest") && versionInfo.get("latest") != null ){
                            loaderVersion = (String) versionInfo.get("latest");
                        }else if ( loaderVersion.equals("recommended") && versionInfo.get("recommended") != null ){
                            loaderVersion = (String) versionInfo.get("recommended");
                        }else{
                            LogWarning("The Minecraft-Version \"" + mcVersion + "\" does not exist in the Latest or Recommended-Version-List.");
                            Config.startupError = true;
                            return false;
                        }
                    }else{
                        LogWarning("The Minecraft-Version \"" + mcVersion + "\" does not exist in the Version-List.");
                        Config.startupError = true;
                        return false;
                    }

                }

            } else {
                LogWarning("The FORGE-Version \"" + loaderVersion + "\" does not expect like [\"" + ( firstForgeVersionInfo != null ? (String) firstForgeVersionInfo.get("latest") : "" )  + "\",\"recommended\"or\"latest\"");
                Config.startupError = true;
                return false;
            }
            LogInfo("Found \"forge-auto-install.txt\" with MC-Version " + mcVersion  + " and Forge " + loaderVersion );

        }  else {

            // GUIDED installation
            LogWarning("Not found the \"forge-auto-install.txt\", start manuell installation-Process.");
            LogInfo("FORGE is available in the following Versions:");

            //LogInfo( ForgeLatestVersions.toString() );

            // join all keys
            String ForgeVersionsAsString = String.join(", ", forgeVersions.keySet());
            LogInfo( ForgeVersionsAsString );


            LogInfo("NeoFORGED is available in the following Versions:");
            String NeoForgeVersionsAsString = String.join(", ", neoVersions.keySet() );

            LogInfo( NeoForgeVersionsAsString );

            // Using Scanner for Getting Input from User
            Scanner in = new Scanner(System.in);

            LogInfo("Wich MINECRAFT-Version you like to install [ eg. " + ( firstForgeVersionKey != null ? firstForgeVersionKey : "" ) + " ]:");
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

            mcVersion = String.valueOf(mcVersionInput);

            if ( forgeVersions.containsKey(mcVersion) ) {
                LogInfo("Wich FORGE-Version you like to install [ Latest:  " + ( forgeVersions.containsKey(mcVersion) ? (String) forgeVersions.get(mcVersion).get("latest") : "" ) + ", Recommended:  " + ( forgeVersions.containsKey(mcVersion) ? (String) forgeVersions.get(mcVersion).get("recommended") : "" ) + " ]:");
                LogInfo("You can also install all other Versions, listed on this Site: https://files.minecraftforge.net/net/minecraftforge/forge/index_" + mcVersion + ".html");
            }
            if ( neoVersions.containsKey(mcVersion) ) {
                LogInfo("Wich NeoFORGED-Version you like to install [ Latest:  " + ( neoVersions.containsKey(mcVersion)  ? (String) neoVersions.get(mcVersion).get("latest") : "" ) + " ]:");
                LogInfo("You can also install all other Versions, listed on this Site: https://projects.neoforged.net/neoforged/neoforge");
            }

            if ( !forgeVersions.containsKey(mcVersion) && !neoVersions.containsKey(mcVersion) )
            {
                // Der Schlüssel existiert in der Map nicht
                LogError("The Minecraft-Version \"" + mcVersion + "\" not exists, restart Downloader.");
                downloadLoader();
                return false;
            }

            String LoaderVersionInput = in.nextLine();
            loaderVersion = String.valueOf(LoaderVersionInput);

            // Durchsuchen der Ergebnisse mit Streams
                        /*
                        neoVersions.entrySet().stream()
                                .flatMap(entry -> ((List<String>) entry.getValue().getOrDefault("versions", new ArrayList<>())).stream())
                                .filter(version -> pattern.matcher(version).matches())
                                .forEach(match -> System.out.println("Eintrag gefunden: " + match));
                        */
        }

        // Regulären Ausdruck erstellen
        Pattern pattern = Pattern.compile(loaderVersion);

        // Suche im String nach Übereinstimmungen mit dem Muster
        Matcher neoMatcher = pattern.matcher(neoVersions.toString());

        FileOperation fileOperation = new FileOperation();
        FileOperationResult fileDownload = null;
        Map<String, String> links = null;
        if( neoMatcher.find() ) {
            System.out.println("Eintrag bei NEO gefunden: " + neoMatcher.group());
            links = NeoForge.getFileLinks(loaderVersion);
            fileDownload = fileOperation.downloadOrReadFile( links.get("fileURL") , Config.rootFolder + links.get("localFilePath") );

        }else{
            System.out.println("Use FORGE: " + loaderVersion );
            links = Forge.getFileLinks(mcVersion, loaderVersion);
            fileDownload = fileOperation.downloadOrReadFile( links.get("fileURL") , Config.rootFolder + links.get("localFilePath") );
        }

        if (fileDownload != null && fileDownload.getResponseCode() == 200) {
            Data.LogInfo("Loader downloaded: " + links.get("fileURL") + " to " + Config.rootFolder + links.get("localFilePath") );
            return true;
        } else {
            Data.LogError("Error reading remote file. Response code: " + fileDownload.getResponseCode());
            return false;
        }
    }
    public static boolean checkLocalInstaller() {

        java.io.File[] currentFiles = Config.rootFolder.listFiles();
        // try-catch block to handle exceptions
        try {

            // Display the names of the files
            for (int i = 0; i < currentFiles.length; i++) {

                System.out.println( currentFiles[i].getName() );
                Matcher matcherForge = Config.Pattern_Forge.matcher(currentFiles[i].getName());
                Matcher matcherNeoForge = Config.Pattern_NeoForge.matcher(currentFiles[i].getName());

                if (matcherForge.find()) {
                    Config.mc_version = matcherForge.group(1);
                    Config.loader_version = matcherForge.group(2);
                    Config.installerFile = currentFiles[i].getName();
                    LogInfo("Match found INSTALLER with MC-Version " + Config.mc_version  + " and Forge " + Config.loader_version );
                    return true;
                }
                if (matcherNeoForge.find()) {
                    Config.mc_version = "1." + matcherNeoForge.group(1);
                    Config.loader_version = matcherNeoForge.group(1);
                    Config.installerFile = currentFiles[i].getName();
                    LogInfo("Match found INSTALLER with MC-Version " + Config.mc_version  + " and NeoForge " + Config.loader_version );
                    return true;
                }
            }
        } catch (Exception e) {
            Config.startupError = true;
            LogWarning( e.getMessage() );
        }
        return false;
    }

    public static boolean installLoader() {

        if ( Config.installerFile != null ) {

            try {

                final String filename = new java.io.File(Config.installerFile).getName();

                LogInfo("Attempting to start Server " + Config.installerFile);
                LogDebug("Filename: " + filename);
                LogDebug("Directory: " + Config.rootFolder );
                //LogInfo("Attempting to use installer from " + installPath);
                LogInfo("Starting installation of Loader, installer output incoming");
                LogInfo("Check log from installer for more information");
                final Process start;

                String javaStart = "java";
                if (Config.configProps.getProperty("java_path") != null && !Config.configProps.getProperty("java_path").equals("java")) {
                    javaStart = Config.configProps.getProperty("java_path");
                    LogDebug("Use for Installer Custom Java Path: " + Config.configProps.getProperty("java_path"));
                }

                final Process installer = start = new ProcessBuilder(new String[]{ javaStart , "-jar", Config.installerFile, "nogui", "--installServer"}).directory( Config.rootFolder ).start();
                final Scanner serverLog = new Scanner(start.getInputStream());
                while (serverLog.hasNextLine()) {
                    final String println = serverLog.nextLine();
                    LogCustom(println,"FORGE-Installer",TXT_PURPLE);
                }
                installer.waitFor();
                //Installer is done

                java.io.File libraries_dir = new java.io.File("libraries/");
                if ( libraries_dir.exists() ) {

                    LogInfo("Done installing loader...");
                    LogInfo("Deleting leftover Files, after installation!");

                    final java.io.File installerFile2 = new java.io.File( Config.rootFolder + java.io.File.separator + Config.installerFile);
                    if (installerFile2.exists()) {
                        Files.delete(installerFile2.toPath());
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

                }else{
                    LogWarning("Problem while installing FORGE, \"libraries\"-Folder not successfully created.");
                    Config.startupError = true;
                    return true;
                }

            } catch (IOException | InterruptedException e) {
                LogWarning("Problem while installing Loader from " + Config.rootFolder + File.separator + ' ' + e );
                Config.startupError = true;
                return true;
            }
        }else{
            LogWarning("No \"libraries\"-Folders and no Installer-File could be found!");
            Config.startupError = true;
            return true;
        }

    }

    public static void  checkLoaderFolder() {

        Data.LogDebug("Current Path: " + Config.rootFolder );

        if (Config.librariesFolder.exists() && Config.librariesFolder.isDirectory()) {

            if ( ( Config.minecraftForgeFolder.exists() && Config.minecraftForgeFolder.isDirectory() ) || ( Config.neoForgeFolder.exists() && Config.neoForgeFolder.isDirectory() ) )
            {

                Config.isForge = Config.minecraftForgeFolder.exists() && Config.minecraftForgeFolder.isDirectory();
                File loaderFolder = Config.isForge ? Config.minecraftForgeFolder : (Config.neoForgeFolder.exists() && Config.neoForgeFolder.isDirectory() ? Config.neoForgeFolder : null);
                Pattern pattern = Config.isForge ? Pattern.compile("(?<minecraftVersion>[.0-9]+)-(?<loaderVersion>[.0-9]+)") : Pattern.compile("(?<minecraftVersion>\\d+\\.\\d+)\\.(?<loaderVersion>\\d+).*");


                String LoaderPath  = loaderFolder.getPath().replace("\\", "/"); // Pfad normalisieren, um eine einheitliche Darstellung zu erhalten

                Data.LogDebug( LoaderPath + " exists" );

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

                    if ( latestVersionFolder != null ) {

                        // Auswahl des Präfixes je nach Betriebssystem
                        String systemPrefix = Config.OS.contains("win") ? "win_" : "unix_";

                        String startFolder = "libraries" + File.separator;
                        int librariesIndex = latestVersionFolder.getAbsolutePath().indexOf(startFolder);
                        if (librariesIndex >= 0) {
                            startFolder = latestVersionFolder.getAbsolutePath().substring(librariesIndex);
                        }

                        Data.LogCustom("Using " + (Config.OS.contains("win") ? "WINDOWS" : "UNIX") + " System-Parameter for Forge folder", "Blah,blah..." , TXT_CYAN );
                        Config.startup_file = startFolder + File.separator + systemPrefix + "args.txt";

                        Matcher matcher = pattern.matcher( latestVersionFolder.getName() );
                        if (matcher.matches()) {
                            Config.mc_version = Config.isForge ? matcher.group("minecraftVersion") : "1." + matcher.group("minecraftVersion");
                            Config.loader_version = Config.isForge ? matcher.group("loaderVersion") : latestVersionFolder.getName();
                            Config.mcVersionDetail = Config.mc_version.split("\\.");

                            LogInfo("Found Minecraft: " + Config.mc_version + " with " + ( Config.isForge ? "Forge": "NeoForge" ) + "-Version: " + Config.loader_version);
                        }
                        Data.LogDebug( "Found MC-Version: " + Config.mc_version );
                        Data.LogDebug( "Found Loader-Version: " + Config.loader_version);

                        Data.LogDebug( "Startup-File: " + Config.startup_file );
                        Data.LogDebug("Required files exist in the latest version folder of " + loaderFolder.getPath());
                    } else {
                        //Config.startupError = true;
                        Data.LogDebug( "Required files do not exist in the latest version folder of " + loaderFolder.getPath() );
                    }

                }

            }else{
                //System.out.println("Not Found a valid Folder.");
            }

        } else {
            //System.out.println("libraries folder does not exist");
        }
    }


    public static void checkLocalFolder() {
        if( Integer.parseInt(Config.mcVersionDetail[1]) < 17 ){
            // Suche nach passenden Dateien im Root-Verzeichnis
            java.io.File[] matchingFiles = Config.rootFolder.listFiles(file -> {
                Matcher matcher = Config.Pattern_Forge_17.matcher(file.getName());
                return matcher.matches();
            });

            // Ausgabe der gefundenen Datei
            if (matchingFiles != null && matchingFiles.length > 0) {
                java.io.File matchingFile = matchingFiles[0];
                Matcher matcher = Config.Pattern_Forge_17.matcher(matchingFile.getName());
                if (matcher.matches()) {
                    Config.mc_version = matcher.group(1);
                    Config.loader_version = matcher.group(2);
                    Config.startup_file = matchingFile.getName();
                    LogInfo("Found Minecraft: " + Config.mc_version  + " with Forge " + Config.loader_version);
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
