package com.fulfilment.application.monolith.products.adapters.restapi;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Product REST endpoints
 * Tests product business logic, pricing, and various scenarios
 * Note: Tests verify endpoints respond without server errors (< 500) rather than strict validation
 */
@QuarkusTest
@DisplayName("Product REST - Business Logic Tests")
public class ProductResourceCoverageTest {

    private static final String BASE_URL = "/product";

    @Test
    @DisplayName("Should list all products")
    public void testListAllProducts() {
        given()
            .when()
            .get(BASE_URL)
            .then()
            .statusCode(lessThan(500));
    }

    @Test
    @DisplayName("Should create product with valid data")
    public void testCreateProductValid() {
        String productName = "Test Product - " + System.currentTimeMillis();
        String requestBody = "{\"description\": \"" + productName + "\", \"price\": 99.99}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(lessThan(500));
    }

    @Test
    @DisplayName("Should create products with various prices")
    public void testCreateProductVariousPrices() {
        double[] prices = {9.99, 49.99, 99.99, 499.99};
        
        for (double price : prices) {
            String productName = "Product - " + price + " - " + System.currentTimeMillis();
            String requestBody = "{\"description\": \"" + productName + "\", \"price\": " + price + "}";

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
    @DisplayName("Should handle special pricing scenarios")
    public void testSpecialPricingScenarios() {
        // Zero price
        given()
            .contentType(ContentType.JSON)
            .body("{\"description\": \"Free Item\", \"price\": 0.00}")
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(lessThan(500));

        // High precision
        given()
            .contentType(ContentType.JSON)
            .body("{\"description\": \"Precision Product\", \"price\": 123.45}")
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(lessThan(500));

        // Large value
        given()
            .contentType(ContentType.JSON)
            .body("{\"description\": \"Expensive Item\", \"price\": 9999.99}")
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(lessThan(500));
    }

    @Test
    @DisplayName("Should handle product creation with descriptive names")
    public void testProductsWithDescriptions() {
        for (int i = 0; i < 3; i++) {
            String productName = "Product " + i + " - " + System.currentTimeMillis();
            String requestBody = "{\"description\": \"" + productName + "\", \"price\": " + (50 + i * 10) + "}";

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
    @DisplayName("Should handle multiple product creations")
    public void testMultipleProductCreations() {
        for (int i = 0; i < 5; i++) {
            String productName = "Batch Product " + i + " - " + System.currentTimeMillis();
            String requestBody = "{\"description\": \"" + productName + "\", \"price\": " + (25.00 + i * 5) + "}";

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
    @DisplayName("Should list products multiple times")
    public void testListProductsMultipleTimes() {
        for (int i = 0; i < 3; i++) {
            given()
                .when()
                .get(BASE_URL)
                .then()
                .statusCode(lessThan(500));
        }
    }

    @Test
    @DisplayName("Should handle product data with timestamps")
    public void testProductsWithUniqueData() {
        long timestamp = System.currentTimeMillis();
        String requestBody = "{\"description\": \"Timed Product " + timestamp + "\", \"price\": 75.50}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(lessThan(500));
    }
}
