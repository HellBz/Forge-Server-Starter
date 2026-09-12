package de.hellbz.forge;

import de.hellbz.forge.Utils.*;

import javax.swing.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
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

        if (Arrays.toString(args).toLowerCase().contains("-autofile")) {

            FileOperation.downloadOrReadFile("/res/forge-auto-install.txt", Config.rootFolder + File.separator + "forge-auto-install.txt");

            LogWarning("Auto Installation-File successfully created.");
            LogError("EXIT FORGE-Server-Starter ");
            LogError("-----------------------------------------------");
            System.exit(-1);
        }

        //WELCOME
        LogInfo("Checking System ...");

        //Get System-Variables like xmx and xms
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMxBean.getInputArguments();
        Config.startupParameter = arguments.toArray(new String[0]);

        Data.logSelectedSystemProperties();
        LogDebug(Config.PROPERTIES_FILE + ": " + Config.configProps.toString());

        //DEBUG
        String joinedStartupParameter = Arrays.toString(Config.startupParameter);
        LogDebug("STARTUP-PARAMETER: " + TXT_CYAN + joinedStartupParameter + TXT_RESET);

        String joinedStartupArgs = Arrays.toString(args);
        LogDebug("STARTUP-ARGS: " + TXT_CYAN + joinedStartupArgs + TXT_RESET);

        LogDebug("-----------------------------------------------");

        if (isReallyHeadless()) {
            //Headless, all Fine
            LogDebug("This is Headless Client");
        } else {
            if (Data.containsMemoryParameters(args) || Data.containsMemoryParameters(Config.startupParameter)) {
                LogDebug("SCRIPT USE -Xmx and -Xms for Start.");
            } else {
                Config.startupError = true;
                Document.StartFile();
                LogWarning("Please use -Xmx and -Xms for startup this script.");
                LogInfo("A startup file for Batch-Mode has been created: start_server." + (Config.OS.contains("win") ? "bat" : "sh"));
                try {
                    if (!isReallyHeadless()) {
                        JOptionPane.showMessageDialog(null,
                            "Script only works in Batch-Mode!\n" +
                            "A startup file for Batch-Mode has been created.\n\n" +
                            "Please use: start_server." + (Config.OS.contains("win") ? "bat" : "sh"));
                    }
                } catch (Exception e) {
                    // GUI not available, message already logged to console
                }
            }
        }

        //No Internet Connection, only manually installation
        if (!Config.startupError) {
            String networkCheckSetting = Config.configProps.getProperty("network_check", "true");
            if (!Net.isConnected && !networkCheckSetting.equalsIgnoreCase("false")) {
                LogInfo("Place your Forge-Installer-JAR directly next to the current JAR.");
                Config.startupError = true;
            } else if (Net.isConnected) {
                Remote.checkForUpdate();
            } else {
                LogInfo("Network-Check is disabled, skipping update check. Trying offline start ...");
            }
        }

        //Try Auto-Installer
        if (!Config.librariesFolder.exists() && !Config.startupError && !Loader.checkLocalInstaller()) {
            if (Loader.checkLoaderVersion()) {
                Loader.downloadLoader();
            }
        }

        //Try to use Installer-File
        if (!Config.librariesFolder.exists() && !Config.startupError && Loader.checkLocalInstaller(true)) {
            LogInfo("Check for Loader-Installation-File ...");
            Loader.installLoader();
        }

        if (Config.librariesFolder.exists() && !Config.startupError) {
            Loader.checkLoaderFolder();
            Loader.checkLocalFolder();
        }

        // Forge-Version-Update: check and update Forge/NeoForge to the latest version on every start
        if (Config.librariesFolder.exists() && !Config.startupError && Net.isConnected) {
            Loader.checkAndUpdateLoader();
        }

        if (!Config.startupError) {

            if (Document.checkExist(Config.startupFile)) {
                checkContent(Config.startupFile);
                LogInfo("Building Startup-Parameter ...");

                List<String> where = new ArrayList<>();
                String javaPath = Config.configProps.getProperty("java_path");
                String timezone = Config.configProps.getProperty("timezone", "UTC");

                if (javaPath != null && !javaPath.equals("java")) {
                    where.add(javaPath);
                    LogDebug("Use Custom Java Path: " + javaPath);
                } else {
                    where.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
                    LogDebug("Use Standard Java Path");
                }

                if (timezone != null && !timezone.isEmpty()) {
                    if (!timezone.equals("UTC")) {
                        where.add("-Duser.timezone=" + timezone);
                    }
                }

                LogDebug(Config.startupFile);

                if (Config.startupFile.endsWith(".jar")) {
                    where.add("-jar");
                    Collections.addAll(where, Config.startupParameter);
                    where.add(Config.startupFile);
                } else {
                    File installerFileJavaArgs = new File(Config.rootFolder + File.separator + "user_jvm_args.txt");
                    if (installerFileJavaArgs.exists()) {
                        where.add("@user_jvm_args.txt");
                    } else {
                        Collections.addAll(where, Config.startupParameter);
                    }
                    where.add("@" + System.getProperty("user.dir") + File.separator + Config.startupFile);

                    // Java class version check - updated for Java 21+ and Java 25+
                    // Java 17 = 61, Java 21 = 65, Java 25 = 69
                    if (Config.javaVersion < 61) {
                        LogWarning("The Java-Class-Version is with \"" + Config.javaVersion + "\" too low to start the Server!");
                        LogWarning("Minimum required: Java 17 (class version 61). Your version: class version " + Config.javaVersion);
                        Config.startupError = true;
                    }
                }

                where.add("nogui");

                Config.CMD_ARRAY = new String[where.size()];
                where.toArray(Config.CMD_ARRAY);
            } else {
                LogWarning("The Start-File \"" + Config.startupFile + "\" does not exist!");
            }
        }

        //Check Eula-File
        if (!Config.startupError) {
            Document.Eula();
        }

        if (!Config.startupError) {
            if (Config.CMD_ARRAY != null) {
                // Server restart loop - supports /restart command (Faster Restart feature)
                boolean shouldRestart = true;
                while (shouldRestart) {
                    shouldRestart = false;

                    LogInfo("");
                    LogInfo("Server is Running in TimeZone: " + Config.configProps.getProperty("timezone"));
                    LogInfo("Setup your own timezone in " + Config.PROPERTIES_FILE);
                    LogInfo("");
                    LogInfo("Start " + (Config.isForge ? "Forge" : "NeoForge") + " " + Config.loaderVersion + " Server");
                    LogInfo("-----------------------------------------------");
                    LogInfo("Tip: Type '/restart' in server console to trigger a fast restart.");
                    LogInfo("-----------------------------------------------");

                    LogDebug("Startup-ARRAY " + TXT_BLUE + Arrays.toString(Config.CMD_ARRAY) + TXT_RESET);

                    ProcessBuilder pb = new ProcessBuilder(Config.CMD_ARRAY);
                    pb.redirectErrorStream(true);

                    Process serverProcess = pb.start();

                    // Shared restart flag between stdin and stdout threads
                    final boolean[] restartRequested = {false};

                    // Thread to handle stdin forwarding (fixes Issue #18 - console broken with Java 17+)
                    Thread stdinForwarder = new Thread(() -> {
                        try {
                            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
                            OutputStream serverInput = serverProcess.getOutputStream();
                            PrintWriter serverWriter = new PrintWriter(new OutputStreamWriter(serverInput), true);
                            String line;
                            while ((line = consoleReader.readLine()) != null) {
                                if (line.trim().equalsIgnoreCase("/restart")) {
                                    restartRequested[0] = true;
                                    LogInfo("Restart command detected from console - restarting server gracefully...");
                                    try {
                                        OutputStream out = serverProcess.getOutputStream();
                                        out.write(("stop\n").getBytes());
                                        out.flush();
                                    } catch (IOException ignored) {}
                                    continue;
                                }
                                serverWriter.println(line);
                            }
                        } catch (IOException e) {
                            // console closed
                        }
                    });
                    stdinForwarder.setDaemon(true);
                    stdinForwarder.start();

                    // Thread to handle stdout/stderr and detect /restart (Faster Restart feature)
                    Thread stdoutReader = new Thread(() -> {
                        try {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(serverProcess.getInputStream()));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                System.out.println(line);
                                // Detect restart command issued by player or console
                                if (line.toLowerCase().contains("issued server command: /restart")
                                        || line.toLowerCase().contains("[f-s-s/restart]")
                                        || line.contains("F-S-S: RESTART")) {
                                    restartRequested[0] = true;
                                    LogInfo("Restart command detected - restarting server gracefully...");
                                    // Send stop command to server
                                    try {
                                        OutputStream out = serverProcess.getOutputStream();
                                        out.write(("stop\n").getBytes());
                                        out.flush();
                                    } catch (IOException ignored) {}
                                }
                            }
                        } catch (IOException e) {
                            // process ended
                        }
                    });
                    stdoutReader.setDaemon(false);
                    stdoutReader.start();

                    int exitCode = serverProcess.waitFor();
                    stdoutReader.join(5000);

                    if (restartRequested[0] || exitCode == Config.RESTART_EXIT_CODE) {
                        LogWarning("Server is restarting...");
                        LogInfo("-----------------------------------------------");
                        shouldRestart = true;
                    } else if (exitCode == 0) {
                        LogWarning("Server is successfully stopped.");
                        System.exit(0);
                    } else {
                        LogError("Server is Crashed with Exit-Code: " + exitCode);
                        LogWarning("Please check your files and upload them to the server again if necessary.");
                        LogError("EXIT Server-Starter ");
                        System.exit(exitCode);
                    }
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
