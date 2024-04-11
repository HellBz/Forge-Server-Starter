package de.hellbz.forge.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.hellbz.forge.Utils.FileOperation.downloadOrReadFile;

public class Remote {

    private static final String API_URL = "https://api.hellbz.de/update/forge-server-starter/";

    public static void checkForUpdate() {

        String localVersionPath = "/res/modInfo.json"; // Lokaler Pfad zur XML-Datei im Ressourcenordner
        String remoteVersionUrl = "https://raw.githubusercontent.com/HellBz/Forge-Server-Starter/master" + localVersionPath; // Remote-URL zur XML-Datei auf GitHub

        String localVersion = Data.getJsonValue( (String) FileOperation.downloadOrReadFile(localVersionPath).getContent() , "version" );

        if ( Config.configProps.getProperty("unique_id_request","true" ).equals("true") ){
            try {
                requestUniqueID(localVersion);
            } catch (IOException e) {
                Data.LogDebug("An Error Occurs, while calling API : " + e );
            }
        }

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
            String committedDate = getGitHubCommittedDate("https://github.com/HellBz/Forge-Server-Starter/commits/master/res/modInfo.json");
            if (versionComparator.compare(localVersion, remoteVersion) < 0) {
                Data.LogWarning("----------------------------------------------------------------");
                Data.LogWarning(Data.CYAN_BRIGHT + "Update is available" + Data.TXT_RESET + ", New Version: " + Data.GREEN_BRIGHT + remoteVersion + Data.TXT_RESET + ", Your local Version is. " + Data.RED_BOLD + localVersion + " " + Data.TXT_RESET);
                Data.LogWarning("Latest Update if from: " + committedDate + " on GitHub.");
                Data.LogWarning("You find the newest Versions there:");
                Data.LogWarning("https://www.curseforge.com/minecraft/mc-mods/forge-server-starter");
                Data.LogWarning("----------------------------------------------------------------");
            } else {
                Data.LogInfo("----------------------------------------------------------------");
                Data.LogInfo("You have the latest version of F-S-S, with: " + Data.GREEN_BRIGHT + remoteVersion + Data.TXT_RESET);
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


                } /* else {
                    //System.out.println("Committed Date nicht gefunden.");
                } */

            } /* else {
                //System.out.println("Fehler beim Lesen der Remote-Datei. Response-Code: " + remoteReadResult.getResponseCode());
                //System.out.println("Zusätzliche Informationen: " + remoteReadResult.getAdditionalData());
            } */
        }
        return committedDate;
    }
    public static void requestUniqueID(String localVersion) throws IOException {

        String uniqueId = Config.configProps.getProperty("unique_id", "" );

        String response = sendApiRequest(localVersion, uniqueId);

        Data.LogDebug( "API-UniqueID-Response: " + response );

        try {
            JSONObject jsonResponse = new JSONObject(response);
            String newUniqueId = null;

            // Check if the "data" object is present and not null.
            if (jsonResponse.has("data") && !jsonResponse.isNull("data")) {
                JSONObject dataObject = jsonResponse.getJSONObject("data");
                // Ensure that "unique_id" is present before accessing it.
                if (dataObject.has("unique_id") && !dataObject.isNull("unique_id")) {
                    newUniqueId = dataObject.getString("unique_id");
                }
            }

            // Check if newUniqueId is not null and differs from uniqueId before calling updateProperty.
            if (newUniqueId != null && !newUniqueId.equals(uniqueId)) {
                Data.updateProperty(Config.PROPERTIES_FILE, "unique_id", newUniqueId);
            }

            if (jsonResponse.has("error") && !jsonResponse.isNull("error") && jsonResponse.has("message") && !jsonResponse.isNull("message")) {

                if ( jsonResponse.getBoolean("error") ) {
                    Data.LogDebug("API-UniqueID, " + Data.RED_BOLD + jsonResponse.getString("message") + Data.TXT_RESET );
                }
            }

        } catch (JSONException e) {
            System.err.println("Fehler beim Parsen der JSON-Antwort: " + e.getMessage());
        }




    }

    private static String sendApiRequest(String localVersion, String uniqueId) throws IOException {

        // URL mit Query-Parametern vorbereiten
        String urlString = API_URL + "?version=" + URLEncoder.encode(localVersion, StandardCharsets.UTF_8.name()) +
                (!uniqueId.isEmpty() ? "&unique_id=" + URLEncoder.encode(uniqueId, StandardCharsets.UTF_8.name()) : "") +
                (Config.macAddress!= null ? "&macAddress=" + URLEncoder.encode(Config.macAddress, StandardCharsets.UTF_8.name()) : "");

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // POST-Anfrage konfigurieren
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        // Properties-Datei-Inhalt in einen String umwandeln
        String propertiesToPostData = Data.propertiesToURL("server.properties");
        if ( propertiesToPostData != null ) {
            connection.setDoOutput(true);
            // Properties-Datei-Inhalt in den Request-Body schreiben
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = propertiesToPostData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } else {
            throw new IOException("Failed to get response from the server. HTTP Response Code: " + responseCode);
        }
    }
}
