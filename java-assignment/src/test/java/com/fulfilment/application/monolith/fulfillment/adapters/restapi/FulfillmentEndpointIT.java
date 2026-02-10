package com.fulfilment.application.monolith.fulfillment.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class FulfillmentEndpointIT {

  @Test
  public void testAssociateWarehouseToProductStore() {
    final String path = "fulfillment/warehouses/1/1/MWH.001";

    // Create an association between a warehouse, product, and store
    given()
        .when()
        .post(path)
        .then()
        .statusCode(201);
  }

  @Test
  public void testGetWarehousesForProductStore() {
    final String path = "fulfillment/warehouses/1";

    given()
        .when()
        .get(path)
        .then()
        .statusCode(200);
  }

  @Test
  public void testConstraintMaxWarehousesPerProductStore() {
    final String productId = "2";
    final String storeId = "2";

    // Associate first warehouse (should succeed)
    given()
        .when()
        .post("fulfillment/warehouses/" + productId + "/" + storeId + "/MWH.001")
        .then()
        .statusCode(201);

    // Associate second warehouse (should succeed, max is 2)
    given()
        .when()
        .post("fulfillment/warehouses/" + productId + "/" + storeId + "/MWH.012")
        .then()
        .statusCode(201);

    // Try to associate third warehouse (should fail, max is 2)
    given()
        .when()
        .post("fulfillment/warehouses/" + productId + "/" + storeId + "/MWH.023")
        .then()
        .statusCode(409);
  }

  @Test
  public void testRemoveWarehouseProductStoreAssociation() {
    final String productId = "1";
    final String storeId = "1";
    final String warehouseCode = "MWH.001";

    // First, create association
    given()
        .when()
        .post("fulfillment/warehouses/" + productId + "/" + storeId + "/" + warehouseCode)
        .then()
        .statusCode(201);

    // Then, remove it
    given()
        .when()
        .delete("fulfillment/warehouses/" + productId + "/" + storeId + "/" + warehouseCode)
        .then()
        .statusCode(204);
  }

  @Test
  public void testConstraintMaxWarehousesPerStore() {
    final String productId = "3";
    final String storeId = "3";

    // Associate to max 3 warehouses for this store
    for (int i = 1; i <= 3; i++) {
      String warehouseCode = "MWH." + String.format("%03d", i);
      given()
          .when()
          .post("fulfillment/warehouses/" + productId + "/" + storeId + "/" + warehouseCode)
          .then()
          .statusCode(201);
    }

    // Try 4th warehouse for same store (should fail - max 3 per store)
    given()
        .when()
        .post("fulfillment/warehouses/" + productId + "/" + storeId + "/MWH.999")
        .then()
        .statusCode(409);
  }

  @Test
  public void testDuplicateAssociationPrevention() {
    final String productId = "1";
    final String storeId = "1";
    final String warehouseCode = "MWH.001";
    final String path = "fulfillment/warehouses/" + productId + "/" + storeId + "/" + warehouseCode;

    // First association succeeds
    given()
        .when()
        .post(path)
        .then()
        .statusCode(201);

    // Duplicate association should fail with 409 Conflict
    given()
        .when()
        .post(path)
        .then()
        .statusCode(409);
  }
}
