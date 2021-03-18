package net.adultart.imageservice.service;

import net.adultart.imageservice.dto.ImageUploadRequestDto;
import net.adultart.imageservice.model.ImageStatus;

public interface ImageService {
    String createImageEntry(ImageUploadRequestDto imageUploadRequestDto);

    void updateImageState(String externalKey, ImageStatus imageStatus);
}
