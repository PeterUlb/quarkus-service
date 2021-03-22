package net.adultart.imageservice.service;

import net.adultart.imageservice.dto.ImageUploadRequestDto;
import net.adultart.imageservice.model.Image;
import net.adultart.imageservice.model.ImageStatus;
import net.adultart.imageservice.repository.ImageRepository;
import org.apache.commons.lang3.RandomStringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class ImageServiceImpl implements ImageService {
    @Inject
    ImageRepository imageRepository;

    @Override
    @Transactional
    public String createImageEntry(ImageUploadRequestDto imageUploadRequestDto, long accountId) {
        Image image = Image.withInitialState(generateExternalKey(),
                accountId,
                imageUploadRequestDto.getTitle(),
                imageUploadRequestDto.getDescription(),
                imageUploadRequestDto.getFileName(),
                imageUploadRequestDto.getSize());
        imageRepository.persist(image);
        return image.getExternalKey();
    }

    @Override
    @Transactional
    public void updateImageState(String externalKey, ImageStatus imageStatus) {
        Image image = imageRepository.findByExternalId(externalKey).orElse(null);
        if (image == null) {
            return;
        }

        image.setImageStatus(ImageStatus.UPLOADED);
    }

    private String generateExternalKey() {
        return RandomStringUtils.randomAlphanumeric(10);
    }
}
