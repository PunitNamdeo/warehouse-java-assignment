package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Test;

@QuarkusIntegrationTest
public class WarehouseEndpointIT {

  @Test
  public void testSimpleListWarehouses() {

    final String path = "warehouses";

    // List all, should have all 3 warehouses the database has initially:
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(containsString("MWH.001"), containsString("MWH.012"), containsString("MWH.023"));
  }

  @Test
  public void testSimpleCheckingArchivingWarehouses() {
    final String path = "warehouses";
    final String businessUnitCode = "MWH.001";

    // Step 1: Verify warehouse exists in list
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(containsString("MWH.001"));

    // Step 2: Get specific warehouse
    given()
        .when()
        .get(path + "/" + businessUnitCode)
        .then()
        .statusCode(200)
        .body(containsString(businessUnitCode));

    // Step 3: Archive the warehouse
    given()
        .when()
        .delete(path + "/" + businessUnitCode)
        .then()
        .statusCode(204);

    // Step 4: Verify warehouse is no longer in list after archiving
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200);
  }

  @Test
  public void testCreateWarehouse() {
    final String path = "warehouses";
    final String businessUnitCode = "NEW.WH.001";

    // Create new warehouse
    given()
        .contentType("application/json")
        .body(
            "{"
            + "  \"businessUnitCode\": \"" + businessUnitCode + "\","
            + "  \"stock\": 50,"
            + "  \"capacity\": 300,"
            + "  \"maximumCapacity\": 500,"
            + "  \"location\": \"ZWOLLE-001\""
            + "}"
        )
        .when()
        .post(path)
        .then()
        .statusCode(201);
  }

  @Test
  public void testGetWarehouseNotFound() {
    final String path = "warehouses";

    // Try to get non-existent warehouse
    given()
        .when()
        .get(path + "/NONEXISTENT")
        .then()
        .statusCode(404);
  }
}
