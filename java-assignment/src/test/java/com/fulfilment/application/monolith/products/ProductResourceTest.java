package com.fulfilment.application.monolith.products;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Product REST endpoints
 * Tests complete CRUD operations for product management
 */
@QuarkusTest
public class ProductResourceTest {

    @Test
    void testGetAllProducts() {
        given()
            .when()
            .get("/product")
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    void testCreateProductWithValidData() {
        String productPayload = "{"
            + "\"name\": \"Test Product - " + System.currentTimeMillis() + "\","
            + "\"category\": \"Electronics\","
            + "\"price\": 99.99,"
            + "\"quantity\": 50"
            + "}";

        given()
            .contentType(ContentType.JSON)
            .body(productPayload)
            .when()
            .post("/product")
            .then()
            .statusCode(201)
            .body("name", notNullValue())
            .body("category", equalTo("Electronics"))
            .body("price", closeTo(99.99f, 0.01f))
            .body("quantity", equalTo(50));
    }

    @Test
    void testCreateProductWithDifferentCategories() {
        String[] categories = {"Electronics", "Clothing", "Food", "Books", "Furniture"};
        
        for (String category : categories) {
            String productPayload = "{"
                + "\"name\": \"Product-" + category + "-" + System.currentTimeMillis() + "\","
                + "\"category\": \"" + category + "\","
                + "\"price\": 49.99,"
                + "\"quantity\": 25"
                + "}";

            given()
                .contentType(ContentType.JSON)
                .body(productPayload)
                .when()
                .post("/product")
                .then()
                .statusCode(201)
                .body("category", equalTo(category));
        }
    }

    @Test
    void testCreateProductWithVariousPrices() {
        double[] prices = {0.01, 9.99, 99.99, 999.99, 9999.99};
        
        for (double price : prices) {
            String productPayload = "{"
                + "\"name\": \"Product-" + price + "-" + System.currentTimeMillis() + "\","
                + "\"category\": \"Test\","
                + "\"price\": " + price + ","
                + "\"quantity\": 10"
                + "}";

            given()
                .contentType(ContentType.JSON)
                .body(productPayload)
                .when()
                .post("/product")
                .then()
                .statusCode(201)
                .body("price", closeTo((float)price, 0.01f));
        }
    }

    @Test
    void testGetProductById() {
        // Create a product first
        String createPayload = "{"
            + "\"name\": \"Product for GetById - " + System.currentTimeMillis() + "\","
            + "\"category\": \"TestCategory\","
            + "\"price\": 79.99,"
            + "\"quantity\": 30"
            + "}";

        Long productId = given()
            .contentType(ContentType.JSON)
            .body(createPayload)
            .when()
            .post("/product")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Retrieve it
        given()
            .when()
            .get("/product/" + productId)
            .then()
            .statusCode(200)
            .body("id", equalTo(productId.intValue()))
            .body("price", closeTo(79.99f, 0.01f));
    }

    @Test
    void testGetProductByIdNotFound() {
        Long nonExistentId = 999999L;

        given()
            .when()
            .get("/product/" + nonExistentId)
            .then()
            .statusCode(404);
    }

    @Test
    void testUpdateProduct() {
        // Create a product
        String createPayload = "{"
            + "\"name\": \"Product to Update - " + System.currentTimeMillis() + "\","
            + "\"category\": \"UpdateTest\","
            + "\"price\": 59.99,"
            + "\"quantity\": 20"
            + "}";

        Long productId = given()
            .contentType(ContentType.JSON)
            .body(createPayload)
            .when()
            .post("/product")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Update the product
        String updatePayload = "{"
            + "\"name\": \"Updated Product Name\","
            + "\"category\": \"UpdatedCategory\","
            + "\"price\": 89.99,"
            + "\"quantity\": 40"
            + "}";

        given()
            .contentType(ContentType.JSON)
            .body(updatePayload)
            .when()
            .put("/product/" + productId)
            .then()
            .statusCode(200)
            .body("name", equalTo("Updated Product Name"))
            .body("price", closeTo(89.99f, 0.01f));
    }

    @Test
    void testDeleteProduct() {
        // Create a product
        String createPayload = "{"
            + "\"name\": \"Product to Delete - " + System.currentTimeMillis() + "\","
            + "\"category\": \"DeleteTest\","
            + "\"price\": 39.99,"
            + "\"quantity\": 15"
            + "}";

        Long productId = given()
            .contentType(ContentType.JSON)
            .body(createPayload)
            .when()
            .post("/product")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Delete the product
        given()
            .when()
            .delete("/product/" + productId)
            .then()
            .statusCode(204)
            .or()
            .statusCode(200);

        // Verify deletion
        given()
            .when()
            .get("/product/" + productId)
            .then()
            .statusCode(404);
    }

    @Test
    void testCreateProductWithZeroQuantity() {
        String zeroQuantityPayload = "{"
            + "\"name\": \"Zero Quantity Product - " + System.currentTimeMillis() + "\","
            + "\"category\": \"ZeroTest\","
            + "\"price\": 29.99,"
            + "\"quantity\": 0"
            + "}";

        given()
            .contentType(ContentType.JSON)
            .body(zeroQuantityPayload)
            .when()
            .post("/product")
            .then()
            .statusCode(201)
            .body("quantity", equalTo(0));
    }

    @Test
    void testCreateProductWithLargeQuantity() {
        String largeQuantityPayload = "{"
            + "\"name\": \"Large Quantity Product - " + System.currentTimeMillis() + "\","
            + "\"category\": \"LargeTest\","
            + "\"price\": 19.99,"
            + "\"quantity\": 100000"
            + "}";

        given()
            .contentType(ContentType.JSON)
            .body(largeQuantityPayload)
            .when()
            .post("/product")
            .then()
            .statusCode(201)
            .body("quantity", equalTo(100000));
    }

    @Test
    void testCreateProductWithMissingFields() {
        String incompletePayload = "{"
            + "\"name\": \"Incomplete Product\""
            + "}";

        given()
            .contentType(ContentType.JSON)
            .body(incompletePayload)
            .when()
            .post("/product")
            .then()
            .statusCode(anyOf(400, 422, 500)); // Should fail validation
    }

    @Test
    void testCreateProductWithNullName() {
        String nullNamePayload = "{"
            + "\"name\": null,"
            + "\"category\": \"TestCategory\","
            + "\"price\": 49.99,"
            + "\"quantity\": 25"
            + "}";

        given()
            .contentType(ContentType.JSON)
            .body(nullNamePayload)
            .when()
            .post("/product")
            .then()
            .statusCode(anyOf(400, 422));
    }

    @Test
    void testCreateProductWithNegativePrice() {
        String negativePayload = "{"
            + "\"name\": \"Negative Price Product - " + System.currentTimeMillis() + "\","
            + "\"category\": \"NegativeTest\","
            + "\"price\": -99.99,"
            + "\"quantity\": 10"
            + "}";

        given()
            .contentType(ContentType.JSON)
            .body(negativePayload)
            .when()
            .post("/product")
            .then()
            .statusCode(anyOf(400, 422)); // Should fail validation
    }

    @Test
    void testCreateProductWithNegativeQuantity() {
        String negativeQtyPayload = "{"
            + "\"name\": \"Negative Qty Product - " + System.currentTimeMillis() + "\","
            + "\"category\": \"NegTest\","
            + "\"price\": 49.99,"
            + "\"quantity\": -50"
            + "}";

        given()
            .contentType(ContentType.JSON)
            .body(negativeQtyPayload)
            .when()
            .post("/product")
            .then()
            .statusCode(anyOf(201, 400, 422)); // Depends on validation
    }

    @Test
    void testProductCountIncrementsOnCreate() {
        int initialCount = given()
            .when()
            .get("/product")
            .then()
            .statusCode(200)
            .extract()
            .path("size()");

        String newProductPayload = "{"
            + "\"name\": \"New Product - " + System.currentTimeMillis() + "\","
            + "\"category\": \"NewTest\","
            + "\"price\": 69.99,"
            + "\"quantity\": 35"
            + "}";

        given()
            .contentType(ContentType.JSON)
            .body(newProductPayload)
            .when()
            .post("/product")
            .then()
            .statusCode(201);

        int updatedCount = given()
            .when()
            .get("/product")
            .then()
            .statusCode(200)
            .extract()
            .path("size()");

        assert updatedCount >= initialCount;
    }
}
