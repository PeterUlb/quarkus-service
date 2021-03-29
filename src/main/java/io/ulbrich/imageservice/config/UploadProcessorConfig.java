package io.ulbrich.imageservice.config;

import io.quarkus.arc.config.ConfigProperties;

@ConfigProperties(prefix = "upload.processor")
public class UploadProcessorConfig {
    private String bucket;
    private String subscriptionName;
    private int poolSize;
    private long queueSize;

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public void setSubscriptionName(String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public long getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(long queueSize) {
        this.queueSize = queueSize;
    }
}
