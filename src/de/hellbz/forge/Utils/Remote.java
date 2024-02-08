package de.hellbz.forge.Utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Remote {

    public static boolean isConnected;

    static {
        // Test URLs, um die Internetverbindung beim Laden der Klasse zu prüfen
        String[] checkUrls = {"http://www.google.com", "http://www.github.com"};
        isConnected = checkInternetConnection(checkUrls);
    }

    // Konstruktor angepasst, um isConnected nicht zu ändern
    public Remote() {
        // Konstruktor logik, falls erforderlich
    }

    private static boolean checkInternetConnection(String[] urls) {
        for (String url : urls) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("HEAD");
                int responseCode = connection.getResponseCode();
                if (responseCode > 199 && responseCode < 400) {
                    return true; // Verbindung erfolgreich
                }
            } catch (IOException e) {
                // Verbindungsfehler, versuche die nächste URL
            }
        }
        return false; // Keine Verbindung zu irgendeiner URL
    }

    /* Zusätzliche oder überflüssige Funktion */
    /*
    public static Object downloadOrReadFile(String fileUrl, String destinationPath, boolean readOnly) {
        if (!isConnected) {
            System.out.println("No internet connection available.");
            return false; // Verbindung fehlt, Aktion wird nicht gestartet
        }

        if (readOnly) {
            return readFileContent(fileUrl);
        } else {
            return downloadFile(fileUrl, destinationPath);
        }
    }
    */

    public static Object downloadOrReadFile(String source) {
        return downloadOrReadFile(source, null );
    }
    public static Object downloadOrReadFile(String source, String destinationPath) {
        // Prüfen, ob es sich um eine URL oder einen lokalen Pfad handelt
        boolean isUrl = source.toLowerCase().startsWith("http://") || source.toLowerCase().startsWith("https://");

        // Wenn destinationPath gesetzt ist und es sich um eine URL handelt, versuchen Sie, die Datei herunterzuladen
        if (destinationPath != null && !destinationPath.isEmpty() && isUrl) {
            try (InputStream in = new URL(source).openStream()) {
                Files.copy(in, Paths.get(destinationPath), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("File downloaded successfully.");
                return true;
            } catch (Exception e) {
                System.out.println("Download failed: " + e.getMessage());
                return false;
            }
        } else {
            // Andernfalls versuchen Sie, die Datei lokal zu lesen
            StringBuilder content = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(source), StandardCharsets.UTF_8))) {
                String line;
                while ((line = in.readLine()) != null) {
                    content.append(line).append("\n");
                }
                System.out.println("File content:");
                System.out.println(content);
                return content.toString();
            } catch (Exception e) {
                System.out.println("Reading failed: " + e.getMessage());
                return null;
            }
        }
    }

    public static void checkForUpdate() {

        String localVersionPath = "jar:/res/version.xml"; // Lokaler Pfad zur XML-Datei im Ressourcenordner
        String remoteVersionUrl = "https://raw.githubusercontent.com/HellBz/Forge-Server-Starter/master/res/version.xml"; // Remote-URL zur XML-Datei auf GitHub

        String localVersion = Data.getFromXML( (String) downloadOrReadFile(localVersionPath) ,"version");
        String remoteVersion = Data.getFromXML( (String) downloadOrReadFile(remoteVersionUrl) ,"version");

        if ( remoteVersion != null && isConnected ) {
            // Vergleich der Versionen mit der benutzerdefinierten Vergleichsfunktion
            Data.VersionComparator versionComparator = new Data.VersionComparator();
            if (versionComparator.compare(localVersion, remoteVersion) < 0) {
                System.out.println("A new version is available: " + remoteVersion);
                // Weitere Aktionen für das Update...
            } else {
                System.out.println("You have the latest version.");
            }
        }
    }

}
