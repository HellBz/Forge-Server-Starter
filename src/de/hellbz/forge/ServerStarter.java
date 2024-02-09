package de.hellbz.forge;

import de.hellbz.forge.Utils.*;

import javax.swing.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.hellbz.forge.Utils.Data.*;

public class ServerStarter {




    static {

        LogInfo("-----------------------------------------------");
        LogInfo("FORGE-Server-Starter");
        LogInfo("");
        LogInfo("By " + TXT_GREEN + "HellBz" + TXT_RESET + ".de");
        LogInfo("");
        LogInfo("-----------------------------------------------");

    }
    public static void main(String[] args) throws IOException, InterruptedException {

        // Google.LogToGForm();

        File.LogFile();

        //WELCOME
        LogInfo("Checking System ...");

        //Get System-Variables like xmx and xms
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMxBean.getInputArguments();
        Config.startupParameter = arguments.toArray(new String[0]);

        Properties properties = System.getProperties();
        properties.forEach((k, v) -> LogDebug(k + ":" + v));

        //DEBUG
        String joinedStartupParameter = Arrays.toString( Config.startupParameter );
        LogDebug("STARTUP-PARAMETER " + TXT_CYAN + joinedStartupParameter + TXT_RESET);

        String joinedStartupArgs = Arrays.toString(args);
        LogDebug("STARTUP-ARGS " + TXT_CYAN + joinedStartupArgs + TXT_RESET);

        String currentPath = new java.io.File(".").getCanonicalPath();
        LogDebug("DIRECTORY " + TXT_CYAN + currentPath + TXT_RESET);

        LogDebug("-----------------------------------------------");

        // if ( isReallyHeadless() ) {
        if ( true ) {
            //Headless, all Fine
            LogDebug("This is Headless Client");

        } else {

            LogDebug(Arrays.toString(args));
            if (Arrays.toString(args).toLowerCase().contains("-xmx") || Arrays.toString(args).toLowerCase().contains("-xms") || Arrays.toString(Config.startupParameter).toLowerCase().contains("-xmx") || Arrays.toString( Config.startupParameter ).toLowerCase().contains("-xms")) {
                LogDebug("SCRIPT USE -Xmx and -Xms for Start.");
            } else {
                // Config.startupError = true;
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

        //No Internet Connection, only manually installation
        if ( !Net.isConnected ) {
            LogInfo("Place your Forge-Installer-JAR directly next to the current JAR.");
            Config.startupError = true;
        }else {
            Remote.checkForUpdate();
        }

        //Try Auto-Installer
        if ( !libraries_dir.exists() && !Config.startupError  ) {
            LogInfo("Check for Auto-Installation-File ...");
            Curse.downloadLoader( currentPath );
        }

        //Try to use Installer-File
        if ( !libraries_dir.exists() && !Config.startupError ) {
            LogInfo("Check for Forge-Installation-File ...");
            Curse.installLoader( currentPath );
        }

        if ( libraries_dir.exists() && !Config.startupError ) {

            if ( forge_dir.exists()  ) {
                //Search for new Forge Folder

                Pattern pattern = Pattern.compile("([.0-9]+)-([.0-9]+)", Pattern.CASE_INSENSITIVE);

                //List all Folders
                java.io.File[] forge_files = forge_dir.listFiles();

                // try-catch block to handle exceptions
                try {
                    // Display the names of the files
                    for ( java.io.File forge_file : forge_files) {

                        Matcher matcher = pattern.matcher(forge_file.getName());
                        //LogInfo( forge_files[i].getName() );

                        if (matcher.find()) {
                            Config.mc_version = matcher.group(1);
                            Config.forge_version = matcher.group(2);
                            Config.mcVersionDetail = Config.mc_version.split("\\.");
                            if (Integer.parseInt(Config.mcVersionDetail[1]) >= 17) {
                                if (Config.OS.contains("win")) {
                                    LogDebug("Using WINDOWS System-Parameter");
                                    startup_file = "libraries/net/minecraftforge/forge/" + Config.mc_version + "-" + Config.forge_version + "/win_args.txt";
                                } else {
                                    LogDebug("Using UNIX System-Parameter");
                                    startup_file = "libraries/net/minecraftforge/forge/" + Config.mc_version + "-" + Config.forge_version + "/unix_args.txt";
                                }

                                LogInfo("Found Minecraft: " + Config.mc_version + " with new Forge " + Config.forge_version);

                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    Config.startupError = true;
                    LogWarning( e.getMessage() );
                }

            }

            if( !forge_dir.exists() || Integer.parseInt(Config.mcVersionDetail[1]) < 17 ){

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
                            Config.mc_version = matcher.group(1);
                            Config.forge_version = matcher.group(2);
                            startup_file = root_files[i].getName();
                            LogInfo("Found Minecraft: " + Config.mc_version  + " with Forge " + Config.forge_version );
                            break;
                        }
                    }
                } catch (Exception e) {
                    Config.startupError = true;
                    LogWarning( e.getMessage() );
                }
            }

            if ( Config.mc_version == null && Config.forge_version == null ) {
                LogWarning("No Forge-Version could be Found!");
                Config.startupError = true;
            }

        }

        if ( !Config.startupError ) {

            if (File.checkExist(startup_file)) {

                checkContent(startup_file);

                LogInfo("Building Startup-Parameter ...");

                List<String> where = new ArrayList<>();

                if (Config.configProps.getProperty("java_path") != null && !Config.configProps.getProperty("java_path").equals("java")) {
                    where.add(Config.configProps.getProperty("java_path"));
                    LogDebug("Use Custom Java Path: " + Config.configProps.getProperty("java_path"));
                } else {
                    where.add("java");
                    LogDebug("Use Standard Java Path");
                }

                Collections.addAll(where, Config.startupParameter );

                if (Config.configProps.getProperty("timezone") != null) {
                    where.add("-Duser.timezone=" + Config.configProps.getProperty("timezone"));
                }

                if (Integer.parseInt(Config.mcVersionDetail[1]) >= 17) {
                    where.add("@" + startup_file);

                    if (Config.javaVersion < 60) {
                        LogWarning("The Java-Class-Version is with \"" + Config.javaVersion.toString() + "\" to low, to start the Server!");
                        Config.startupError = true;
                    }

                } else {
                    where.add("-jar");
                    where.add(startup_file);
                }

                where.add("nogui");

                Config.CMD_ARRAY = new String[where.size()];
                where.toArray(Config.CMD_ARRAY);
            } else {
                //Config.startupError = true;
                LogWarning("The Start-File \"" + startup_file + "\" does not exist!");
            }
        }

            //Check Eula-File
        if ( !Config.startupError ) { File.Eula(); }

        if ( !Config.startupError ) {

            if ( Config.CMD_ARRAY != null ) {
                LogInfo("");
                LogInfo("Server is Running in TimeZone: " + Config.configProps.getProperty("timezone"));
                LogInfo("More timezones in this list: https://en.wikipedia.org/wiki/List_of_tz_database_time_zones ");
                LogInfo("Setup your own timezone in server_starter.conf");
                LogInfo("");
                LogInfo("Start FORGE " + Config.forge_version + " Server");
                LogInfo("-----------------------------------------------");

                //DEBUG StartUp Commands
                LogDebug("Startup-ARRAY " + TXT_BLUE + Arrays.toString(Config.CMD_ARRAY) + TXT_RESET);

                Process serverProcess = new ProcessBuilder(Config.CMD_ARRAY)
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
            } else {
                Config.startupError = true;
                LogWarning("Could not build Start-Parameter!");
            }
        }

        if ( Config.startupError ) {

            LogError("EXIT FORGE-Server-Starter ");
            LogError("-----------------------------------------------");
            System.exit(-1);

        }
    }
}