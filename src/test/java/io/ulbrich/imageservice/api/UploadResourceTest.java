package io.ulbrich.imageservice.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.Header;
import io.ulbrich.imageservice.dto.ImageUploadRequestDto;
import io.ulbrich.imageservice.model.ImagePrivacy;
import io.ulbrich.util.JwtTokenGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.*;

@QuarkusTest
class UploadResourceTest {
    @Inject
    JwtTokenGenerator jwtTokenGenerator;

    @BeforeAll
    static void beforeAll() {
        RestAssured.filters(
                (requestSpec, responseSpec, ctx) -> {
                    requestSpec.header(new Header(CONTENT_TYPE, APPLICATION_JSON));
                    requestSpec.header(new Header(ACCEPT, APPLICATION_JSON));
                    return ctx.next(requestSpec, responseSpec);
                },
                new RequestLoggingFilter(),
                new ResponseLoggingFilter());

        RestAssured.config = RestAssured.config().objectMapperConfig(new ObjectMapperConfig());
    }

    @AfterAll
    static void afterAll() {
        RestAssured.reset();
    }

    @Test
    public void testSignRequestSizeRejected() {
        String jwtToken = jwtTokenGenerator.generateToken();

        long size = 50 * 1024 * 1024;
        ImageUploadRequestDto hugeImage = new ImageUploadRequestDto("Huge Image", "A really huge image", "test.png", size, ImagePrivacy.PRIVATE, Set.of("test"));

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .body(hugeImage)
                .when()
                .post("/image/request")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testSignRequestAccepted() {
        String jwtToken = jwtTokenGenerator.generateToken();

        long size = 3 * 1024 * 1024;
        ImageUploadRequestDto normalImage = new ImageUploadRequestDto("Normal Image", "Just a normal image", "test.png", size, ImagePrivacy.PRIVATE, Set.of("test"));

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .body(normalImage)
                .when()
                .post("/image/request")
                .then()
                .statusCode(OK.getStatusCode());
    }

    @Test
    public void testSignRequestUnauthenticated() {
        String jwtToken = "INVALID";

        long size = 3 * 1024 * 1024;
        ImageUploadRequestDto hugeImage = new ImageUploadRequestDto("Huge Image", "A really huge image", "test.png", size, ImagePrivacy.PRIVATE, Set.of("test"));

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .body(hugeImage)
                .when()
                .post("/image/request")
                .then()
                .statusCode(UNAUTHORIZED.getStatusCode());
    }
}