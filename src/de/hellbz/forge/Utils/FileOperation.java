package de.hellbz.forge.Utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileOperation {

    private final int responseCode;
    private final Object content;
    private final Object additionalData;

    // Konstruktor
    public FileOperation(int responseCode, Object content, Object additionalData) {
        this.responseCode = responseCode;
        this.content = content;
        this.additionalData = additionalData;
    }

    // Getter-Methoden
    public int getResponseCode() {
        return responseCode;
    }

    public Object getContent() {
        return content;
    }

    public Object getAdditionalData() {
        return additionalData;
    }

    /**
     * Calls a URL without waiting for a response.
     *
     * @param urlString The URL as a String.
     * @return A FileOperation object with the status of the operation.
     */
    public static FileOperation callUrlWithoutResponse(String urlString) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET"); // or POST, depending on the requirement
            connection.setConnectTimeout(5000); // Sets a timeout
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                // Successfully called, but content is ignored.
                return new FileOperation(responseCode, "URL successfully called, response ignored.", null);
            } else {
                // The server returned an error
                return new FileOperation(responseCode, null, "Server returned an error.");
            }
        } catch (IOException e) {
            return new FileOperation(500, null, "URL call failed: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // Methode zum Lesen oder Herunterladen von Dateien
    public static FileOperation downloadOrReadFile(File source) {
        return downloadOrReadFile(source, null);
    }

    public static FileOperation downloadOrReadFile(File source, String destinationPath) {
        try ( InputStream in = Files.newInputStream(source.toPath()) ) {
            return readFileContent(in, destinationPath);
        } catch (IOException e) {
            return new FileOperation(500, null, "File-Operation failed: " + e.getMessage());
        }
    }

    public static FileOperation downloadOrReadFile(String source) {
        return downloadOrReadFile(source, null);
    }

    public static FileOperation downloadOrReadFile(String source, String destinationPath) {
        boolean isUrl = source.toLowerCase().startsWith("http://") || source.toLowerCase().startsWith("https://");

        try (InputStream in = isUrl ? new URL(source).openStream() : FileOperation.class.getResourceAsStream(source) != null ? FileOperation.class.getResourceAsStream(source) : Files.newInputStream(Paths.get(source))) {
            return readFileContent(in, destinationPath);
        } catch (IOException e) {
            return new FileOperation(500, null, "File-Operation failed: " + e.getMessage());
        }
    }

    /**
     * Downloads or reads a file with optional cache support.
     *
     * @param source URL or file path
     * @param destinationPath Local path to save or check the file
     * @param maxAge Optional maximum age of the file cache in milliseconds, if 0 caching is not used
     * @return FileOperation object with status and content or error info
     */
    public static FileOperation downloadOrReadFile(String source, String destinationPath, long maxAge) {
        if (maxAge > 0) {
            File file = new File(destinationPath);
            if (file.exists() && (System.currentTimeMillis() - file.lastModified() < maxAge)) {
                // Read from cache if the file is still valid
                return downloadOrReadFile(file);
            }
        }
        // Download and cache if the file does not exist, is outdated, or if caching is not used
        return downloadOrReadFile(source, destinationPath);
    }

    // Hilfsmethode zum Lesen des Dateiinhalts
    private static FileOperation readFileContent(InputStream in, String destinationPath) throws IOException {
        // Puffer für das Lesen der Daten
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append("\n");
        }

        // Wenn ein Ziel angegeben wurde, speichern wir den Inhalt
        if (destinationPath != null && !destinationPath.isEmpty()) {
            Files.write(Paths.get(destinationPath), content.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            // Inhalt bleibt in content und zusätzliche Informationen über den Speicherstatus in additionalData
            return new FileOperation(200, content.toString(), "File downloaded and saved");
        } else {
            // Keine zusätzlichen Daten benötigt, da keine Speicheraktion erfolgt
            return new FileOperation(200, content.toString(), "Read File from Cache");
        }
    }

    // Hauptmethode zum Testen
    public static void main(String[] args) {
        // Testen des Lesens lokaler Dateien
        FileOperation localReadResult = downloadOrReadFile(new File("local_file.txt"));
        System.out.println("Lokaler Dateiinhalt: " + localReadResult.getContent());

        // Testen des Speicherns und Lesens lokaler Dateien
        FileOperation localSaveReadResult = downloadOrReadFile(new File("local_file.txt"), "destination_path.txt");
        System.out.println("Lokaler Dateiinhalt (gespeichert): " + localSaveReadResult.getContent());

        // Testen des Speicherns und Lesens lokaler Dateien
        FileOperation localExtractResult = downloadOrReadFile("/res/version.xml", "version.xml");
        System.out.println("Lokaler Dateiinhalt (gespeichert): " + localExtractResult.getContent());

        // Testen des Lesens von Remote-Dateien
        FileOperation remoteReadResult = downloadOrReadFile("https://example.com/remote_file.txt");
        if (remoteReadResult.getResponseCode() == 200) {
            System.out.println("Remote-Dateiinhalt: " + remoteReadResult.getContent());
        } else {
            System.out.println("Fehler beim Lesen der Remote-Datei. Response-Code: " + remoteReadResult.getResponseCode());
            System.out.println("Zusätzliche Informationen: " + remoteReadResult.getAdditionalData());
        }

        // Testen des Speicherns und Lesens von Remote-Dateien
        FileOperation remoteSaveReadResult = downloadOrReadFile("https://api.curseforge.com/v1/minecraft/modloader/", "ModLoader.json");
        if (remoteSaveReadResult.getResponseCode() == 200) {
            //System.out.println("Remote-Dateiinhalt (gespeichert): " + remoteSaveReadResult.getContent());
            System.out.println("Datei erfolgreich gespeichert: " + remoteSaveReadResult.getAdditionalData());
        } else {
            System.out.println("Fehler beim Lesen der Remote-Datei. Response-Code: " + remoteSaveReadResult.getResponseCode());
            System.out.println("Zusätzliche Informationen: " + remoteSaveReadResult.getAdditionalData());
        }

        // Testen des Speicherns und Lesens von Remote-Dateien mit caching file
        FileOperation remoteSaveReadResultCache = downloadOrReadFile("https://api.curseforge.com/v1/minecraft/modloader/", "ModLoaderCahce.json", 60000 );
        if (remoteSaveReadResult.getResponseCode() == 200) {
            //System.out.println("Remote-Dateiinhalt (gespeichert): " + remoteSaveReadResultCache.getContent());
            System.out.println("Datei erfolgreich gespeichert: " + remoteSaveReadResultCache.getAdditionalData());
        } else {
            System.out.println("Fehler beim Lesen der Remote-Datei. Response-Code: " + remoteSaveReadResultCache.getResponseCode());
            System.out.println("Zusätzliche Informationen: " + remoteSaveReadResultCache.getAdditionalData());
        }
    }
}
