package net.nitrado.forge;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static javax.imageio.ImageIO.read;
import static net.nitrado.forge.Until.*;


//public class ServerStarter implements ActionListener {
public class ServerStarter {

    public static Properties configProps;
    private static Process server;
    static int count = 0;
    private static JLabel label;
    private JFrame frame;

    static JTextArea display;
    private static final Until LOGGER = null;

    //Set OS-Name in String
    public static final String OS = System.getProperty("os.name").toLowerCase();

    private ServerStarter() throws IOException {

        JPanel middlePanel = new JPanel();
        middlePanel.setBorder(BorderFactory.createEmptyBorder(30,30,10 ,30));
        middlePanel.setBorder ( new TitledBorder( new EtchedBorder(), "Server-Log" ) );

        // create the middle panel components
        display = new JTextArea ( 17, 58 );
        display.setEditable ( false ); // set textArea non-editable
        JScrollPane scroll = new JScrollPane ( display );

        scroll.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setPreferredSize(new Dimension(600, 350));

        DefaultCaret caret = (DefaultCaret)display.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        //Add Textarea in to middle panel
        middlePanel.add ( scroll );

        JFrame frame = new JFrame ();

        frame.setTitle("Forge-Server-Starter");

        Image icon = Toolkit.getDefaultToolkit().getImage("res/nitrado.png");
        frame.setIconImage( icon );

        frame.add (middlePanel);
        frame.pack ();
        frame.setLocationRelativeTo ( null );
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE );
        frame.setVisible ( true );

    }

    public static void main(String[] args) throws IOException {

        //Get System-Variables like xmx and xms
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMxBean.getInputArguments();
        String[] startupParameter = arguments.toArray(new String[0]);

        CheckFiles.configFile();
        //Set local Zimetone
        if ( ServerStarter.configProps.getProperty("timezone") != "UTC" ) {
            System.setProperty("user.timezone", ServerStarter.configProps.getProperty("timezone") );
        }

        if ( isReallyHeadless() ) {
            //Headless, all Fine
            LogInfo("This is Headless Client");
        }else{
            //
            if ( Arrays.toString(args).toLowerCase().contains("-xmx") || Arrays.toString(args).toLowerCase().contains("-xms") || Arrays.toString(startupParameter).toLowerCase().contains("-xmx") || Arrays.toString(startupParameter).toLowerCase().contains("-xms") ){
                new ServerStarter();
                LogInfo("SCRIPT USE -Xmx and -Xms for Start.");
            }else{
                LogInfo("PLS use -Xmx and -Xms for start up this script.");
                JFrame jFrame = new JFrame();
                JOptionPane.showMessageDialog(jFrame, "Script only work in Batch-Mode!\nStartfile for Batch-Mode is created.");
                CheckFiles.createStartFile();
                System.exit(0);
            }
        }



        //Until.LogInfo("-----------------------------------------------");
        //Until.LogInfo(TXT_YELLOW + "|\\| o _|_ __ _  _| _    __  _ _|_" + TXT_RESET);
        //Until.LogInfo(TXT_YELLOW + "| | |  |_ | (_|(_|(_) o | |(/_ |_" + TXT_RESET);

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
        Until.LogDebug( "STARTUP-PARAMETER " + TXT_CYAN +  joinedStartupParameter + TXT_RESET );

        String joinedStartupArgs = Arrays.toString(args);
        Until.LogDebug( "STARTUP-ARGS " + TXT_CYAN +  joinedStartupArgs + TXT_RESET );

        String currentPath = new File(".").getCanonicalPath();
        Until.LogDebug( "DIRECTORY " + TXT_CYAN +  currentPath  + TXT_RESET );

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
            Until.LogInfo("Using WINDOWS System-Parameter");
            startup_file = "libraries/net/minecraftforge/forge/" + forge_version + "/win_args.txt";
        } else {
            Until.LogInfo("Using UNIX System-Parameter");
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
            if ( line.toLowerCase().contains("xmx") || line.toLowerCase().contains("xms") ) {
                Until.LogWarning("-----------------------------------------------");
                Until.LogWarning("Illegal characters found in Server-Args ");
                Until.LogError("EXIT Server-Starter ");
                Until.LogError("-----------------------------------------------");
                System.exit(0);
            }
        }

        Until.LogInfo("Building Startup-Parameter");

        List<String> where = new ArrayList<>();
        where.add("java");
        Collections.addAll(where, startupParameter);

        if ( ServerStarter.configProps.getProperty("timezone") != null ) {
            where.add("-Duser.timezone=" + configProps.getProperty("timezone"));
        }

        where.add("@" + startup_file);
        where.add("nogui");

        String[] CMD_ARRAY = new String[where.size()];
        where.toArray(CMD_ARRAY);

        //Check Eula-File
        CheckFiles.EULA();

        Until.LogInfo("");
        Until.LogInfo("Server is Running in TimeZone: " + ServerStarter.configProps.getProperty("timezone") );
        Until.LogInfo("More timezones in this list: https://en.wikipedia.org/wiki/List_of_tz_database_time_zones " );
        Until.LogInfo("Setup your own timezone in server_starter.conf");
        Until.LogInfo("");

        //Until.LogInfo("DEBUG: " + configProps.getProperty("debug") );

        Until.LogInfo("Start FORGE " + forge_version + " Server");
        Until.LogInfo("-----------------------------------------------");
        ProcessBuilder server_builder = new ProcessBuilder(CMD_ARRAY);


        //DEBUG StartUp Commands
        Until.LogDebug( "CMD-ARRAY " + TXT_BLUE +  Arrays.toString(CMD_ARRAY) + TXT_RESET );
        //System.out.println(Arrays.toString(CMD_ARRAY));

        try {
            server = server_builder.start();
        } catch (IOException e) {
            Until.LogError("General I/O exception: " + e.getMessage());
            e.printStackTrace();
        }

        Until.LogInfo("Server-Starter - LOG-Printer is started");
        Until.LogInfo("-----------------------------------------------");

        final Scanner server_log = new Scanner(server.getInputStream());

        Thread logger = new Thread(() -> {
            while (server_log.hasNextLine()) {
                String println = server_log.nextLine();
                System.out.println(println);
                if (display != null )
                    display.append(println + System.lineSeparator() );
            }
        });
        logger.start();

        Until.LogInfo("Server-Starter - Console-Scanner is started");
        Until.LogInfo("-----------------------------------------------");

        OutputStream stdin = server.getOutputStream(); // <- Eh?
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));

        Scanner scanner = new Scanner(System.in);
        Thread console = new Thread(() -> {
            while (true) {
                String input = scanner.nextLine();
                if (input.equals(""))
                    break;
                //System.out.println( input );
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
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                if (!server.isAlive()) {
                    Until.LogWarning("-----------------------------------------------");
                    Until.LogWarning("Server is not correctly started up. ");
                    Until.LogWarning("Please check your files and upload them to the server again if necessary. ");
                    Until.LogError("EXIT Server-Starter ");
                    Until.LogError("-----------------------------------------------");
                    System.exit(0);
                }
            }
        });
        check_server.start();
    }
}
