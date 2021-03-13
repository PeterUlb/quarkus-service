package net.adultart.imageservice.config;

import io.quarkus.arc.config.ConfigProperties;
import org.hibernate.validator.constraints.URL;

@ConfigProperties(prefix = "aws.image")
public class AwsImageConfig {
    private String bucket;
    @URL
    private String createdQueueUrl;

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getCreatedQueueUrl() {
        return createdQueueUrl;
    }

    public void setCreatedQueueUrl(String createdQueueUrl) {
        this.createdQueueUrl = createdQueueUrl;
    }
}
