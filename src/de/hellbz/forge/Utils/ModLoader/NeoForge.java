package de.hellbz.forge.Utils.ModLoader;

import de.hellbz.forge.Utils.Data;
import de.hellbz.forge.Utils.FileOperation;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import static de.hellbz.forge.Utils.Data.LogError;

public class NeoForge {

    // https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/neoforge
    // https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml
    // https://maven.neoforged.net/#/releases/net/neoforged/neoforge
    // https://maven.neoforged.net/api/maven/details/releases/net/neoforged/neoforge
    // https://maven.neoforged.net/api/maven/details/releases/net/neoforged/neoforge/20.4.159-beta
    // https://maven.neoforged.net/releases/net/neoforged/neoforge/20.4.159-beta/neoforge-20.4.159-beta-installer.jar
    // https://maven.neoforged.net/api/maven/details/releases/net/neoforged/neoforge/20.4.159-beta/neoforge-20.4.159-beta-installer.jar

    public static Map<String, Map<String, Object>> getVersions() {
        String neoJsonUrl = "https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/neoforge";

        FileOperation getVersionJSON = FileOperation.downloadOrReadFile(neoJsonUrl);
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
                        String[] parts = version.split("\\.");

                        // NeoForge versioning (from docs.neoforged.net/docs/gettingstarted/versioning):
                        //   major = MC minor version, minor = MC patch version
                        //   e.g. NeoForge 20.2.x -> MC 1.20.2
                        //        NeoForge 21.1.x -> MC 1.21.1
                        // From MC 26.x, Minecraft dropped the leading '1.' in its own version string,
                        //   e.g. NeoForge 26.1.x.y -> MC 26.1  (NOT 1.26.1)
                        String neoMajor = parts[0]; // e.g. 20, 21, 26
                        String neoMinor = parts.length > 1 ? parts[1] : "0"; // e.g. 2, 1
                        int neoMajorInt = 0;
                        try { neoMajorInt = Integer.parseInt(neoMajor); } catch (NumberFormatException ignored) {}

                        // MC 1.x era: NeoForge major <= 21 (or whenever MC kept "1." prefix)
                        // MC 26+ era: NeoForge major >= 26, MC version is just "major.minor"
                        String mcKey;
                        if (neoMajorInt < 26) {
                            mcKey = "1." + neoMajor + "." + neoMinor;
                        } else {
                            mcKey = neoMajor + "." + neoMinor;
                        }

                        Map<String, Object> versionInfo = NeoVersions.getOrDefault(mcKey, new HashMap<>());

                        Object versionsObject = versionInfo.getOrDefault("versions", new ArrayList<String>());
                        List<String> versionsList = new ArrayList<>();

                        if (versionsObject instanceof List<?>) {
                            for (Object item : (List<?>) versionsObject) {
                                if (item instanceof String) {
                                    versionsList.add((String) item);
                                } else {
                                    Data.LogDebug("An element was not a String, has been skipped: " + item);
                                }
                            }
                        }

                        versionsList.add(version);
                        Collections.reverse(versionsList);
                        versionInfo.put("versions", versionsList);

                        if (!NeoVersions.containsKey(mcKey) ||
                                versionComparator.compare(version, (String) NeoVersions.get(mcKey).get("latest")) > 0) {
                            versionInfo.put("latest", version);
                        }
                        NeoVersions.put(mcKey, versionInfo);
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

    public static Map<String, String> getFileLinks(String build) {
        String fileURL;
        String localFilePath;


        fileURL = "https://maven.neoforged.net/releases/net/neoforged/neoforge/" + build + "/neoforge-" + build + "-installer.jar";
        localFilePath = "/" + "neoforge-" + build + "-installer.jar";


        Map<String, String> links = new HashMap<>();
        links.put("fileURL", fileURL);
        links.put("localFilePath", localFilePath);

        return links;
    }
}