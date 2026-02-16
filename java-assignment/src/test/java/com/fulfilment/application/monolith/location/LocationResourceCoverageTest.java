package com.fulfilment.application.monolith.location;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class LocationResourceCoverageTest {

  @Test
  void resolve_location_success() {
    given()
        .when()
        .get("/location/ZWOLLE-001")
        .then()
        .statusCode(200)
        .body("identification", equalTo("ZWOLLE-001"))
        .body("maxNumberOfWarehouses", equalTo(1))
        .body("maxCapacity", equalTo(40));
  }

  @Test
  void resolve_location_not_found() {
    given()
        .when()
        .get("/location/INVALID-999")
        .then()
        .statusCode(404)
        .body("exceptionType", equalTo("jakarta.ws.rs.NotFoundException"))
        .body("code", equalTo(404));
  }
}
