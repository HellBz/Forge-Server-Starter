package de.hellbz.forge.Utils;

import java.io.*;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.hellbz.forge.Utils.Data.*;
import static de.hellbz.forge.Utils.Data.LogWarning;

public class Loader {

    public static boolean downloadLoader( String installPath ) throws IOException {

        //Set current Directory
        java.io.File currentDir = new java.io.File(installPath);

        java.io.File[] currentFiles = currentDir.listFiles();

        Pattern pattern_auto = Pattern.compile("forge-auto-install.txt", Pattern.CASE_INSENSITIVE);

        Boolean installerFile = false;
        Boolean autoFile = false;
        String mcVersion = null;
        String forgeVersion = null;

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
        if ( autoFile && autoConfigFile.exists() ) {

            // Search MC-Version and Forge in Auto-Installer-File
            FileReader autoReader = new FileReader(autoConfigFile);
            Config.autoProps = new Properties();
            Config.autoProps.load(autoReader);
            autoReader.close();

            mcVersion = Config.autoProps.getProperty("mc-version");
            forgeVersion = Config.autoProps.getProperty("forge-version");

            if ( mcVersion == null || mcVersion.trim().isEmpty() || forgeVersion == null || forgeVersion.trim().isEmpty() ) {

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
                        if ( forgeVersion.equals("latest") && versionInfo.get("latest") != null ){
                            forgeVersion = (String) versionInfo.get("latest");
                        }else if ( forgeVersion.equals("recommended") && versionInfo.get("recommended") != null ){
                            forgeVersion = (String) versionInfo.get("recommended");
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
                LogWarning("The FORGE-Version \"" + forgeVersion + "\" does not expect like [\"" + ( firstForgeVersionInfo != null ? (String) firstForgeVersionInfo.get("latest") : "" )  + "\",\"recommended\"or\"latest\"");
                Config.startupError = true;
                return false;
            }


            LogInfo("Found \"forge-auto-install.txt\" with MC-Version " + mcVersion  + " and Forge " + forgeVersion );


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
                LogInfo("Wich FORGE-Version you like to install [ Latest:  " + ( firstForgeVersionInfo != null ? (String) firstForgeVersionInfo.get("latest") : "" ) + ", Recommended:  " + ( firstForgeVersionInfo != null ? (String) firstForgeVersionInfo.get("recommended") : "" ) + " ]:");
                LogInfo("You can also install all other Versions, listed on this Site: https://files.minecraftforge.net/net/minecraftforge/forge/index_" + mcVersion + ".html");
            }
            if ( neoVersions.containsKey(mcVersion) ) {
                LogInfo("Wich NeoFORGED-Version you like to install [ Latest:  " + ( firstNeoVersionInfo != null ? (String) firstNeoVersionInfo.get("latest") : "" ) + " ]:");
                LogInfo("You can also install all other Versions, listed on this Site: https://projects.neoforged.net/neoforged/neoforge");
            }

            if ( !forgeVersions.containsKey(mcVersion) && !neoVersions.containsKey(mcVersion) )
            {
                // Der Schlüssel existiert in der Map nicht
                LogError("The Minecraft-Version \"" + mcVersion + "\" not exists, restart Downloader.");
                downloadLoader( installPath );
                return false;
            }

            String forgeVersionInput = in.nextLine();
            forgeVersion = String.valueOf(forgeVersionInput);

            // Regulären Ausdruck erstellen
            Pattern pattern = Pattern.compile(forgeVersionInput);

            // Suche im String nach Übereinstimmungen mit dem Muster
            Matcher matcher = pattern.matcher(neoVersions.toString());
            if( matcher.find() ) {
                System.out.println("Eintrag gefunden: " + matcher.group());
            }

            // Durchsuchen der Ergebnisse mit Streams
                        /*
                        neoVersions.entrySet().stream()
                                .flatMap(entry -> ((List<String>) entry.getValue().getOrDefault("versions", new ArrayList<>())).stream())
                                .filter(version -> pattern.matcher(version).matches())
                                .forEach(match -> System.out.println("Eintrag gefunden: " + match));
                        */


        }

        return downloadInstallerFile(installPath,mcVersion,forgeVersion);
    }

