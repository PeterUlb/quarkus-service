package net.adultart.imageservice.dto;

import java.util.List;
import java.util.Map;

public class ImageUploadInfoDto {
    private String awsUrl;
    private String httpMethod;
    private Map<String, List<String>> headers;

    public ImageUploadInfoDto(String awsUrl, String httpMethod, Map<String, List<String>> headers) {
        this.awsUrl = awsUrl;
        this.httpMethod = httpMethod;
        this.headers = headers;
    }

    public String getAwsUrl() {
        return awsUrl;
    }

    public void setAwsUrl(String awsUrl) {
        this.awsUrl = awsUrl;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }
}
