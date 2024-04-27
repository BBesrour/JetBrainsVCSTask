package com.example.jetbrainsvcstask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class GitHubService {

    private final String orgUrl;

    private final String accessToken;

    private String orgName;

    private List<String> reposUrl;

    private List<String> reposName;

    private List<Boolean> reposContainsHello;

    private static final String REPO_URLS_API = "https://api.github.com/orgs/%s/repos";

    private static final String REPO_README_CONTENTS_API = "https://api.github.com/repos/%s/%s/readme";

    private final String searchString;


    public GitHubService(String orgUrl, String accessToken, boolean ignoreCase, String searchString) throws IOException, URISyntaxException, NotFoundException {
        this.orgUrl = orgUrl;
        this.accessToken = accessToken;
        this.searchString = searchString;
        this.reposUrl = new ArrayList<>();
        this.reposName = new ArrayList<>();
        this.reposContainsHello = new ArrayList<>();
        setOrgNameFromUrl();
        setReposUrlAndName();
        setReposContainsHello(ignoreCase);
        if (this.reposUrl.size() != this.reposName.size() || this.reposUrl.size() != this.reposContainsHello.size()) {
            throw new RuntimeException("Number of repositories, repository names, and repository contents do not match");
        }
    }

    private void setOrgNameFromUrl() throws MalformedURLException, URISyntaxException {
        if (this.orgUrl == null || this.orgUrl.isEmpty()) {
            throw new MalformedURLException("URL is null or empty");
        }


        URL url = (new URI(this.orgUrl)).toURL();
        String path = url.getPath();
        if (path == null || path.isEmpty()) {
            throw new MalformedURLException("URL path is null or empty");
        }

        // Remove trailing slash if present
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        String[] pathParts = path.split("/");
        this.orgName = pathParts[pathParts.length - 1];
    }

    private void setReposUrlAndName() throws URISyntaxException, IOException, NotFoundException {
        // Send a request to GitHub API to get the list of repositories and set the repoUrls field
        String apiUrl = String.format(REPO_URLS_API, this.orgName);
        String response = sendRequest(apiUrl, this.accessToken, "application/vnd.github+json");

        JSONArray jsonArray = new JSONArray(response);
        for (int i = 0; i < jsonArray.length(); i++) {
            this.reposUrl.add(jsonArray.getJSONObject(i).getString("html_url"));
            this.reposName.add(jsonArray.getJSONObject(i).getString("name"));
        }
    }

    public void setReposContainsHello(boolean ignoreCase) throws URISyntaxException, IOException {
        for (String repoName : this.reposName) {
            String apiUrl = String.format(REPO_README_CONTENTS_API, this.orgName, repoName);
            try {
                String response = sendRequest(apiUrl, this.accessToken, "application/vnd.github+json");
                // convert the response to a JSON object
                JSONObject jsonObject = new JSONObject(response);

                // get the content field from the JSON object
                String content = jsonObject.getString("content");

                // remove new lines and decode the base64 encoded content
                content = content.replaceAll("\n", "");
                content = new String(java.util.Base64.getDecoder().decode(content));
                this.reposContainsHello.add(containsWithIgnoreCaseOption(content, searchString, ignoreCase));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e.getMessage());
            } catch (NotFoundException e) {
                this.reposContainsHello.add(false);
            }
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

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public List<String> getReposUrl() {
        return reposUrl;
    }

    public void setReposUrlAndName(List<String> repoUrls) {
        this.reposUrl = repoUrls;
    }

    public List<String> getReposName() {
        return reposName;
    }

    public void setReposName(List<String> reposName) {
        this.reposName = reposName;
    }

    public List<Boolean> getReposContainsHello() {
        return reposContainsHello;
    }

    public void setReposContainsHello(List<Boolean> reposContainsHello) {
        this.reposContainsHello = reposContainsHello;
    }

    public static String sendRequest(String url, String accessToken, String acceptHeader) throws IOException, URISyntaxException, NotFoundException {
        URL requestUrl = (new URI(url)).toURL();
        HttpURLConnection conn = (HttpURLConnection) requestUrl.openConnection();
        conn.setRequestMethod("GET");
        if (accessToken != null && !accessToken.isEmpty()) {
            conn.setRequestProperty("Authorization", "token " + accessToken);
        }
        if (acceptHeader != null && !acceptHeader.isEmpty()) {
            conn.setRequestProperty("Accept", acceptHeader);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new NotFoundException("Resource not found");
        } else if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed : HTTP error code : " + responseCode);
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
