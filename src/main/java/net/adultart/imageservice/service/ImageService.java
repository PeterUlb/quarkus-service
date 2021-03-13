package net.adultart.imageservice.service;

import net.adultart.imageservice.model.ImageStatus;

public interface ImageService {
    String createImageEntry(String imageName, long imageSize);
    void updateImageState(String externalKey, ImageStatus imageStatus);
}
