package net.adultart.imageservice.service;

import net.adultart.imageservice.api.UploadResource;
import net.adultart.imageservice.config.AwsImageConfig;
import net.adultart.imageservice.dto.ImageUploadRequestDto;
import net.adultart.imageservice.exception.UnsupportedImageException;
import net.adultart.imageservice.model.Image;
import net.adultart.imageservice.model.ImageStatus;
import net.adultart.imageservice.repository.ImageRepository;
import net.adultart.imageservice.util.TikaUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.jboss.logging.Logger;
import org.xml.sax.SAXException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;

@ApplicationScoped
public class ImageServiceImpl implements ImageService {
    private static final Logger LOGGER = Logger.getLogger(UploadResource.class);

    @Inject
    ImageRepository imageRepository;

    @Inject
    S3Client s3Client;

    @Inject
    AwsImageConfig awsImageConfig;

    @Inject
    TikaUtil tikaUtil;

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
    public void processImageAfterUpload(String imageKey) throws UnsupportedImageException {
        Metadata metadata;
        try (ResponseInputStream<GetObjectResponse> responseInputStream = s3Client.getObject(builder -> builder.bucket(awsImageConfig.getBucket()).key(imageKey))) {
            metadata = tikaUtil.extractMetadata(responseInputStream);
        } catch (IOException | TikaException | SAXException e) {
            LOGGER.error("Error while reading object", e);
            return;
        }

        String mimeType = tikaUtil.getContentType(metadata).orElseThrow(); // TODO
        LOGGER.debug(mimeType);

        boolean supported;
        switch (mimeType) {
            case "image/png":
            case "image/jpeg":
            case "image/gif":
                supported = true;
                break;
            default:
                supported = false;
        }

        if (!supported) {
            // DELETE, SET IMAGE STATUS, ...
            throw new UnsupportedImageException(mimeType);
        }

        long height = tikaUtil.getHeight(metadata);
        long width = tikaUtil.getWidth(metadata);
        LOGGER.debug("Height: " + height);
        LOGGER.debug("Width: " + width);

        String externalId = imageKey;
        int pos = imageKey.lastIndexOf("/");
        if (pos != -1) {
            externalId = imageKey.substring(pos + 1);
        }

        Image image = imageRepository.findByExternalId(externalId).orElse(null);
        if (image == null) {
            LOGGER.error("Could not find db entry for object in message " + imageKey);
            return;
        }

        MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
        String mimeTypeExtension = null;
        try {
            mimeTypeExtension = allTypes.forName(mimeType).getExtension();
        } catch (MimeTypeException e) {
            mimeTypeExtension = ""; // None
        }
        image.setImageStatus(ImageStatus.VERIFIED);
        image.setWidth(width);
        image.setHeight(height);
        image.setMimeType(mimeType);
        image.setExtension(mimeTypeExtension);
    }

    private String generateExternalKey() {
        return RandomStringUtils.randomAlphanumeric(10);
    }
}
