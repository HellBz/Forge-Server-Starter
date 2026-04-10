package de.hellbz.forge.Utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileOperation {

    private final int responseCode;
    private final Object content;
    private final Object additionalData;

    // Constructor
    public FileOperation(int responseCode, Object content, Object additionalData) {
        this.responseCode = responseCode;
        this.content = content;
        this.additionalData = additionalData;
    }

    // Getter methods
    public int getResponseCode() {
        return responseCode;
    }

    public Object getContent() {
        return content;
    }

    public Object getAdditionalData() {
        return additionalData;
    }

    /**
     * Calls a URL without waiting for a response.
     *
     * @param urlString The URL as a String.
     * @return A FileOperation object with the status of the operation.
     */
    public static FileOperation callUrlWithoutResponse(String urlString) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                return new FileOperation(responseCode, "URL successfully called, response ignored.", null);
            } else {
                return new FileOperation(responseCode, null, "Server returned an error.");
            }
        } catch (IOException e) {
            return new FileOperation(500, null, "URL call failed: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static FileOperation downloadOrReadFile(File source) {
        return downloadOrReadFile(source, null);
    }

    public static FileOperation downloadOrReadFile(File source, String destinationPath) {
        try (InputStream in = Files.newInputStream(source.toPath())) {
            return readFileContent(in, destinationPath);
        } catch (IOException e) {
            return new FileOperation(500, null, "File-Operation failed: " + e.getMessage());
        }
    }

    public static FileOperation downloadOrReadFile(String source) {
        return downloadOrReadFile(source, null);
    }

    /**
     * Downloads a file from a URL or reads it from classpath/filesystem.
     *
     * Fix for Issue #19 / #2: HTTP connections now properly report the actual
     * HTTP response code instead of always returning 500 on any error.
     * A User-Agent header is set to avoid 403/500 from servers that block
     * headless Java clients (e.g. GitHub raw, minecraftforge.net).
     */
    public static FileOperation downloadOrReadFile(String source, String destinationPath) {
        boolean isUrl = source.toLowerCase().startsWith("http://") || source.toLowerCase().startsWith("https://");

        if (isUrl) {
            return downloadFromUrl(source, destinationPath);
        }

        // Classpath resource or local file
        try {
            InputStream in = FileOperation.class.getResourceAsStream(source);
            if (in == null) {
                in = Files.newInputStream(Paths.get(source));
            }
            return readFileContent(in, destinationPath);
        } catch (IOException e) {
            return new FileOperation(500, null, "File-Operation failed: " + e.getMessage());
        }
    }

    /**
     * Downloads a file from a URL with proper HTTP handling.
     * Sets a User-Agent to avoid being blocked by CDNs and properly
     * returns the actual HTTP response code on errors.
     */
    private static FileOperation downloadFromUrl(String urlString, String destinationPath) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Forge-Server-Starter/3.6 (Java/" + System.getProperty("java.version") + ")");
            connection.setRequestProperty("Accept", "application/json, text/plain, */*");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            connection.setInstanceFollowRedirects(true);

            int responseCode = connection.getResponseCode();

            if (responseCode >= 200 && responseCode < 300) {
                try (InputStream in = connection.getInputStream()) {
                    return readFileContent(in, destinationPath);
                }
            } else {
                // Return actual HTTP error code, not 500
                String errorBody = null;
                try (InputStream errStream = connection.getErrorStream()) {
                    if (errStream != null) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(errStream, StandardCharsets.UTF_8));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) sb.append(line).append("\n");
                        errorBody = sb.toString();
                    }
                }
                return new FileOperation(responseCode, null, "HTTP error: " + responseCode + (errorBody != null ? " - " + errorBody : ""));
            }
        } catch (IOException e) {
            int code = 500;
            if (connection != null) {
                try { code = connection.getResponseCode(); } catch (IOException ignored) {}
            }
            return new FileOperation(code, null, "Download failed: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Downloads or reads a file with optional cache support.
     *
     * @param source          URL or file path
     * @param destinationPath Local path to save or check the file
     * @param maxAge          Maximum age of the file cache in milliseconds, 0 = no caching
     * @return FileOperation object with status and content or error info
     */
    public static FileOperation downloadOrReadFile(String source, String destinationPath, long maxAge) {
        if (maxAge > 0) {
            File file = new File(destinationPath);
            if (file.exists() && (System.currentTimeMillis() - file.lastModified() < maxAge)) {
                return downloadOrReadFile(file);
            }
        }
        return downloadOrReadFile(source, destinationPath);
    }

    // Helper method to read file content from InputStream
    private static FileOperation readFileContent(InputStream in, String destinationPath) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append("\n");
        }

        if (destinationPath != null && !destinationPath.isEmpty()) {
            Files.write(Paths.get(destinationPath), content.toString().getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return new FileOperation(200, content.toString(), "File downloaded and saved");
        } else {
            return new FileOperation(200, content.toString(), "Read File from Cache");
        }
    }
}
