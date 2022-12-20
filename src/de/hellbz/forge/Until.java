package de.hellbz.forge;

import java.awt.*;
import java.io.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

public class Until {

    // Define Text Colors
    public static final String TXT_RESET =  "\u001B[0m";
    public static final String TXT_BLACK =  "\u001B[30m";
    public static final String TXT_RED =    "\u001B[31m";
    public static final String TXT_GREEN =  "\u001B[32m";
    public static final String TXT_YELLOW = "\u001B[33m";
    public static final String TXT_BLUE =   "\u001B[34m";
    public static final String TXT_PURPLE = "\u001B[35m";
    public static final String TXT_CYAN =   "\u001B[36m";
    public static final String TXT_WHITE =  "\u001B[37m";

    //Define Background Colors
    public static final String BG_BLACK =   "\u001B[40m";
    public static final String BG_RED =     "\u001B[41m";
    public static final String BG_GREEN =   "\u001B[42m";
    public static final String BG_YELLOW =  "\u001B[43m";
    public static final String BG_BLUE =    "\u001B[44m";
    public static final String BG_PURPLE =  "\u001B[45m";
    public static final String BG_CYAN =    "\u001B[46m";
    public static final String BG_WHITE =   "\u001B[47m";

    public static void LogInfo(final String message) {

        System.out.println( CurrentTime() + TXT_GREEN + "[F-S-S/INFO] " + TXT_RESET + message );
        try {
            Until.doLog( CurrentTime() + "[F-S-S/INFO] " + cleanLog(message) );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void LogWarning(final String message) {

        System.out.println( CurrentTime() + TXT_YELLOW + "[F-S-S/WARNING] " + TXT_RESET + message );
        try {
            Until.doLog( CurrentTime() + "[F-S-S/WARNING] " + cleanLog(message) );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void LogError(final String message) {

        System.out.println( CurrentTime() + TXT_RED + "[F-S-S/ERROR] " + TXT_RESET + message );
        try {
            Until.doLog( CurrentTime() + "[F-S-S/ERROR] " + cleanLog(message) );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void LogDebug(final String message) {
        if (Objects.equals(ServerStarter.configProps.getProperty("debug"), "true")) {
            System.out.println(CurrentTime() + TXT_CYAN + "[F-S-S/DEBUG] " + TXT_RESET + message);
            try {
                Until.doLog( CurrentTime() + "[F-S-S/DEBUG] " + cleanLog(message) );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String CurrentTime() {
        if (!ServerStarter.configProps.getProperty("timezone").equals("UTC")) {
            ZoneId z = ZoneId.of(ServerStarter.configProps.getProperty("timezone"));
            return "[" + ZonedDateTime.now(z).format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT)) + "] ";
        }else{
            return "[" + ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT)) + "] ";
        }
    }

    private static String cleanLog(String message) {
        return message.replaceAll("\\x1b\\[[\\d;]*m", "");
    }

    public static void doLog(final String message) throws IOException {
        if (Objects.equals( ServerStarter.configProps.getProperty("log_to_file"), "true")) {
            FileWriter fileWriter = new FileWriter("logs/server-starter.log", true); //Set true for append mode
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(message);  //New line
            printWriter.close();
        }
    }

    static boolean isReallyHeadless() {
        if (GraphicsEnvironment.isHeadless()) {
            return true;
        }
        try {
            GraphicsDevice[] screenDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
            return screenDevices == null || screenDevices.length == 0;
        } catch (HeadlessException e) {
            e.printStackTrace();
            return true;
        }
    }
}
