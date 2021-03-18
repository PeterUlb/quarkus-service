package net.adultart.imageservice.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String externalKey;

    @Column(nullable = false)
    private Long accountId;

    @Column
    private String title;

    @Column
    private String description;

    @Column(nullable = false)
    private String fileName;

    @Column
    private String extension;

    @Column
    private String mimeType;

    @Column
    private Long size;

    @Column
    private Long width;

    @Column
    private Long height;

    @Enumerated(EnumType.STRING)
    private ImageStatus imageStatus;

    @Column(nullable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, insertable = false)
    private LocalDateTime updatedAt;

    public static Image withInitialState(String externalKey, Long accountId, String title, String description, String fileName, Long size) {
        Image image = new Image();
        image.setExternalKey(externalKey);
        image.setAccountId(accountId);
        image.setTitle(title);
        image.setDescription(description);
        image.setFileName(fileName);
        image.setSize(size);
        image.setImageStatus(ImageStatus.REQUESTED);
        return image;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalKey() {
        return externalKey;
    }

    public void setExternalKey(String externalKey) {
        this.externalKey = externalKey;
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

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getWidth() {
        return width;
    }

    public void setWidth(Long width) {
        this.width = width;
    }

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) {
        this.height = height;
    }

    public ImageStatus getImageStatus() {
        return imageStatus;
    }

    public void setImageStatus(ImageStatus imageStatus) {
        this.imageStatus = imageStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
