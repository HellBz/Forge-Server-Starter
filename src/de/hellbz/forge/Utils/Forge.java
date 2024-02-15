package de.hellbz.forge.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import static de.hellbz.forge.Utils.Data.LogError;
import static de.hellbz.forge.Utils.Data.VersionComparator;

public class Forge {

        public static Map<String, Map<String, Object>> getVersions() {
                String forgeJsonUrl = "https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json";

                FileOperationResult getVersionJSON = FileOperation.downloadOrReadFile(forgeJsonUrl);
                if (getVersionJSON.getResponseCode() == 200) {

                        String jsonString = (String) getVersionJSON.getContent();

                        if (jsonString != null) {
                                try {
                                        Map<String, Map<String, Object>> forgeVersions = new TreeMap<>(Collections.reverseOrder(new VersionComparator()));

                                        JSONObject jsonObject = new JSONObject(jsonString);
                                        JSONObject promosObject = jsonObject.getJSONObject("promos");

                                        Iterator<String> keys = promosObject.keys();
                                        while (keys.hasNext()) {
                                                String key = keys.next();
                                                String mcVersion = key.replace("-latest", "").replace("-recommended", "");
                                                String forgeVersion = promosObject.getString(key);

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

        public static Map<String, String> getOldVersions(String mode) {
                boolean isConnected = Net.isConnected;
                Map<String, String> promoMap = null;


                /*
                if (!mode.equals("-latest") && !mode.equals("-recommended")) {
                        throw new IllegalArgumentException("Invalid mode. Mode must be either '-recommended' or '-latest'.");
                }
                */

                // Set the default value to "-recommended" if mode is not "-latest"
                if (!mode.equals("-latest")) {
                        mode = "-recommended";
                }else
                        mode = "-latest";

                try {
                        URL url = new URL("https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json");
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");

                        int responseCode = connection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                StringBuilder response = new StringBuilder();
                                String line;

                                while ((line = reader.readLine()) != null) {
                                        response.append(line);
                                }
                                reader.close();

                                String jsonData = response.toString();

                                // Parse the JSON data
                                JSONObject jsonObject = new JSONObject(jsonData);

                                // Access the "promos" object
                                JSONObject promosObject = jsonObject.getJSONObject("promos");

                                // Create a HashMap for the found key-value pairs
                                promoMap = new HashMap<>();

                                // Get the keys in the JSON object
                                Iterator<String> keys = promosObject.keys();
                                while (keys.hasNext()) {
                                        String key = keys.next();
                                        if (key.endsWith(mode)) {
                                                String value = promosObject.getString(key);
                                                // Remove the mode from the key
                                                key = key.replace( mode, "");

                                                // Check if the cleanedKey contains only numbers and dots
                                                //if (key.matches("[0-9.]+")) {
                                                        promoMap.put(key, value);
                                                //}

                                        }
                                }
                        } else {
                                LogError("Failed to get FORGE-Version with Error-Code: " + responseCode);
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                }

                // Sort the keys in a TreeMap and return it
                TreeMap<String, String> sortedPromoMap = new TreeMap<>(Collections.reverseOrder(new VersionComparator()));
                sortedPromoMap.putAll(promoMap);

                return sortedPromoMap;
        }
}

