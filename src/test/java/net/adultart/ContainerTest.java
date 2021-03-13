package net.adultart;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import net.adultart.containers.PostgresResource;
import net.adultart.imageservice.model.Image;
import net.adultart.imageservice.repository.ImageRepository;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
public class ContainerTest {
    @Inject
    ImageRepository imageRepository;

    @Test
    @Transactional
    public void testWithContainers() {
        imageRepository.persist(Image.withInitialState("TEST"));
        imageRepository.flush();
        Optional<Image> image = imageRepository.findByExternalId("TEST");
        assertNotNull(image.orElse(null));
    }

}