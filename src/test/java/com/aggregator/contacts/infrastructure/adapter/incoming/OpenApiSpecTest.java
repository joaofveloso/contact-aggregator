package com.aggregator.contacts.infrastructure.adapter.incoming;

import static io.restassured.RestAssured.given;
import static io.restassured.config.JsonConfig.jsonConfig;
import static io.restassured.config.RestAssuredConfig.config;
import static io.restassured.path.json.config.JsonPathConfig.NumberReturnType.BIG_DECIMAL;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class OpenApiSpecTest {

    @Test
    void shouldHaveValidOpenApiSpec() {
        given().config(config().jsonConfig(jsonConfig().numberReturnType(BIG_DECIMAL)))
                .accept("application/json")
                .when()
                .get("/q/openapi")
                .then()
                .statusCode(200)
                .body("openapi", equalTo("3.1.0"))
                .body("info.title", equalTo("Contacts API Aggregator"))
                .body("paths.'/v1/contacts'.get.summary", containsString("Retrieve all contacts"));
    }

    @Test
    void shouldIncludeSecurityScheme() {
        given().config(config().jsonConfig(jsonConfig().numberReturnType(BIG_DECIMAL)))
                .accept("application/json")
                .when()
                .get("/q/openapi")
                .then()
                .statusCode(200)
                .body("paths.'/v1/contacts'.get.summary", containsString("Retrieve all contacts"))
                .body("paths.'/v1/contacts'.get.security", equalTo(null));
    }
}
