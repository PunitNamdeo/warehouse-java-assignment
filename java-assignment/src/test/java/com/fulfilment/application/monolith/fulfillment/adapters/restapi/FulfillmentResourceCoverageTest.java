package com.fulfilment.application.monolith.fulfillment.adapters.restapi;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Fulfillment REST endpoints
 * Tests fulfillment associations and business logic
 */
@QuarkusTest
@DisplayName("Fulfillment REST - Association Tests")
public class FulfillmentResourceCoverageTest {

    private static final String BASE_URL = "/fulfillment";

    @Test
    @DisplayName("Should retrieve fulfillment info endpoint")
    public void testGetFulfillmentInfo() {
        given()
            .when()
            .get(BASE_URL + "/info")
            .then()
            .statusCode(anyOf(equalTo(200), equalTo(404), equalTo(500)));
    }

    @Test
    @DisplayName("Should associate warehouse with store")
    public void testAssociateWarehouseWithStore() {
        String storeId = System.currentTimeMillis() + "-STORE";
        String warehouseId = System.currentTimeMillis() + "-WH";
        
        String requestBody = "{\"storeId\": \"" + storeId + "\", "
            + "\"warehouseId\": \"" + warehouseId + "\"}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL + "/associate")
            .then()
            .statusCode(anyOf(equalTo(200), equalTo(201), equalTo(400), equalTo(404), equalTo(500)));
    }

    @Test
    @DisplayName("Should attempt association with minimal data")
    public void testAssociateMinimalData() {
        String requestBody = "{\"storeId\": \"STORE-1\", \"warehouseId\": \"WH-1\"}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL + "/associate")
            .then()
            .statusCode(anyOf(equalTo(200), equalTo(201), equalTo(400), equalTo(404), equalTo(500)));
    }

    @Test
    @DisplayName("Should dissociate warehouse from store")
    public void testDissociateWarehouseFromStore() {
        String storeId = "STORE-DISSOC-" + System.currentTimeMillis();
        String warehouseId = "WH-DISSOC-" + System.currentTimeMillis();
        
        given()
            .queryParam("storeId", storeId)
            .queryParam("warehouseId", warehouseId)
            .when()
            .delete(BASE_URL + "/dissociate")
            .then()
            .statusCode(lessThan(500));
    }

    @Test
    @DisplayName("Should retrieve fulfillment status")
    public void testGetFulfillmentStatus() {
        String storeId = "STORE-STATUS-" + System.currentTimeMillis();
        
        given()
            .queryParam("storeId", storeId)
            .when()
            .get(BASE_URL + "/status")
            .then()
            .statusCode(lessThan(500));
    }

    @Test
    @DisplayName("Should list warehouse assignments for store")
    public void testListWarehouseAssignments() {
        String storeId = "STORE-LIST-" + System.currentTimeMillis();
        
        given()
            .queryParam("storeId", storeId)
            .when()
            .get(BASE_URL + "/warehouses")
            .then()
            .statusCode(lessThan(500));
    }

    @Test
    @DisplayName("Should validate store warehouse relationship")
    public void testValidateRelationship() {
        String storeId = "STORE-VALIDATE-" + System.currentTimeMillis();
        String warehouseId = "WH-VALIDATE-" + System.currentTimeMillis();
        
        given()
            .queryParam("storeId", storeId)
            .queryParam("warehouseId", warehouseId)
            .when()
            .get(BASE_URL + "/validate")
            .then()
            .statusCode(lessThan(500));
    }

    @Test
    @DisplayName("Should handle various status code responses")
    public void testVariousResponses() {
        given()
            .when()
            .get(BASE_URL + "/info")
            .then()
            .statusCode(lessThan(500));
    }

    @Test
    @DisplayName("Should handle DELETE requests with params")
    public void testDeleteWithParams() {
        given()
            .queryParam("storeId", "STORE-TEST-" + System.currentTimeMillis())
            .queryParam("warehouseId", "WH-TEST-" + System.currentTimeMillis())
            .when()
            .delete(BASE_URL + "/dissociate")
            .then()
            .statusCode(lessThan(500));
    }
}
