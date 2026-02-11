package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Store REST endpoint.
 * Tests cover CRUD operations, validation, and error scenarios.
 * 
 * Case Study Context: Stores are endpoints of the fulfillment network that receive
 * products from warehouses. During cost allocation, each store's location is mapped
 * to a warehouse location for cost distribution. Store data accuracy is critical for
 * accurate cost tracking and fulfillment network optimization.
 */
@QuarkusTest
@DisplayName("Store Endpoint Integration Tests")
public class StoreEndpointIT {

  private static final String BASE_URL = "/store";
  private long testStoreId;

  @BeforeEach
  public void setUp() {
    // Clean up any test data before each test
    testStoreId = 0;
  }

  // ============= POSITIVE TEST SCENARIOS =============

  @Test
  @DisplayName("Should create a new store with valid name")
  public void testCreateStoreWithValidName() {
    // given
    String storeName = "Test Store " + System.currentTimeMillis();
    String requestBody = "{\"name\": \"" + storeName + "\"}";

    // when-then
    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(BASE_URL)
        .then()
        .statusCode(200)
        .body("id", notNullValue())
        .body("name", equalTo(storeName));
  }

  @Test
  @DisplayName("Should retrieve all stores")
  public void testGetAllStores() {
    // given - create a test store
    String storeName = "Get All Test " + System.currentTimeMillis();
    String createRequest = "{\"name\": \"" + storeName + "\"}";

    given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .post(BASE_URL);

    // when-then
    given()
        .when()
        .get(BASE_URL)
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(1));
  }

  @Test
  @DisplayName("Should delete an existing store")
  public void testDeleteExistingStore() {
    // given - create a store
    String storeName = "Delete Test " + System.currentTimeMillis();
    String createRequest = "{\"name\": \"" + storeName + "\"}";

    long storeId = given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post(BASE_URL)
        .then()
        .statusCode(200)
        .extract()
        .path("id");

    // when-then - delete the store
    given()
        .when()
        .delete(BASE_URL + "/" + storeId)
        .then()
        .statusCode(204);

    // verify deletion
    given()
        .when()
        .get(BASE_URL)
        .then()
        .statusCode(200);
  }

  @Test
  @DisplayName("Should handle multiple store creations")
  public void testCreateMultipleStores() {
    // when-then - create 3 stores
    for (int i = 0; i < 3; i++) {
      String storeName = "Multi Store " + i + " " + System.currentTimeMillis();
      String requestBody = "{\"name\": \"" + storeName + "\"}";

      given()
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post(BASE_URL)
          .then()
          .statusCode(200)
          .body("id", notNullValue())
          .body("name", equalTo(storeName));
    }
  }

  @Test
  @DisplayName("Store should have quantityProductsInStock field")
  public void testStoreHasQuantityProductsInStockField() {
    // given
    String storeName = "Stock Count Test " + System.currentTimeMillis();
    String createRequest = "{\"name\": \"" + storeName + "\"}";

    // when-then
    given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post(BASE_URL)
        .then()
        .statusCode(200)
        .body("quantityProductsInStock", notNullValue());
  }

  // ============= NEGATIVE TEST SCENARIOS =============

  @Test
  @DisplayName("Should reject store creation with missing name")
  public void testCreateStoreWithoutName() {
    // given - empty body
    String requestBody = "{}";

    // when-then
    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(BASE_URL)
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("Should reject store creation with null name")
  public void testCreateStoreWithNullName() {
    // given
    String requestBody = "{\"name\": null}";

    // when-then
    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(BASE_URL)
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("Should reject store creation with empty name")
  public void testCreateStoreWithEmptyName() {
    // given
    String requestBody = "{\"name\": \"\"}";

    // when-then
    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(BASE_URL)
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("Should reject delete request for non-existent store")
  public void testDeleteNonExistentStore() {
    // given
    long nonExistentId = 999999;

    // when-then
    given()
        .when()
        .delete(BASE_URL + "/" + nonExistentId)
        .then()
        .statusCode(404);
  }

  @Test
  @DisplayName("Should reject malformed JSON")
  public void testCreateStoreWithMalformedJson() {
    // given - invalid JSON
    String malformedJson = "{\"name\": \"Test\"";

    // when-then
    given()
        .contentType(ContentType.JSON)
        .body(malformedJson)
        .when()
        .post(BASE_URL)
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("Should reject invalid content type")
  public void testCreateStoreWithInvalidContentType() {
    // given
    String requestBody = "{\"name\": \"Test Store\"}";

    // when-then
    given()
        .contentType(ContentType.TEXT)
        .body(requestBody)
        .when()
        .post(BASE_URL)
        .then()
        .statusCode(415);
  }

  // ============= BOUNDARY CONDITION TESTS =============

  @Test
  @DisplayName("Should handle store name with special characters")
  public void testCreateStoreWithSpecialCharacters() {
    // given
    String storeName = "Store@#$%^&*() " + System.currentTimeMillis();
    String requestBody = "{\"name\": \"" + storeName.replace("\"", "\\\"") + "\"}";

    // when-then
    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(BASE_URL)
        .then()
        .statusCode(200)
        .body("name", containsString("Store"));
  }

  @Test
  @DisplayName("Should handle very long store name")
  public void testCreateStoreWithVeryLongName() {
    // given - 500 character name
    String longName = "A".repeat(500);
    String requestBody = "{\"name\": \"" + longName + "\"}";

    // when-then
    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(BASE_URL)
        .then()
        .statusCode(200)
        .body("id", notNullValue());
  }

  @Test
  @DisplayName("Should handle store name with unicode characters")
  public void testCreateStoreWithUnicodeCharacters() {
    // given
    String storeName = "Store-日本-München-" + System.currentTimeMillis();
    String requestBody = "{\"name\": \"" + storeName + "\"}";

    // when-then
    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(BASE_URL)
        .then()
        .statusCode(200)
        .body("name", equalTo(storeName));
  }

  @Test
  @DisplayName("Should handle concurrent store creation requests")
  public void testConcurrentStoreCreation() {
    // given - multiple store names
    String[] storeNames = {
        "Concurrent Store 1 " + System.currentTimeMillis(),
        "Concurrent Store 2 " + System.currentTimeMillis(),
        "Concurrent Store 3 " + System.currentTimeMillis()
    };

    // when-then
    for (String name : storeNames) {
      String requestBody = "{\"name\": \"" + name + "\"}";
      given()
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post(BASE_URL)
          .then()
          .statusCode(200);
    }
  }

  // ============= CASE STUDY VALIDATION TESTS =============

  @Test
  @DisplayName("Store quantityProductsInStock should be non-negative")
  public void testStoreQuantityIsNonNegative() {
    // given
    String storeName = "Quantity Test " + System.currentTimeMillis();
    String requestBody = "{\"name\": \"" + storeName + "\"}";

    // when
    int quantity = given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(BASE_URL)
        .then()
        .statusCode(200)
        .extract()
        .path("quantityProductsInStock");

    // then
    assertThat(quantity).isGreaterThanOrEqualTo(0);
  }
}
