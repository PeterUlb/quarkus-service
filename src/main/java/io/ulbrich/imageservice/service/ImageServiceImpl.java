package io.ulbrich.imageservice.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import io.ulbrich.imageservice.api.UploadResource;
import io.ulbrich.imageservice.config.UploadProcessorConfig;
import io.ulbrich.imageservice.dto.ImageUploadRequestDto;
import io.ulbrich.imageservice.exception.UnsupportedImageException;
import io.ulbrich.imageservice.model.Image;
import io.ulbrich.imageservice.model.ImageStatus;
import io.ulbrich.imageservice.model.ImageTag;
import io.ulbrich.imageservice.repository.ImageRepository;
import io.ulbrich.imageservice.repository.ImageTagRepository;
import io.ulbrich.imageservice.util.TikaUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.jboss.logging.Logger;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@ApplicationScoped
public class ImageServiceImpl implements ImageService {
    private static final Logger LOGGER = Logger.getLogger(UploadResource.class);

    @Inject
    ImageRepository imageRepository;

    @Inject
    ImageTagRepository imageTagRepository;

    @Inject
    TikaUtil tikaUtil;

    @Inject
    Storage storage;

    @Inject
    UploadProcessorConfig uploadProcessorConfig;

    @Override
    @Transactional
    public String createImageEntry(ImageUploadRequestDto imageUploadRequestDto, UUID accountId) {
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
        Blob blob = storage.get(uploadProcessorConfig.getBucket(), imageKey);
        if (blob == null) {
            return;
        }

        try (InputStream inputStream = Channels.newInputStream(blob.reader())) {
            metadata = tikaUtil.extractMetadata(inputStream);
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

            BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(uploadProcessorConfig.getBucket(), "images/" + image.getExternalKey())).build();
            URL url = storage.signUrl(blobInfo, 10, TimeUnit.MINUTES, Storage.SignUrlOption.withV4Signature());
            signedUrls.add(url.toExternalForm());
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
