package de.hellbz.forge.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
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

        String localVersionPath = "/res/modInfo.json";
        // Use raw GitHub URL which returns plain JSON (no HTML wrapper, no bot blocking)
        String remoteVersionUrl = "https://raw.githubusercontent.com/HellBz/Forge-Server-Starter/HEAD/res/modInfo.json";

        FileOperation localContent = FileOperation.downloadOrReadFile(localVersionPath);
        String localVersion = null;
        if (localContent != null && localContent.getContent() != null) {
            localVersion = Data.getJsonValue((String) localContent.getContent(), "version");
        }
        if (localVersion == null) {
            Data.LogDebug("Could not read local version from modInfo.json, skipping update check.");
            return;
        }

        if (Config.isUniqueIdRequestEnabled()) {
            try {
                requestUniqueID(localVersion);
            } catch (IOException e) {
                Data.LogDebug("An Error Occurs, while calling API: " + e);
            }
        }

        FileOperation remoteContent = FileOperation.downloadOrReadFile(remoteVersionUrl);
        String remoteVersion = null;
        if (remoteContent.getResponseCode() == 200) {
            remoteVersion = Data.getJsonValue((String) remoteContent.getContent(), "version");
        } else {
            Data.LogDebug("Could not fetch remote version (HTTP " + remoteContent.getResponseCode() + "), skipping update check.");
        }

        if ((remoteVersion != null || localVersion != null) && Net.isConnected) {
            Data.LogDebug("Local version: " + localVersion);
            Data.LogDebug("Remote version: " + remoteVersion);

            Data.VersionComparator versionComparator = new Data.VersionComparator();
            // Get commit date via GitHub API instead of scraping HTML page
            String committedDate = getGitHubCommittedDateViaApi();

            if (remoteVersion != null && versionComparator.compare(localVersion, remoteVersion) < 0) {
                Data.LogWarning("----------------------------------------------------------------");
                Data.LogWarning(Data.CYAN_BRIGHT + "Update is available" + Data.TXT_RESET
                        + ", New Version: " + Data.GREEN_BRIGHT + remoteVersion + Data.TXT_RESET
                        + ", Your local Version is: " + Data.RED_BOLD + localVersion + " " + Data.TXT_RESET);
                if (committedDate != null) {
                    Data.LogWarning("Latest Update is from: " + committedDate + " on GitHub.");
                }
                Data.LogWarning("You find the newest Versions there:");
                Data.LogWarning("https://www.curseforge.com/minecraft/mc-mods/forge-server-starter");
                Data.LogWarning("----------------------------------------------------------------");
            } else {
                Data.LogInfo("----------------------------------------------------------------");
                Data.LogInfo("You have the latest version of F-S-S, with: " + Data.GREEN_BRIGHT + (remoteVersion != null ? remoteVersion : localVersion) + Data.TXT_RESET);
                if (committedDate != null) {
                    Data.LogInfo("Latest Update is from: " + committedDate + " on GitHub.");
                }
                Data.LogInfo("You find all Versions there:");
                Data.LogInfo("https://www.curseforge.com/minecraft/mc-mods/forge-server-starter");
                Data.LogInfo("----------------------------------------------------------------");
            }
        }
    }

    /**
     * Gets the last commit date for the modInfo.json file via the GitHub REST API.
     * This is more reliable than scraping the HTML commits page (which returned HTTP 500).
     */
    private static String getGitHubCommittedDateViaApi() {
        if (!Net.isConnected) return null;
        try {
            String apiUrl = "https://api.github.com/repos/HellBz/Forge-Server-Starter/commits?path=res/modInfo.json&per_page=1";
            FileOperation result = downloadOrReadFile(apiUrl);
            if (result.getResponseCode() == 200 && result.getContent() != null) {
                // Extract date from JSON array: [{"commit":{"author":{"date":"..."}}}]
                String content = result.getContent().toString();
                Pattern pattern = Pattern.compile("\"date\"\\s*:\\s*\"([^\"]+)\"");
                Matcher matcher = pattern.matcher(content);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        } catch (Exception e) {
            Data.LogDebug("Could not fetch commit date: " + e.getMessage());
        }
        return null;
    }

    /**
     * @deprecated Use getGitHubCommittedDateViaApi() instead.
     * This method scraped the GitHub HTML commits page which frequently returns HTTP 500.
     */
    @Deprecated
    public static String getGitHubCommittedDate(String url) {
        if (!Net.isConnected) return null;
        FileOperation remoteReadResult = downloadOrReadFile(url);
        if (remoteReadResult.getResponseCode() == 200) {
            Pattern pattern = Pattern.compile("\"committedDate\":\"(.*?)\"");
            Matcher matcher = pattern.matcher(remoteReadResult.getContent().toString());
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    public static void requestUniqueID(String localVersion) throws IOException {

        String uniqueId = Config.getUniqueId();

        String response;
        try {
            response = sendApiRequest(localVersion, uniqueId);
        } catch (IOException e) {
            Data.LogDebug("UniqueID API request failed: " + e.getMessage());
            return;
        }

        Data.LogDebug("API-UniqueID-Response: " + response);

        try {
            JSONObject jsonResponse = new JSONObject(response);
            String newUniqueId = null;

            if (jsonResponse.has("data") && !jsonResponse.isNull("data")) {
                JSONObject dataObject = jsonResponse.getJSONObject("data");
                if (dataObject.has("unique_id") && !dataObject.isNull("unique_id")) {
                    newUniqueId = dataObject.getString("unique_id");
                }
            }

            if (newUniqueId != null && !newUniqueId.equals(uniqueId)) {
                Data.updateProperty(Config.PROPERTIES_FILE, "unique_id", newUniqueId);
            }

            if (jsonResponse.has("error") && !jsonResponse.isNull("error")
                    && jsonResponse.has("message") && !jsonResponse.isNull("message")) {
                if (jsonResponse.getBoolean("error")) {
                    Data.LogDebug("API-UniqueID, " + Data.RED_BOLD + jsonResponse.getString("message") + Data.TXT_RESET);
                }
            }

        } catch (JSONException e) {
            Data.LogDebug("Error parsing UniqueID JSON response: " + e.getMessage());
        }
    }

    private static String sendApiRequest(String localVersion, String uniqueId) throws IOException {

        String urlString = API_URL
                + "?version=" + URLEncoder.encode(localVersion != null ? localVersion : "", StandardCharsets.UTF_8.name())
                + (!uniqueId.isEmpty() ? "&unique_id=" + URLEncoder.encode(uniqueId, StandardCharsets.UTF_8.name()) : "")
                + (Config.macAddress != null ? "&macAddress=" + URLEncoder.encode(Config.macAddress, StandardCharsets.UTF_8.name()) : "");

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("User-Agent", "Forge-Server-Starter/3.6 (Java/" + System.getProperty("java.version") + ")");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        String propertiesToPostData = Data.propertiesToURL("server.properties");
        if (propertiesToPostData != null) {
            connection.setDoOutput(true);
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
