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

    public FileOperation(int responseCode, Object content, Object additionalData) {
        this.responseCode = responseCode;
        this.content = content;
        this.additionalData = additionalData;
    }

    public int getResponseCode() { return responseCode; }
    public Object getContent() { return content; }
    public Object getAdditionalData() { return additionalData; }

    /**
     * Returns true if the given path/URL points to a binary file (JAR/ZIP)
     * that must be saved as raw bytes and NOT decoded as text.
     */
    private static boolean isBinaryPath(String path) {
        if (path == null) return false;
        String lower = path.toLowerCase();
        return lower.endsWith(".jar") || lower.endsWith(".zip");
    }

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
            if (connection != null) connection.disconnect();
        }
    }

    public static FileOperation downloadOrReadFile(File source) {
        return downloadOrReadFile(source, null);
    }

    public static FileOperation downloadOrReadFile(File source, String destinationPath) {
        try (InputStream in = Files.newInputStream(source.toPath())) {
            long size = Files.size(source.toPath());
            return readContent(in, destinationPath, isBinaryPath(source.getName()), size);
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
     * Binary files (JAR, ZIP) are saved as raw bytes to avoid corruption.
     * Text files (JSON, conf, xml) are decoded as UTF-8 text.
     *
     * Fixes:
     * - Issue #19: proper HTTP error codes via HttpURLConnection + User-Agent header
     * - Binary corruption: JAR/ZIP are streamed as bytes, not decoded as UTF-8 text
     */
    public static FileOperation downloadOrReadFile(String source, String destinationPath) {
        boolean isUrl = source.toLowerCase().startsWith("http://") || source.toLowerCase().startsWith("https://");

        if (isUrl) {
            return downloadFromUrl(source, destinationPath);
        }

        // Classpath resource or local file
        try {
            InputStream in = FileOperation.class.getResourceAsStream(source);
            long size = -1;
            if (in == null) {
                in = Files.newInputStream(Paths.get(source));
                size = Files.size(Paths.get(source));
            }
            return readContent(in, destinationPath, isBinaryPath(source), size);
        } catch (IOException e) {
            return new FileOperation(500, null, "File-Operation failed: " + e.getMessage());
        }
    }

    /**
     * Downloads a file from URL with proper HTTP handling.
     * Binary files (JAR, ZIP) are saved using raw byte streaming.
     * Retries automatically on 5xx server errors.
     */
    private static FileOperation downloadFromUrl(String urlString, String destinationPath) {
        int maxRetries = 2;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            FileOperation result = downloadFromUrlOnce(urlString, destinationPath, attempt);
            int code = result.getResponseCode();
            if (code < 500 || attempt == maxRetries) {
                return result;
            }
            Data.LogWarning("Download attempt " + attempt + " returned HTTP " + code + ", retrying ...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
        }
        return new FileOperation(500, null, "Download failed after " + maxRetries + " attempts");
    }

    private static FileOperation downloadFromUrlOnce(String urlString, String destinationPath, int attempt) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent",
                    "Forge-Server-Starter/3.6 (Java/" + System.getProperty("java.version") + ")");
            connection.setRequestProperty("Accept", "application/java-archive, application/zip, application/json, text/plain, */*");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(60000); // JAR downloads can be slow
            connection.setInstanceFollowRedirects(true);

            int responseCode = connection.getResponseCode();

            if (responseCode >= 200 && responseCode < 300) {
                boolean binary = isBinaryPath(urlString) || isBinaryPath(destinationPath);
                long size = connection.getContentLengthLong();
                try (InputStream in = connection.getInputStream()) {
                    return readContent(in, destinationPath, binary, size);
                }
            } else {
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
                return new FileOperation(responseCode, null,
                        "HTTP error: " + responseCode + (errorBody != null ? " - " + errorBody : ""));
            }
        } catch (IOException e) {
            int code = 500;
            if (connection != null) {
                try { code = connection.getResponseCode(); } catch (IOException ignored) {}
            }
            return new FileOperation(code, null, "Download failed: " + e.getMessage());
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    public static FileOperation downloadOrReadFile(String source, String destinationPath, long maxAge) {
        if (maxAge > 0) {
            File file = new File(destinationPath);
            if (file.exists() && (System.currentTimeMillis() - file.lastModified() < maxAge)) {
                return downloadOrReadFile(file);
            }
        }
        return downloadOrReadFile(source, destinationPath);
    }

    /**
     * Reads content from an InputStream.
     *
     * @param in              the input stream
     * @param destinationPath where to save the file, or null to just return content
     * @param binary          if true, streams raw bytes (for JAR/ZIP); if false, decodes as UTF-8 text
     * @param totalSize       total content length in bytes, or -1 if unknown
     */
    private static FileOperation readContent(InputStream in, String destinationPath, boolean binary, long totalSize) throws IOException {
        if (binary && destinationPath != null && !destinationPath.isEmpty()) {
            // Binary mode: stream raw bytes directly to file, no String conversion
            File dest = new File(destinationPath);
            dest.getParentFile().mkdirs();
            try (OutputStream out = new FileOutputStream(dest)) {
                byte[] buf = new byte[8192];
                int read;
                long total = 0;
                long lastLog = 0;
                long lastMbLog = -1;
                while ((read = in.read(buf)) != -1) {
                    out.write(buf, 0, read);
                    total += read;

                    // Progress logging
                    if (totalSize > 0) {
                        long percent = (total * 100) / totalSize;
                        if (percent >= lastLog + 10) {
                            Data.LogInfo("Download progress: " + percent + "% (" + (total / 1024 / 1024) + " / " + (totalSize / 1024 / 1024) + " MB)");
                            lastLog = percent;
                        }
                    } else {
                        long mb = total / 1024 / 1024;
                        if (mb > lastMbLog) {
                            Data.LogInfo("Downloaded: " + mb + " MB ...");
                            lastMbLog = mb;
                        }
                    }
                }
                return new FileOperation(200, "Binary file saved (" + total + " bytes)", "File downloaded and saved");
            }
        }

        // Text mode: decode as UTF-8
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
