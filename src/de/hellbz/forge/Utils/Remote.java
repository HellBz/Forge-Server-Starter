package de.hellbz.forge.Utils;

public class Remote {

    public static void checkForUpdate() {

        String localVersionPath = "/res/version.xml"; // Lokaler Pfad zur XML-Datei im Ressourcenordner
        String remoteVersionUrl = "https://raw.githubusercontent.com/HellBz/Forge-Server-Starter/master/res/version.xml"; // Remote-URL zur XML-Datei auf GitHub

        // Lokale Datei speichern und lesen

        String localVersion = Data.getFromXML( (String) FileOperation.downloadOrReadFile(localVersionPath).getContent(), "application/version");

        FileOperation remoteContent = FileOperation.downloadOrReadFile(remoteVersionUrl);
        String remoteVersion = null;
        if (remoteContent.getResponseCode() == 200) {
            remoteVersion = Data.getFromXML( (String) remoteContent.getContent(), "application/version");
        }
        if ((remoteVersion != null || localVersion != null) && Net.isConnected) {
            //System.out.println("Local version: " + localVersion);
            //System.out.println("Remote version: " + remoteVersion);
            //localVersion = "1.0"; //Just for DEBUG purposes
            // Vergleich der Versionen mit der benutzerdefinierten Vergleichsfunktion
            Data.VersionComparator versionComparator = new Data.VersionComparator();
            if (versionComparator.compare(localVersion, remoteVersion) < 0) {
                Data.LogWarning("----------------------------------------------------------------");
                Data.LogWarning(Data.CYAN_BRIGHT + "Update is available" + Data.TXT_RESET + ", New Version: " + Data.GREEN_BRIGHT + remoteVersion + Data.TXT_RESET + ", Your local Version is. " + Data.RED_BOLD + localVersion + " " + Data.TXT_RESET);
                // Beispielaufrufe mit verschiedenen Parametern

                String last_git_update = GitHubAPI.sendRequest("repos/HellBz/Forge-Server-Starter/commits", "0/commit/committer/date", "res/version.xml");

                Data.LogWarning("Latest Update if from: " + last_git_update + " on GitHub.");

                Data.LogWarning("You find the newest Versions there:");
                Data.LogWarning("https://www.curseforge.com/minecraft/mc-mods/forge-server-starter");
                Data.LogWarning("----------------------------------------------------------------");
            } else {
                Data.LogInfo("----------------------------------------------------------------");
                Data.LogInfo("You have the latest version of F-S-S, with: " + Data.GREEN_BRIGHT + remoteVersion + Data.TXT_RESET);
                Data.LogInfo("You find all Versions there:");
                Data.LogInfo("https://www.curseforge.com/minecraft/mc-mods/forge-server-starter");
                Data.LogInfo("----------------------------------------------------------------");
            }
        }

    }
}
