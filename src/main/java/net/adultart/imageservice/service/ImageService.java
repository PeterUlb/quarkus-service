package net.adultart.imageservice.service;

import net.adultart.imageservice.dto.ImageUploadRequestDto;
import net.adultart.imageservice.exception.UnsupportedImageException;

import java.util.Set;

public interface ImageService {
    String createImageEntry(ImageUploadRequestDto imageUploadRequestDto, long accountId);

    void processImageAfterUpload(String imageKey) throws UnsupportedImageException;

    Set<String> getSignedUrlsByTag(String tag);
}
