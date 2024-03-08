package de.hellbz.forge.Utils.ModLoader;

import de.hellbz.forge.Utils.Config;
import de.hellbz.forge.Utils.FileOperation;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import static de.hellbz.forge.Utils.Data.*;

public class Forge {

    public static Map<String, Map<String, Object>> getVersions() {
        String forgeJsonUrl = "https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json";

        FileOperation getVersionJSON = FileOperation.downloadOrReadFile(forgeJsonUrl);
        if (getVersionJSON.getResponseCode() == 200) {

            String jsonString = (String) getVersionJSON.getContent();

            if (jsonString != null) {
                try {
                    Map<String, Map<String, Object>> forgeVersions = new TreeMap<>(Collections.reverseOrder(new VersionComparator()));

                    JSONObject jsonObject = new JSONObject(jsonString);
                    JSONObject promosObject = jsonObject.getJSONObject("promos");
                    Comparator<String> versionComparator = new VersionComparator();


                    Iterator<String> keys = promosObject.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        String mcVersion = key.replace("-latest", "").replace("-recommended", "");
                        String forgeVersion = promosObject.getString(key);

                        int check_1_5_2 = versionComparator.compare(mcVersion, "1.5.2");
                        if (check_1_5_2 >= 0) {
                            Map<String, Object> versionInfo = forgeVersions.getOrDefault(mcVersion, new HashMap<>());
                            versionInfo.put("versions", versionInfo.getOrDefault("versions", new JSONArray()));
                            ((JSONArray) versionInfo.get("versions")).put(forgeVersion);

                            if (key.endsWith("-recommended")) {
                                versionInfo.put("recommended", forgeVersion);
                            } else {
                                versionInfo.put("latest", forgeVersion);
                            }

                            forgeVersions.put(mcVersion, versionInfo);
                        }
                    }

                    return forgeVersions;

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

    public static Map<String, String> getFileLinks(String version, String build) {
        String fileURL = "";
        String localFilePath = "";

        Comparator<String> versionComparator = new VersionComparator();
        int check_1_7_10 = versionComparator.compare(version, "1.7.10");
        int check_1_7_10_pre = versionComparator.compare(version, "1.7.10_pre4");
        int check_1_7_2 = versionComparator.compare(version, "1.7.2");
        int check_1_5_2 = versionComparator.compare(version, "1.5.2");
        //int check_1_3_2 = versionComparator.compare(version, "1.3.2");

        /*
        if (check_1_5_2 < 0) {
            fileURL = "https://maven.minecraftforge.net/net/minecraftforge/forge/" + version + "-" + build + "/forge-" + version + "-" + build + "-universal.zip";
            localFilePath = "/" + "forge-" + version + "-" + build + "-universal.zip";
        } else if (check_1_3_2 < 0) {
            fileURL = "https://maven.minecraftforge.net/net/minecraftforge/forge/" + version + "-" + build + "/forge-" + version + "-" + build + "-server.zip";
            localFilePath = "/" + "forge-" + version + "-" + build + "-server.zip";
        } else
        */
        if (check_1_5_2 < 0) {
            LogError( "!!! Version " + version + " is below 1.5.2 and not Supported for Forge-Server.");
            Config.startupError = true;
        } else if (check_1_7_10 == 0) {
            fileURL = "https://maven.minecraftforge.net/net/minecraftforge/forge/" + version + "-" + build + "-" + version + "/forge-" + version + "-" + build + "-" + version + "-installer.jar";
            localFilePath = "/" + "forge-" + version + "-" + build + "-installer.jar";
        } else if (check_1_7_10_pre == 0) {
            fileURL = "https://maven.minecraftforge.net/net/minecraftforge/forge/" + version + "-" + build + "-prerelease/forge-" + version + "-" + build + "-prerelease-installer.jar";
            localFilePath = "/" + "forge-1.7.10-" + build + "-universal.zip";
        } else if (check_1_7_2 == 0) {
            fileURL = "https://maven.minecraftforge.net/net/minecraftforge/forge/" + version + "-" + build + "-mc172/forge-" + version + "-" + build + "-mc172-installer.jar";
            localFilePath = "/" + "forge-" + version + "-" + build + "-installer.jar";
        } else {
            fileURL = "https://maven.minecraftforge.net/net/minecraftforge/forge/" + version + "-" + build + "/forge-" + version + "-" + build + "-installer.jar";
            localFilePath = "/" + "forge-" + version + "-" + build + "-installer.jar";
        }

        Map<String, String> links = new HashMap<>();
        links.put("fileURL", fileURL);
        links.put("localFilePath", localFilePath);

        return links;
    }
}

