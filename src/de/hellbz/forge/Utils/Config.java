package de.hellbz.forge.Utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class Config {

    public static final String PROPERTIES_FILE = "server_starter.conf";
    public static String startupFile = null;
    public static String[] startupParameter = null;
    public static boolean startupError = false;
    public static Properties configProps;
    public static Properties autoProps;
    public static String[] CMD_ARRAY = null;
    public static String installerFile = null;

    public static boolean isForge = false;
    public static String minecraftVersion = null;
    public static String loaderVersion = null;

    public static java.io.File rootFolder = new java.io.File("./");
    public static java.io.File librariesFolder = new java.io.File(rootFolder, "libraries");

    public static java.io.File minecraftForgeFolder = new java.io.File(librariesFolder, "net/minecraftforge/forge");
    public static Pattern Pattern_Forge = Pattern.compile("forge-([.0-9]+)-([.0-9]+)-(universal|installer).([jar|zip]+)", Pattern.CASE_INSENSITIVE);
    public static Pattern Pattern_Forge_startfile = Pattern.compile("(minecraftforge-universal-|forge-)([0-9.]+)-([0-9.]+)(\\.jar|universal\\.jar|-universal\\.jar|-shim\\.jar)", Pattern.CASE_INSENSITIVE);
    public static Map<String, Map<String, Object>> forgeVersions = null;

    public static java.io.File neoForgeFolder = new java.io.File(librariesFolder, "net/neoforged/neoforge");
    public static Pattern Pattern_NeoForge = Pattern.compile("neoforge-([\\d]+(?:\\.[\\d]+)+)(?:-[a-zA-Z]+\\d*)?-installer\\.(?:jar|zip)", Pattern.CASE_INSENSITIVE);

    public static Map<String, Map<String, Object>> neoVersions = null;

    public static String macAddress = Data.getMacAddress();

    public static final String OS = System.getProperty("os.name").toLowerCase();

    /**
     * Java class version mapping:
     * Java 8  = 52, Java 11 = 55, Java 17 = 61,
     * Java 21 = 65, Java 24 = 68, Java 25 = 69
     * We parse as double and cast to int to support future versions correctly.
     */
    public static Integer javaVersion = (int) Double.parseDouble(System.getProperty("java.class.version"));

    /**
     * Exit code that triggers a restart loop in ServerStarter.
     * Servers can be configured to exit with this code to signal restart.
     */
    public static final int RESTART_EXIT_CODE = 3;

    public static String fileStartLnxFileString = "java -jar " + Document.getJarFileName() + " -Xmx1024M -Xms1024M nogui";
    public static String fileStartWinFileString = "@echo off\n" +
            fileStartLnxFileString + "\n" +
            "pause\n";
    public static String fileAutoLnxFileString = "java -jar " + Document.getJarFileName() + " -autoFile nogui";
    public static String fileAutoWinFileString = "@echo off\n" +
            fileAutoLnxFileString + "\n" +
            "pause\n";

    static {
        try {
            initServerConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String timezone = getTimezone();

        if (timezone != null && !timezone.isEmpty()) {
            if (!timezone.equals("UTC")) {
                System.setProperty("user.timezone", timezone);
            }
        }
    }

    public static void initServerConfig() throws IOException {

        java.io.File configFile = new java.io.File(Config.PROPERTIES_FILE);

        if (!configFile.exists()) {
            FileOperation.downloadOrReadFile("/res/server_starter.conf", Config.rootFolder + File.separator + Config.PROPERTIES_FILE);
        }

        FileReader configReader = new FileReader(configFile);
        Config.configProps = new Properties();
        Config.configProps.load(configReader);
        configReader.close();
    }

    // --- Typed configuration helpers ---

    private static String getString(String key, String defaultValue) {
        return configProps != null ? configProps.getProperty(key, defaultValue) : defaultValue;
    }

    private static boolean getBoolean(String key, boolean defaultValue) {
        if (configProps == null) return defaultValue;
        String value = configProps.getProperty(key);
        return value != null ? value.equalsIgnoreCase("true") : defaultValue;
    }

    /**
     * Returns the configured timezone or "UTC" by default.
     */
    public static String getTimezone() {
        return getString("timezone", "UTC");
    }

    /**
     * Returns the configured Java command or "java" by default.
     */
    public static String getJavaPath() {
        return getString("java_path", "java");
    }

    /**
     * Returns true if debug logging is enabled.
     */
    public static boolean isDebugEnabled() {
        return getBoolean("debug", false);
    }

    /**
     * Returns true if file logging is enabled.
     */
    public static boolean isLogToFileEnabled() {
        return getBoolean("log_to_file", true);
    }

    /**
     * Returns true if the startup internet check should run.
     */
    public static boolean isNetworkCheckEnabled() {
        return getBoolean("network_check", true);
    }

    /**
     * Returns true if the loader should auto-update to the latest version.
     */
    public static boolean isAutoUpdateLoaderEnabled() {
        return getBoolean("auto_update_loader", false);
    }

    /**
     * Returns true if the unique ID should be sent to the update API.
     */
    public static boolean isUniqueIdRequestEnabled() {
        return getBoolean("unique_id_request", true);
    }

    /**
     * Returns the configured unique ID or an empty string.
     */
    public static String getUniqueId() {
        return getString("unique_id", "");
    }
}
