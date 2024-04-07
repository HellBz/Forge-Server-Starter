package de.hellbz.forge.Utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.awt.*;
import java.io.*;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;


public class Data {

    // Define Text Colors
    public static final String TXT_RESET = "\u001B[0m";
    public static final String TXT_BLACK = "\u001B[30m";
    public static final String TXT_RED = "\u001B[31m";
    public static final String TXT_GREEN = "\u001B[32m";
    public static final String TXT_YELLOW = "\u001B[33m";
    public static final String TXT_BLUE = "\u001B[34m";
    public static final String TXT_PURPLE = "\u001B[35m";
    public static final String TXT_CYAN = "\u001B[36m";
    public static final String TXT_WHITE = "\u001B[37m";

    //Define Background Colors
    public static final String BG_BLACK = "\u001B[40m";
    public static final String BG_RED = "\u001B[41m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String BG_YELLOW = "\u001B[43m";
    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_PURPLE = "\u001B[45m";
    public static final String BG_CYAN = "\u001B[46m";
    public static final String BG_WHITE = "\u001B[47m";

    // Reset
    public static final String RESET = "\033[0m";  // Text Reset

    // Regular Colors
    public static final String BLACK = "\033[0;30m";   // BLACK
    public static final String RED = "\033[0;31m";     // RED
    public static final String GREEN = "\033[0;32m";   // GREEN
    public static final String YELLOW = "\033[0;33m";  // YELLOW
    public static final String BLUE = "\033[0;34m";    // BLUE
    public static final String PURPLE = "\033[0;35m";  // PURPLE
    public static final String CYAN = "\033[0;36m";    // CYAN
    public static final String WHITE = "\033[0;37m";   // WHITE

    // Bold
    public static final String BLACK_BOLD = "\033[1;30m";  // BLACK
    public static final String RED_BOLD = "\033[1;31m";    // RED
    public static final String GREEN_BOLD = "\033[1;32m";  // GREEN
    public static final String YELLOW_BOLD = "\033[1;33m"; // YELLOW
    public static final String BLUE_BOLD = "\033[1;34m";   // BLUE
    public static final String PURPLE_BOLD = "\033[1;35m"; // PURPLE
    public static final String CYAN_BOLD = "\033[1;36m";   // CYAN
    public static final String WHITE_BOLD = "\033[1;37m";  // WHITE

    // Underline
    public static final String BLACK_UNDERLINED = "\033[4;30m";  // BLACK
    public static final String RED_UNDERLINED = "\033[4;31m";    // RED
    public static final String GREEN_UNDERLINED = "\033[4;32m";  // GREEN
    public static final String YELLOW_UNDERLINED = "\033[4;33m"; // YELLOW
    public static final String BLUE_UNDERLINED = "\033[4;34m";   // BLUE
    public static final String PURPLE_UNDERLINED = "\033[4;35m"; // PURPLE
    public static final String CYAN_UNDERLINED = "\033[4;36m";   // CYAN
    public static final String WHITE_UNDERLINED = "\033[4;37m";  // WHITE

    // Background
    public static final String BLACK_BACKGROUND = "\033[40m";  // BLACK
    public static final String RED_BACKGROUND = "\033[41m";    // RED
    public static final String GREEN_BACKGROUND = "\033[42m";  // GREEN
    public static final String YELLOW_BACKGROUND = "\033[43m"; // YELLOW
    public static final String BLUE_BACKGROUND = "\033[44m";   // BLUE
    public static final String PURPLE_BACKGROUND = "\033[45m"; // PURPLE
    public static final String CYAN_BACKGROUND = "\033[46m";   // CYAN
    public static final String WHITE_BACKGROUND = "\033[47m";  // WHITE

    // High Intensity
    public static final String BLACK_BRIGHT = "\033[0;90m";  // BLACK
    public static final String RED_BRIGHT = "\033[0;91m";    // RED
    public static final String GREEN_BRIGHT = "\033[0;92m";  // GREEN
    public static final String YELLOW_BRIGHT = "\033[0;93m"; // YELLOW
    public static final String BLUE_BRIGHT = "\033[0;94m";   // BLUE
    public static final String PURPLE_BRIGHT = "\033[0;95m"; // PURPLE
    public static final String CYAN_BRIGHT = "\033[0;96m";   // CYAN
    public static final String WHITE_BRIGHT = "\033[0;97m";  // WHITE

    // Bold High Intensity
    public static final String BLACK_BOLD_BRIGHT = "\033[1;90m"; // BLACK
    public static final String RED_BOLD_BRIGHT = "\033[1;91m";   // RED
    public static final String GREEN_BOLD_BRIGHT = "\033[1;92m"; // GREEN
    public static final String YELLOW_BOLD_BRIGHT = "\033[1;93m";// YELLOW
    public static final String BLUE_BOLD_BRIGHT = "\033[1;94m";  // BLUE
    public static final String PURPLE_BOLD_BRIGHT = "\033[1;95m";// PURPLE
    public static final String CYAN_BOLD_BRIGHT = "\033[1;96m";  // CYAN
    public static final String WHITE_BOLD_BRIGHT = "\033[1;97m"; // WHITE

    // High Intensity backgrounds
    public static final String BLACK_BACKGROUND_BRIGHT = "\033[0;100m";// BLACK
    public static final String RED_BACKGROUND_BRIGHT = "\033[0;101m";// RED
    public static final String GREEN_BACKGROUND_BRIGHT = "\033[0;102m";// GREEN
    public static final String YELLOW_BACKGROUND_BRIGHT = "\033[0;103m";// YELLOW
    public static final String BLUE_BACKGROUND_BRIGHT = "\033[0;104m";// BLUE
    public static final String PURPLE_BACKGROUND_BRIGHT = "\033[0;105m"; // PURPLE
    public static final String CYAN_BACKGROUND_BRIGHT = "\033[0;106m";  // CYAN
    public static final String WHITE_BACKGROUND_BRIGHT = "\033[0;107m";   // WHITE

    public static void LogInfo(final String message) {

        System.out.println(CurrentTime() + TXT_GREEN + "[F-S-S/INFO] " + TXT_RESET + message);
        try {
            Data.doLog(CurrentTime() + "[F-S-S/INFO] " + cleanLog(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void LogWarning(final String message) {

        System.out.println(CurrentTime() + TXT_YELLOW + "[F-S-S/WARNING] " + TXT_RESET + message);
        try {
            Data.doLog(CurrentTime() + "[F-S-S/WARNING] " + cleanLog(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void LogError(final String message) {

        System.out.println(CurrentTime() + TXT_RED + "[F-S-S/ERROR] " + TXT_RESET + message);
        try {
            Data.doLog(CurrentTime() + "[F-S-S/ERROR] " + cleanLog(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void LogDebug(final String message) {
        if (Objects.equals(Config.configProps.getProperty("debug"), "true")) {
            System.out.println(CurrentTime() + TXT_CYAN + "[F-S-S/DEBUG] " + TXT_RESET + message);
            try {
                Data.doLog(CurrentTime() + "[F-S-S/DEBUG] " + cleanLog(message));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void LogCustom(final String message, String name, String color) {
        System.out.println(CurrentTime() + color + "[F-S-S/" + name + "] " + TXT_RESET + message);
        try {
            Data.doLog(CurrentTime() + "[F-S-S/" + name + "] " + cleanLog(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String CurrentTime() {
        if (!Config.configProps.getProperty("timezone").equals("UTC")) {
            ZoneId z = ZoneId.of(Config.configProps.getProperty("timezone"));
            return "[" + ZonedDateTime.now(z).format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT)) + "] ";
        } else {
            return "[" + ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT)) + "] ";
        }
    }

    private static String cleanLog(String message) {
        return message.replaceAll("\\x1b\\[[\\d;]*m", "");
    }

    public static void doLog(final String message) throws IOException {
        if (Objects.equals(Config.configProps.getProperty("log_to_file"), "true")) {
            FileWriter fileWriter = new FileWriter("logs/server-starter.log", true); //Set true for append mode
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(message);  //New line
            printWriter.close();
        }
    }

    public static boolean isReallyHeadless() {
        if (GraphicsEnvironment.isHeadless()) {
            return true;
        }
        try {
            GraphicsDevice[] screenDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
            return screenDevices == null || screenDevices.length == 0;
        } catch (HeadlessException e) {
            e.printStackTrace();
            return true;
        }
    }

    public static void checkContent(String startup_file) {
        // Get the startup-file
        java.io.File check_file = new java.io.File(startup_file);

        // Check if the specified file
        // Exists or not
        Scanner file_content = null;
        try {
            file_content = new Scanner(check_file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // Let's loop through each line of the file
        while (file_content != null && file_content.hasNext()) {
            String line = file_content.nextLine();

            // Now, check if this line contains our keyword. If it does, print the line
            if (line.toLowerCase().contains("xmx") || line.toLowerCase().contains("xms")) {
                LogWarning("Illegal characters found in Server-Args ");
                Config.startupError = true;
            }
        }
    }

    static String getFromXML(String xmlContent, String xpathExpression) {

        if (xmlContent == null || xmlContent.trim().isEmpty() || !xmlContent.contains("<") || !xmlContent.contains(">")) {
            return null; // null oder "" für einen leeren String
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));

            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            XPathExpression expr = xpath.compile(xpathExpression);

            // Anpassung, um direkte Tags oder Pfade zu unterstützen
            return (String) expr.evaluate(doc, XPathConstants.STRING);
        } catch (Exception e) {
            Data.LogError("Failed to read content from XML: " + e.getMessage());
            return null;
        }
    }

    public static String getJsonValue(String json, String path) {
        try {
            // Überprüfen, ob das JSON-String mit "[" beginnt (Array)
            char firstChar = json.trim().charAt(0);
            boolean isArray = (firstChar == '[');

            // JSON-Objekt oder JSON-Array entsprechend verarbeiten
            if (isArray) {
                JSONArray jsonArray = new JSONArray(json);
                return getValueFromJSONArray(jsonArray, path);
            } else {
                JSONObject jsonObject = new JSONObject(json);
                return getValueFromJSONObject(jsonObject, path);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Fehler beim Extrahieren des Werts
        }
    }

    private static String getValueFromJSONObject(JSONObject jsonObject, String path) {
        String[] parts = path.split("/");
        JSONObject currentObject = jsonObject;

        for (int i = 0; i < parts.length - 1; i++) {
            if (!currentObject.has(parts[i])) {
                return null; // Pfad existiert nicht im JSON
            }
            currentObject = currentObject.getJSONObject(parts[i]);
        }

        return currentObject.optString(parts[parts.length - 1]);
    }

    private static String getValueFromJSONArray(JSONArray jsonArray, String path) {
        String[] parts = path.split("/");
        int index = Integer.parseInt(parts[0]); // Index des gewünschten Elements im Array
        JSONObject currentObject = jsonArray.getJSONObject(index);

        for (int i = 1; i < parts.length - 1; i++) {

            if (!currentObject.has(parts[i])) {
                return null; // Pfad existiert nicht im JSON
            }
            currentObject = currentObject.getJSONObject(parts[i]);
        }

        return currentObject.optString(parts[parts.length - 1]);
    }

    public static class VersionComparator implements Comparator<String> {
        @Override
        public int compare(String v1, String v2) {

            if (v1 == null && v2 == null) {
                return 0; // Both null, consider them equal
            } else if (v1 == null) {
                return -1; // v1 is null, consider it less than v2
            } else if (v2 == null) {
                return 1; // v2 is null, consider it greater than v1
            }

            // Keep only digits and dots
            String sanitizedV1 = v1.replaceAll("[^\\d.]", "");
            String sanitizedV2 = v2.replaceAll("[^\\d.]", "");

            String[] parts1 = sanitizedV1.split("\\.");
            String[] parts2 = sanitizedV2.split("\\.");

            int minLength = Math.min(parts1.length, parts2.length);

            for (int i = 0; i < minLength; i++) {
                if (parts1[i].equals(parts2[i])) {
                    continue; // Parts are equal, move to the next part
                }

                if (parts1[i].matches("\\d+") && parts2[i].matches("\\d+")) {
                    int num1 = Integer.parseInt(parts1[i]);
                    int num2 = Integer.parseInt(parts2[i]);

                    return Integer.compare(num1, num2);
                } else {
                    return parts1[i].compareTo(parts2[i]);
                }
            }

            return Integer.compare(parts1.length, parts2.length);
        }
    }

    public static boolean containsMemoryParameters(String[] args) {
        return Arrays.stream(args)
                .map(String::toLowerCase)
                .anyMatch(arg -> arg.contains("-xmx") || arg.contains("-xms"));
    }

    public static void logSelectedSystemProperties() {

        List<String> relevantProperties = Arrays.asList(
                "java.home", "java.version", "java.runtime.version", "java.vm.version",
                "java.vm.name", "java.vm.vendor", "os.name", "os.arch",
                "os.version", "file.separator", "path.separator", "user.name",
                "user.dir", "user.home", "java.class.path", "java.vm.specification.version",
                "sun.management.compiler", "user.timezone"
        );

        Properties properties = System.getProperties();
        relevantProperties.forEach(prop -> {
            if (properties.containsKey(prop)) {
                LogDebug(prop + ": " + properties.getProperty(prop));
            }
        });
    }

    public static void updateProperty(String propertiesFilePath, String key, String newValue) throws IOException {
        List<String> updatedLines = new ArrayList<>();
        boolean keyUpdated = false;

        Path filePath = Paths.get(propertiesFilePath);
        List<String> lines = Files.readAllLines( filePath, StandardCharsets.UTF_8);
        for (String line : lines) {
            if (line.startsWith(key + "=")) {
                updatedLines.add(key + "=" + newValue);
                keyUpdated = true;
            } else {
                updatedLines.add(line);
            }
        }

        if (!keyUpdated) {
            updatedLines.add(""); // Add an empty line
            updatedLines.add("#Auto added \"" + key + "\" with function updateProperty()"); // Fügt eine Leerzeile hinzu
            updatedLines.add(key + "=" + newValue);
        }

        Files.write( filePath, updatedLines, StandardCharsets.UTF_8);
    }

    public static String propertiesToURL(String propertiesFilePath) {
        File file = new File(propertiesFilePath);
        if (!file.exists()) {
            return null; // Datei existiert nicht, gib null zurück
        }

        Properties properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null; // Bei einem Lesefehler, gib null zurück
        }

        // Definiere die Schlüssel, die in postData aufgenommen werden sollen
        String[] keys = {
                "motd",
                "max-players",
                "level-seed",
                "gamemode",
                "server-ip",
                "server-port",
                "query.port",
                "rcon.port"
        };

        StringBuilder postData = new StringBuilder();
        for (String key : keys) {
            String value = properties.getProperty(key);
            if (value != null) { // Nur hinzufügen, wenn der Schlüssel existiert
                if (postData.length() != 0) {
                    postData.append('&');
                }
                try {
                    postData.append(URLEncoder.encode(key, StandardCharsets.UTF_8.name()));
                    postData.append('=');
                    postData.append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()));
                } catch (IOException e) {
                    LogWarning("Decoding Problem: " + Config.rootFolder + File.separator + ' ' + e);
                    // Das sollte nie passieren, da UTF-8 ein bekannter Zeichensatz ist
                    //throw new RuntimeException("Fehler beim Kodieren der URL", e);
                }
            }
        }

        return postData.toString();
    }

    public static String getMacAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                // Skip loopback addresses and disabled interfaces.
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac == null) {
                    continue;
                }
                // Conversion of the byte array into a readable string."
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < mac.length; i++) {
                    sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
                }
                return sb.toString(); // Returns the MAC address of the first matching interface.
            }
        } catch (SocketException e) {
            // e.printStackTrace();
            return null;
        }
        return null;
    }

}
