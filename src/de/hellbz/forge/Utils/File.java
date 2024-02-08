package de.hellbz.forge.Utils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;

public class File {

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

    public static void StartFile() throws IOException {
        String fileName;
        String command;

        if (Config.OS.contains("win")) {
            fileName = "start_server.bat";
            command = "@echo off\njava -jar minecraft_server.jar -Xmx1024M -Xms1024M nogui\npause\n";
            Data.LogInfo("Creating Windows Start-File.");
        } else {
            fileName = "start_server.sh";
            command = "java -jar minecraft_server.jar -Xmx1024M -Xms1024M nogui\n";
            Data.LogInfo("Creating UNIX Start-File.");
        }

        try (PrintWriter startFileWriter = new PrintWriter(new FileWriter(fileName, false))) {
            startFileWriter.println(command);
        }
    }


    /* AI optimized https://chat.openai.com/share/de913bc3-3958-477d-aefd-0a5387bda14a */
    public static void LogFile() {
        if (Objects.equals(Config.configProps.getProperty("log_to_file"), "true")) {

            String logFolder = "logs/";

            // Create a File object for the log directory
            java.io.File directory = new java.io.File( logFolder );

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
                    if (!file.createNewFile()) {
                        // Error creating the file
                    }
                } catch (IOException e) {
                    // e.printStackTrace();
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

        // Try to download the remote file.
        byte[] fileContent = null;
        try (InputStream in = remoteURL.openStream()) {
            fileContent = new byte[in.available()];
            in.read(fileContent);
        } catch (IOException e) {
            // If there's an error during download, read the existing local file.
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
}