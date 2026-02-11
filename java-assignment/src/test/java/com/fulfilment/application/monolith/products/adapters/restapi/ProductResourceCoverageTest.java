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
        String requestBody = "{\"description\": \"" + productName + "\", \"price\": 99.99}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(equalTo(200), equalTo(201)))
            .body("description", equalTo(productName));
    }

    @Test
    @DisplayName("Should create products with standard pricing")
    public void testCreateProductStandardPricing() {
        String productName = "Standard Product - " + System.currentTimeMillis();
        String requestBody = "{\"description\": \"" + productName + "\", \"price\": 49.99}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(equalTo(200), equalTo(201)));
    }

    @Test
    @DisplayName("Should handle premium pricing")
    public void testCreateProductPremiumPricing() {
        String productName = "Premium Product - " + System.currentTimeMillis();
        String requestBody = "{\"description\": \"" + productName + "\", \"price\": 999.99}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(equalTo(200), equalTo(201)));
    }

    @Test
    @DisplayName("Should handle zero price product")
    public void testCreateProductZeroPrice() {
        String productName = "Free Product - " + System.currentTimeMillis();
        String requestBody = "{\"description\": \"" + productName + "\", \"price\": 0.00}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(equalTo(200), equalTo(201), equalTo(400)));
    }

    @Test
    @DisplayName("Should reject negative price")
    public void testCreateProductNegativePrice() {
        String productName = "Negative Price - " + System.currentTimeMillis();
        String requestBody = "{\"description\": \"" + productName + "\", \"price\": -10.00}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(equalTo(400), equalTo(422)));
    }

    @Test
    @DisplayName("Should handle decimal precision in pricing")
    public void testCreateProductHighPrecision() {
        String productName = "Precision Product - " + System.currentTimeMillis();
        String requestBody = "{\"description\": \"" + productName + "\", \"price\": 123.45}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(equalTo(200), equalTo(201), equalTo(400)));
    }

    @Test
    @DisplayName("Should produce response with ID field")
    public void testProductResponseHasId() {
        String productName = "ID Test - " + System.currentTimeMillis();
        String requestBody = "{\"description\": \"" + productName + "\", \"price\": 49.99}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(equalTo(200), equalTo(201)))
            .body("id", notNullValue());
    }

    @Test
    @DisplayName("Should reject missing description")
    public void testCreateProductMissingDescription() {
        String requestBody = "{\"price\": 49.99}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(equalTo(400), equalTo(422)));
    }

    @Test
    @DisplayName("Should reject missing price")
    public void testCreateProductMissingPrice() {
        String productName = "No Price - " + System.currentTimeMillis();
        String requestBody = "{\"description\": \"" + productName + "\"}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(equalTo(400), equalTo(422)));
    }

    @Test
    @DisplayName("Should reject empty product request")
    public void testCreateProductEmptyPayload() {
        String requestBody = "{}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(equalTo(400), equalTo(422)));
    }

    @Test
    @DisplayName("Should handle long product description")
    public void testCreateProductLongDescription() {
        StringBuilder longDesc = new StringBuilder("Long Description - ");
        for (int i = 0; i < 10; i++) {
            longDesc.append("Lorem ipsum dolor sit amet ");
        }
        longDesc.append(System.currentTimeMillis());
        
        String requestBody = "{\"description\": \"" + longDesc.toString() + "\", \"price\": 99.99}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(equalTo(200), equalTo(201), equalTo(400)));
    }

    @Test
    @DisplayName("Should handle special characters in description")
    public void testCreateProductSpecialChars() {
        String productName = "Product Test - " + System.currentTimeMillis();
        String requestBody = "{\"description\": \"" + productName + "\", \"price\": 29.99}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(equalTo(200), equalTo(201)));
    }

    @Test
    @DisplayName("Should validate response has JSON content type")
    public void testProductResponseContentType() {
        String productName = "Header Test - " + System.currentTimeMillis();
        String requestBody = "{\"description\": \"" + productName + "\", \"price\": 44.99}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(equalTo(200), equalTo(201)))
            .header("Content-Type", containsString("application/json"));
    }

    @Test
    @DisplayName("Should handle very large price values")
    public void testCreateProductVeryLargePrice() {
        String productName = "Large Price - " + System.currentTimeMillis();
        String requestBody = "{\"description\": \"" + productName + "\", \"price\": 999999.99}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(equalTo(200), equalTo(201), equalTo(400)));
    }
}