    private static boolean downloadInstallerFile(String installPath, String version, String build) {

        boolean isConnected = Net.isConnected;

        String fileURL = "https://maven.minecraftforge.net/net/minecraftforge/forge/" + version + "-" + build + "/forge-" + version + "-" + build + "-installer.jar";
        String localFilePath = installPath + "/" + "forge-" + version + "-" + build + "-installer.jar";
        Comparator<String> versionComparator = new VersionComparator();
        int check_1_7_10 = versionComparator.compare(version, "1.7.10");
        int check_1_7_10_pre = versionComparator.compare(version, "1.7.10_pre4");
        int check_1_7_2 = versionComparator.compare(version, "1.7.2");
        int check_1_5_2 = versionComparator.compare(version, "1.5.2");
        int check_1_3_2 = versionComparator.compare(version, "1.3.2");

        if (check_1_5_2 < 0) {
            fileURL = "https://maven.minecraftforge.net/net/minecraftforge/forge/" + version + "-" + build + "/forge-" + version + "-" + build + "-universal.zip";
            localFilePath = installPath + "/" + "forge-" + version + "-" + build + "-universal.zip";
        } else if (check_1_3_2 < 0) {
            fileURL = "https://maven.minecraftforge.net/net/minecraftforge/forge/" + version + "-" + build + "/forge-" + version + "-" + build + "-server.zip";
            localFilePath = installPath + "/" + "forge-" + version + "-" + build + "-server.zip";
        } else if (check_1_7_10 == 0) {
            fileURL = "https://maven.minecraftforge.net/net/minecraftforge/forge/" + version + "-" + build + "-" + version + "/forge-" + version + "-" + build + "-" + version + "-installer.jar";
        } else if (check_1_7_10_pre == 0) {
            fileURL = "https://maven.minecraftforge.net/net/minecraftforge/forge/" + version + "-" + build + "-prerelease/forge-" + version + "-" + build + "-prerelease-installer.jar";
            localFilePath = installPath + "/" + "forge-1.7.10-" + build + "-universal.zip";
        } else if (check_1_7_2 == 0) {
            fileURL = "https://maven.minecraftforge.net/net/minecraftforge/forge/" + version + "-" + build + "-mc172/forge-" + version + "-" + build + "-mc172-installer.jar";
        }

        LogDebug( "Download FORGE from: " + fileURL );

        try {
            URL url = new URL(fileURL);
            URLConnection connection = url.openConnection();

            // Dateigröße überprüfen (optional)
            int fileSize = connection.getContentLength();
            if (fileSize <= 0) {
                System.out.println("Die Datei hat eine ungültige Größe.");
                return false;
            }

            // InputStream für den Download öffnen
            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(localFilePath)) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            LogCustom("File downloaded successfully: " + localFilePath,"FORGE-Installer",TXT_PURPLE);
            return true;
        } catch (IOException e) {
            LogError("Fehler beim Download: " + e.getMessage());
            return false;
        }
    }

    public static boolean installLoader(String installPath ) {

        //Set current Directory
        java.io.File currentDir = new java.io.File(installPath);

        java.io.File[] currentFiles = currentDir.listFiles();

        String installerFile = null;
        String mcVersion = null;
        String forgeVersion = null;

        // try-catch block to handle exceptions
        try {
            //LogInfo("Files are:");

            // Display the names of the files
            for (int i = 0; i < currentFiles.length; i++) {

                Matcher matcher = Config.Pattern_Forge.matcher(currentFiles[i].getName());

                if (matcher.find()) {
                    mcVersion = matcher.group(1);
                    forgeVersion = matcher.group(2);
                    installerFile = currentFiles[i].getName();
                    LogInfo("Match found INSTALLER with MC-Version " + mcVersion  + " and Forge " + forgeVersion );
                    break;
                }
            }
        } catch (Exception e) {
            Config.startupError = true;
            LogWarning( e.getMessage() );
        }

        if ( installerFile != null ) {

            try {
                final String filename = new java.io.File(installerFile).getName();

                LogInfo("Attempting to start Server " + installerFile);
                LogDebug("Filename: " + filename);
                LogDebug("Directory: " + installPath);
                //LogInfo("Attempting to use installer from " + installPath);
                LogInfo("Starting installation of Loader, installer output incoming");
                LogInfo("Check log from installer for more information");
                final Process start;

                String javaStart = "java";
                if (Config.configProps.getProperty("java_path") != null && !Config.configProps.getProperty("java_path").equals("java")) {
                    javaStart = Config.configProps.getProperty("java_path");
                    LogDebug("Use for Installer Custom Java Path: " + Config.configProps.getProperty("java_path"));
                }

                final Process installer = start = new ProcessBuilder(new String[]{ javaStart , "-jar", installerFile, "nogui", "--installServer"}).directory(new java.io.File(installPath)).start();
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

                    final java.io.File installerFile2 = new java.io.File( installPath + java.io.File.separator + installerFile);
                    if (installerFile2.exists()) {
                        Files.delete(installerFile2.toPath());
                    }

                    final java.io.File installerFileLog = new java.io.File(installPath + java.io.File.separator + installerFile + ".log");
                    if (installerFileLog.exists()) {
                        Files.delete(installerFileLog.toPath());
                    }

                    final java.io.File installerFileRunBat = new java.io.File(installPath + java.io.File.separator + "run.bat");
                    if (installerFileRunBat.exists()) {
                        Files.delete(installerFileRunBat.toPath());
                    }

                    final java.io.File installerFileRunSh = new java.io.File(installPath + java.io.File.separator + "run.sh");
                    if (installerFileRunSh.exists()) {
                        Files.delete(installerFileRunSh.toPath());
                    }

                    final java.io.File installerFileJavaArgs = new java.io.File(installPath + java.io.File.separator + "user_jvm_args.txt");
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
                LogWarning("Problem while installing Loader from " + installPath + File.separator + ' ' + e );
                Config.startupError = true;
                return true;
            }
        }else{
            LogWarning("No \"libraries\"-Folders and no Installer-File could be found!");
            Config.startupError = true;
            return true;
        }

    }
}
