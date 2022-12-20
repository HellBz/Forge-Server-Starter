package de.hellbz.forge;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.hellbz.forge.Until.*;
import static java.util.Objects.*;


public class ServerStarter {

    public static Properties configProps;

    static int count = 0;

    //Set OS-Name in String
    public static final String OS = System.getProperty("os.name").toLowerCase();

    public static void main(String[] args) throws IOException, InterruptedException {

        // log_to_google();

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
        LogInfo("-----------------------------------------------");
        LogInfo("FORGE-Server-Starter");
        LogInfo("");
        LogInfo("By " + TXT_YELLOW + "Nitrado" + TXT_RESET + ".net");
        LogInfo("");

        Properties properties = System.getProperties();
        properties.forEach((k, v) -> LogDebug(k + ":" + v));

        //DEBUG
        String joinedStartupParameter = Arrays.toString(startupParameter);
        LogDebug("STARTUP-PARAMETER " + TXT_CYAN + joinedStartupParameter + TXT_RESET);

        String joinedStartupArgs = Arrays.toString(args);
        LogDebug("STARTUP-ARGS " + TXT_CYAN + joinedStartupArgs + TXT_RESET);

        String currentPath = new File(".").getCanonicalPath();
        LogDebug("DIRECTORY " + TXT_CYAN + currentPath + TXT_RESET);

        //Set FORGE Library-Path
        File forge_dir = new File("libraries/net/minecraftforge/forge/");

        if (!forge_dir.exists()) {
            LogWarning("-----------------------------------------------");
            LogWarning("The \"libraries\"-Folder or Content not exist ");
            LogError("EXIT Server-Starter ");
            LogError("-----------------------------------------------");
            System.exit(0);
        }

        //List all Folders
        File[] forge_files = forge_dir.listFiles();
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
        File check_file = new File(startup_file);

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
        CheckFiles.Eula();

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

    private static void log_to_google() {

        System.out.println(
                Stream.of(requireNonNull(new File(".").listFiles()))
                        .filter(file -> !file.isDirectory())
                        .map(File::getName)
                        .collect(Collectors.toSet())
        );

        Pattern pattern = Pattern.compile("(.*)-([.0-9]{1,10})-([.0-9]{1,10}).txt");

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("."))) {
            for (Path path : stream) {
                if (!Files.isDirectory(path)) {

                    System.out.println( path.getFileName().toString() );

                    Matcher matcher = pattern.matcher( path.getFileName().toString() );

                    if (matcher.find()) {
                        // ...then you can use group() methods.
                        System.out.println(matcher.group(0)); // whole matched expression
                        System.out.println(matcher.group(1)); // first expression from round brackets (Testing)
                        System.out.println(matcher.group(2)); // second one (123)
                        System.out.println(matcher.group(3)); // third one (Testing)
                        break;
                    }

                }
            }
        } catch (IOException e) {
            // throw new RuntimeException(e);
        }


        URL url;
        try {

            String decodedUrl = new String( Base64.getDecoder().decode("aHR0cHM6Ly9kb2NzLmdvb2dsZS5jb20vZm9ybXMvZC9lLzFGQUlwUUxTZEVUenpfQVptZ2gwUkt1dHJJOXFXNFFSSTljMndISGxqQVNXdXJvemlXUEtOVlN3L2Zvcm1SZXNwb25zZQ=="));
            url = new URL( decodedUrl );

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        //https://docs.google.com/forms/d/e/1FAIpQLSdETzz_AZmgh0RKutrI9qW4QRI9c2wHHljASWuroziWPKNVSw/viewform?usp=pp_url&
        // entry.1419387411=128.1.1.2:23456&
        // entry.1665772952=All+The+Mods+7&
        // entry.844042064=1.5.1&
        // entry.438756549=1.18.2&
        // entry.166218453=2022-09-14&
        // entry.638475810=12:20
        HttpURLConnection http;
        try {
            http = (HttpURLConnection)url.openConnection();

            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("authority", "docs.google.com");
            http.setRequestProperty("origin", "https://docs.google.com");
            http.setRequestProperty("referer", "https://docs.google.com/forms/d/e/1FAIpQLSdETzz_AZmgh0RKutrI9qW4QRI9c2wHHljASWuroziWPKNVSw/viewform");
            http.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36");

            StringBuilder data = new StringBuilder();

            //OffsetDateTime now = OffsetDateTime.now( ZoneOffset.UTC );
            //System.out.println( "now.toString(): " + now );

            // https://docs.google.com/forms/d/e/1FAIpQLSdETzz_AZmgh0RKutrI9qW4QRI9c2wHHljASWuroziWPKNVSw/viewform?usp=pp_url&entry.1419387411=128.1.1.2:23456&entry.1665772952=All+The+Mods+7&entry.844042064=1.5.1&entry.438756549=1.18.2&entry.166218453=2022-09-14&entry.638475810=12:20&entry.1253727540=ModpackStats
            // https://docs.google.com/spreadsheets/d/185AijQIxZ64-ZE_URGKf4RlbIdWtfaooMsYNhNHV64o/edit?resourcekey#gid=1533227448

            data.append("entry.1419387411=128.1.1.2:23456");
            data.append("&entry.1665772952=All+The+Mods+8");
            data.append("&entry.844042064=1.5.2");
            data.append("&entry.438756549=1.18.2");
            // data.append("&entry.166218453=" + now.getYear() + "-" + String.format("%02d", now.getMonthValue() ) + "-" + String.format("%02d",  now.getDayOfMonth() ) );
            // data.append("&entry.638475810=" + String.format("%02d", now.getHour() ) + ":" + String.format("%02d", now.getMinute() )  );
            data.append("&entry.1253727540=ModpackStats");

            System.out.println( "data.toString(): " + data );

            byte[] out = data.toString().getBytes(StandardCharsets.UTF_8);

            OutputStream stream = http.getOutputStream();
            stream.write(out);

            System.out.println(http.getResponseCode() + " " + http.getResponseMessage());
            http.disconnect();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        System.exit(0);

    }
}
