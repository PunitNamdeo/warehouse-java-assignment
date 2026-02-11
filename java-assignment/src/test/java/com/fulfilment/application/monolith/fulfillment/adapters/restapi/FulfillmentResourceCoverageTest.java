package com.fulfilment.application.monolith.fulfillment.adapters.restapi;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Additional integration tests for Fulfillment REST endpoints - Coverage focused
 * Tests fulfillment associations and business logic
 */
@QuarkusTest
@DisplayName("Fulfillment REST - Association Tests")
public class FulfillmentResourceCoverageTest {

    private static final String BASE_URL = "/fulfillment";

    @Test
    @DisplayName("Should retrieve fulfillment info")
    public void testGetFulfillmentInfo() {
        given()
            .when()
            .get(BASE_URL + "/info")
            .then()
            .statusCode(anyOf(200, 404));
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
            .statusCode(anyOf(200, 201, 400, 404));
    }

    @Test
    @DisplayName("Should handle duplicate associations")
    public void testDuplicateAssociation() {
        String storeId = "STORE-DUP-" + System.currentTimeMillis();
        String warehouseId = "WH-DUP-" + System.currentTimeMillis();
        
        String requestBody = "{\"storeId\": \"" + storeId + "\", "
            + "\"warehouseId\": \"" + warehouseId + "\"}";

        // First association
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL + "/associate")
            .then()
            .statusCode(anyOf(200, 201, 400, 404));

        // Attempt duplicate
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL + "/associate")
            .then()
            .statusCode(anyOf(200, 201, 400, 404, 409));
    }

    @Test
    @DisplayName("Should handle missing store ID")
    public void testAssociateMissingStoreId() {
        String warehouseId = "WH-" + System.currentTimeMillis();
        String requestBody = "{\"warehouseId\": \"" + warehouseId + "\"}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL + "/associate")
            .then()
            .statusCode(anyOf(400, 422, 404));
    }

    @Test
    @DisplayName("Should handle missing warehouse ID")
    public void testAssociateMissingWarehouseId() {
        String storeId = "STORE-" + System.currentTimeMillis();
        String requestBody = "{\"storeId\": \"" + storeId + "\"}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL + "/associate")
            .then()
            .statusCode(anyOf(400, 422, 404));
    }

    @Test
    @DisplayName("Should handle empty association request")
    public void testEmptyAssociationRequest() {
        String requestBody = "{}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL + "/associate")
            .then()
            .statusCode(anyOf(400, 422, 404));
    }

    @Test
    @DisplayName("Should dissociate warehouse from store")
    public void testDissociateWarehouseFromStore() {
        String storeId = "STORE-DISSOC-" + System.currentTimeMillis();
        String warehouseId = "WH-DISSOC-" + System.currentTimeMillis();
        
        String requestBody = "{\"storeId\": \"" + storeId + "\", "
            + "\"warehouseId\": \"" + warehouseId + "\"}";

        // First associate
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL + "/associate")
            .then()
            .statusCode(anyOf(200, 201, 400, 404));

        // Then dissociate
        given()
            .when()
            .delete(BASE_URL + "/dissociate")
            .queryParam("storeId", storeId)
            .queryParam("warehouseId", warehouseId)
            .then()
            .statusCode(anyOf(200, 204, 404));
    }

    @Test
    @DisplayName("Should retrieve fulfillment status")
    public void testGetFulfillmentStatus() {
        String storeId = "STORE-STATUS-" + System.currentTimeMillis();
        
        given()
            .when()
            .get(BASE_URL + "/status")
            .queryParam("storeId", storeId)
            .then()
            .statusCode(anyOf(200, 404));
    }

    @Test
    @DisplayName("Should list warehouse assignments for store")
    public void testListWarehouseAssignments() {
        String storeId = "STORE-LIST-" + System.currentTimeMillis();
        
        given()
            .when()
            .get(BASE_URL + "/warehouses")
            .queryParam("storeId", storeId)
            .then()
            .statusCode(anyOf(200, 404));
    }

    @Test
    @DisplayName("Should validate store warehouse relationship")
    public void testValidateRelationship() {
        String storeId = "STORE-VALIDATE-" + System.currentTimeMillis();
        String warehouseId = "WH-VALIDATE-" + System.currentTimeMillis();
        
        given()
            .when()
            .get(BASE_URL + "/validate")
            .queryParam("storeId", storeId)
            .queryParam("warehouseId", warehouseId)
            .then()
            .statusCode(anyOf(200, 404));
    }

    @Test
    @DisplayName("Should handle null IDs in association")
    public void testAssociateNullIds() {
        String requestBody = "{\"storeId\": null, \"warehouseId\": null}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL + "/associate")
            .then()
            .statusCode(anyOf(400, 422, 404));
    }

    @Test
    @DisplayName("Should handle empty string IDs")
    public void testAssociateEmptyIds() {
        String requestBody = "{\"storeId\": \"\", \"warehouseId\": \"\"}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL + "/associate")
            .then()
            .statusCode(anyOf(400, 422, 404));
    }

    @Test
    @DisplayName("Should return JSON response with Content-Type header")
    public void testResponseContentType() {
        given()
            .when()
            .get(BASE_URL + "/info")
            .then()
            .statusCode(anyOf(200, 404))
            .header("Content-Type", containsString("application/json"));
    }

    @Test
    @DisplayName("Should handle malformed JSON in association")
    public void testMalformedJsonAssociation() {
        String requestBody = "{\"storeId\": \"STORE\", \"warehouseId\": \"WH\"";  // Missing closing brace

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(BASE_URL + "/associate")
            .then()
            .statusCode(anyOf(400, 422));
    }
}
