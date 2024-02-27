package de.hellbz.forge.Utils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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

    // Hilfsmethode zum Lesen des Dateiinhalts
    private static FileOperation readFileContent(InputStream in, String destinationPath) throws IOException {
        if (destinationPath != null && !destinationPath.isEmpty()) {
            Files.copy(in, Paths.get(destinationPath), StandardCopyOption.REPLACE_EXISTING);
            return new FileOperation(200, "Datei erfolgreich gespeichert", true);
        } else {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return new FileOperation(200, content.toString(), null);
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
        FileOperation remoteSaveReadResult = downloadOrReadFile("https://mediafilez.forgecdn.net/files/5113/957/Server-Files-0.2.48.zip", "Server-Files-0.2.48.zip");
        if (remoteSaveReadResult.getResponseCode() == 200) {
            System.out.println("Remote-Dateiinhalt (gespeichert): " + remoteSaveReadResult.getContent());
            System.out.println("Datei erfolgreich gespeichert: " + remoteSaveReadResult.getAdditionalData());
        } else {
            System.out.println("Fehler beim Lesen der Remote-Datei. Response-Code: " + remoteSaveReadResult.getResponseCode());
            System.out.println("Zusätzliche Informationen: " + remoteSaveReadResult.getAdditionalData());
        }
    }
}
