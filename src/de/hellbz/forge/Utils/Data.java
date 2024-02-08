package de.hellbz.forge.Utils;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;

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

    public static void LogForge(final String message) {
        System.out.println(CurrentTime() + TXT_PURPLE + "[F-S-S/FORGE-Installer] " + TXT_RESET + message);
        try {
            Data.doLog(CurrentTime() + "[F-S-S/FORGE-Installer] " + cleanLog(message));
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

    public static void checkContent(String startup_file ) {
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

    // Benutzerdefinierte Vergleichsfunktion für Versionsnummern
    public static class VersionComparator implements Comparator<String> {
        @Override
        public int compare(String v1, String v2) {
            String[] parts1 = v1.split("\\.");
            String[] parts2 = v2.split("\\.");

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

    static String getFromXML(String xmlContent, String versionTag) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new java.io.ByteArrayInputStream(xmlContent.getBytes()));

            // Extrahieren des Inhalts des angegebenen Tags aus dem XML
            NodeList nodeList = doc.getElementsByTagName(versionTag);
            if (nodeList.getLength() > 0) {
                return nodeList.item(0).getTextContent();
            }
        } catch (Exception e) {
            System.out.println("Failed to read content from XML: " + e.getMessage());
        }
        return null;
    }
}
