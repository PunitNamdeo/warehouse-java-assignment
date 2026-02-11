package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Additional integration tests for Store REST endpoints - Coverage focused
 * Tests quantity product in stock handling and extended scenarios
 */
@QuarkusTest
@DisplayName("Store REST - Quantity Stock Tests")
public class StoreResourceQuantityTest {

    private static final String BASE_URL = "/store";

    @Test
    @DisplayName("Should create store with quantity products in stock")
    public void testCreateStoreWithQuantityStock() {
        String storeName = "Qty Test Store - " + System.currentTimeMillis();
        String requestBody = "{\"name\": \"" + storeName + "\", \"quantityProductsInStock\": 50}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(200)
            .body("name", equalTo(storeName))
            .body("quantityProductsInStock", equalTo(50));
    }

    @Test
    @DisplayName("Should create store with zero quantity")
    public void testCreateStoreWithZeroQuantity() {
        String storeName = "Zero Qty Store - " + System.currentTimeMillis();
        String requestBody = "{\"name\": \"" + storeName + "\", \"quantityProductsInStock\": 0}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(200)
            .body("quantityProductsInStock", equalTo(0));
    }

    @Test
    @DisplayName("Should create store with large quantity")
    public void testCreateStoreWithLargeQuantity() {
        String storeName = "Large Qty Store - " + System.currentTimeMillis();
        String requestBody = "{\"name\": \"" + storeName + "\", \"quantityProductsInStock\": 10000}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(200)
            .body("quantityProductsInStock", equalTo(10000));
    }

    @Test
    @DisplayName("Should retrieve store with quantity field")
    public void testGetStoreWithQuantityField() {
        String storeName = "Get Qty Store - " + System.currentTimeMillis();
        String createRequest = "{\"name\": \"" + storeName + "\", \"quantityProductsInStock\": 75}";

        long storeId = given()
            .contentType(ContentType.JSON)
            .body(createRequest)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(200)
            .extract()
            .path("id");

        given()
            .when()
            .get(BASE_URL + "/" + storeId)
            .then()
            .statusCode(200)
            .body("quantityProductsInStock", equalTo(75));
    }

    @Test
    @DisplayName("Should update store quantity")
    public void testUpdateStoreQuantity() {
        String storeName = "Update Qty Store - " + System.currentTimeMillis();
        String createRequest = "{\"name\": \"" + storeName + "\", \"quantityProductsInStock\": 30}";

        long storeId = given()
            .contentType(ContentType.JSON)
            .body(createRequest)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(200)
            .extract()
            .path("id");

        String updateRequest = "{\"name\": \"" + storeName + " Updated\", \"quantityProductsInStock\": 60}";

        given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
            .when()
            .put(BASE_URL + "/" + storeId)
            .then()
            .statusCode(200)
            .body("quantityProductsInStock", equalTo(60));
    }
}
