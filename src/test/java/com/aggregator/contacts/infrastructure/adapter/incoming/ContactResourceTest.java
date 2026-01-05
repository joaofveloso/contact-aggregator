package com.aggregator.contacts.infrastructure.adapter.incoming;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ContactResourceTest {

    @Test
    void shouldReturnContactsArray() {
        given().when()
                .get("/v1/contacts")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body(notNullValue());
    }

    @Test
    void shouldInvalidateCache() {
        given().when()
                .post("/v1/contacts/invalidate-cache")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("message", equalTo("Cache invalidated successfully"));
    }
}
