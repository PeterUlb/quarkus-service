package io.ulbrich;

import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import io.quarkus.test.junit.QuarkusTest;
import io.ulbrich.imageservice.model.Image;
import io.ulbrich.imageservice.model.ImagePrivacy;
import io.ulbrich.imageservice.repository.ImageRepository;
import io.ulbrich.mock.TestGcpCredentials;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class ContainerTest {
    @Inject
    ImageRepository imageRepository;

    @Inject
    Storage storage;

    @Test
    @Transactional
    public void testWithContainers() {
        imageRepository.persist(Image.withInitialState("TEST", 1L, "", "", "", 1L, ImagePrivacy.PRIVATE, Set.of()));
        imageRepository.flush();
        Optional<Image> image = imageRepository.findByExternalId("TEST");
        assertNotNull(image.orElse(null));
    }

    @Test
    public void testGcpStorageContainer() {
        Bucket bucket = storage.create(BucketInfo.newBuilder("my-bucket").build());
        bucket.create("my-test-file.txt", "Test-Content".getBytes(StandardCharsets.UTF_8));
        String content = new String(bucket.get("my-test-file.txt").getContent(), StandardCharsets.UTF_8);

        Assertions.assertEquals(content, "Test-Content");
        Assertions.assertEquals(storage.getOptions().getCredentials().getClass(), TestGcpCredentials.class);
    }

}