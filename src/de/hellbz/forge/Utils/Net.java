package de.hellbz.forge.Utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import static de.hellbz.forge.Utils.Data.LogDebug;
import static de.hellbz.forge.Utils.Data.LogInfo;
import static de.hellbz.forge.Utils.Data.LogWarning;

public class Net {

    public static boolean isConnected;

    static {
        if (!Config.isNetworkCheckEnabled()) {
            isConnected = false;
            LogInfo("Checking Internet... Skipped (network_check=false in " + Config.PROPERTIES_FILE + ").");
        } else {
            checkInternetConnection();
            if (isConnected) {
                LogInfo("Checking Internet... Connected to the Web.");
            } else {
                LogWarning("Checking Internet-Connection... Connection Failed.");
            }
        }
    }

    public Net() {
        isConnected = false;
    }

    public static void checkInternetConnection() {
        String[] hosts = {"1.1.1.1", "8.8.8.8"};

        for (String host : hosts) {
            if (canReachHost(host, 443)) {
                isConnected = true;
                return;
            }
        }
    }

    private static boolean canReachHost(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 1500);
            LogDebug("Host " + host + " is reachable on port " + port);
            return true;
        } catch (IOException e) {
            LogDebug("Could not reach " + host + " on port " + port + ": " + e.getMessage());
            return false;
        }
    }

    public static boolean isInternetConnected() {
        return isConnected;
    }

}