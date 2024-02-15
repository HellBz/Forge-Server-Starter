package de.hellbz.forge.Utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;


public class Remote {

    public static Object downloadOrReadFile(String source) {
        return downloadOrReadFile(source, null );
    }
    public static Object downloadOrReadFile(String source, String destinationPath) {
        // Prüfen, ob es sich um eine URL oder einen lokalen Pfad handelt
        boolean isUrl = source.toLowerCase().startsWith("http://") || source.toLowerCase().startsWith("https://");

        try (InputStream in = isUrl ? new URL(source).openStream() : Remote.class.getResourceAsStream(source) != null ? Remote.class.getResourceAsStream(source) : new FileInputStream(source)) {
            if (destinationPath != null && !destinationPath.isEmpty()) {
                // Wenn ein Ziel angegeben ist, versuche, die Datei zu speichern
                Files.copy(Objects.requireNonNull(in), Paths.get(destinationPath), StandardCopyOption.REPLACE_EXISTING);
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


        if ( ( remoteVersion != null || localVersion != null )  && Net.isConnected ) {
            //System.out.println("Local version: " + localVersion);
            //System.out.println("Remote version: " + remoteVersion);
            localVersion = "1.0"; //Just for DEBUG purposes
            // Vergleich der Versionen mit der benutzerdefinierten Vergleichsfunktion
            Data.VersionComparator versionComparator = new Data.VersionComparator();
            if ( versionComparator.compare(localVersion, remoteVersion) < 0 ) {
                Data.LogWarning("----------------------------------------------------------------");
                Data.LogWarning( Data.CYAN_BRIGHT  + "Update is available" + Data.TXT_RESET +", New Version: " + Data.GREEN_BRIGHT  + remoteVersion + Data.TXT_RESET + ", Your local Version is. " +  Data.RED_BOLD +localVersion + " " + Data.TXT_RESET );
                // Beispielaufrufe mit verschiedenen Parametern

                String last_git_update = GitHubAPI.sendRequest("repos/HellBz/Forge-Server-Starter/commits","0/commit/committer/date", "res/version.xml");

                Data.LogWarning("Latest Update if from: " + last_git_update + " on GitHub.");

                Data.LogWarning("You find the newest Versions there:");
                Data.LogWarning("https://www.curseforge.com/minecraft/mc-mods/forge-server-starter");
                Data.LogWarning("----------------------------------------------------------------");
            } else {
                Data.LogInfo("----------------------------------------------------------------");
                Data.LogInfo( "You have the latest version of F-S-S, with: " + Data.GREEN_BRIGHT + remoteVersion + Data.TXT_RESET );
                Data.LogInfo("You find all Versions there:");
                Data.LogInfo("https://www.curseforge.com/minecraft/mc-mods/forge-server-starter");
                Data.LogInfo("----------------------------------------------------------------");
            }
        }

    }

}
