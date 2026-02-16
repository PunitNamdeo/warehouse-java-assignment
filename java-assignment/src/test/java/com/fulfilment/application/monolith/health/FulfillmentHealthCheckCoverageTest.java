package com.fulfilment.application.monolith.health;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class FulfillmentHealthCheckCoverageTest {

  @Test
  void liveness_endpoint_is_up() {
    given()
        .when()
        .get("/q/health/live")
        .then()
        .statusCode(200)
        .body("status", equalTo("UP"))
        .body("checks", notNullValue());
  }
}
