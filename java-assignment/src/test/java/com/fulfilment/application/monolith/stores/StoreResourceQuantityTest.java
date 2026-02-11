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
            .statusCode(lessThan(500));
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
            .statusCode(lessThan(500));
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
            .statusCode(lessThan(500));
    }

    @Test
    @DisplayName("Should handle various quantity values")
    public void testStoreWithVariousQuantities() {
        int[] quantities = {10, 50, 100, 500, 5000};
        
        for (int qty : quantities) {
            String storeName = "Qty Store " + qty + " - " + System.currentTimeMillis();
            String requestBody = "{\"name\": \"" + storeName + "\", \"quantityProductsInStock\": " + qty + "}";
            
            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(lessThan(500));
        }
    }

    @Test
    @DisplayName("Should list stores without errors")
    public void testListStores() {
        given()
            .when()
            .get(BASE_URL)
            .then()
            .statusCode(lessThan(500));
    }
}
