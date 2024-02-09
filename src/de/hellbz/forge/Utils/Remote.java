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

        try (InputStream in = isUrl ? new URL(source).openStream() : Remote.class.getResourceAsStream(source) != null ? Remote.class.getResourceAsStream(source) : new FileInputStream(source)) {
            if (destinationPath != null && !destinationPath.isEmpty()) {
                // Wenn ein Ziel angegeben ist, versuche, die Datei zu speichern
                Files.copy(in, Paths.get(destinationPath), StandardCopyOption.REPLACE_EXISTING);
                Data.LogDebug("File saved. (" + source + ") to (" + destinationPath + ")");
                return true;
            } else {
                // Andernfalls versuche, die Datei zu lesen und den Inhalt zurückzugeben
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                Data.LogDebug("File read (" + source + ")");
                return content.toString();
            }
        } catch (Exception e) {
            Data.LogWarning("File-Operation failed: " + e.getMessage() + "(" + source + ")");
            return destinationPath != null ? false : "";
        }
    }

    public static void checkForUpdate() {

        String localVersionPath = "/res/version.xml"; // Lokaler Pfad zur XML-Datei im Ressourcenordner
        String remoteVersionUrl = "https://raw.githubusercontent.com/HellBz/Forge-Server-Starter/master/res/version.xml"; // Remote-URL zur XML-Datei auf GitHub

        String localVersion = Data.getFromXML( (String) downloadOrReadFile(localVersionPath) ,"application/version");
        String remoteVersion = Data.getFromXML( (String) downloadOrReadFile(remoteVersionUrl) ,"application/version");

        if ( ( remoteVersion != null || localVersion != null )  && isConnected ) {

            //System.out.println("Local version: " + localVersion);
            //System.out.println("Remote version: " + remoteVersion);

            // Vergleich der Versionen mit der benutzerdefinierten Vergleichsfunktion
            Data.VersionComparator versionComparator = new Data.VersionComparator();
            if ( versionComparator.compare(localVersion, remoteVersion) < 0 ) {
                Data.LogInfo( "A new version is available: " + remoteVersion + ", Your local Version is. " + localVersion );
            } else {
                Data.LogInfo( "You have the latest version of: " + remoteVersion );
            }
        }
    }

}
