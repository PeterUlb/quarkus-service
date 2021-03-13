package net.adultart.imageservice.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;

public class ImageUploadRequestDto {
    @Max(10485760)
    private long bytes;
    @NotBlank
    private String fileName;

    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}