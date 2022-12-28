package de.hellbz.forge.Utils;

import de.hellbz.forge.ServerStarter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.hellbz.forge.ServerStarter.startupError;
import static de.hellbz.forge.Utils.Data.*;

public class Curse {

        public static boolean installLoader(String installPath ) {

                //Set current Directory
                java.io.File currentDir = new java.io.File(installPath);

                java.io.File[] currentFiles = currentDir.listFiles();

                Pattern pattern = Pattern.compile("forge-([.0-9]+)-([.0-9]+)-([universal|installer]+).([jar|zip]+)", Pattern.CASE_INSENSITIVE);

                String installerFile = null;
                String mcVersion = null;
                String forgeVersion = null;

                // try-catch block to handle exceptions
                try {
                        //LogInfo("Files are:");

                        // Display the names of the files
                        for (int i = 0; i < currentFiles.length; i++) {

                                Matcher matcher = pattern.matcher(currentFiles[i].getName());

                                if (matcher.find()) {
                                        mcVersion = matcher.group(1);
                                        forgeVersion = matcher.group(2);
                                        installerFile = currentFiles[i].getName();
                                        LogInfo("Match found INSTALLER with MC-Version " + mcVersion  + " and Forge " + forgeVersion );
                                        break;
                                }
                        }
                } catch (Exception e) {
                        ServerStarter.startupError = true;
                        LogWarning( e.getMessage() );
                }

                if ( installerFile != null ) {

                        try {
                                final String filename = new File(installerFile).getName();

                                LogInfo("Attempting to start Server " + installerFile);
                                LogInfo("Filename: " + filename);
                                LogInfo("Directory: " + installPath);
                                //LogInfo("Attempting to use installer from " + installPath);
                                LogInfo("Starting installation of Loader, installer output incoming");
                                LogInfo("Check log from installer for more information");
                                final Process start;

                                String javaStart = "java";
                                if (ServerStarter.configProps.getProperty("java_path") != null && !ServerStarter.configProps.getProperty("java_path").equals("java")) {
                                        javaStart = ServerStarter.configProps.getProperty("java_path");
                                        LogDebug("Use for Installer Custom Java Path: " + ServerStarter.configProps.getProperty("java_path"));
                                }

                                final Process installer = start = new ProcessBuilder(new String[]{ javaStart , "-jar", installerFile, "nogui", "--installServer"}).directory(new File(installPath)).start();
                                final Scanner serverLog = new Scanner(start.getInputStream());
                                while (serverLog.hasNextLine()) {
                                        final String println = serverLog.nextLine();
                                        LogForge(println);
                                }
                                installer.waitFor();
                                LogInfo("Done installing loader, deleting installer!");

                                final File installerFile2 = new File( installPath + File.separator + installerFile);
                                if (installerFile2.exists()) {
                                        Files.delete(installerFile2.toPath());
                                }

                                final File installerFileLog = new File(installPath + File.separator + installerFile + ".log");
                                if (installerFileLog.exists()) {
                                        Files.delete(installerFileLog.toPath());
                                }

                                final File installerFileRunBat = new File(installPath + File.separator + "run.bat");
                                if (installerFileRunBat.exists()) {
                                        Files.delete(installerFileRunBat.toPath());
                                }

                                final File installerFileRunSh = new File(installPath + File.separator + "run.sh");
                                if (installerFileRunSh.exists()) {
                                        Files.delete(installerFileRunSh.toPath());
                                }

                                final File installerFileJavaArgs = new File(installPath + File.separator + "user_jvm_args.txt");
                                if (installerFileJavaArgs.exists()) {
                                        Files.delete(installerFileJavaArgs.toPath());
                                }

                                return false;
                        } catch (IOException | InterruptedException e) {
                                LogWarning("Problem while installing Loader from " + installPath + File.separator + ' ' + e );
                                startupError = true;
                                return true;
                        }
                }else{
                        LogWarning("No \"libraries\"-Folders and no Installer-File could be found!");
                        startupError = true;
                        return true;
                }

        }
}

