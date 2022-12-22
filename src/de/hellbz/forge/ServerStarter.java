package de.hellbz.forge;

import de.hellbz.forge.Utils.Curse;
import de.hellbz.forge.Utils.Data;
import de.hellbz.forge.Utils.File;

import javax.swing.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.hellbz.forge.Utils.Data.*;

public class ServerStarter {

    public static Properties configProps;
    public static boolean startupError = false;

    public static String mc_version = null;
    public static String forge_version = null;
    public static String[] mcVersionDetail = null;
    public static String[] CMD_ARRAY = null;

    static int count = 0;

    //Set OS-Name in String
    public static final String OS = System.getProperty("os.name").toLowerCase();

    public static void main(String[] args) throws IOException, InterruptedException {

        // Google.LogToGForm();

        //Get System-Variables like xmx and xms
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMxBean.getInputArguments();
        String[] startupParameter = arguments.toArray(new String[0]);

        File.ConfigFile();
        File.LogFile();

        LogDebug( ServerStarter.configProps.toString() );
        //Set local Timezone
        if (!ServerStarter.configProps.getProperty("timezone").equals("UTC")) {
            System.setProperty("user.timezone", ServerStarter.configProps.getProperty("timezone"));
        }

        //WELCOME
        LogInfo("-----------------------------------------------");
        LogInfo("FORGE-Server-Starter");
        LogInfo("");
        LogInfo("By " + TXT_GREEN + "HellBz" + TXT_RESET + ".de");
        LogInfo("");
        LogInfo("-----------------------------------------------");
        LogInfo("Checking System ...");

        Properties properties = System.getProperties();
        properties.forEach((k, v) -> LogDebug(k + ":" + v));

        //DEBUG
        String joinedStartupParameter = Arrays.toString(startupParameter);
        LogDebug("STARTUP-PARAMETER " + TXT_CYAN + joinedStartupParameter + TXT_RESET);

        String joinedStartupArgs = Arrays.toString(args);
        LogDebug("STARTUP-ARGS " + TXT_CYAN + joinedStartupArgs + TXT_RESET);

        String currentPath = new java.io.File(".").getCanonicalPath();
        LogDebug("DIRECTORY " + TXT_CYAN + currentPath + TXT_RESET);

        LogDebug("-----------------------------------------------");

        if ( isReallyHeadless() ) {
            //Headless, all Fine
            LogDebug("This is Headless Client");

        } else {

            LogDebug( Arrays.toString(args).toString() );
            if (Arrays.toString(args).toLowerCase().contains("-xmx") || Arrays.toString(args).toLowerCase().contains("-xms") || Arrays.toString(startupParameter).toLowerCase().contains("-xmx") || Arrays.toString(startupParameter).toLowerCase().contains("-xms")) {
                LogDebug("SCRIPT USE -Xmx and -Xms for Start.");
            } else {
                startupError = true;
                LogWarning("PLS use -Xmx and -Xms for start up this script.");
                JFrame jFrame = new JFrame();
                JOptionPane.showMessageDialog(jFrame, "Script only work in Batch-Mode!\nStartfile for Batch-Mode is created.");
                File.StartFile();
                // System.exit(0);
            }
        }

        //Set Library-Path's
        java.io.File forge_dir = new java.io.File("libraries/net/minecraftforge/forge/");
        java.io.File libraries_dir = new java.io.File("libraries/");
        java.io.File root_dir = new java.io.File("./");

        //Start-File
        String startup_file = null;

        if ( !libraries_dir.exists() ) {
            LogInfo("Check for Forge-Installation-File ...");
            startupError = Curse.installLoader( currentPath );
        }

        if ( forge_dir.exists()  ) {

            Pattern pattern = Pattern.compile("([.0-9]+)-([.0-9]+)", Pattern.CASE_INSENSITIVE);

            //List all Folders
            java.io.File[] forge_files = forge_dir.listFiles();

            // try-catch block to handle exceptions
            try {
                // Display the names of the files
                for (int i = 0; i < forge_files.length; i++) {

                    Matcher matcher = pattern.matcher(forge_files[i].getName());

                    LogInfo( forge_files[i].getName() );

                    if (matcher.find()) {
                        mc_version = matcher.group(1);
                        forge_version = matcher.group(2);
                        mcVersionDetail = mc_version.split("\\.");
                        if ( Integer.parseInt(mcVersionDetail[1]) >= 17 ) {
                            if (OS.contains("win")) {
                                LogDebug("Using WINDOWS System-Parameter");
                                startup_file = "libraries/net/minecraftforge/forge/" + mc_version + "-" + forge_version + "/win_args.txt";
                            } else {
                                LogDebug("Using UNIX System-Parameter");
                                startup_file = "libraries/net/minecraftforge/forge/" + mc_version + "-" + forge_version + "/unix_args.txt";
                            }

                            LogInfo("Found Minecraft: " + mc_version + " with new Forge " + forge_version);

                            break;
                        }
                    }
                }
            } catch (Exception e) {
                ServerStarter.startupError = true;
                LogWarning( e.getMessage() );
            }

            if ( mc_version == null && forge_version == null ) {
                LogWarning("No Forge-Folder Found ");
                startupError = true;
            }
        }

        if ( ( !forge_dir.exists() && libraries_dir.exists()) || ( libraries_dir.exists() && Integer.parseInt(mcVersionDetail[1]) < 17 ) ) {

            Pattern pattern = Pattern.compile("forge-([.0-9]+)-([.0-9]+)([universal.jar|.jar]+)", Pattern.CASE_INSENSITIVE);

            //List all Folders
            java.io.File[] root_files = root_dir.listFiles();

            // try-catch block to handle exceptions
            try {
                // Display the names of the files
                for (int i = 0; i < root_files.length; i++) {

                    Matcher matcher = pattern.matcher(root_files[i].getName());

                    // LogInfo( root_files[i].getName() );

                    if ( matcher.find() ) {
                        mc_version = matcher.group(1);
                        forge_version = matcher.group(2);
                        startup_file = root_files[i].getName();
                        LogInfo("Found Minecraft: " + mc_version  + " with Forge " + forge_version );
                        break;
                    }
                }
            } catch (Exception e) {
                ServerStarter.startupError = true;
                LogWarning( e.getMessage() );
            }

            if ( mc_version == null && forge_version == null ) {
                LogWarning("No Forge-Folder Found ");
                startupError = true;
            }
        } else {
            LogWarning("The \"libraries\"-Folder or Content not exist ");
            startupError = true;
        }

        if ( File.checkExist( startup_file ) ) {

            Data.checkContent(startup_file);

            LogDebug("Building Startup-Parameter");

            List<String> where = new ArrayList<>();

            if (ServerStarter.configProps.getProperty("java_path") != null && !ServerStarter.configProps.getProperty("java_path").equals("java")) {
                where.add(ServerStarter.configProps.getProperty("java_path"));
                LogDebug("Use Custom Java Path: " + ServerStarter.configProps.getProperty("java_path"));
            } else {
                where.add("java");
                LogDebug("Use Standart Java Path");
            }

            Collections.addAll(where, startupParameter);

            if (ServerStarter.configProps.getProperty("timezone") != null) {
                where.add("-Duser.timezone=" + configProps.getProperty("timezone"));
            }

            String[] mcVersionDetail = mc_version.split("\\.");

            if (Integer.parseInt(mcVersionDetail[1]) >= 17) {
                where.add("@" + startup_file);
            } else {
                where.add("-jar");
                where.add( startup_file );

            }

            where.add("nogui");

            CMD_ARRAY = new String[where.size()];
            where.toArray(CMD_ARRAY);

        }

            //Check Eula-File
            File.Eula();

        if ( CMD_ARRAY != null ) {
            LogInfo("");
            LogInfo("Server is Running in TimeZone: " + ServerStarter.configProps.getProperty("timezone"));
            LogInfo("More timezones in this list: https://en.wikipedia.org/wiki/List_of_tz_database_time_zones ");
            LogInfo("Setup your own timezone in server_starter.conf");
            LogInfo("");
            LogInfo("Start FORGE " + forge_version + " Server");
            LogInfo("-----------------------------------------------");

            //DEBUG StartUp Commands
            LogDebug("Startup-ARRAY " + TXT_BLUE + Arrays.toString(CMD_ARRAY) + TXT_RESET);

            Process serverProcess = new ProcessBuilder(CMD_ARRAY)
                    .inheritIO()
                    .start();

            int exitCode = serverProcess.waitFor();

            if (exitCode == 0) {
                LogWarning("Server is successfully stopped.");
                System.exit(0);
            } else {
                LogError("Server is Crashed with Exit-Code: " + exitCode);
                LogWarning("Please check your files and upload them to the server again if necessary. ");
                LogError("EXIT Server-Starter ");
                System.exit(Integer.parseInt(String.valueOf(exitCode)));
            }
        }else{
            startupError = true;
            LogWarning("Could not build Start-Parameter.");
        }


        if ( startupError ) {

            LogError("EXIT Server-Starter ");
            LogError("-----------------------------------------------");
            System.exit(-1);

        }
    }
}