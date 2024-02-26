package de.hellbz.forge;

import de.hellbz.forge.Utils.*;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static de.hellbz.forge.Utils.Data.*;

public class ServerStarter {

    static {
        Document.LogFile();
        LogInfo("-----------------------------------------------");
        LogInfo("FORGE-Server-Starter");
        LogInfo("Now support MinecraftForge and NeoForged");
        LogInfo("");
        LogInfo("By " + TXT_GREEN + "HellBz" + TXT_RESET + ".de");
        LogInfo("");
        LogInfo("-----------------------------------------------");

    }

    public static void main(String[] args) throws IOException, InterruptedException {

        // Google.LogToGForm();

        if (Arrays.toString(args).toLowerCase().contains("-autofile") ) {

            try {
            Files.write( Paths.get(Config.rootFolder + File.separator + "forge-auto-install.txt"),  Config.fileAutoFileString.getBytes(StandardCharsets.UTF_8 ) );
            } catch (Exception e) {
                //e.printStackTrace();
            }

            LogWarning("Auto Installation-File successfully created.");
            LogError("EXIT FORGE-Server-Starter ");
            LogError("-----------------------------------------------");
            System.exit(-1);
        }

        //WELCOME
        LogInfo("Checking System ...");

        //System.out.println( NeoForge.getVersions().toString() );

        //System.out.println( Forge.getVersions().toString() );

        //Get System-Variables like xmx and xms
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMxBean.getInputArguments();
        Config.startupParameter = arguments.toArray(new String[0]);

        Properties properties = System.getProperties();
        properties.forEach((k, v) -> LogDebug(k + ":" + v));
        LogDebug("server_starter.conf: " + Config.configProps.toString());

        //DEBUG
        String joinedStartupParameter = Arrays.toString(Config.startupParameter);
        LogDebug("STARTUP-PARAMETER: " + TXT_CYAN + joinedStartupParameter + TXT_RESET);

        String joinedStartupArgs = Arrays.toString(args);
        LogDebug("STARTUP-ARGS: " + TXT_CYAN + joinedStartupArgs + TXT_RESET);

        String currentPath = new java.io.File(".").getCanonicalPath();
        LogDebug("DIRECTORY: " + TXT_CYAN + currentPath + TXT_RESET);

        LogDebug("-----------------------------------------------");

        if (isReallyHeadless()) {
            //Headless, all Fine
            LogDebug("This is Headless Client");

        } else {

            LogDebug(Arrays.toString(args));
            if (Arrays.toString(args).toLowerCase().contains("-xmx") || Arrays.toString(args).toLowerCase().contains("-xms") || Arrays.toString(Config.startupParameter).toLowerCase().contains("-xmx") || Arrays.toString(Config.startupParameter).toLowerCase().contains("-xms")) {
                LogDebug("SCRIPT USE -Xmx and -Xms for Start.");
            } else {
                // Config.startupError = true;
                LogWarning("PLS use -Xmx and -Xms for start up this script.");
                JFrame jFrame = new JFrame();
                JOptionPane.showMessageDialog(jFrame, "Script only work in Batch-Mode!\nStartfile for Batch-Mode is created.");
                Document.StartFile();
                // System.exit(0);
            }
        }

        //No Internet Connection, only manually installation
        if (!Net.isConnected) {
            LogInfo("Place your Forge-Installer-JAR directly next to the current JAR.");
            Config.startupError = true;
        } else {
            Remote.checkForUpdate();
        }

        //Try Auto-Installer
        if (!Config.librariesFolder.exists() && !Config.startupError && !Loader.checkLocalInstaller() ) {
            //checking Minecraft Version and download, if installer-File not already exist or if error occurs
            if ( Loader.checkLoaderVersion() ) {
                Loader.downloadLoader();
            }
        }

        //Try to use Installer-File
        if (!Config.librariesFolder.exists() && !Config.startupError && Loader.checkLocalInstaller(true) ) {
            LogInfo("Check for Loader-Installation-File ...");
            Loader.installLoader();
        }

        if (Config.librariesFolder.exists() && !Config.startupError) {
            //Try to find loader in Libraries and Forge Folders
            Loader.checkLoaderFolder();
            Loader.checkLocalFolder();
        }

        if (!Config.startupError) {

            if (Document.checkExist(Config.startupFile)) {
                checkContent(Config.startupFile);
                LogInfo("Building Startup-Parameter ...");

                List<String> where = new ArrayList<>();
                String javaPath = Config.configProps.getProperty("java_path");
                String timezone = Config.configProps.getProperty("timezone");

                if (javaPath != null && !javaPath.equals("java")) {
                    where.add(javaPath);
                    LogDebug("Use Custom Java Path: " + javaPath);
                } else {
                    where.add("java");
                    LogDebug("Use Standard Java Path");
                }

                Collections.addAll(where, Config.startupParameter);

                if (timezone != null) {
                    where.add("-Duser.timezone=" + timezone);
                }

                Comparator<String> versionComparator = new VersionComparator();

                LogDebug( Config.startupFile.toString() );

                if (Config.startupFile.endsWith(".jar")) {
                    where.add("-jar");
                    where.add(Config.startupFile);
                } else {
                    where.add("@" + Config.startupFile);

                    if (Config.javaVersion < 60) {
                        LogWarning("The Java-Class-Version is with \"" + Config.javaVersion + "\" too low to start the Server!");
                        Config.startupError = true;
                    }
                }

                where.add("nogui");

                Config.CMD_ARRAY = new String[where.size()];
                where.toArray(Config.CMD_ARRAY);
            } else {
                //Config.startupError = true;
                LogWarning("The Start-File \"" + Config.startupFile + "\" does not exist!");
            }
        }

        //Check Eula-File
        if (!Config.startupError) {
            Document.Eula();
        }

        if (!Config.startupError) {

            if (Config.CMD_ARRAY != null) {
                LogInfo("");
                LogInfo("Server is Running in TimeZone: " + Config.configProps.getProperty("timezone"));
                LogInfo("Setup your own timezone in server_starter.conf");
                LogInfo("");
                LogInfo("Start " + (Config.isForge ? "Forge" : "NeoForge") + " " + Config.loaderVersion + " Server");
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

        if (Config.startupError) {

            LogError("EXIT FORGE-Server-Starter ");
            LogError("-----------------------------------------------");
            System.exit(-1);

        }
    }
}