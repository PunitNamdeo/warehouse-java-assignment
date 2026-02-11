package com.fulfilment.application.monolith.products.adapters.restapi;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Additional integration tests for Product REST endpoints - Coverage focused
 * Tests product business logic, pricing, and various scenarios
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
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("Should create product with valid data")
    public void testCreateProductValid() {
        String productName = "Test Product - " + System.currentTimeMillis();
        String requestBody = "{\"description\": \"" + productName + "\", "
            + "\"price\": 99.99}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(200, 201))
            .body("description", equalTo(productName))
            .body("price", notNullValue());
    }

    @Test
    @DisplayName("Should create products with various prices")
    public void testCreateProductVariousPrices() {
        double[] prices = {9.99, 49.99, 99.99, 499.99, 999.99};
        
        for (double price : prices) {
            String productName = "Product Price " + price + " - " + System.currentTimeMillis();
            String requestBody = "{\"description\": \"" + productName + "\", "
                + "\"price\": " + price + "}";

            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(anyOf(200, 201));
        }
    }

    @Test
    @DisplayName("Should handle zero price product")
    public void testCreateProductZeroPrice() {
        String productName = "Free Product - " + System.currentTimeMillis();
        String requestBody = "{\"description\": \"" + productName + "\", "
            + "\"price\": 0.00}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(200, 201, 400));
    }

    @Test
    @DisplayName("Should handle negative price")
    public void testCreateProductNegativePrice() {
        String productName = "Negative Price - " + System.currentTimeMillis();
        String requestBody = "{\"description\": \"" + productName + "\", "
            + "\"price\": -10.00}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(400, 422, 500));
    }

    @Test
    @DisplayName("Should handle high precision prices")
    public void testCreateProductHighPrecision() {
        String productName = "Precision Product - " + System.currentTimeMillis();
        String requestBody = "{\"description\": \"" + productName + "\", "
            + "\"price\": 123.4567}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(200, 201, 400));
    }

    @Test
    @DisplayName("Should produce response with ID")
    public void testProductResponseHasId() {
        String productName = "ID Test - " + System.currentTimeMillis();
        String requestBody = "{\"description\": \"" + productName + "\", "
            + "\"price\": 49.99}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(200, 201))
            .body("id", notNullValue());
    }

    @Test
    @DisplayName("Should handle missing description")
    public void testCreateProductMissingDescription() {
        String requestBody = "{\"price\": 49.99}"; // Missing description

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(400, 422, 500));
    }

    @Test
    @DisplayName("Should handle missing price")
    public void testCreateProductMissingPrice() {
        String productName = "No Price - " + System.currentTimeMillis();
        String requestBody = "{\"description\": \"" + productName + "\"}"; // Missing price

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(400, 422, 500));
    }

    @Test
    @DisplayName("Should handle empty payload")
    public void testCreateProductEmptyPayload() {
        String requestBody = "{}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(400, 422, 500));
    }

    @Test
    @DisplayName("Should handle long description")
    public void testCreateProductLongDescription() {
        StringBuilder longDesc = new StringBuilder("Long Description - ");
        for (int i = 0; i < 20; i++) {
            longDesc.append("Lorem ipsum dolor sit amet ");
        }
        longDesc.append(System.currentTimeMillis());
        
        String requestBody = "{\"description\": \"" + longDesc.toString() + "\", "
            + "\"price\": 99.99}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(200, 201, 400, 422));
    }

    @Test
    @DisplayName("Should handle special characters in description")
    public void testCreateProductSpecialChars() {
        String productName = "Product @#$%^& - " + System.currentTimeMillis();
        String requestBody = "{\"description\": \"" + productName + "\", "
            + "\"price\": 29.99}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(200, 201, 400));
    }

    @Test
    @DisplayName("Should retrieve product response headers")
    public void testProductResponseHeaders() {
        String productName = "Header Test - " + System.currentTimeMillis();
        String requestBody = "{\"description\": \"" + productName + "\", "
            + "\"price\": 44.99}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(200, 201))
            .header("Content-Type", containsString("application/json"));
    }

    @Test
    @DisplayName("Should handle very large price")
    public void testCreateProductVeryLargePrice() {
        String productName = "Large Price - " + System.currentTimeMillis();
        String requestBody = "{\"description\": \"" + productName + "\", "
            + "\"price\": 999999.99}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(200, 201, 400));
    }
}
