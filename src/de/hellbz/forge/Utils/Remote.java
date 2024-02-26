package de.hellbz.forge.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.hellbz.forge.Utils.FileOperation.downloadOrReadFile;

public class Remote {

    public static void checkForUpdate() {

        String localVersionPath = "/res/modInfo.json"; // Lokaler Pfad zur XML-Datei im Ressourcenordner
        String remoteVersionUrl = "https://raw.githubusercontent.com/HellBz/Forge-Server-Starter/master" + localVersionPath; // Remote-URL zur XML-Datei auf GitHub

        // Lokale Datei speichern und lesen

        String localVersion = Data.getJsonValue( (String) FileOperation.downloadOrReadFile(localVersionPath).getContent() , "version" );

        FileOperation remoteContent = FileOperation.downloadOrReadFile(remoteVersionUrl);
        String remoteVersion = null;
        if (remoteContent.getResponseCode() == 200) {
            remoteVersion = Data.getJsonValue( (String) remoteContent.getContent(), "version");
        }
        if ((remoteVersion != null || localVersion != null) && Net.isConnected) {
            Data.LogDebug("Local version: " + localVersion);
            Data.LogDebug("Remote version: " + remoteVersion);
            //localVersion = "1.0"; //Just for DEBUG purposes
            // Vergleich der Versionen mit der benutzerdefinierten Vergleichsfunktion
            Data.VersionComparator versionComparator = new Data.VersionComparator();
            if (versionComparator.compare(localVersion, remoteVersion) < 0) {
                Data.LogWarning("----------------------------------------------------------------");
                Data.LogWarning(Data.CYAN_BRIGHT + "Update is available" + Data.TXT_RESET + ", New Version: " + Data.GREEN_BRIGHT + remoteVersion + Data.TXT_RESET + ", Your local Version is. " + Data.RED_BOLD + localVersion + " " + Data.TXT_RESET);
                // Beispielaufrufe mit verschiedenen Parametern

                //String last_git_update = GitHubAPI.sendRequest("repos/HellBz/Forge-Server-Starter/commits", "0/commit/committer/date", "res/version.xml");

                //Data.LogWarning("Latest Update if from: " + last_git_update + " on GitHub.");
                getGitHubCommittedDate("https://github.com/HellBz/Forge-Server-Starter/commits/master/res/version.xml");
                String committedDate = getGitHubCommittedDate( remoteVersionUrl );
                Data.LogWarning("Latest Update if from: " + committedDate + " on GitHub.");
                Data.LogWarning("You find the newest Versions there:");
                Data.LogWarning("https://www.curseforge.com/minecraft/mc-mods/forge-server-starter");
                Data.LogWarning("----------------------------------------------------------------");
            } else {
                Data.LogInfo("----------------------------------------------------------------");
                Data.LogInfo("You have the latest version of F-S-S, with: " + Data.GREEN_BRIGHT + remoteVersion + Data.TXT_RESET);
                String committedDate = getGitHubCommittedDate(remoteVersionUrl);
                Data.LogInfo("Latest Update if from: " + committedDate + " on GitHub.");
                Data.LogInfo("You find all Versions there:");
                Data.LogInfo("https://www.curseforge.com/minecraft/mc-mods/forge-server-starter");
                Data.LogInfo("----------------------------------------------------------------");
            }
        }

    }
    public static String getGitHubCommittedDate(String url) {
        String committedDate = null;
        if ( Net.isConnected ) {
            FileOperation remoteReadResult = downloadOrReadFile(url);
            if (remoteReadResult.getResponseCode() == 200) {

                // Regulärer Ausdruck, um "committedDate" zu finden und zu extrahieren
                Pattern pattern = Pattern.compile("\"committedDate\":\"(.*?)\"");
                Matcher matcher = pattern.matcher(remoteReadResult.getContent().toString());

                // Wenn das "committedDate" gefunden wird
                if (matcher.find()) {
                    committedDate = matcher.group(1);
                    //System.out.println("Committed Date: " + committedDate);
                    
                    
                } else {
                    //System.out.println("Committed Date nicht gefunden.");
                }

            } else {
                //System.out.println("Fehler beim Lesen der Remote-Datei. Response-Code: " + remoteReadResult.getResponseCode());
                //System.out.println("Zusätzliche Informationen: " + remoteReadResult.getAdditionalData());
            }
        }
        return committedDate;
    }
}
