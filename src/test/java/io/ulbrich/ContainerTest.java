package io.ulbrich;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.ulbrich.containers.PostgresResource;
import io.ulbrich.imageservice.model.Image;
import io.ulbrich.imageservice.model.ImagePrivacy;
import io.ulbrich.imageservice.repository.ImageRepository;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
public class ContainerTest {
    @Inject
    ImageRepository imageRepository;

    @Test
    @Transactional
    public void testWithContainers() {
        imageRepository.persist(Image.withInitialState("TEST", 1L, "", "", "", 1L, ImagePrivacy.PRIVATE, Set.of()));
        imageRepository.flush();
        Optional<Image> image = imageRepository.findByExternalId("TEST");
        assertNotNull(image.orElse(null));
    }

}