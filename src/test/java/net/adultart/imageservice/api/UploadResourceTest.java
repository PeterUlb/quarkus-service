package net.adultart.imageservice.api;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.Header;
import net.adultart.containers.LocalStackResource;
import net.adultart.containers.PostgresResource;
import net.adultart.imageservice.dto.ImageUploadRequestDto;
import net.adultart.util.JwtTokenGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.*;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
@QuarkusTestResource(LocalStackResource.class)
class UploadResourceTest {
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
        String jwtToken = JwtTokenGenerator.generateToken();

        long size = 50 * 1024 * 1024;
        ImageUploadRequestDto hugeImage = new ImageUploadRequestDto("Huge Image", "A really huge image", "test.png", size);

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
        String jwtToken = JwtTokenGenerator.generateToken();

        long size = 3 * 1024 * 1024;
        ImageUploadRequestDto hugeImage = new ImageUploadRequestDto("Huge Image", "A really huge image", "test.png", size);

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .body(hugeImage)
                .when()
                .post("/image/request")
                .then()
                .statusCode(OK.getStatusCode());
    }

    @Test
    public void testSignRequestUnauthenticated() {
        String jwtToken = "INVALID";

        long size = 3 * 1024 * 1024;
        ImageUploadRequestDto hugeImage = new ImageUploadRequestDto("Huge Image", "A really huge image", "test.png", size);

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .body(hugeImage)
                .when()
                .post("/image/request")
                .then()
                .statusCode(UNAUTHORIZED.getStatusCode());
    }
}