package de.hellbz.forge;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ServerStarter {

    public static void main(String[] args) throws IOException {

        //Get System-Variables like xmx and xms
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMxBean.getInputArguments();
        String[] startupParameter = arguments.toArray(new String[0]);

        //Set OS-Name in String
        String OS = System.getProperty("os.name").toLowerCase();

        //WELCOME
        System.out.println("--");
        System.out.println("-----------------------------------------------");
        System.out.println("-- FORGE-Server-Starter by HellBz ");
        System.out.println("--");
        System.out.println("-- Website www.hellbz.de ");
        System.out.println("--");

        //DEBUG
        //String joinedString = Arrays.toString(startupParameter);
        //System.out.println("STARTUP-PARAMETER: " + joinedString );

        //Set FORGE Library-Path
        File forge_dir = new File("libraries/net/minecraftforge/forge/");

        if ( !forge_dir.exists() ){
            System.out.println("-----------------------------------------------");
            System.out.println("---- WARNING: \"libraries\"-Folder or Content not exist ");
            System.out.println("---- ERROR: EXIT Server-Starter ");
            System.out.println("-----------------------------------------------");
            System.exit(0);
        }

        //List all Folders
        File[] forge_files = forge_dir.listFiles();
        String forge_version = null;

        if ( forge_files != null && forge_files.length > 0 && !forge_files[0].isFile() ) {
            //for (int i=0; i<children.length; i++) {
            //String filename = children[i];
            forge_version = forge_files[0].getName();
            System.out.println("-- Found FORGE: " + forge_version);
            //}
        }else{
            System.out.println("-----------------------------------------------");
            System.out.println("---- WARNING: No Forge-Folder Found ");
            System.out.println("---- ERROR: EXIT Server-Starter ");
            System.out.println("-----------------------------------------------");
            System.exit(0);
        }

        //String[] startupArray = list.toArray(new String[0]);
        String startup_file;
        if ( OS.contains("win") ) {
            System.out.println("-- Using WINDOWS System-Parameter");
            startup_file = "libraries/net/minecraftforge/forge/" + forge_version + "/win_args.txt";
        }else{
            System.out.println("-- Using UNIX System-Parameter");
            startup_file = "libraries/net/minecraftforge/forge/" + forge_version + "/unix_args.txt";
        }

        // Get the startup-file
        File check_file = new File( startup_file );

        // Check if the specified file
        // Exists or not
        if ( !check_file.exists() ){
            System.out.println("-----------------------------------------------");
            System.out.println("---- WARNING: Start-File cannot be Found. ");
            System.out.println("---- ERROR: EXIT Server-Starter ");
            System.out.println("-----------------------------------------------");
            System.exit(0);
        }

        //Old StartUp-Command
        //String[] CMD_ARRAY = { "java" ,  startup_arg , "nogui"  };

        Scanner file_content = null;
        try {
            file_content = new Scanner( check_file );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // Let's loop through each line of the file
        while ( file_content != null && file_content.hasNext() ) {
            String line = file_content.nextLine();

            // Now, check if this line contains our keyword. If it does, print the line
            if (line.toLowerCase().contains("xmx") || line.toLowerCase().contains("xms")) {
                System.out.println("-----------------------------------------------");
                System.out.println("---- WARNING: Illegal characters found in Server-Args ");
                System.out.println("---- ERROR: EXIT Server-Starter ");
                System.out.println("-----------------------------------------------");
                System.exit(0);
            }
        }

        System.out.println("-- Building Startup-Parameter");

        List<String> where = new ArrayList<>();
        where.add("java");

        Collections.addAll(where, startupParameter);

        where.add("@" + startup_file);
        where.add("nogui");

        String[] CMD_ARRAY = new String[where.size()];
        where.toArray(CMD_ARRAY);

        System.out.println("-- Start FORGE " + forge_version + " Server");
        System.out.println("-----------------------------------------------");
        ProcessBuilder server_builder = new ProcessBuilder(CMD_ARRAY);

        //DEBUG StartUp Commands
        //System.out.println(Arrays.toString(CMD_ARRAY));

        Process server = server_builder.start();

        System.out.println("-- Server-Starter - LOG-Printer is started");
        System.out.println("-----------------------------------------------");
        final Scanner  server_log = new Scanner(server.getInputStream());

        Thread logger = new Thread(() -> {
            while (server_log.hasNextLine()) {
                System.out.println(server_log.nextLine());
            }
        });
        logger.start();

        System.out.println("-- Server-Starter - Console-Scanner is started");
        System.out.println("-----------------------------------------------");

        OutputStream stdin = server.getOutputStream(); // <- Eh?
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));

        Scanner scanner = new Scanner(System.in);
        Thread console = new Thread(() -> {
            while (true) {
                String input = scanner.nextLine();
                if (input.equals(""))
                    break;
                // System.out.println( input );
                try {
                    writer.write(input + System.lineSeparator());
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        console.start();

        Thread check_server = new Thread(() -> {
            while (true) {

                try{
                    TimeUnit.SECONDS.sleep(5);
                }catch(Exception ex){
                    ex.printStackTrace();
                }

                if (!server.isAlive()) {
                    System.out.println("-----------------------------------------------");
                    System.out.println("---- WARNING: Server is not correctly started up. ");
                    System.out.println("---- INFO: Please check your files and upload them to the server again if necessary. ");
                    System.out.println("---- ERROR: EXIT Server-Starter ");
                    System.out.println("-----------------------------------------------");
                    System.exit(0);
                }
            }
        });
        check_server.start();
    }
}