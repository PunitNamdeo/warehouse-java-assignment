package com.fulfilment.application.monolith.fulfillment.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.assertj.core.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * Integration tests for Fulfillment REST endpoint.
 * Tests cover association of warehouses to products and stores with constraint validation.
 * 
 * Case Study Context: Fulfillment associations represent the distribution of products
 * from warehouses to stores. These associations are critical for cost allocation:
 * - Each association triggers cost transactions (fulfillment cost, transportation cost)
 * - Cost must be allocated from warehouse to store based on association frequency and volume
 * - Constraints ensure network efficiency:
 *   * Max 3 warehouses per store (avoid over-fragmentation of supply chains)
 *   * Max 2 products per warehouse-store pair (simplify logistics)
 *   * Max 1 association per warehouse-product-store triple (idempotency)
 */
@QuarkusTest
@DisplayName("Fulfillment Endpoint Integration Tests")
public class FulfillmentEndpointIT {

  private static final String BASE_PATH = "/fulfillment/warehouse-product-store";

  // ============= POSITIVE TEST SCENARIOS =============

  @Test
  @DisplayName("Should create association between warehouse, product, and store")
  public void testAssociateWarehouseToProductStore() {
    // given
    String requestBody = "{\"warehouseBusinessUnitCode\": \"MWH.001\", \"productId\": 1, \"storeId\": 1}";

    // when-then - Create association
    given()
        .contentType("application/json")
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201);
  }

  @Test
  @DisplayName("Should retrieve all fulfillment associations")
  public void testGetAllFulfillmentAssociations() {
    // given
    String requestBody = "{\"warehouseBusinessUnitCode\": \"MWH.002\", \"productId\": 2, \"storeId\": 2}";

    // Create an association first
    given()
        .contentType("application/json")
        .body(requestBody)
        .when()
        .post(BASE_PATH);

    // when-then
    given()
        .when()
        .get(BASE_PATH)
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(0));
  }

  @Test
  @DisplayName("Should delete fulfillment association")
  public void testRemoveWarehouseProductStoreAssociation() {
    // given - Create association first
    String createRequest = "{\"warehouseBusinessUnitCode\": \"MWH.001\", \"productId\": 3, \"storeId\": 3}";
    
    given()
        .contentType("application/json")
        .body(createRequest)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201);

    // when-then - Delete the association
    String deletePath = BASE_PATH + "/product/3/store/3/warehouse/MWH.001";
    given()
        .when()
        .delete(deletePath)
        .then()
        .statusCode(204);
  }

  @Test
  @DisplayName("Should support multiple associations for different products")
  public void testMultipleProductAssociations() {
    // given
    for (int productId = 1; productId <= 3; productId++) {
      String requestBody = String.format(
          "{\"warehouseBusinessUnitCode\": \"MWH.001\", \"productId\": %d, \"storeId\": 1}",
          productId);

      // when-then
      given()
          .contentType("application/json")
          .body(requestBody)
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(201);
    }
  }

  @Test
  @DisplayName("Should support multiple warehouses for same product-store")
  public void testMultipleWarehouseAssociations() {
    // given - Associate multiple warehouses
    String[] warehouses = {"MWH.001", "MWH.002"};
    int productId = 5;
    int storeId = 5;

    for (String warehouse : warehouses) {
      String requestBody = String.format(
          "{\"warehouseBusinessUnitCode\": \"%s\", \"productId\": %d, \"storeId\": %d}",
          warehouse, productId, storeId);

      // when-then
      given()
          .contentType("application/json")
          .body(requestBody)
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(201);
    }
  }

  // ============= NEGATIVE TEST SCENARIOS =============

  @Test
  @DisplayName("Should reject duplicate association (idempotency)")
  public void testDuplicateAssociationPrevention() {
    // given
    String requestBody = "{\"warehouseBusinessUnitCode\": \"MWH.001\", \"productId\": 10, \"storeId\": 10}";

    // Create first association
    given()
        .contentType("application/json")
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201);

    // when-then - Duplicate should fail with 409 Conflict
    given()
        .contentType("application/json")
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(409);
  }

  @Test
  @DisplayName("Should reject association with missing warehouseBusinessUnitCode")
  public void testCreateAssociationMissingWarehouse() {
    // given
    String requestBody = "{\"productId\": 1, \"storeId\": 1}";

    // when-then
    given()
        .contentType("application/json")
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("Should reject association with missing productId")
  public void testCreateAssociationMissingProductId() {
    // given
    String requestBody = "{\"warehouseBusinessUnitCode\": \"MWH.001\", \"storeId\": 1}";

    // when-then
    given()
        .contentType("application/json")
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("Should reject association with missing storeId")
  public void testCreateAssociationMissingStoreId() {
    // given
    String requestBody = "{\"warehouseBusinessUnitCode\": \"MWH.001\", \"productId\": 1}";

    // when-then
    given()
        .contentType("application/json")
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("Should reject association with null values")
  public void testCreateAssociationWithNullValues() {
    // given
    String requestBody = "{\"warehouseBusinessUnitCode\": null, \"productId\": 1, \"storeId\": 1}";

    // when-then
    given()
        .contentType("application/json")
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("Should reject delete request for non-existent association")
  public void testDeleteNonExistentAssociation() {
    // given
    String deletePath = BASE_PATH + "/product/999999/store/999999/warehouse/INVALID";

    // when-then
    given()
        .when()
        .delete(deletePath)
        .then()
        .statusCode(404);
  }

  @Test
  @DisplayName("Should reject malformed JSON")
  public void testCreateAssociationWithMalformedJson() {
    // given - Invalid JSON
    String malformedJson = "{\"warehouseBusinessUnitCode\": \"MWH.001\"";

    // when-then
    given()
        .contentType("application/json")
        .body(malformedJson)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(400);
  }

  // ============= CONSTRAINT TEST SCENARIOS =============

  @Test
  @DisplayName("Should enforce max 3 warehouses per store constraint")
  public void testConstraintMaxWarehousesPerStore() {
    // given
    int productId = 20;
    int storeId = 20;

    // when - Associate up to 3 warehouses (should succeed)
    for (int i = 1; i <= 3; i++) {
      String warehouse = "MWH." + String.format("%03d", i);
      String requestBody = String.format(
          "{\"warehouseBusinessUnitCode\": \"%s\", \"productId\": %d, \"storeId\": %d}",
          warehouse, productId, storeId);

      given()
          .contentType("application/json")
          .body(requestBody)
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(201);
    }

    // then - 4th warehouse should be rejected
    String requestBody = String.format(
        "{\"warehouseBusinessUnitCode\": \"MWH.999\", \"productId\": %d, \"storeId\": %d}",
        productId, storeId);

    given()
        .contentType("application/json")
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(409);
  }

  @Test
  @DisplayName("Should enforce max 2 products per warehouse-store pair constraint")
  public void testConstraintMaxProductsPerWarehouseStore() {
    // given
    String warehouse = "MWH.100";
    int storeId = 30;

    // when - Associate 2 products (should succeed)
    for (int productId = 1; productId <= 2; productId++) {
      String requestBody = String.format(
          "{\"warehouseBusinessUnitCode\": \"%s\", \"productId\": %d, \"storeId\": %d}",
          warehouse, productId, storeId);

      given()
          .contentType("application/json")
          .body(requestBody)
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(201);
    }

    // then - 3rd product should be rejected
    String requestBody = String.format(
        "{\"warehouseBusinessUnitCode\": \"%s\", \"productId\": 3, \"storeId\": %d}",
        warehouse, storeId);

    given()
        .contentType("application/json")
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(409);
  }

  @Test
  @DisplayName("Should prevent duplicate association (unique constraint)")
  public void testConstraintUniqueAssociation() {
    // given
    String warehouse = "MWH.200";
    int productId = 40;
    int storeId = 40;

    String requestBody = String.format(
        "{\"warehouseBusinessUnitCode\": \"%s\", \"productId\": %d, \"storeId\": %d}",
        warehouse, productId, storeId);

    // when - First association succeeds
    given()
        .contentType("application/json")
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201);

    // then - Duplicate should fail with 409
    given()
        .contentType("application/json")
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(409);
  }

  // ============= BOUNDARY CONDITION TESTS =============

  @Test
  @DisplayName("Should handle large product and store IDs")
  public void testLargeIdsHandling() {
    // given
    long largeProductId = Long.MAX_VALUE - 1;
    long largeStoreId = Long.MAX_VALUE - 1;
    String requestBody = String.format(
        "{\"warehouseBusinessUnitCode\": \"MWH.001\", \"productId\": %d, \"storeId\": %d}",
        largeProductId, largeStoreId);

    // when-then
    given()
        .contentType("application/json")
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201);
  }

  @Test
  @DisplayName("Should handle zero IDs")
  public void testZeroIds() {
    // given
    String requestBody = "{\"warehouseBusinessUnitCode\": \"MWH.001\", \"productId\": 0, \"storeId\": 0}";

    // when-then
    given()
        .contentType("application/json")
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("Should handle negative IDs")
  public void testNegativeIds() {
    // given
    String requestBody = "{\"warehouseBusinessUnitCode\": \"MWH.001\", \"productId\": -1, \"storeId\": -1}";

    // when-then
    given()
        .contentType("application/json")
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(400);
  }
}

