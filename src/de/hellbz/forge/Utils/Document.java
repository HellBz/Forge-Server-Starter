package de.hellbz.forge.Utils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;

public class Document {

    public static void Eula() throws IOException {

        java.io.File eulaFile = new java.io.File("eula.txt");

        boolean eulaFileWrite = false;

        if (eulaFile.exists()) {
            Properties eulaProps = new Properties();
            FileReader eulaReader = new FileReader(eulaFile);
            // load the properties file and close
            eulaProps.load(eulaReader);
            eulaReader.close();

            if (Objects.equals(eulaProps.getProperty("eula"), "false")) {
                eulaFileWrite = true;
                Data.LogInfo("Set Eula-File to true");
            } else {
                Data.LogInfo("Eula-File is already accepted.");
            }
        } else {
            if (eulaFile.createNewFile()) {
                eulaFileWrite = true;
                Data.LogInfo("Creating a new Eula-File");
            }
        }

        if (eulaFileWrite) {
            FileWriter eulaWriter = new FileWriter(eulaFile);
            Properties eulaPropsNew = new Properties();
            eulaPropsNew.setProperty("eula", "true");
            eulaPropsNew.store(eulaWriter, "By changing the setting below to TRUE you are indicating your agreement to our EULA (https://account.mojang.com/documents/minecraft_eula).");
            eulaWriter.close();
        }
    }

    public static void StartFile() {

        Data.LogInfo("Creating " + (Config.OS.contains("win") ? "WINDOWS" : "UNIX") + "  Start-File.");

        try {
            Files.write( Paths.get(Config.rootFolder + File.separator  + "start_server." + (Config.OS.contains("win") ? "bat" : "sh" )),  (Config.OS.contains("win") ? Config.fileStartWinFileString : Config.fileStartLnxFileString ).getBytes(StandardCharsets.UTF_8 ) );
            Files.write( Paths.get(Config.rootFolder + File.separator  + "generate_auto_installation_file." + (Config.OS.contains("win") ? "bat" : "sh")),  (Config.OS.contains("win") ? Config.fileAutoWinFileString : Config.fileAutoLnxFileString ).getBytes(StandardCharsets.UTF_8 ) );

            FileOperation.downloadOrReadFile("/res/start_server" + (Config.OS.contains("win") ? "_win" : "_lnx") + ".txt"  , Config.rootFolder + File.separator  + "start_server." + (Config.OS.contains("win") ? "bat" : "sh") );
            FileOperation.downloadOrReadFile("/res/generate_auto_installation_file" + (Config.OS.contains("win") ? "_win" : "_lnx")  + ".txt" , Config.rootFolder + File.separator  + "generate_auto_installation_file." + (Config.OS.contains("win") ? "bat" : "sh") );

        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    /* AI optimized https://chat.openai.com/share/de913bc3-3958-477d-aefd-0a5387bda14a */
    public static void LogFile() {
        if (Objects.equals(Config.configProps.getProperty("log_to_file"), "true")) {

            String logFolder = "logs" + File.separator;

            // Create a File object for the log directory
            java.io.File directory = new java.io.File(logFolder);

            if (!directory.exists() && !directory.mkdirs()) {
                // Error creating the directory
                return;
            }

            // Create a File object for the log file
            java.io.File file = new java.io.File(logFolder + "server-starter.log");

            // Check if the file exists and is a regular file, then delete it
            if (file.exists() && file.isFile()) {
                if (!file.delete()) {
                    // Error deleting the file
                    return;
                }
            }

            // If the file doesn't exist, create it
            if (!file.exists()) {
                try {
                    boolean isFileCreated = file.createNewFile();
                    if (!isFileCreated) {
                        // Logge die Information oder handle den Fall, dass die Datei nicht erstellt werden konnte.
                        Data.LogDebug("The file \"server-starter.log\" could not be created.");
                    }
                } catch (IOException e) {
                    // e.printStackTrace();
                    Data.LogDebug("Failed to create the file: " + file.getPath() + " Message: " + e );
                }
            }
        }
    }

    public static boolean checkExist(String startup_file) {
        // Get the startup-file
        if (startup_file != null) {
            java.io.File check_file = new java.io.File(startup_file);

            // Check if the specified file
            // Exists or not
            if (!check_file.exists()) {
                Data.LogWarning("Start-File cannot be Found. ");
                Config.startupError = true;
                return false;
            } else {
                //Data.LogInfo("Start-File Found. ");
                Config.startupError = false;
                return true;
            }
        } else {
            Data.LogWarning("Start-File is empty!");
            Config.startupError = true;
            return false;
        }
    }

    /* https://chat.openai.com/share/cda966e3-ce50-4fe7-8e7d-7b4b86a63bd1 */
    //Right now unused
    public static byte[] cacheFile(String remoteFileURL, String localFileURL, long cacheTimeMillis) throws IOException {
        // Create a URL instance for the remote file.
        URL remoteURL = new URL(remoteFileURL);

        // Determine the file path for the local file.
        Path localFilePath = Paths.get(localFileURL);

        // Check if the local folder exists; create it if it doesn't.
        Path parentDirectory = localFilePath.getParent();
        if (parentDirectory != null && !Files.exists(parentDirectory)) {
            Files.createDirectories(parentDirectory);
        }

        // Check if the local file exists and if it is valid within the cache time.
        if (Files.exists(localFilePath)) {
            long lastModifiedTime = Files.getLastModifiedTime(localFilePath).toMillis();
            long currentTime = new Date().getTime();
            if (currentTime - lastModifiedTime < cacheTimeMillis) {
                // The local file is valid, no need to download it again.
                return Files.readAllBytes(localFilePath);
            }
        }
        byte[] fileContent;
        // Try to download the remote file.
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (InputStream in = remoteURL.openStream()) {
            byte[] data = new byte[1024]; // Ein temporäres Buffer-Array
            int bytesRead;
            while ((bytesRead = in.read(data)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            fileContent = buffer.toByteArray();
        } catch (IOException e) {
            // Wenn ein Fehler beim Download auftritt, versuchen Sie, die vorhandene lokale Datei zu lesen.
            if (Files.exists(localFilePath)) {
                return Files.readAllBytes(localFilePath);
            } else {
                throw e;
            }
        }

        // If download was successful, save the remote file locally.
        try (OutputStream out = Files.newOutputStream(localFilePath)) {
            out.write(fileContent);
        }

        return fileContent;
    }

    // Method to return the file name or a default name based on file existence and type
    public static String getJarFileName() {
        String defaultName = "minecraft_server.jar"; // Default file name
        try {
            Path jarPath = Paths.get( System.getProperty("java.class.path") );
            if (Files.exists(jarPath) && Files.isRegularFile(jarPath)) {
                defaultName = jarPath.getFileName().toString(); // Returns the actual file name if it's a regular file
            }
        } catch ( InvalidPathException e) {
            // Log or handle exceptions if necessary
        }
        return defaultName; // Returns default name if the file doesn't exist, is a directory, or in case of exception
    }

    // Overloaded method to directly return true or false based on the file existence and type when true is passed
    public static boolean getJarFileName(boolean checkExistence) {
        if (!checkExistence) {
            return false;
        }

        try {
            Path jarPath = Paths.get( System.getProperty("java.class.path") );
            return Files.exists(jarPath) && Files.isRegularFile(jarPath); // Checks if the path points to a regular file
        } catch (InvalidPathException e) {
            return false;
        }
    }

    public static void main(String[] args) {
        // Example usage
        System.out.println(getJarFileName()); // Print the jar file name or default
        System.out.println(getJarFileName(true)); // Print true if the path is a regular file, false otherwise
    }

}