package net.adultart.imageservice.config;

import io.quarkus.arc.config.ConfigProperties;

import javax.validation.constraints.Max;

@ConfigProperties(prefix = "upload.processor")
public class UploadProcessorConfig {
    private int poolSize;
    private int queueSize;
    private int pollDelay;
    @Max(20)
    private int longPollingWait;

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

    public int getPollDelay() {
        return pollDelay;
    }

    public void setPollDelay(int pollDelay) {
        this.pollDelay = pollDelay;
    }

    public int getLongPollingWait() {
        return longPollingWait;
    }

    public void setLongPollingWait(int longPollingWait) {
        this.longPollingWait = longPollingWait;
    }
}
