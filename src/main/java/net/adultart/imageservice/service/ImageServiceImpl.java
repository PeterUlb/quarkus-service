package net.adultart.imageservice.service;

import net.adultart.imageservice.model.Image;
import net.adultart.imageservice.model.ImageStatus;
import net.adultart.imageservice.repository.ImageRepository;
import org.apache.commons.lang3.RandomStringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Optional;

@ApplicationScoped
public class ImageServiceImpl implements ImageService {
    @Inject
    ImageRepository imageRepository;

    @Override
    @Transactional
    public String createImageEntry(String imageName, long imageSize) {
        Image image = Image.withInitialState(generateExternalKey());
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
