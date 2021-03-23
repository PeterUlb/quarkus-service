package net.adultart.imageservice.service;

import net.adultart.imageservice.dto.ImageUploadRequestDto;
import net.adultart.imageservice.exception.UnsupportedImageException;

public interface ImageService {
    String createImageEntry(ImageUploadRequestDto imageUploadRequestDto, long accountId);

    void processImageAfterUpload(String imageKey) throws UnsupportedImageException;
}
