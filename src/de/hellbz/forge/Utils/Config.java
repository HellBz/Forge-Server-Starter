package de.hellbz.forge.Utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

public class Config {

    public static final String OS = System.getProperty("os.name").toLowerCase();
    //Set Library-Path's
    public static java.io.File rootFolder = new java.io.File("./");
    public static java.io.File librariesFolder = new java.io.File(rootFolder, "libraries");
    public static java.io.File minecraftForgeFolder = new java.io.File(librariesFolder, "net/minecraftforge/forge");
    public static java.io.File neoForgeFolder = new java.io.File(librariesFolder, "net/neoforged/neoforge");
    public static Pattern Pattern_Forge = Pattern.compile("forge-([.0-9]+)-([.0-9]+)-([universal|installer]+).([jar|zip]+)", Pattern.CASE_INSENSITIVE);
    public static Pattern Pattern_Forge_startfile = Pattern.compile("(minecraftforge-universal-|forge-)([0-9.]+)-([0-9.]+)(\\.jar|universal\\.jar|-universal\\.jar|-shim\\.jar)", Pattern.CASE_INSENSITIVE);
    public static Pattern Pattern_NeoForge = Pattern.compile("neoforge-(\\d+\\.\\d+\\.\\d+)(?:-beta)?-installer\\.(?:jar|zip)", Pattern.CASE_INSENSITIVE);
    public static Properties configProps;
    public static Properties autoProps;
    public static boolean startupError = false;
    public static boolean isForge = false;
    public static String minecraftVersion = null;
    public static String loaderVersion = null;
    public static String[] CMD_ARRAY = null;
    public static Integer javaVersion = (int) Double.parseDouble(System.getProperty("java.class.version"));
    public static String[] startupParameter = null;

    public static String startupFile = null;

    public static String installerFile = null;

    static {
        // System.out.println("Config loaded.");

        try {
            initServerConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Set local Timezone
        if (!Config.configProps.getProperty("timezone").equals("UTC")) {
            System.setProperty("user.timezone", Config.configProps.getProperty("timezone"));
        }

    }


    public static void initServerConfig() throws IOException {

        java.io.File configFile = new java.io.File("server_starter.conf");

        if ( !configFile.exists()) {
            FileOperation.downloadOrReadFile("/res/server_starter.conf", Config.rootFolder + File.separator + "server_starter.conf");
        }

            FileReader configReader = new FileReader(configFile);
            Config.configProps = new Properties();
            Config.configProps.load(configReader);
            configReader.close();
    }

}