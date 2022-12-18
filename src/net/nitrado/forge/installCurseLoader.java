package net.nitrado.forge;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.nitrado.forge.Until.LogInfo;

public class installCurseLoader {


        public static boolean installCurseLoader( String installerFile ) {

                /*

                TODO: getFileList

                try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir))) {
                        for (Path path : stream) {
                                if (!Files.isDirectory(path)) {
                                        fileSet.add(path.getFileName()
                                                .toString());
                                }
                        }
                }
                return fileSet;

                 */


                Pattern pattern = Pattern.compile("forge-([.0-9]+)-([.0-9]+)-([universal|installer]+).([jar|zip]+)" , Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher("Visit W3Schools!");
                boolean matchFound = matcher.find();
                if( matchFound ) {
                        System.out.println("Match found");
                } else {
                        System.out.println("Match not found");
                }


                try {
                        final String filename = new File(installerFile).getName();
                        final String basePath = installerFile.replace(filename,"");
                        LogInfo("Attempting to start Server " + installerFile);
                        LogInfo("Filename: " +  filename);
                        LogInfo("Directory: " + basePath);
                        LogInfo("Attempting to use installer from " + basePath);
                        LogInfo("Starting installation of Loader, installer output incoming");
                        LogInfo("Check log from installer for more information");
                        final Process start;
                        final Process installer = start = new ProcessBuilder(new String[]{"java", "-jar", filename, "nogui", "--installServer"}).directory(new File(basePath)).start();
                        final Scanner serverLog = new Scanner(start.getInputStream());
                        while (serverLog.hasNextLine()) {
                                final String println = serverLog.nextLine();
                                LogInfo(println);
                        }
                        installer.waitFor();
                        LogInfo("Done installing loader, deleting installer!");
                        LogInfo("Delete: " + installer + ' ');
                        final File installerFile2 = new File( installerFile );
                        if (installerFile2.exists()) {
                                Files.delete(installerFile2.toPath());
                        }
                        final File installerFileLog = new File(installerFile2 + ".log");
                        if (installerFileLog.exists()) {
                                Files.delete(installerFileLog.toPath());
                        }
                        final File installerFileRunBat = new File( basePath + "run.bat");
                        if (installerFileRunBat.exists()) {
                                Files.delete(installerFileRunBat.toPath());
                        }
                        final File installerFileRunSh = new File( basePath +"run.sh");
                        if (installerFileRunSh.exists()) {
                                Files.delete(installerFileRunSh.toPath());
                        }
                        final File installerFileJavaArgs = new File( basePath + "user_jvm_args.txt");
                        if (installerFileJavaArgs.exists()) {
                                Files.delete(installerFileJavaArgs.toPath());
                        }
                        return true;
                } catch (IOException | InterruptedException e) {
                        LogInfo("Problem while installing Loader from " + installerFile + ' ' + e );
                        return false;
                }
        }
}

