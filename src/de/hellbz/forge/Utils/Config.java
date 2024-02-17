package de.hellbz.forge.Utils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

import static de.hellbz.forge.Utils.Data.LogDebug;

public class Config {

    //Set Library-Path's
    public static java.io.File rootFolder = new java.io.File("./");
    public static java.io.File librariesFolder = new java.io.File( rootFolder, "libraries");
    public static java.io.File minecraftForgeFolder = new java.io.File(librariesFolder, "net/minecraftforge/forge");
    public static java.io.File neoForgeFolder = new java.io.File(librariesFolder, "net/neoforged/neoforge");

    public static Pattern Pattern_Forge = Pattern.compile("forge-([.0-9]+)-([.0-9]+)-([universal|installer]+).([jar|zip]+)", Pattern.CASE_INSENSITIVE);
    public static Pattern Pattern_Forge_17 = Pattern.compile("(minecraftforge-universal-|forge-)([0-9.]+)-([0-9.]+)(\\.jar|universal\\.jar)", Pattern.CASE_INSENSITIVE);
    public static Pattern Pattern_NeoForge = Pattern.compile("neoforge-(\\d+\\.\\d+\\.\\d+)(?:-beta)?-installer\\.(?:jar|zip)", Pattern.CASE_INSENSITIVE);

    public static Properties configProps;

    public static Properties autoProps;

    public static boolean startupError = false;

    public static boolean isForge = false;

    public static String mc_version = null;

    public static String loader_version = null;

    public static String[] mcVersionDetail = { "0", "0", "0" };

    public static String[] CMD_ARRAY = null;

    public static Integer javaVersion = (int) Double.parseDouble(System.getProperty("java.class.version"));

    public static final String OS = System.getProperty("os.name").toLowerCase();

    public static String[] startupParameter = null;

    public static String startup_file = null;

    public static String installerFile = null;

    static {
        // System.out.println("Config loaded.");

        try {
            initServerConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LogDebug( Config.configProps.toString() );
        //Set local Timezone
        if (!Config.configProps.getProperty("timezone").equals("UTC")) {
            System.setProperty("user.timezone", Config.configProps.getProperty("timezone"));
        }

    }
    public static void initServerConfig() throws IOException {

        java.io.File configFile = new java.io.File("server_starter.conf");

        if (configFile.exists()) {

            FileReader configReader = new FileReader(configFile);
            Config.configProps = new Properties();
            Config.configProps.load(configReader);
            configReader.close();

        } else {
            if (configFile.createNewFile()) {

                FileWriter writerConfig = new FileWriter(configFile);

                Config.configProps = new Properties();
                Config.configProps.setProperty("debug", "false");
                Config.configProps.setProperty("log_to_file", "false");
                Config.configProps.setProperty("timezone", "UTC");
                Config.configProps.setProperty("java_path", "java");

                Config.configProps.store(writerConfig, "Forge Server-Starter Configuration");

                writerConfig.close();
            }
        }
    }

}