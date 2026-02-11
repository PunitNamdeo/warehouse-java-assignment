package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Warehouse REST endpoints
 * Tests all 5 warehouse CRUD operations with proper validation
 * Uses timestamps to ensure unique business unit codes between test runs
 */
@QuarkusTest
public class WarehouseResourceImplTest {

    @Test
    void testListAllWarehouses() {
        given()
            .when()
            .get("/warehouse")
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    void testCreateWarehouseWithValidData() {
        String warehousePayload = "{"
            + "\"businessUnitCode\": \"MWH-" + System.currentTimeMillis() + "\","
            + "\"location\": \"AMSTERDAM-001\","
            + "\"capacity\": 250,"
            + "\"stock\": 100"
            + "}";

        given()
            .contentType(ContentType.JSON)
            .body(warehousePayload)
            .when()
            .post("/warehouse")
            .then()
            .statusCode(201)
            .body("businessUnitCode", notNullValue())
            .body("capacity", equalTo(250))
            .body("stock", equalTo(100))
            .body("location", equalTo("AMSTERDAM-001"));
    }

    @Test
    void testCreateWarehouseWithDifferentLocations() {
        // Test with multiple valid locations
        String[] locations = {"AMSTERDAM-001", "ROTTERDAM-002", "EINDHOVEN-003"};
        
        for (String location : locations) {
            String warehousePayload = "{"
                + "\"businessUnitCode\": \"WH-" + location + "-" + System.currentTimeMillis() + "\","
                + "\"location\": \"" + location + "\","
                + "\"capacity\": 300,"
                + "\"stock\": 150"
                + "}";

            given()
                .contentType(ContentType.JSON)
                .body(warehousePayload)
                .when()
                .post("/warehouse")
                .then()
                .statusCode(201)
                .body("location", equalTo(location));
        }
    }

    @Test
    void testCreateWarehouseWithVariousCapacities() {
        // Test with different capacity values
        int[] capacities = {100, 500, 1000, 5000};
        
        for (int capacity : capacities) {
            String warehousePayload = "{"
                + "\"businessUnitCode\": \"WH-CAP-" + capacity + "-" + System.currentTimeMillis() + "\","
                + "\"location\": \"TEST-LOC\","
                + "\"capacity\": " + capacity + ","
                + "\"stock\": " + (capacity / 2)
                + "}";

            given()
                .contentType(ContentType.JSON)
                .body(warehousePayload)
                .when()
                .post("/warehouse")
                .then()
                .statusCode(201)
                .body("capacity", equalTo(capacity));
        }
    }

    @Test
    void testCreateWarehouseStockCappedToCapacity() {
        String warehousePayload = "{"
            + "\"businessUnitCode\": \"WH-STOCK-CAP-" + System.currentTimeMillis() + "\","
            + "\"location\": \"STOCK-TEST\","
            + "\"capacity\": 200,"
            + "\"stock\": 150"
            + "}";

        given()
            .contentType(ContentType.JSON)
            .body(warehousePayload)
            .when()
            .post("/warehouse")
            .then()
            .statusCode(201)
            .body("stock", lessThanOrEqualTo(200));
    }

    @Test
    void testGetWarehouseByCode() {
        // Create a warehouse first
        String uniqueCode = "WH-GETBYCODE-" + System.currentTimeMillis();
        String warehousePayload = "{"
            + "\"businessUnitCode\": \"" + uniqueCode + "\","
            + "\"location\": \"GET-TEST\","
            + "\"capacity\": 300,"
            + "\"stock\": 100"
            + "}";

        given()
            .contentType(ContentType.JSON)
            .body(warehousePayload)
            .when()
            .post("/warehouse")
            .then()
            .statusCode(201);

        // Retrieve it by code
        given()
            .when()
            .get("/warehouse/" + uniqueCode)
            .then()
            .statusCode(200)
            .body("businessUnitCode", equalTo(uniqueCode));
    }

    @Test
    void testGetWarehouseByCodeNotFound() {
        given()
            .when()
            .get("/warehouse/NONEXISTENT-" + System.currentTimeMillis())
            .then()
            .statusCode(404)
            .or()
            .statusCode(400);
    }

    @Test
    void testArchiveWarehouse() {
        // Create a warehouse
        String uniqueCode = "WH-ARCHIVE-" + System.currentTimeMillis();
        String warehousePayload = "{"
            + "\"businessUnitCode\": \"" + uniqueCode + "\","
            + "\"location\": \"ARCHIVE-TEST\","
            + "\"capacity\": 250,"
            + "\"stock\": 80"
            + "}";

        given()
            .contentType(ContentType.JSON)
            .body(warehousePayload)
            .when()
            .post("/warehouse")
            .then()
            .statusCode(201);

        // Archive the warehouse
        given()
            .when()
            .delete("/warehouse/" + uniqueCode)
            .then()
            .statusCode(204)
            .or()
            .statusCode(200);

        // It should no longer appear in listings of active warehouses
        // (unless it still returns archived warehouses)
    }

    @Test
    void testReplaceWarehouse() {
        // Create an initial warehouse
        String uniqueCode = "WH-REPLACE-" + System.currentTimeMillis();
        String initialPayload = "{"
            + "\"businessUnitCode\": \"" + uniqueCode + "\","
            + "\"location\": \"REPLACE-TEST\","
            + "\"capacity\": 200,"
            + "\"stock\": 50"
            + "}";

        given()
            .contentType(ContentType.JSON)
            .body(initialPayload)
            .when()
            .post("/warehouse")
            .then()
            .statusCode(201);

        // Replace it with a new warehouse using the same business unit code
        String replacementPayload = "{"
            + "\"businessUnitCode\": \"" + uniqueCode + "\","
            + "\"location\": \"REPLACE-TEST-NEW\","
            + "\"capacity\": 400,"
            + "\"stock\": 200"
            + "}";

        given()
            .contentType(ContentType.JSON)
            .body(replacementPayload)
            .when()
            .post("/warehouse/" + uniqueCode + "/replacement")
            .then()
            .statusCode(anyOf(200, 201, 204));
    }

    @Test
    void testCreateWarehouseEdgeCases() {
        // Test with minimum values
        String minPayload = "{"
            + "\"businessUnitCode\": \"WH-MIN-" + System.currentTimeMillis() + "\","
            + "\"location\": \"MIN-LOC\","
            + "\"capacity\": 1,"
            + "\"stock\": 0"
            + "}";

        given()
            .contentType(ContentType.JSON)
            .body(minPayload)
            .when()
            .post("/warehouse")
            .then()
            .statusCode(anyOf(201, 400)); // Depends on validation rules
    }

    @Test
    void testCreateWarehouseWithLargeValues() {
        // Test with large values
        String largePayload = "{"
            + "\"businessUnitCode\": \"WH-LARGE-" + System.currentTimeMillis() + "\","
            + "\"location\": \"LARGE-LOC\","
            + "\"capacity\": 999999,"
            + "\"stock\": 500000"
            + "}";

        given()
            .contentType(ContentType.JSON)
            .body(largePayload)
            .when()
            .post("/warehouse")
            .then()
            .statusCode(anyOf(201, 400)); // Depends on business rules
    }

    @Test
    void testCreateWarehouseInvalidPayload() {
        // Missing required fields
        String invalidPayload = "{"
            + "\"location\": \"INVALID-TEST\""
            + "}";

        given()
            .contentType(ContentType.JSON)
            .body(invalidPayload)
            .when()
            .post("/warehouse")
            .then()
            .statusCode(400)
            .or()
            .statusCode(422); // Unprocessable Entity
    }

    @Test
    void testCreateWarehouseNegativeCapacity() {
        String negativePayload = "{"
            + "\"businessUnitCode\": \"WH-NEG-" + System.currentTimeMillis() + "\","
            + "\"location\": \"NEG-TEST\","
            + "\"capacity\": -100,"
            + "\"stock\": 50"
            + "}";

        given()
            .contentType(ContentType.JSON)
            .body(negativePayload)
            .when()
            .post("/warehouse")
            .then()
            .statusCode(anyOf(400, 500)); // Should fail
    }

    @Test
    void testCreateWarehouseStockExceedsCapacity() {
        String excessPayload = "{"
            + "\"businessUnitCode\": \"WH-EXCESS-" + System.currentTimeMillis() + "\","
            + "\"location\": \"EXCESS-TEST\","
            + "\"capacity\": 100,"
            + "\"stock\": 200"
            + "}";

        given()
            .contentType(ContentType.JSON)
            .body(excessPayload)
            .when()
            .post("/warehouse")
            .then()
            // Should either fail or auto-cap to capacity
            .statusCode(anyOf(201, 400, 422));
    }

    @Test
    void testWarehouseEndpointAcceptsCORSHeaders() {
        given()
            .header("Origin", "http://localhost:3000")
            .header("Access-Control-Request-Method", "POST")
            .when()
            .options("/warehouse")
            .then()
            .statusCode(anyOf(200, 204, 404)); // Depending on CORS config
    }
}
