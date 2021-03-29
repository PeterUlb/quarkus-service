package io.ulbrich.imageservice.api;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.Header;
import io.ulbrich.containers.WiremockCountries;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@QuarkusTestResource(WiremockCountries.class)
class CountryResourceTest {
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
    public void testCountryNameEndpoint() {
        given()
                .when().get("/country/germany")
                .then()
                .statusCode(200)
                .body("$.size()", is(1),
                        "[0].name", is("MockDeutschland"),
                        "[0].capital", is("MockBerlin")
//                        "[0].currencies.size()", is(1),
//                        "[0].currencies[0].name", is("Euro")
                );
    }
}