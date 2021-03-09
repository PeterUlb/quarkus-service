package net.adultart.imageservice.config;

import io.quarkus.arc.config.ConfigProperties;

@ConfigProperties(prefix = "aws")
public class AwsConfig {
    private String region;
    private String accessKeyId;
    private String secretAccessKey;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }
}
