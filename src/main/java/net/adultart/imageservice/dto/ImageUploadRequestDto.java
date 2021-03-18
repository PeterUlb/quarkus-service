package net.adultart.imageservice.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ImageUploadRequestDto {
    @NotNull
    private Long accountId;
    @NotBlank
    @Size(max = 255)
    private String title;
    @NotNull
    private String description;
    @NotBlank
    @Size(max = 255)
    private String fileName;
    @Max(10485760)
    private Long size;

    public ImageUploadRequestDto() {
    }

    public ImageUploadRequestDto(@NotNull Long accountId, @NotBlank String title, @NotNull String description, @NotBlank String fileName, @Max(10485760) Long size) {
        this.accountId = accountId;
        this.title = title;
        this.description = description;
        this.fileName = fileName;
        this.size = size;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }
}
