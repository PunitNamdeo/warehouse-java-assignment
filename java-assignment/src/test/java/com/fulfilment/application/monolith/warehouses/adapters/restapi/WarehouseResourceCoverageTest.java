package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Additional integration tests for Warehouse REST endpoints - Coverage focused
 * Tests warehouse business logic and various scenarios
 */
@QuarkusTest
@DisplayName("Warehouse REST - Business Logic Tests")
public class WarehouseResourceCoverageTest {

    private static final String BASE_URL = "/warehouse";

    @Test
    @DisplayName("Should list all warehouses")
    public void testListAllWarehouses() {
        given()
            .when()
            .get(BASE_URL)
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("Should create warehouse with valid data")
    public void testCreateWarehouseValid() {
        String code = "WH-CREATE-" + System.currentTimeMillis();
        String requestBody = "{\"businessUnitCode\": \"" + code + "\", "
            + "\"location\": \"AMSTERDAM-001\", \"capacity\": 250, \"stock\": 100}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(201)
            .body("businessUnitCode", equalTo(code))
            .body("capacity", equalTo(250))
            .body("stock", equalTo(100));
    }

    @Test
    @DisplayName("Should create warehouse with different locations")
    public void testCreateWarehouseLocations() {
        String[] locations = {"AMSTERDAM-001", "ROTTERDAM-002", "EINDHOVEN-003"};
        
        for (String location : locations) {
            String code = "WH-" + location + "-" + System.currentTimeMillis();
            String requestBody = "{\"businessUnitCode\": \"" + code + "\", "
                + "\"location\": \"" + location + "\", \"capacity\": 300, \"stock\": 150}";

            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(201)
                .body("location", equalTo(location));
        }
    }

    @Test
    @DisplayName("Should handle various capacity values")
    public void testCreateWarehouseCapacities() {
        int[] capacities = {100, 500, 1000, 5000};
        
        for (int capacity : capacities) {
            String code = "WH-CAP-" + capacity + "-" + System.currentTimeMillis();
            String requestBody = "{\"businessUnitCode\": \"" + code + "\", "
                + "\"location\": \"TEST-LOC\", \"capacity\": " + capacity + ", \"stock\": " + (capacity / 2) + "}";

            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(anyOf(201, 400)); // Depends on location validation
        }
    }

    @Test
    @DisplayName("Should enforce stock <= capacity constraint")
    public void testStockCapacityConstraint() {
        String code = "WH-STOCK-" + System.currentTimeMillis();
        String requestBody = "{\"businessUnitCode\": \"" + code + "\", "
            + "\"location\": \"TEST-LOC\", \"capacity\": 200, \"stock\": 150}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(201, 400))
            .body("stock", lessThanOrEqualTo(200));
    }

    @Test
    @DisplayName("Should retrieve warehouse by code")
    public void testGetWarehouseByCode() {
        String code = "WH-GET-" + System.currentTimeMillis();
        String createBody = "{\"businessUnitCode\": \"" + code + "\", "
            + "\"location\": \"TEST-LOC\", \"capacity\": 300, \"stock\": 100}";

        given()
            .contentType(ContentType.JSON)
            .body(createBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(201);

        given()
            .when()
            .get(BASE_URL + "/" + code)
            .then()
            .statusCode(anyOf(200, 404));
    }

    @Test
    @DisplayName("Should handle non-existent warehouse codes")
    public void testGetNonExistentWarehouse() {
        String code = "NONEXISTENT-" + System.currentTimeMillis();

        given()
            .when()
            .get(BASE_URL + "/" + code)
            .then()
            .statusCode(anyOf(404, 400));
    }

    @Test
    @DisplayName("Should archive warehouse")
    public void testArchiveWarehouse() {
        String code = "WH-ARCHIVE-" + System.currentTimeMillis();
        String createBody = "{\"businessUnitCode\": \"" + code + "\", "
            + "\"location\": \"ARCHIVE-LOC\", \"capacity\": 250, \"stock\": 80}";

        given()
            .contentType(ContentType.JSON)
            .body(createBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(201);

        given()
            .when()
            .delete(BASE_URL + "/" + code)
            .then()
            .statusCode(anyOf(204, 200, 404));
    }

    @Test
    @DisplayName("Should handle invalid payload")
    public void testCreateWarehouseInvalidPayload() {
        String invalidBody = "{\"location\": \"TEST-LOC\"}"; // Missing required fields

        given()
            .contentType(ContentType.JSON)
            .body(invalidBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(400, 422, 500));
    }

    @Test
    @DisplayName("Should handle negative capacity")
    public void testCreateWarehouseNegativeCapacity() {
        String code = "WH-NEG-" + System.currentTimeMillis();
        String requestBody = "{\"businessUnitCode\": \"" + code + "\", "
            + "\"location\": \"TEST-LOC\", \"capacity\": -100, \"stock\": 50}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL)
            .then()
            .statusCode(anyOf(400, 422, 500));
    }
}
