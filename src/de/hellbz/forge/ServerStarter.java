package de.hellbz.forge;

import de.hellbz.forge.Utils.File;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.*;

import static de.hellbz.forge.Utils.Data.*;

public class ServerStarter {

    public static Properties configProps;

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
        //Set local Zimetone
        if (!ServerStarter.configProps.getProperty("timezone").equals("UTC")) {
            System.setProperty("user.timezone", ServerStarter.configProps.getProperty("timezone"));
        }

        if ( isReallyHeadless() ) {
            //Headless, all Fine
            LogDebug("This is Headless Client");
        } else {
            //
            if (Arrays.toString(args).toLowerCase().contains("-xmx") || Arrays.toString(args).toLowerCase().contains("-xms") || Arrays.toString(startupParameter).toLowerCase().contains("-xmx") || Arrays.toString(startupParameter).toLowerCase().contains("-xms")) {
                LogDebug("SCRIPT USE -Xmx and -Xms for Start.");
            } else {
                LogInfo("PLS use -Xmx and -Xms for start up this script.");
                JFrame jFrame = new JFrame();
                JOptionPane.showMessageDialog(jFrame, "Script only work in Batch-Mode!\nStartfile for Batch-Mode is created.");
                File.StartFile();
                System.exit(0);
            }
        }

        //WELCOME
        LogInfo("-----------------------------------------------");
        LogInfo("FORGE-Server-Starter");
        LogInfo("");
        LogInfo("By " + TXT_GREEN + "HellBz" + TXT_RESET + ".de");
        LogInfo("");

        Properties properties = System.getProperties();
        properties.forEach((k, v) -> LogDebug(k + ":" + v));

        //DEBUG
        String joinedStartupParameter = Arrays.toString(startupParameter);
        LogDebug("STARTUP-PARAMETER " + TXT_CYAN + joinedStartupParameter + TXT_RESET);

        String joinedStartupArgs = Arrays.toString(args);
        LogDebug("STARTUP-ARGS " + TXT_CYAN + joinedStartupArgs + TXT_RESET);

        String currentPath = new java.io.File(".").getCanonicalPath();
        LogDebug("DIRECTORY " + TXT_CYAN + currentPath + TXT_RESET);

        //Set FORGE Library-Path
        java.io.File forge_dir = new java.io.File("libraries/net/minecraftforge/forge/");

        if (!forge_dir.exists()) {
            LogWarning("-----------------------------------------------");
            LogWarning("The \"libraries\"-Folder or Content not exist ");
            LogError("EXIT Server-Starter ");
            LogError("-----------------------------------------------");
            System.exit(0);
        }

        //List all Folders
        java.io.File[] forge_files = forge_dir.listFiles();
        String forge_version = null;

        if (forge_files != null && forge_files.length > 0 && !forge_files[0].isFile()) {
            //for (int i=0; i<children.length; i++) {
            //String filename = children[i];
            forge_version = forge_files[0].getName();
            LogInfo("Found FORGE: " + forge_version);
            //}
        } else {
            LogWarning("-----------------------------------------------");
            LogWarning("No Forge-Folder Found ");
            LogError("EXIT Server-Starter ");
            LogError("-----------------------------------------------");
            System.exit(0);
        }

        //String[] startupArray = list.toArray(new String[0]);
        String startup_file;
        if (OS.contains("win")) {
            LogDebug("Using WINDOWS System-Parameter");
            startup_file = "libraries/net/minecraftforge/forge/" + forge_version + "/win_args.txt";
        } else {
            LogDebug("Using UNIX System-Parameter");
            startup_file = "libraries/net/minecraftforge/forge/" + forge_version + "/unix_args.txt";
        }

        // Get the startup-file
        java.io.File check_file = new java.io.File(startup_file);

        // Check if the specified file
        // Exists or not
        if (!check_file.exists()) {
            LogWarning("-----------------------------------------------");
            LogWarning("Start-File cannot be Found. ");
            LogError("EXIT Server-Starter ");
            LogError("-----------------------------------------------");
            System.exit(0);
        }

        //Old StartUp-Command
        //String[] CMD_ARRAY = { "java" ,  startup_arg , "nogui"  };

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
                LogWarning("-----------------------------------------------");
                LogWarning("Illegal characters found in Server-Args ");
                LogError("EXIT Server-Starter ");
                LogError("-----------------------------------------------");
                System.exit(0);
            }
        }

        LogDebug("Building Startup-Parameter");

        List<String> where = new ArrayList<>();

        if ( ServerStarter.configProps.getProperty("java_path") != null && !ServerStarter.configProps.getProperty("java_path").equals("java")  ) {
            where.add( ServerStarter.configProps.getProperty("java_path") );
            LogDebug("Use Custom Java Path: " + ServerStarter.configProps.getProperty("java_path") );
        }else{
            where.add("java");
            LogDebug("Use Standart Java Path");

        }

        Collections.addAll(where, startupParameter);

        if (ServerStarter.configProps.getProperty("timezone") != null) {
            where.add("-Duser.timezone=" + configProps.getProperty("timezone"));
        }

        where.add("@" + startup_file);
        where.add("nogui");

        String[] CMD_ARRAY = new String[where.size()];
        where.toArray(CMD_ARRAY);

        //Check Eula-File
        File.Eula();

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
    }


}
