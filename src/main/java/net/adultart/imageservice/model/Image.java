package net.adultart.imageservice.model;

import javax.persistence.*;

@Entity
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String externalKey;

    @Enumerated(EnumType.STRING)
    private ImageStatus imageStatus;

    public static Image withInitialState(String externalKey) {
        Image image = new Image();
        image.setExternalKey(externalKey);
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

    public ImageStatus getImageStatus() {
        return imageStatus;
    }

    public void setImageStatus(ImageStatus imageStatus) {
        this.imageStatus = imageStatus;
    }
}
