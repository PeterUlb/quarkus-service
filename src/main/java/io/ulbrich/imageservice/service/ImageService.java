package io.ulbrich.imageservice.service;

import io.ulbrich.imageservice.dto.ImageUploadRequestDto;
import io.ulbrich.imageservice.exception.UnsupportedImageException;

import java.util.Set;

public interface ImageService {
    String createImageEntry(ImageUploadRequestDto imageUploadRequestDto, long accountId);

    void processImageAfterUpload(String imageKey) throws UnsupportedImageException;

    Set<String> getSignedUrlsByTag(String tag);
}
