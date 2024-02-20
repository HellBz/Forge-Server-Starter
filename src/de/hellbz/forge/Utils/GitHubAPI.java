package de.hellbz.forge.Utils;

public class GitHubAPI {


    public static void main(String[] args) {

        // Example call with different parameters
        String repository = "repos/HellBz/Forge-Server-Starter/commits";
        String array_get = "0/commit/committer/date";
        String path = "res/version.xml";
        String result = GitHubAPI.sendRequest(repository, array_get, path);

        System.out.println("Result: " + result);
    }


    public static String sendRequest(String endpoint) {
        return sendRequest(endpoint, null, null);
    }

    public static String sendRequest(String endpoint, String array_get) {
        return sendRequest(endpoint, array_get, null);
    }

    public static String sendRequest(String endpoint, String array_get, String path) {

        // Erstelle die URL zur GitHub-API
        StringBuilder apiUrlBuilder = new StringBuilder("https://api.github.com/");

        /*
        String[] endpointParts = endpoint.split("/");
        for (String part : endpointParts) {
            apiUrlBuilder.append("/").append(part);
        }
        */

        if (endpoint == null || endpoint.isEmpty()) {
            return "Ungültiger Endpunkt";
        } else {
            apiUrlBuilder.append(endpoint);
        }

        if (path != null && !path.isEmpty()) {
            apiUrlBuilder.append("?path=").append(path);
        }

        String apiUrl = apiUrlBuilder.toString();

        // Andernfalls versuche, den Inhalt herunterzuladen oder zu lesen,
        FileOperation fileOperationResult = FileOperation.downloadOrReadFile(apiUrl);
        if (fileOperationResult.getResponseCode() == 200) {
            if (array_get != null && !array_get.isEmpty()) {
                return Data.getJsonValue((String) fileOperationResult.getContent(), "0/commit/committer/date");
            } else {
                return (String) fileOperationResult.getContent();
            }

        } else {
            return "Error Interact with GitHub-API: Error-Code " + fileOperationResult.getResponseCode();
        }
    }
}