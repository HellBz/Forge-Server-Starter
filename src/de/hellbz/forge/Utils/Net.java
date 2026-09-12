package de.hellbz.forge.Utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.*;

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
            if (isConnected)
                LogInfo("Checking Internet... Connected to the Web.");
            else
                LogWarning("Checking Internet-Connection... Connection Failed.");
        }
    }

    public Net() {
        isConnected = false;
    }

    public static void checkInternetConnection() {
        String[] hosts = {"www.google.com", "www.github.com"};
        ExecutorService executor = Executors.newFixedThreadPool(hosts.length);

        try {
            for (String host : hosts) {
                Future<Boolean> future = executor.submit(() -> canReachHost(host));
                try {
                    if (future.get(2, TimeUnit.SECONDS)) {
                        isConnected = true;
                        return;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    LogDebug("Could not reach " + host + ": " + e.getMessage());
                } catch (TimeoutException e) {
                    LogDebug("Timeout while checking " + host);
                    future.cancel(true);
                }
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private static Boolean canReachHost(String host) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, 80), 1500);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean isInternetConnected() {
        return isConnected;
    }

}