package de.hellbz.forge.Utils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

class FileOperationResult {
    private int responseCode;
    private Object content;
    private Object additionalData;

    public FileOperationResult(int responseCode, Object content, Object additionalData) {
        this.responseCode = responseCode;
        this.content = content;
        this.additionalData = additionalData;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public Object getContent() {
        return content;
    }

    public Object getAdditionalData() {
        return additionalData;
    }
}

public class FileOperation {
    public static void main(String[] args) {
        // Lokale Datei lesen
        FileOperationResult result1 = downloadOrReadFile("local_file.txt");
        System.out.println("Lokaler Dateiinhalt: " + (String) result1.getContent());

        // Lokale Datei speichern und lesen
        FileOperationResult result2 = downloadOrReadFile("local_file.txt", "destination_path.txt");
        System.out.println("Lokaler Dateiinhalt (gespeichert): " + (String) result2.getContent());
        System.out.println("Datei erfolgreich gespeichert: " + (boolean) result2.getAdditionalData());

        // Remote-Datei lesen
        FileOperationResult result3 = downloadOrReadFile("https://example.com/remote_file.txt");
        if (result3.getResponseCode() == 200) {
            String remoteContent = (String) result3.getContent();
            System.out.println("Remote-Dateiinhalt: " + remoteContent);
        } else {
            System.out.println("Fehler beim Lesen der Remote-Datei. Response-Code: " + result3.getResponseCode());
        }

        // Remote-Datei speichern und lesen
        FileOperationResult result4 = downloadOrReadFile("https://example.com/remote_file.txt", "destination_path.txt");
        if (result4.getResponseCode() == 200) {
            String remoteContent = (String) result4.getContent();
            System.out.println("Remote-Dateiinhalt (gespeichert): " + remoteContent);
            System.out.println("Datei erfolgreich gespeichert: " + (boolean) result4.getAdditionalData());
        } else {
            System.out.println("Fehler beim Lesen der Remote-Datei. Response-Code: " + result4.getResponseCode());
        }
    }

    public static FileOperationResult downloadOrReadFile(String source) {
        return downloadOrReadFile(source, null);
    }

    public static FileOperationResult downloadOrReadFile(String source, String destinationPath) {
        boolean isUrl = source.toLowerCase().startsWith("http://") || source.toLowerCase().startsWith("https://");

        try (InputStream in = isUrl ? new URL(source).openStream() : FileOperation.class.getResourceAsStream(source) != null ? FileOperation.class.getResourceAsStream(source) : new FileInputStream(source)) {
            if (destinationPath != null && !destinationPath.isEmpty()) {
                Files.copy(in, Paths.get(destinationPath), StandardCopyOption.REPLACE_EXISTING);
                return new FileOperationResult(200, "Datei erfolgreich gespeichert", true);
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                return new FileOperationResult(200, content.toString(), null);
            }
        } catch (IOException e) {
            return new FileOperationResult(500, null, "File-Operation failed: " + e.getMessage());
        }
    }
}
