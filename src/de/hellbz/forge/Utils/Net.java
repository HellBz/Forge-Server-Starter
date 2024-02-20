package de.hellbz.forge.Utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import static de.hellbz.forge.Utils.Data.LogInfo;
import static de.hellbz.forge.Utils.Data.LogWarning;

public class Net {

    public static boolean isConnected;

    static {

        checkInternetConnection();
        if (isConnected)
            LogInfo("Checking Internet... Connected to the Web.");
        else
            LogWarning("Checking Internet-Connection... Connection Failed.");
    }

    public Net() {
        isConnected = false;
    }

    public static void checkInternetConnection() {
        String[] hosts = {"www.google.com", "www.github.com"};

        for (String host : hosts) {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(host, 80), 1000);
                socket.close();
                isConnected = true;
                break; // Verbindung erfolgreich, beende die Schleife
            } catch (IOException e) {
                // Verbindung fehlgeschlagen, versuche den nächsten Host
            }
        }
    }

    public static boolean isInternetConnected() {
        return isConnected;
    }

}