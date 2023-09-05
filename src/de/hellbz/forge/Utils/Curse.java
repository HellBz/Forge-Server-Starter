package de.hellbz.forge.Utils;

import de.hellbz.forge.ServerStarter;

import java.io.*;
import java.io.File;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

import static de.hellbz.forge.ServerStarter.startupError;
import static de.hellbz.forge.Utils.Data.*;

public class Curse {

        public static Map<String, String> getLatestVersions( ) {

                Map<String, String> promoMap = null;
                try {
                        URL url = new URL("https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json");
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");

                        int responseCode = connection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                StringBuilder response = new StringBuilder();
                                String line;

                                while ((line = reader.readLine()) != null) {
                                        response.append(line);
                                }
                                reader.close();

                                String jsonData = response.toString();

                                // Definiere den regulären Ausdruck, um das gewünschte Muster zu finden
                                String regexPattern = "\"([0-9\\.]+-latest)\": \"([0-9\\.]+)\"";
                                Pattern pattern = Pattern.compile(regexPattern);
                                Matcher matcher = pattern.matcher(jsonData);

                                // Erstelle ein HashMap für die gefundenen Schlüssel-Wert-Paare
                                promoMap = new HashMap<>();

                                while (matcher.find()) {
                                        String key = matcher.group(1).replace("-latest", "");
                                        String value = matcher.group(2);

                                        promoMap.put(key, value);
                                }

                                // Gib das HashMap zurück
                                //System.out.println(promoMap);
                                //return promoMap;
                        } else {
                                LogError("Failed to get FORGE-Version with Error-Code: " + responseCode);
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                }

                // Schlüssel in einer TreeMap sortieren und als Stream abrufen
                TreeMap<String, String> sortedPromoMap = new TreeMap<>(Collections.reverseOrder(new VersionComparator()));
                sortedPromoMap.putAll(promoMap);

                return sortedPromoMap;
        }

        public static Map<String, String> getRecommendedVersions( ) {

                Map<String, String> promoMap = null;
                try {
                        URL url = new URL("https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json");
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");

                        int responseCode = connection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                StringBuilder response = new StringBuilder();
                                String line;

                                while ((line = reader.readLine()) != null) {
                                        response.append(line);
                                }
                                reader.close();

                                String jsonData = response.toString();

                                // Definiere den regulären Ausdruck, um das gewünschte Muster zu finden
                                String regexPattern = "\"([0-9\\.]+-recommended)\": \"([0-9\\.]+)\"";
                                Pattern pattern = Pattern.compile(regexPattern);
                                Matcher matcher = pattern.matcher(jsonData);

                                // Erstelle ein HashMap für die gefundenen Schlüssel-Wert-Paare
                                promoMap = new HashMap<>();

                                while (matcher.find()) {
                                        String key = matcher.group(1).replace("-recommended", "");
                                        String value = matcher.group(2);

                                        promoMap.put(key, value);
                                }

                                // Gib das HashMap zurück
                                //System.out.println(promoMap);
                                //return promoMap;
                        } else {
                                LogError("Failed to get FORGE-Version with Error-Code: " + responseCode);
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                }

                // Schlüssel in einer TreeMap sortieren und als Stream abrufen
                TreeMap<String, String> sortedPromoMap = new TreeMap<>(Collections.reverseOrder(new VersionComparator()));
                sortedPromoMap.putAll(promoMap);

                return sortedPromoMap;
        }

