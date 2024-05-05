package com.example.jetbrainsvcstask.service;

import com.example.jetbrainsvcstask.domain.GitHubWebhook;
import com.example.jetbrainsvcstask.NotFoundException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class GitHubOrgService {

    private static final Logger logger = LoggerFactory.getLogger(GitHubOrgService.class);

    private final String orgUrl;

    private final String accessToken;

    private String orgName;

    private static final String REPO_URLS_API = "https://api.github.com/orgs/%s/repos";

    private static final String REPO_README_CONTENTS_API = "https://api.github.com/repos/%s/%s/readme";

    private static final String WEBHOOKS_API = "https://api.github.com/orgs/%s/hooks";


    public GitHubOrgService(String orgUrl, String accessToken) throws IOException, URISyntaxException {
        this.orgUrl = orgUrl;
        this.accessToken = accessToken;
        setOrgNameFromUrl();
    }

    public void setOrgNameFromUrl() throws MalformedURLException, URISyntaxException {
        if (this.orgUrl == null || this.orgUrl.isEmpty()) {
            logger.error("URL path is null or empty");
            throw new MalformedURLException("URL is null or empty");
        }


        URL url = (new URI(this.orgUrl)).toURL();
        String path = url.getPath();
        if (path == null || path.isEmpty()) {
            logger.error("URL path is null or empty");
            throw new MalformedURLException("URL path is null or empty");
        }

        // Remove trailing slash if present
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        String[] pathParts = path.split("/");
        this.orgName = pathParts[pathParts.length - 1];
    }

    public List<String> setReposName() throws URISyntaxException, IOException, NotFoundException {
        List<String> reposName = new ArrayList<>();
        // Send a request to GitHub API to get the list of repositories and set the repoUrls field
        String apiUrl = String.format(REPO_URLS_API, this.orgName);
        String response = sendGetRequest(apiUrl, this.accessToken, "application/vnd.github+json");

        JSONArray jsonArray = new JSONArray(response);
        for (int i = 0; i < jsonArray.length(); i++) {
            reposName.add(jsonArray.getJSONObject(i).getString("name"));
        }
        return reposName;
    }

    public List<Boolean> setReposContainsHello(List<String> reposName, String searchString, boolean ignoreCase) throws URISyntaxException, IOException {
        List<Boolean> reposContainsHello = new ArrayList<>();
        for (String repoName : reposName) {
            String apiUrl = String.format(REPO_README_CONTENTS_API, this.orgName, repoName);
            try {
                String response = sendGetRequest(apiUrl, this.accessToken, "application/vnd.github+json");
                // convert the response to a JSON object
                JSONObject jsonObject = new JSONObject(response);

                // get the content field from the JSON object
                String content = jsonObject.getString("content");

                // remove new lines and decode the base64 encoded content
                content = content.replaceAll("\n", "");
                content = new String(java.util.Base64.getDecoder().decode(content));
                reposContainsHello.add(containsWithIgnoreCaseOption(content, searchString, ignoreCase));
            } catch (IllegalArgumentException e) {
                logger.error("Error decoding base64 content", e);
                throw e;
            } catch (NotFoundException e) {
                logger.info("Error getting content for repository {}, setting containsHello to false. Error: {}", repoName, e.getMessage());
                reposContainsHello.add(false);
            }
        }
        return reposContainsHello;
    }

    public List<GitHubWebhook> getWebhooks() throws IOException {
        List<GitHubWebhook> webhooks = new ArrayList<>();
        // Get the list of webhooks for the organization
        String apiUrl = String.format(WEBHOOKS_API, this.orgName);
        try {
            String response = sendGetRequest(apiUrl, this.accessToken, "application/vnd.github+json");
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                // Convert the JSON object to a GitHubWebhook object using object mapper
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                GitHubWebhook webhook = objectMapper.readValue(jsonArray.getJSONObject(i).toString(), GitHubWebhook.class);
                webhooks.add(webhook);
            }
        } catch (IOException | URISyntaxException | NotFoundException e) {
            logger.error("Error getting webhooks", e);
            throw new IOException(e.getMessage());
        }
        return webhooks;
    }

    public GitHubWebhook createWebhook(GitHubWebhook webhook) throws IOException, NotFoundException, URISyntaxException {
        // Convert the GitHubWebhook object to JSON
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String json = mapper.writeValueAsString(webhook);
        try {
            // Send a POST request to create a new webhook
            String apiUrl = String.format(WEBHOOKS_API, this.orgName);
            String response = sendPostRequest(apiUrl, this.accessToken, "application/vnd.github+json", json.getBytes());
            // Convert the response to a GitHubWebhook object
            return mapper.readValue(response, GitHubWebhook.class);
        } catch (IOException | URISyntaxException | NotFoundException e) {
            logger.error("Error creating webhook", e);
            throw e;
        }
    }

    public String getOrgUrl() {
        return orgUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getOrgName() {
        return orgName;
    }


    // Helper methods

    // Send a GET request to the specified URL
    public static String sendGetRequest(String url, String accessToken, String acceptHeader) throws IOException, URISyntaxException, NotFoundException {
        return sendRequest("GET", url, accessToken, acceptHeader, null);
    }


    // Send a POST request to the specified URL
    public static String sendPostRequest(String url, String accessToken, String acceptHeader, byte[] body) throws IOException, URISyntaxException, NotFoundException {
        return sendRequest("POST", url, accessToken, acceptHeader, body);
    }

    private static String sendRequest(String method, String url, String accessToken, String acceptHeader, byte[] body) throws IOException, URISyntaxException, NotFoundException {
        URL requestUrl = (new URI(url)).toURL();
        HttpURLConnection conn = (HttpURLConnection) requestUrl.openConnection();
        conn.setRequestMethod(method);
        if (accessToken != null && !accessToken.isEmpty()) {
            conn.setRequestProperty("Authorization", "token " + accessToken);
        }
        if (acceptHeader != null && !acceptHeader.isEmpty()) {
            conn.setRequestProperty("Accept", acceptHeader);
        }
        if (body != null) {
            conn.setDoOutput(true);
            conn.getOutputStream().write(body);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new NotFoundException("Resource not found");
        } else if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_CREATED) {
            throw new IOException("Failed : HTTP error code : " + responseCode);
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        StringBuilder response = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            response.append(output);
        }

        conn.disconnect();
        return response.toString();
    }

    // Taken from stackoverflow: https://stackoverflow.com/questions/14018478/string-contains-ignore-case
    // We could also use Apache Commons Lang library to achieve the same
    public static boolean containsWithIgnoreCaseOption(String str, String searchStr, boolean ignoreCase) {
        if (str == null || searchStr == null) return false;

        final int length = searchStr.length();
        if (length == 0)
            return true;

        for (int i = str.length() - length; i >= 0; i--) {
            if (str.regionMatches(ignoreCase, i, searchStr, 0, length))
                return true;
        }
        return false;
    }
}
