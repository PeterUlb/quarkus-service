package net.adultart;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import net.adultart.imageservice.model.Image;
import net.adultart.imageservice.repository.ImageRepository;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
//import static io.restassured.RestAssured.given;
//import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
public class UploadResourceTest {
    @Inject
    ImageRepository imageRepository;

//    @Test
//    public void testHelloEndpoint() {
////        given()
////          .when().get("/hello")
////          .then()
////             .statusCode(200)
////             .body(is("Hello RESTEasy"));
//    }

    @Test
    @Transactional
    public void testWithContainers() {
        imageRepository.persist(Image.withInitialState("TEST"));
        imageRepository.flush();
        Optional<Image> image = imageRepository.findByExternalId("TEST");
        assertNotNull(image.orElse(null));
    }

}