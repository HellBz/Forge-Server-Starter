package de.hellbz.forge.Utils.ModLoader;

import de.hellbz.forge.Utils.FileOperation;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import static de.hellbz.forge.Utils.Data.*;

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

                        // NeoForge versioning:
                        // Old (MC 1.x era, NeoForge major < 26):
                        //   3 segments: <mcMinor>.<mcPatch>.<nfBuild>  e.g. 21.1.3 -> MC 1.21.1
                        // New (MC 26+ era, NeoForge major >= 26):
                        //   4 segments: <mcMajor>.<mcMinor>.<mcPatch>.<nfBuild>  e.g. 26.1.2.2-beta -> MC 26.1.2
                        // See: https://neoforged.net/news/26.1release/
                        int neoMajorInt = 0;
                        try { neoMajorInt = Integer.parseInt(parts[0]); } catch (NumberFormatException ignored) {}

                        String mcKey;
                        if (neoMajorInt < 26) {
                            // Old scheme: first two segments = MC minor + patch -> key "1.major.minor"
                            String neoMajor = parts[0];
                            String neoMinor = parts.length > 1 ? parts[1] : "0";
                            mcKey = "1." + neoMajor + "." + neoMinor;
                        } else {
                            // New scheme: first three segments = full MC version -> key "major.minor.patch"
                            String p0 = parts[0];
                            String p1 = parts.length > 1 ? parts[1] : "0";
                            String p2 = parts.length > 2 ? parts[2] : "0";
                            mcKey = p0 + "." + p1 + "." + p2;
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
                    LogWarning("Invalid JSON format.");
                    LogDebug(e.getMessage());
                    return null;
                }
            } else {
                LogWarning("Failed to load JSON.");
                return null;
            }
        } else {
            LogWarning("Could not read remote file. Response-Code: " + getVersionJSON.getResponseCode() + ", continuing offline.");
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