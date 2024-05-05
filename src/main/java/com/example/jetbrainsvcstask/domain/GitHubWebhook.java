package com.example.jetbrainsvcstask.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// POJO class for GitHub Webhook
public class GitHubWebhook {

    private String name;
    private Config config;
    private List<String> events;
    private boolean active;
    private String type;
    @JsonProperty("created_at")
    private String createdAt;

    public static class Config {
        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getConfigUrl() {
            return configUrl;
        }

        public void setConfigUrl(String configUrl) {
            this.configUrl = configUrl;
        }

        @JsonProperty("url")
        private String configUrl;
        @JsonProperty("content_type")
        private String contentType;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        private String secret;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public List<String> getEvents() {
        return events;
    }

    public void setEvents(List<String> events) {
        this.events = events;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }


}
