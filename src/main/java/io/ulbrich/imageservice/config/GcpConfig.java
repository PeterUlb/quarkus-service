package io.ulbrich.imageservice.config;

import io.quarkus.arc.config.ConfigProperties;

import java.util.Optional;

@ConfigProperties(prefix = "gcp")
public class GcpConfig {

    private String projectId;
    private Credentials credentials;
    private PubSub pubSub;
    private CloudStorage cloudStorage;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public PubSub getPubSub() {
        return pubSub;
    }

    public void setPubSub(PubSub pubSub) {
        this.pubSub = pubSub;
    }

    public CloudStorage getCloudStorage() {
        return cloudStorage;
    }

    public void setCloudStorage(CloudStorage cloudStorage) {
        this.cloudStorage = cloudStorage;
    }

    public static class Credentials {
        private String location;

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }

    public static class PubSub {
        private Optional<String> endpointOverride;

        public Optional<String> getEndpointOverride() {
            return endpointOverride;
        }

        public void setEndpointOverride(Optional<String> endpointOverride) {
            this.endpointOverride = endpointOverride;
        }
    }

    public static class CloudStorage {
        private Optional<String> endpointOverride;

        public Optional<String> getEndpointOverride() {
            return endpointOverride;
        }

        public void setEndpointOverride(Optional<String> endpointOverride) {
            this.endpointOverride = endpointOverride;
        }
    }
}
