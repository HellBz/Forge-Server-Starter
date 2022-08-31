package net.nitrado.forge;

import javax.swing.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.*;
import java.util.List;

import static net.nitrado.forge.Until.*;


public class ServerStarter {

    public static Properties configProps;

    static int count = 0;

    //Set OS-Name in String
    public static final String OS = System.getProperty("os.name").toLowerCase();

    public static void main(String[] args) throws IOException, InterruptedException {

        //Get System-Variables like xmx and xms
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMxBean.getInputArguments();
        String[] startupParameter = arguments.toArray(new String[0]);

        CheckFiles.ConfigFile();
        CheckFiles.LogFile();
        //Set local Zimetone
        if (!ServerStarter.configProps.getProperty("timezone").equals("UTC")) {
            System.setProperty("user.timezone", ServerStarter.configProps.getProperty("timezone"));
        }

        if (isReallyHeadless()) {
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
                CheckFiles.StartFile();
                System.exit(0);
            }
        }

        //WELCOME
        Until.LogInfo("-----------------------------------------------");
        Until.LogInfo("FORGE-Server-Starter");
        Until.LogInfo("");
        Until.LogInfo("By " + TXT_YELLOW + "Nitrado" + TXT_RESET + ".net");
        Until.LogInfo("");

        Properties properties = System.getProperties();
        properties.forEach((k, v) -> Until.LogDebug(k + ":" + v));

        //DEBUG
        String joinedStartupParameter = Arrays.toString(startupParameter);
        Until.LogDebug("STARTUP-PARAMETER " + TXT_CYAN + joinedStartupParameter + TXT_RESET);

        String joinedStartupArgs = Arrays.toString(args);
        Until.LogDebug("STARTUP-ARGS " + TXT_CYAN + joinedStartupArgs + TXT_RESET);

        String currentPath = new File(".").getCanonicalPath();
        Until.LogDebug("DIRECTORY " + TXT_CYAN + currentPath + TXT_RESET);

        //Set FORGE Library-Path
        File forge_dir = new File("libraries/net/minecraftforge/forge/");

        if (!forge_dir.exists()) {
            Until.LogWarning("-----------------------------------------------");
            Until.LogWarning("The \"libraries\"-Folder or Content not exist ");
            Until.LogError("EXIT Server-Starter ");
            Until.LogError("-----------------------------------------------");
            System.exit(0);
        }

        //List all Folders
        File[] forge_files = forge_dir.listFiles();
        String forge_version = null;

        if (forge_files != null && forge_files.length > 0 && !forge_files[0].isFile()) {
            //for (int i=0; i<children.length; i++) {
            //String filename = children[i];
            forge_version = forge_files[0].getName();
            Until.LogInfo("Found FORGE: " + forge_version);
            //}
        } else {
            Until.LogWarning("-----------------------------------------------");
            Until.LogWarning("No Forge-Folder Found ");
            Until.LogError("EXIT Server-Starter ");
            Until.LogError("-----------------------------------------------");
            System.exit(0);
        }

        //String[] startupArray = list.toArray(new String[0]);
        String startup_file;
        if (OS.contains("win")) {
            Until.LogDebug("Using WINDOWS System-Parameter");
            startup_file = "libraries/net/minecraftforge/forge/" + forge_version + "/win_args.txt";
        } else {
            Until.LogDebug("Using UNIX System-Parameter");
            startup_file = "libraries/net/minecraftforge/forge/" + forge_version + "/unix_args.txt";
        }

        // Get the startup-file
        File check_file = new File(startup_file);

        // Check if the specified file
        // Exists or not
        if (!check_file.exists()) {
            Until.LogWarning("-----------------------------------------------");
            Until.LogWarning("Start-File cannot be Found. ");
            Until.LogError("EXIT Server-Starter ");
            Until.LogError("-----------------------------------------------");
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
                Until.LogWarning("-----------------------------------------------");
                Until.LogWarning("Illegal characters found in Server-Args ");
                Until.LogError("EXIT Server-Starter ");
                Until.LogError("-----------------------------------------------");
                System.exit(0);
            }
        }

        Until.LogDebug("Building Startup-Parameter");

        List<String> where = new ArrayList<>();

        if ( ServerStarter.configProps.getProperty("java_path") != null && !ServerStarter.configProps.getProperty("java_path").equals("java")  ) {
            where.add( ServerStarter.configProps.getProperty("java_path") );
            Until.LogDebug("Use Custom Java Path: " + ServerStarter.configProps.getProperty("java_path") );
        }else{
            where.add("java");
            Until.LogDebug("Use Standart Java Path");

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
        CheckFiles.Eula();

        Until.LogInfo("");
        Until.LogInfo("Server is Running in TimeZone: " + ServerStarter.configProps.getProperty("timezone"));
        Until.LogInfo("More timezones in this list: https://en.wikipedia.org/wiki/List_of_tz_database_time_zones ");
        Until.LogInfo("Setup your own timezone in server_starter.conf");
        Until.LogInfo("");
        Until.LogInfo("Start FORGE " + forge_version + " Server");
        Until.LogInfo("-----------------------------------------------");

        //DEBUG StartUp Commands
        Until.LogDebug("Startup-ARRAY " + TXT_BLUE + Arrays.toString(CMD_ARRAY) + TXT_RESET);

        Process serverProcess = new ProcessBuilder(CMD_ARRAY)
                .inheritIO()
                .start();

        int exitCode = serverProcess.waitFor();

        if (exitCode == 0) {
            Until.LogWarning("Server is successfully stopped.");
            System.exit(0);
        } else {
            Until.LogError("Server is Crashed with Exit-Code: " + exitCode);
            Until.LogWarning("Please check your files and upload them to the server again if necessary. ");
            Until.LogError("EXIT Server-Starter ");
            System.exit(Integer.parseInt(String.valueOf(exitCode)));
        }
    }
}
