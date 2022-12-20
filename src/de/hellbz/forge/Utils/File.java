package de.hellbz.forge.Utils;

import de.hellbz.forge.ServerStarter;
import de.hellbz.forge.Utils.Data;

import java.io.*;
import java.util.Objects;
import java.util.Properties;

public class File {

    public static void Eula() throws IOException {

        java.io.File eulaFile = new java.io.File("eula.txt");

        boolean eulaFileWrite = false;

        if (eulaFile.exists()) {
            Properties eulaProps = new Properties();
            FileReader eulaReader = new FileReader(eulaFile);
            // load the properties file and close
            eulaProps.load(eulaReader);
            eulaReader.close();

            if (Objects.equals(eulaProps.getProperty("eula"), "false")) {
                eulaFileWrite = true;
                Data.LogInfo("Set Eula-File to true");
            } else {
                Data.LogInfo("Eula-File is already accepted.");
            }
        } else {
            if (eulaFile.createNewFile()) {
                eulaFileWrite = true;
                Data.LogInfo("Creating a new Eula-File");
            }
        }

        if (eulaFileWrite) {
            FileWriter eulaWriter = new FileWriter(eulaFile);
            Properties eulaPropsNew = new Properties();
            eulaPropsNew.setProperty("eula", "true");
            eulaPropsNew.store(eulaWriter, "By changing the setting below to TRUE you are indicating your agreement to our EULA (https://account.mojang.com/documents/minecraft_eula).");
            eulaWriter.close();
        }
    }

    public static void StartFile() throws IOException {

        if (ServerStarter.OS.contains("win")) {
            Data.LogInfo("Creating Windows Start-File.");
            PrintWriter StartFileWriter = new PrintWriter("start_server.bat", "UTF-8");
            StartFileWriter.println("@echo off");
            StartFileWriter.println("java -jar minecraft_server.jar -Xmx1024M -Xms1024M nogui");
            StartFileWriter.println("pause");
            StartFileWriter.close();

        } else {
            Data.LogInfo("Creating UNIX Start-File.");
            PrintWriter StartFileWriter = new PrintWriter("start_server.sh", "UTF-8");
            StartFileWriter.println("java -jar minecraft_server.jar -Xmx1024M -Xms1024M nogui");
            StartFileWriter.close();
        }
    }

    public static void ConfigFile() throws IOException {

        java.io.File configFile = new java.io.File("server_starter.conf");

        if (configFile.exists()) {

            FileReader configReader = new FileReader(configFile);
            ServerStarter.configProps = new Properties();
            ServerStarter.configProps.load(configReader);

            configReader.close();

        } else {
            if (configFile.createNewFile()) {

                FileWriter writerConfig = new FileWriter(configFile);

                ServerStarter.configProps = new Properties();
                ServerStarter.configProps.setProperty("debug", "false");
                ServerStarter.configProps.setProperty("log_to_file", "false");
                ServerStarter.configProps.setProperty("timezone", "UTC");
                ServerStarter.configProps.setProperty("java_path", "java");

                ServerStarter.configProps.store(writerConfig, "Forge Server-Starter Configuration");

                writerConfig.close();
            }
        }
    }

    public static void LogFile(){

        if (Objects.equals( ServerStarter.configProps.getProperty("log_to_file"), "true")) {

            java.io.File directory = new java.io.File("logs/");
            if (!directory.exists()) {
                directory.mkdir();
                // If you require it to make the entire directory path including parents,
                // use directory.mkdirs(); here instead.
            }

            if (directory.exists()) {

                java.io.File file = new java.io.File("logs/server-starter.log");

                if (file.exists() && file.isFile()) {
                    file.delete();
                }

                if (!file.exists()) {

                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }
}
