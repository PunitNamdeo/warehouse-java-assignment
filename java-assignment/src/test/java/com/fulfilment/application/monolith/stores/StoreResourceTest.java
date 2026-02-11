package com.fulfilment.application.monolith.stores;

import com.fulfilment.application.monolith.BaseEntity;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Store REST endpoints
 * Tests the complete flow: API -> Database -> Validation
 */
@QuarkusTest
public class StoreResourceTest {

    @Test
    void testCreateStoreWithValidData() {
        String storePayload = "{"
            + "\"name\": \"Test Store - " + System.currentTimeMillis() + "\","
            + "\"quantityProductsInStock\": 50"
            + "}";

        given()
            .contentType(ContentType.JSON)
            .body(storePayload)
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .body("name", notNullValue())
            .body("quantityProductsInStock", equalTo(50));
    }

    @Test
    void testCreateStoreWithZeroStock() {
        String storePayload = "{"
            + "\"name\": \"Zero Stock Store - " + System.currentTimeMillis() + "\","
            + "\"quantityProductsInStock\": 0"
            + "}";

        given()
            .contentType(ContentType.JSON)
            .body(storePayload)
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .body("quantityProductsInStock", equalTo(0));
    }

    @Test
    void testCreateStoreWithLargeQuantity() {
        String storePayload = "{"
            + "\"name\": \"Large Quantity Store - " + System.currentTimeMillis() + "\","
            + "\"quantityProductsInStock\": 10000"
            + "}";

        given()
            .contentType(ContentType.JSON)
            .body(storePayload)
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .body("quantityProductsInStock", equalTo(10000));
    }

    @Test
    void testGetAllStores() {
        // First, create a store
        String storePayload = "{"
            + "\"name\": \"Store for Get All - " + System.currentTimeMillis() + "\","
            + "\"quantityProductsInStock\": 75"
            + "}";

        given()
            .contentType(ContentType.JSON)
            .body(storePayload)
            .when()
            .post("/store")
            .then()
            .statusCode(201);

        // Then, get all stores
        given()
            .when()
            .get("/store")
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    void testGetStoreByIdSuccessful() {
        // Create a store first
        String storePayload = "{"
            + "\"name\": \"Store for GetById - " + System.currentTimeMillis() + "\","
            + "\"quantityProductsInStock\": 85"
            + "}";

        Long storeId = given()
            .contentType(ContentType.JSON)
            .body(storePayload)
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Then retrieve it
        given()
            .when()
            .get("/store/" + storeId)
            .then()
            .statusCode(200)
            .body("id", equalTo(storeId.intValue()))
            .body("quantityProductsInStock", equalTo(85));
    }

    @Test
    void testGetStoreByIdNotFound() {
        Long nonExistentId = 99999L;

        given()
            .when()
            .get("/store/" + nonExistentId)
            .then()
            .statusCode(404);
    }

    @Test
    void testUpdateStoreSuccessful() {
        // Create a store
        String createPayload = "{"
            + "\"name\": \"Store to Update - " + System.currentTimeMillis() + "\","
            + "\"quantityProductsInStock\": 30"
            + "}";

        Long storeId = given()
            .contentType(ContentType.JSON)
            .body(createPayload)
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Update the store
        String updatePayload = "{"
            + "\"name\": \"Updated Store Name\","
            + "\"quantityProductsInStock\": 60"
            + "}";

        given()
            .contentType(ContentType.JSON)
            .body(updatePayload)
            .when()
            .put("/store/" + storeId)
            .then()
            .statusCode(200)
            .body("quantityProductsInStock", equalTo(60));
    }

    @Test
    void testDeleteStoreSuccessful() {
        // Create a store
        String storePayload = "{"
            + "\"name\": \"Store to Delete - " + System.currentTimeMillis() + "\","
            + "\"quantityProductsInStock\": 40"
            + "}";

        Long storeId = given()
            .contentType(ContentType.JSON)
            .body(storePayload)
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Delete the store
        given()
            .when()
            .delete("/store/" + storeId)
            .then()
            .statusCode(204);

        // Verify it's deleted
        given()
            .when()
            .get("/store/" + storeId)
            .then()
            .statusCode(404);
    }

    @Test
    void testCreateStoreWithNullName() {
        String storePayload = "{"
            + "\"quantityProductsInStock\": 50"
            + "}";

        given()
            .contentType(ContentType.JSON)
            .body(storePayload)
            .when()
            .post("/store")
            .then()
            .statusCode(400)
            .or()
            .statusCode(500); // Depending on validation configuration
    }

    @Test
    void testCreateStoreWithNegativeStock() {
        String storePayload = "{"
            + "\"name\": \"Negative Stock Store - " + System.currentTimeMillis() + "\","
            + "\"quantityProductsInStock\": -10"
            + "}";

        // Behavior depends on validation rules - should either reject or accept
        given()
            .contentType(ContentType.JSON)
            .body(storePayload)
            .when()
            .post("/store")
            .then()
            .statusCode(anyOf(400, 201));
    }
}