        public static boolean downloadLoader( String installPath ) throws IOException {

                //Set current Directory
                File currentDir = new File(installPath);

                File[] currentFiles = currentDir.listFiles();

                Pattern pattern_auto = Pattern.compile("forge-auto-install.txt", Pattern.CASE_INSENSITIVE);
                Pattern pattern_forge = Pattern.compile("forge-([.0-9]+)-([.0-9]+)-([universal|installer]+).([jar|zip]+)", Pattern.CASE_INSENSITIVE);

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
                                Matcher matcher_forge = pattern_forge.matcher(currentFiles[i].getName());

                                //If found Auto-Installer-File, set to true and continue
                                if (matcher_auto.find()) autoFile = true;

                                //If found Forge-Installer, exit downloader
                                if (matcher_forge.find()) return false;

                        }
                } catch (Exception e) {
                        startupError = true;
                        LogWarning( e.getMessage() );
                }

                File autoConfigFile = new File("forge-auto-install.txt");

                Map<String, String> ForgeLatestVersions = Curse.getLatestVersions();
                Map.Entry<String, String> firstEntry = ForgeLatestVersions.entrySet().iterator().next();

                if ( autoFile && autoConfigFile.exists() ) {

                        // Search MC-Version and Forge in Auto-Installer-File
                        FileReader autoReader = new FileReader(autoConfigFile);
                        ServerStarter.autoProps = new Properties();
                        ServerStarter.autoProps.load(autoReader);
                        autoReader.close();

                        mcVersion = ServerStarter.autoProps.getProperty("mc-version");
                        forgeVersion = ServerStarter.autoProps.getProperty("forge-version");

                        if ( mcVersion == null || mcVersion.trim().isEmpty() || forgeVersion == null || forgeVersion.trim().isEmpty() ) {

                                //Write clean Config to File
                                FileWriter writerConfig = new FileWriter(autoConfigFile);
                                ServerStarter.autoProps = new Properties();
                                ServerStarter.autoProps.setProperty("mc-version", "latest" /* firstEntry.getKey() */ );
                                ServerStarter.autoProps.setProperty("mc-version-info", "like " + firstEntry.getKey() + " or latest" /* firstEntry.getValue() */ );
                                ServerStarter.autoProps.setProperty("forge-version", "latest" /* firstEntry.getValue() */ );
                                ServerStarter.autoProps.setProperty("forge-version-info", "like " + firstEntry.getValue() + " , recommended or latest" /* firstEntry.getValue() */ );
                                ServerStarter.autoProps.store(writerConfig, "Forge Auto-Install Configuration");
                                LogWarning("Found Error in the \"forge-auto-install.txt\", saved the File correct, please check the File.");
                                startupError = true;
                                return false;
                        }

                        LogInfo( mcVersion + forgeVersion );
                        String regexmcVersion = "^[0-9.]+$|^latest$";
                        String regexforgeVersion = "^[0-9.]+$|^latest$|^recommended$";

                        if (mcVersion.matches(regexmcVersion)) {
                                if (mcVersion.equals("latest")) {
                                        mcVersion = firstEntry.getKey();
                                }
                        } else {
                                LogWarning("The Minecraft-Version \"" + mcVersion + "\" does not expect like [\"" + firstEntry.getKey() + "\"or\"latest\"");
                                startupError = true;
                                return false;
                        }
                        if (mcVersion.matches(regexmcVersion)) {
                                if ( forgeVersion.equals("latest")) {
                                        if ( ForgeLatestVersions.containsKey( mcVersion ) )  {
                                                forgeVersion = ForgeLatestVersions.get( mcVersion );
                                        }else{
                                                LogWarning("The Minecraft-Version \"" + mcVersion + "\" does not exist in the Latest-Version-List.");
                                                startupError = true;
                                                return false;
                                        }
                                } else if ( forgeVersion.equals("recommended") ) {
                                        Map<String, String> ForgeRecommendedVersions = Curse.getRecommendedVersions();
                                        if ( ForgeRecommendedVersions.containsKey( mcVersion ) )  {
                                                forgeVersion = ForgeRecommendedVersions.get( mcVersion );
                                        } else {
                                                LogWarning("The FORGE-Version \"" + forgeVersion + "\" does not exist in the Recommended-Version-List.");
                                                startupError = true;
                                                return false;
                                        }
                                }
                        } else {
                                LogWarning("The FORGE-Version \"" + forgeVersion + "\" does not expect like [\"" + firstEntry.getValue() + "\",\"recommended\"or\"latest\"");
                                startupError = true;
                                return false;
                        }


                        LogInfo("Found \"forge-auto-install.txt\" with MC-Version " + mcVersion  + " and Forge " + forgeVersion );


                }  else {

                        // GUIDED installation

                        LogWarning("Not found the \"forge-auto-install.txt\", start manuell installation-Process.");
                        LogInfo("FORGE is available in the following Versions:");

                        LogInfo( ForgeLatestVersions.toString() );

                        String ForgeVersionsAsString = ForgeLatestVersions.keySet().stream().collect(Collectors.joining(", "));
                        LogInfo( ForgeVersionsAsString );

                        // Using Scanner for Getting Input from User
                        Scanner in = new Scanner(System.in);

                        LogInfo("Wich MINECRAFT-Version you like to install [ eg. " + firstEntry.getKey() + " ]:");
                        String mcVersionInput = in.nextLine();

                        StringBuilder mcVersionFiltered = new StringBuilder();

                        // Überprüfen und nur Zahlen und Punkte akzeptieren
                        for (char c : mcVersionInput.toCharArray()) {
                                if (Character.isDigit(c) || c == '.') {
                                        mcVersionFiltered.append(c);
                                }
                        }

                        mcVersion = String.valueOf(mcVersionFiltered);

                        if ( !ForgeLatestVersions.containsKey(mcVersion) ) {
                                // Der Schlüssel existiert in der Map nicht
                                LogError("The Minecraft-Version \"" + mcVersion + "\" not exists, restart Downloader.");
                                downloadLoader( installPath );
                        }

                        LogInfo("Wich FORGE-Version you like to install [ eg. " + ForgeLatestVersions.get( mcVersion) + " ]:");
                        String forgeVersionInput = in.nextLine();

                        StringBuilder forgeVersionFiltered = new StringBuilder();

                        // Überprüfen und nur Zahlen und Punkte akzeptieren
                        for (char c : forgeVersionInput.toCharArray()) {
                                if (Character.isDigit(c) || c == '.') {
                                        forgeVersionFiltered.append(c);
                                }
                        }

                        forgeVersion = String.valueOf(forgeVersionFiltered);

                }

                return downloadInstallerFile(installPath,mcVersion,forgeVersion);
        }

        private static boolean downloadInstallerFile(String installPath, String version, String build) {
                String fileURL = "https://maven.minecraftforge.net/net/minecraftforge/forge/"
                        + version + "-" + build + "/forge-" + version + "-" + build + "-installer.jar";
                String localFilePath = installPath + "/forge-" + version + "-" + build + "-installer.jar"; // Passe den Pfad an

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

                        LogForge("File downloaded successfully: " + localFilePath);
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

                Pattern pattern = Pattern.compile("forge-([.0-9]+)-([.0-9]+)-([universal|installer]+).([jar|zip]+)", Pattern.CASE_INSENSITIVE);

                String installerFile = null;
                String mcVersion = null;
                String forgeVersion = null;

                // try-catch block to handle exceptions
                try {
                        //LogInfo("Files are:");

                        // Display the names of the files
                        for (int i = 0; i < currentFiles.length; i++) {

                                Matcher matcher = pattern.matcher(currentFiles[i].getName());

                                if (matcher.find()) {
                                        mcVersion = matcher.group(1);
                                        forgeVersion = matcher.group(2);
                                        installerFile = currentFiles[i].getName();
                                        LogInfo("Match found INSTALLER with MC-Version " + mcVersion  + " and Forge " + forgeVersion );
                                        break;
                                }
                        }
                } catch (Exception e) {
                        ServerStarter.startupError = true;
                        LogWarning( e.getMessage() );
                }

                if ( installerFile != null ) {

                        try {
                                final String filename = new File(installerFile).getName();

                                LogInfo("Attempting to start Server " + installerFile);
                                LogInfo("Filename: " + filename);
                                LogInfo("Directory: " + installPath);
                                //LogInfo("Attempting to use installer from " + installPath);
                                LogInfo("Starting installation of Loader, installer output incoming");
                                LogInfo("Check log from installer for more information");
                                final Process start;

                                String javaStart = "java";
                                if (ServerStarter.configProps.getProperty("java_path") != null && !ServerStarter.configProps.getProperty("java_path").equals("java")) {
                                        javaStart = ServerStarter.configProps.getProperty("java_path");
                                        LogDebug("Use for Installer Custom Java Path: " + ServerStarter.configProps.getProperty("java_path"));
                                }

                                final Process installer = start = new ProcessBuilder(new String[]{ javaStart , "-jar", installerFile, "nogui", "--installServer"}).directory(new File(installPath)).start();
                                final Scanner serverLog = new Scanner(start.getInputStream());
                                while (serverLog.hasNextLine()) {
                                        final String println = serverLog.nextLine();
                                        LogForge(println);
                                }
                                installer.waitFor();
                                LogInfo("Done installing loader, deleting installer!");

                                final File installerFile2 = new File( installPath + File.separator + installerFile);
                                if (installerFile2.exists()) {
                                        Files.delete(installerFile2.toPath());
                                }

                                final File installerFileLog = new File(installPath + File.separator + installerFile + ".log");
                                if (installerFileLog.exists()) {
                                        Files.delete(installerFileLog.toPath());
                                }

                                final File installerFileRunBat = new File(installPath + File.separator + "run.bat");
                                if (installerFileRunBat.exists()) {
                                        Files.delete(installerFileRunBat.toPath());
                                }

                                final File installerFileRunSh = new File(installPath + File.separator + "run.sh");
                                if (installerFileRunSh.exists()) {
                                        Files.delete(installerFileRunSh.toPath());
                                }

                                final File installerFileJavaArgs = new File(installPath + File.separator + "user_jvm_args.txt");
                                if (installerFileJavaArgs.exists()) {
                                        Files.delete(installerFileJavaArgs.toPath());
                                }

                                return false;
                        } catch (IOException | InterruptedException e) {
                                LogWarning("Problem while installing Loader from " + installPath + File.separator + ' ' + e );
                                startupError = true;
                                return true;
                        }
                }else{
                        LogWarning("No \"libraries\"-Folders and no Installer-File could be found!");
                        startupError = true;
                        return true;
                }

        }
}

