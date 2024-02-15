package de.hellbz.forge.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import static de.hellbz.forge.Utils.Data.LogError;

public class NeoForge {

    public static Map<String, Map<String, Object>>  getVersions() {
        String neoJsonUrl = "https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/neoforge";

        FileOperationResult getVersionJSON = FileOperation.downloadOrReadFile(neoJsonUrl);
        if (getVersionJSON.getResponseCode() == 200) {
            String jsonString = (String) getVersionJSON.getContent();
            if (jsonString != null) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonString);
                    JSONArray versionsArray = jsonObject.getJSONArray("versions");

                    Map<String, Map<String, Object>> NeoVersions = new TreeMap<>(Collections.reverseOrder(new Data.VersionComparator()));
                    Data.VersionComparator versionComparator = new Data.VersionComparator();

                    for (int i = 0; i < versionsArray.length(); i++) {
                        String version = versionsArray.getString(i);

                        String mcVersion = version.split("\\.")[0] + "." + version.split("\\.")[1];
                        //String neoVersion = version.replace(mcVersion + ".", "");

                        Map<String, Object> versionInfo = NeoVersions.getOrDefault("1." + mcVersion, new HashMap<>());

                        // Hol dir das JSON-Array der Versionen aus der Map oder erstelle eine neue Liste
                        List<String> versionsList = (List<String>) versionInfo.getOrDefault("versions", new ArrayList<>());

                        // Füge die NeoForge-Version hinzu
                        versionsList.add(version);

                        // Kehre die Reihenfolge der Liste um
                        Collections.reverse(versionsList);

                        // Speichere das JSON-Array wieder in der Map
                        versionInfo.put("versions", versionsList);

                        if (!NeoVersions.containsKey("1." + mcVersion) ||
                                versionComparator.compare(version, (String) NeoVersions.get("1." + mcVersion).get("latest") ) > 0) {
                            //NeoVersions.put("1." + mcVersion, neoVersion);
                            versionInfo.put("latest", version);
                            //System.out.println("Debug: Highest Neo-Version for " + mcVersion + ": " + neoVersion);
                        }
                        NeoVersions.put("1." + mcVersion, versionInfo);
                    }

                    return NeoVersions;

                } catch (Exception e) {
                    LogError("Invalid JSON format.");
                    e.printStackTrace();
                    return null;
                }
            } else {
                LogError("Failed to load JSON.");
                return null;
            }
        } else {
            LogError("Fehler beim Lesen der Remote-Datei. Response-Code: " + getVersionJSON.getResponseCode());
            return null;
        }
    }
}