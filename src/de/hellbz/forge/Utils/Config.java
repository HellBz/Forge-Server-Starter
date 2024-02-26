package de.hellbz.forge.Utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    public static String fileConfigString =         "# Forge Server-Starter Configuration\n" +
                                                    "\n" +
                                                    "# There you can setup your own Timezone\n" +
                                                    "# More timezones in this list:\n" +
                                                    "# -> https://en.wikipedia.org/wiki/List_of_tz_database_time_zones\n" +
                                                    "# For Example timezone=Europe/Berlin\n" +
                                                    "timezone=UTC\n" +
                                                    "\n" +
                                                    "# Here you can set your own JAVA-Path for Starting the Server\n" +
                                                    "# For Example java_path=G:\\\\path\\to\\java_installation\\bin\\java.exe\n" +
                                                    "java_path=java\n" +
                                                    "\n" +
                                                    "# You wish to log all Actions from F-S-S to a File..\n" +
                                                    "# You can find the Log in \"/logs\"-Folder\n" +
                                                    "# For Example log_to_file=false\n" +
                                                    "log_to_file=true\n" +
                                                    "\n" +
                                                    "# You like to have a Debug-Log\n" +
                                                    "# With this you can see all Actions from F-S-S\n" +
                                                    "# For Example debug=false\n" +
                                                    "debug=true";

    public static String fileAutoFileString =       "# Forge Auto-Install Configuration\n" +
                                                    "\n" +
                                                    "# Specify your desired Minecraft-Version.\n" +
                                                    "# Possible options are [Version like: \"1.20.4\" or \"latest\"]\n" +
                                                    "minecraftVersion=latest\n" +
                                                    "\n" +
                                                    "# Specify your desired LoaderType.\n" +
                                                    "# Possible options are [\"Forge\" or \"NeoForge\"]\n" +
                                                    "loaderType=NeoForge\n" +
                                                    "\n" +
                                                    "# Specify your desired Loader-Version.\n" +
                                                    "# Possible options are [Version like: \"20.4.164-beta\" or \"latest\" or \"recommended\"]\n" +
                                                    "loaderVersion=latest";

    public static String fileStartLnxFileString =   "java -jar minecraft_server.jar -Xmx1024M -Xms1024M nogui";

    public static String fileStartWinFileString =   "@echo off\n" +
                                                    fileStartLnxFileString + "\n" +
                                                    "pause\n";


    public static String fileAutoLnxFileString =    "java -jar minecraft_server.jar -autoFile nogui";
    public static String fileAutoWinFileString =    "@echo off\n" +
                                                    fileAutoLnxFileString + "\n" +
                                                    "pause\n";



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

            try {
                Files.write(Paths.get(Config.rootFolder + File.separator + "server_starter.conf"), Config.fileConfigString.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }

            FileReader configReader = new FileReader(configFile);
            Config.configProps = new Properties();
            Config.configProps.load(configReader);
            configReader.close();
    }

}