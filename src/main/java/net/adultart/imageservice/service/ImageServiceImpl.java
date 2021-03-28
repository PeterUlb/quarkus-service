package net.adultart.imageservice.service;

import net.adultart.imageservice.api.UploadResource;
import net.adultart.imageservice.config.AwsImageConfig;
import net.adultart.imageservice.dto.ImageUploadRequestDto;
import net.adultart.imageservice.exception.UnsupportedImageException;
import net.adultart.imageservice.model.Image;
import net.adultart.imageservice.model.ImageStatus;
import net.adultart.imageservice.model.ImageTag;
import net.adultart.imageservice.repository.ImageRepository;
import net.adultart.imageservice.repository.ImageTagRepository;
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
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class ImageServiceImpl implements ImageService {
    private static final Logger LOGGER = Logger.getLogger(UploadResource.class);

    @Inject
    ImageRepository imageRepository;

    @Inject
    ImageTagRepository imageTagRepository;

    @Inject
    S3Client s3Client;

    @Inject
    AwsImageConfig awsImageConfig;

    @Inject
    TikaUtil tikaUtil;

    @Inject
    S3Presigner s3Presigner;

    @Override
    @Transactional
    public String createImageEntry(ImageUploadRequestDto imageUploadRequestDto, long accountId) {
        // First try to insert the missing tags
        insertMissingImageTags(imageUploadRequestDto.getTags());

        // Now get all ids for the relevant tags, at this point all must exist
        Set<ImageTag> tags = imageUploadRequestDto.getTags().stream().map(s -> imageTagRepository.findByTag(s).orElseThrow()).collect(Collectors.toSet());

        Image image = Image.withInitialState(generateExternalKey(),
                accountId,
                imageUploadRequestDto.getTitle(),
                imageUploadRequestDto.getDescription(),
                imageUploadRequestDto.getFileName(),
                imageUploadRequestDto.getSize(),
                imageUploadRequestDto.getPrivacy(),
                tags);
        imageRepository.persist(image);
        return image.getExternalKey();
    }

    @Override
    @Transactional(dontRollbackOn = UnsupportedImageException.class)
    public void processImageAfterUpload(String imageKey) {
        String externalId = imageKey;
        int pos = imageKey.lastIndexOf("/");
        if (pos != -1) {
            externalId = imageKey.substring(pos + 1);
        }

        Metadata metadata;
        try (ResponseInputStream<GetObjectResponse> responseInputStream = s3Client.getObject(builder -> builder.bucket(awsImageConfig.getBucket()).key(imageKey))) {
            metadata = tikaUtil.extractMetadata(responseInputStream);
        } catch (IOException | TikaException | SAXException e) {
            LOGGER.error("Error while reading object: " + e.getMessage());
            imageRepository.findByExternalId(externalId).ifPresent(image -> image.setImageStatus(ImageStatus.REJECTED));
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
            // TODO: DELETE, SET IMAGE STATUS, ...
            imageRepository.findByExternalId(externalId).ifPresent(image -> image.setImageStatus(ImageStatus.REJECTED));
            return;
        }

        long height = tikaUtil.getHeight(metadata);
        long width = tikaUtil.getWidth(metadata);
        LOGGER.debug("Height: " + height);
        LOGGER.debug("Width: " + width);

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

    @Override
    public Set<String> getSignedUrlsByTag(String tag) {
        ImageTag imageTag = imageTagRepository.findByTag(tag).orElse(null);
        if (imageTag == null) {
            return Collections.emptySet();
        }

        Set<Image> images = imageTag.getImages();
        Set<String> signedUrls = new HashSet<>(images.size());
        for (Image image : images) {
            if (image.getImageStatus() != ImageStatus.VERIFIED) {
                continue;
            }
            PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(
                    builder -> builder
                            .getObjectRequest(objReq -> objReq
                                    .bucket(awsImageConfig.getBucket())
                                    .key("images/" + image.getExternalKey()))
                            .signatureDuration(Duration.ofMinutes(10)));
            signedUrls.add(presignedGetObjectRequest.url().toExternalForm());
        }

        return signedUrls;
    }

    void insertMissingImageTags(Set<String> tags) {
        Set<String> existingTags = imageTagRepository.findByTags(tags).stream().map(ImageTag::getTag).collect(Collectors.toSet());

        int inserted = 0;
        for (String tag : tags) {
            if (!existingTags.contains(tag)) {
                insertTag(tag);
                inserted++;
            }
        }

        LOGGER.debug(String.format("Inserted %d missing tags", inserted));
    }

    /**
     * Tries to add the tag to the database. Ignores duplicate exceptions.
     * Note: Since parallel requests might try to insert the same tag, and postgres throws `ERROR: current transaction is aborted, commands ignored until end of transaction block`
     * on duplicate errors, each insert is handed of to a unique transaction
     *
     * @param tag The tag for the image
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    void insertTag(String tag) {
        ImageTag imageTag = new ImageTag();
        imageTag.setTag(tag);
        try {
            imageTagRepository.persistAndFlush(imageTag);
        } catch (PersistenceException ignored) {
        }
    }

    private String generateExternalKey() {
        return RandomStringUtils.randomAlphanumeric(10);
    }
}
