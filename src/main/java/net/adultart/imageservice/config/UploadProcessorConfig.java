package net.adultart.imageservice.config;

import io.quarkus.arc.config.ConfigProperties;

@ConfigProperties(prefix = "upload.processor")
public class UploadProcessorConfig {
    private int poolSize;
    private int queueSize;
    private String pollDelay;

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public String getPollDelay() {
        return pollDelay;
    }

    public void setPollDelay(String pollDelay) {
        this.pollDelay = pollDelay;
    }
}
