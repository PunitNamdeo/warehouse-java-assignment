package com.fulfilment.application.monolith.fulfillment.domain.usecases;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class FulfillmentEndpointIT {

  @Test
  public void testAssociateWarehouseToProductStore() {
    final String path = "fulfillment/warehouse-product-store";

    // Create an association between a warehouse, product, and store
    given()
        .contentType("application/json")
        .body(
            new java.util.HashMap<String, Object>() {
              {
                put("productId", 1L);
                put("storeId", 1L);
                put("warehouseBusinessUnitCode", "MWH.001");
              }
            })
        .when()
        .post(path)
        .then()
        .statusCode(201)
        .body(
            containsString("\"productId\":1"),
            containsString("\"storeId\":1"),
            containsString("\"warehouseBusinessUnitCode\":\"MWH.001\""));
  }

  @Test
  public void testGetWarehousesForProductStore() {
    final String path = "fulfillment/warehouse-product-store/product/1/store/1";

    given().when().get(path).then().statusCode(200);
  }

  @Test
  public void testConstraintMaxWarehousesPerProductStore() {
    final String path = "fulfillment/warehouse-product-store";

    // Try to add 3 warehouses to the same product-store (max is 2)
    for (int i = 1; i <= 3; i++) {
      given()
          .contentType("application/json")
          .body(
              new java.util.HashMap<String, Object>() {
                {
                  put("productId", 2L);
                  put("storeId", 2L);
                  put("warehouseBusinessUnitCode", "MWH." + String.format("%03d", i));
                }
              })
          .when()
          .post(path);
    }

    // The third one should fail (we already have 2)
    given()
        .contentType("application/json")
        .body(
            new java.util.HashMap<String, Object>() {
              {
                put("productId", 2L);
                put("storeId", 2L);
                put("warehouseBusinessUnitCode", "MWH.999");
              }
            })
        .when()
        .post(path)
        .then()
        .statusCode(409);
  }
}
