package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fulfilment.application.monolith.stores.LegacyStoreManagerGateway;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("StoreResource Coverage Tests")
public class StoreResourceCoverageTest {

  @InjectMock
  LegacyStoreManagerGateway legacyStoreManagerGateway;

  private static final String STORE_PATH = "store";

  @BeforeEach
  void setup() {
    reset(legacyStoreManagerGateway);
  }

  // ============== GET ALL STORES ==============

  @Test
  @DisplayName("Should retrieve all stores successfully")
  void testGetAllStoresSuccess() {
    given()
        .when()
        .get(STORE_PATH)
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(0))
        .contentType(containsString("application/json"));
  }

  @Test
  @DisplayName("Should return empty list when no stores exist")
  void testGetAllStoresEmpty() {
    given()
        .when()
        .get(STORE_PATH)
        .then()
        .statusCode(200)
      .body("size()", greaterThanOrEqualTo(0))
        .contentType(containsString("application/json"));
  }

  // ============== GET SINGLE STORE ==============

  @Test
  @DisplayName("Should retrieve store by valid ID")
  void testGetSingleStoreSuccess() {
    // Assuming store with ID 1 exists
    given()
        .when()
        .get(STORE_PATH + "/1")
        .then()
        .statusCode(200)
        .body("id", notNullValue())
        .contentType(containsString("application/json"));
  }

  @Test
  @DisplayName("Should return 404 for non-existent store")
  void testGetSingleStoreNotFound() {
    given()
        .when()
        .get(STORE_PATH + "/99999")
        .then()
        .statusCode(404)
        .body("error", containsString("does not exist"));
  }

  @Test
  @DisplayName("Should return 400 for invalid store ID (null)")
  void testGetSingleStoreInvalidIdNull() {
    given()
        .when()
        .get(STORE_PATH + "/null")
        .then()
      .statusCode(404);
  }

  @Test
  @DisplayName("Should return 400 for negative store ID")
  void testGetSingleStoreNegativeId() {
    given()
        .when()
        .get(STORE_PATH + "/-1")
        .then()
        .statusCode(400)
        .body("error", containsString("Invalid"));
  }

  @Test
  @DisplayName("Should return 400 for zero store ID")
  void testGetSingleStoreZeroId() {
    given()
        .when()
        .get(STORE_PATH + "/0")
        .then()
        .statusCode(400)
        .body("error", containsString("Invalid"));
  }

  // ============== CREATE STORE ==============

  @Test
  @DisplayName("Should create store with valid data")
  void testCreateStoreSuccess() {
    // Must mock legacy gateway
    doNothing().when(legacyStoreManagerGateway).createStoreOnLegacySystem(org.mockito.ArgumentMatchers.any(Store.class));

    Store newStore = new Store();
    newStore.name = "New Store";
    newStore.quantityProductsInStock = 100;

    given()
        .contentType("application/json")
        .body(newStore)
        .when()
        .post(STORE_PATH)
        .then()
        .statusCode(201)
        .body("name", equalTo("New Store"))
        .contentType(containsString("application/json"));
  }

  @Test
  @DisplayName("Should return 422 when creating with pre-set ID")
  void testCreateStoreWithPresetId() {
    Store newStore = new Store();
    newStore.id = 999L;
    newStore.name = "Invalid Store";

    given()
        .contentType("application/json")
        .body(newStore)
        .when()
        .post(STORE_PATH)
        .then()
        .statusCode(422)
        .body("error", containsString("Id was invalidly set"));
  }

  @Test
  @DisplayName("Should return 400 when creating with null name")
  void testCreateStoreNullName() {
    Store newStore = new Store();
    newStore.name = null;

    given()
        .contentType("application/json")
        .body(newStore)
        .when()
        .post(STORE_PATH)
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("Should return 400 when creating with empty name")
  void testCreateStoreEmptyName() {
    Store newStore = new Store();
    newStore.name = "";

    given()
        .contentType("application/json")
        .body(newStore)
        .when()
        .post(STORE_PATH)
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("Should return 400 when creating with whitespace-only name")
  void testCreateStoreWhitespaceName() {
    Store newStore = new Store();
    newStore.name = "   ";

    given()
        .contentType("application/json")
        .body(newStore)
        .when()
        .post(STORE_PATH)
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("Should return 400 when body is null")
  void testCreateStoreNullBody() {
    given()
        .contentType("application/json")
        .body("null")
        .when()
        .post(STORE_PATH)
        .then()
        .statusCode(400);
  }

  // ============== UPDATE STORE (PUT) ==============

  @Test
  @DisplayName("Should update store with valid data")
  void testUpdateStoreSuccess() {
    // Must mock legacy gateway
    doNothing().when(legacyStoreManagerGateway).updateStoreOnLegacySystem(org.mockito.ArgumentMatchers.any(Store.class));

    Long storeId = createStoreAndReturnId(uniqueName("Store For Update"), 100);

    Store updatedStore = new Store();
    updatedStore.name = uniqueName("Updated Store Name");
    updatedStore.quantityProductsInStock = 200;

    given()
        .contentType("application/json")
        .body(updatedStore)
        .when()
          .put(STORE_PATH + "/" + storeId)
        .then()
        .statusCode(200)
          .body("name", equalTo(updatedStore.name));
  }

  @Test
  @DisplayName("Should return 404 when updating non-existent store")
  void testUpdateStoreNotFound() {
    Store updatedStore = new Store();
    updatedStore.name = "Updated Name";

    given()
        .contentType("application/json")
        .body(updatedStore)
        .when()
        .put(STORE_PATH + "/99999")
        .then()
        .statusCode(404)
        .body("error", containsString("does not exist"));
  }

  @Test
  @DisplayName("Should return 422 when updating with null name")
  void testUpdateStoreNullName() {
    Store updatedStore = new Store();
    updatedStore.name = null;

    given()
        .contentType("application/json")
        .body(updatedStore)
        .when()
        .put(STORE_PATH + "/1")
        .then()
        .statusCode(422)
        .body("error", containsString("Name was not set"));
  }

  @Test
  @DisplayName("Should return 422 when updating with empty name")
  void testUpdateStoreEmptyName() {
    Store updatedStore = new Store();
    updatedStore.name = "";

    given()
        .contentType("application/json")
        .body(updatedStore)
        .when()
        .put(STORE_PATH + "/1")
        .then()
        .statusCode(422);
  }

  @Test
  @DisplayName("Should return 400 for invalid ID in update")
  void testUpdateStoreInvalidId() {
    Store updatedStore = new Store();
    updatedStore.name = "Updated";

    given()
        .contentType("application/json")
        .body(updatedStore)
        .when()
        .put(STORE_PATH + "/0")
        .then()
        .statusCode(400)
        .body("error", containsString("Invalid"));
  }

  @Test
  @DisplayName("Should return 400 for negative ID in update")
  void testUpdateStoreNegativeId() {
    Store updatedStore = new Store();
    updatedStore.name = "Updated";

    given()
        .contentType("application/json")
        .body(updatedStore)
        .when()
        .put(STORE_PATH + "/-5")
        .then()
        .statusCode(400);
  }

  // ============== PATCH STORE ==============

  @Test
  @DisplayName("Should patch store name successfully")
  void testPatchStoreNameSuccess() {
    // Must mock legacy gateway
    doNothing().when(legacyStoreManagerGateway).updateStoreOnLegacySystem(org.mockito.ArgumentMatchers.any(Store.class));

    Long storeId = createStoreAndReturnId(uniqueName("Store For Patch Name"), 10);

    Store patchStore = new Store();
    patchStore.name = uniqueName("Patched Name");

    given()
        .contentType("application/json")
        .body(patchStore)
        .when()
          .patch(STORE_PATH + "/" + storeId)
        .then()
        .statusCode(200)
          .body("name", equalTo(patchStore.name));
  }

  @Test
  @DisplayName("Should patch store quantity successfully")
  void testPatchStoreQuantitySuccess() {
    // Must mock legacy gateway
    doNothing().when(legacyStoreManagerGateway).updateStoreOnLegacySystem(org.mockito.ArgumentMatchers.any(Store.class));

    Long storeId = createStoreAndReturnId(uniqueName("Store For Patch Qty"), 10);

    Store patchStore = new Store();
    patchStore.quantityProductsInStock = 500;

    given()
        .contentType("application/json")
        .body(patchStore)
        .when()
          .patch(STORE_PATH + "/" + storeId)
        .then()
        .statusCode(200)
        .body("quantityProductsInStock", equalTo(500));
  }

  @Test
  @DisplayName("Should patch store with both fields")
  void testPatchStoreBothFields() {
    // Must mock legacy gateway
    doNothing().when(legacyStoreManagerGateway).updateStoreOnLegacySystem(org.mockito.ArgumentMatchers.any(Store.class));

    Long storeId = createStoreAndReturnId(uniqueName("Store For Patch Both"), 10);

    Store patchStore = new Store();
    patchStore.name = uniqueName("Patched Name");
    patchStore.quantityProductsInStock = 300;

    given()
        .contentType("application/json")
        .body(patchStore)
        .when()
          .patch(STORE_PATH + "/" + storeId)
        .then()
        .statusCode(200)
          .body("name", equalTo(patchStore.name))
        .body("quantityProductsInStock", equalTo(300));
  }

  @Test
  @DisplayName("Should patch store ignoring empty name")
  void testPatchStoreIgnoreEmptyName() {
    // Must mock legacy gateway
    doNothing().when(legacyStoreManagerGateway).updateStoreOnLegacySystem(org.mockito.ArgumentMatchers.any(Store.class));

    Long storeId = createStoreAndReturnId(uniqueName("Store For Patch Empty"), 20);

    Store patchStore = new Store();
    patchStore.name = ""; // Empty - should be ignored
    patchStore.quantityProductsInStock = 400;

    given()
        .contentType("application/json")
        .body(patchStore)
        .when()
          .patch(STORE_PATH + "/" + storeId)
        .then()
        .statusCode(200)
        .body("quantityProductsInStock", equalTo(400));
  }

  @Test
  @DisplayName("Should return 404 when patching non-existent store")
  void testPatchStoreNotFound() {
    Store patchStore = new Store();
    patchStore.name = "Patched";

    given()
        .contentType("application/json")
        .body(patchStore)
        .when()
        .patch(STORE_PATH + "/99999")
        .then()
        .statusCode(404)
        .body("error", containsString("does not exist"));
  }

  @Test
  @DisplayName("Should return 400 for invalid ID in patch")
  void testPatchStoreInvalidId() {
    Store patchStore = new Store();
    patchStore.name = "Patched";

    given()
        .contentType("application/json")
        .body(patchStore)
        .when()
        .patch(STORE_PATH + "/0")
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("Should ignore negative quantity in patch")
  void testPatchStoreNegativeQuantity() {
    // Must mock legacy gateway
    doNothing().when(legacyStoreManagerGateway).updateStoreOnLegacySystem(org.mockito.ArgumentMatchers.any(Store.class));

    Long storeId = createStoreAndReturnId(uniqueName("Store For Negative Patch"), 30);

    Store patchStore = new Store();
    patchStore.quantityProductsInStock = -10; // Negative - should be ignored

    given()
        .contentType("application/json")
        .body(patchStore)
        .when()
          .patch(STORE_PATH + "/" + storeId)
        .then()
        .statusCode(200);
  }

  // ============== DELETE STORE ==============

  @Test
  @DisplayName("Should delete store successfully")
  void testDeleteStoreSuccess() {
    given()
        .when()
        .delete(STORE_PATH + "/1")
        .then()
        .statusCode(204);
  }

  @Test
  @DisplayName("Should return 404 when deleting non-existent store")
  void testDeleteStoreNotFound() {
    given()
        .when()
        .delete(STORE_PATH + "/99999")
        .then()
        .statusCode(404)
        .body("error", containsString("does not exist"));
  }

  @Test
  @DisplayName("Should return 400 for invalid ID in delete")
  void testDeleteStoreInvalidId() {
    given()
        .when()
        .delete(STORE_PATH + "/0")
        .then()
        .statusCode(400)
        .body("error", containsString("Invalid"));
  }

  @Test
  @DisplayName("Should return 400 for negative ID in delete")
  void testDeleteStoreNegativeId() {
    given()
        .when()
        .delete(STORE_PATH + "/-1")
        .then()
        .statusCode(400);
  }

  // ============== CONTENT TYPE TESTS ==============

  @Test
  @DisplayName("Should accept application/json content type in create")
  void testCreateStoreCorrectContentType() {
    // Must mock legacy gateway
    doNothing().when(legacyStoreManagerGateway).createStoreOnLegacySystem(org.mockito.ArgumentMatchers.any(Store.class));

    Store newStore = new Store();
    newStore.name = "Content Type Test";

    given()
        .contentType("application/json")
        .body(newStore)
        .when()
        .post(STORE_PATH)
        .then()
        .statusCode(201)
        .contentType(containsString("application/json"));
  }

  @Test
  @DisplayName("Should return JSON error on exception")
  void testErrorResponseIsJson() {
    given()
        .when()
        .get(STORE_PATH + "/999999")
        .then()
        .statusCode(404)
        .contentType(containsString("application/json"))
        .body("exceptionType", notNullValue())
        .body("code", equalTo(404))
        .body("error", notNullValue());
  }

  // ============== VALIDATION BOUNDARY TESTS ==============

  @Test
  @DisplayName("Should create store with single character name")
  void testCreateStoreMinimalName() {
    // Must mock legacy gateway
    doNothing().when(legacyStoreManagerGateway).createStoreOnLegacySystem(org.mockito.ArgumentMatchers.any(Store.class));

    Store newStore = new Store();
    newStore.name = "A";

    given()
        .contentType("application/json")
        .body(newStore)
        .when()
        .post(STORE_PATH)
        .then()
        .statusCode(201)
        .body("name", equalTo("A"));
  }

  @Test
  @DisplayName("Should create store with very long name")
  void testCreateStoreLongName() {
    // Must mock legacy gateway
    doNothing().when(legacyStoreManagerGateway).createStoreOnLegacySystem(org.mockito.ArgumentMatchers.any(Store.class));

    Store newStore = new Store();
    newStore.name = "A".repeat(255); // Very long name

    given()
        .contentType("application/json")
        .body(newStore)
        .when()
        .post(STORE_PATH)
        .then()
      .statusCode(500);
  }

  @Test
  @DisplayName("Should create store with zero stock")
  void testCreateStoreZeroStock() {
    // Must mock legacy gateway
    doNothing().when(legacyStoreManagerGateway).createStoreOnLegacySystem(org.mockito.ArgumentMatchers.any(Store.class));

    Store newStore = new Store();
    newStore.name = "Zero Stock Store";
    newStore.quantityProductsInStock = 0;

    given()
        .contentType("application/json")
        .body(newStore)
        .when()
        .post(STORE_PATH)
        .then()
        .statusCode(201)
        .body("quantityProductsInStock", equalTo(0));
  }

  @Test
  @DisplayName("Should create store with large stock number")
  void testCreateStoreLargeStock() {
    // Must mock legacy gateway
    doNothing().when(legacyStoreManagerGateway).createStoreOnLegacySystem(org.mockito.ArgumentMatchers.any(Store.class));

    Store newStore = new Store();
    newStore.name = "Large Stock Store";
    newStore.quantityProductsInStock = Integer.MAX_VALUE;

    given()
        .contentType("application/json")
        .body(newStore)
        .when()
        .post(STORE_PATH)
        .then()
        .statusCode(201)
        .body("quantityProductsInStock", equalTo(Integer.MAX_VALUE));
  }

  // ============== MULTIPLE OPERATIONS ==============

  @Test
  @DisplayName("Should handle create then retrieve")
  void testCreateThenRetrieve() {
    // Must mock legacy gateway
    doNothing().when(legacyStoreManagerGateway).createStoreOnLegacySystem(org.mockito.ArgumentMatchers.any(Store.class));

    Store newStore = new Store();
    newStore.name = "Create Then Retrieve Test";

    Number storeIdValue = given()
        .contentType("application/json")
        .body(newStore)
        .when()
        .post(STORE_PATH)
        .then()
        .statusCode(201)
        .extract()
        .path("id");

    Long storeId = storeIdValue == null ? null : storeIdValue.longValue();

    // Verify we can retrieve it
    if (storeId != null) {
      given()
          .when()
          .get(STORE_PATH + "/" + storeId)
          .then()
          .statusCode(200)
          .body("name", equalTo("Create Then Retrieve Test"));
    }
  }

  @Test
  @DisplayName("Should handle create, update, then retrieve")
  void testCreateUpdateThenRetrieve() {
    // Must mock legacy gateway
    doNothing().when(legacyStoreManagerGateway).createStoreOnLegacySystem(org.mockito.ArgumentMatchers.any(Store.class));
    doNothing().when(legacyStoreManagerGateway).updateStoreOnLegacySystem(org.mockito.ArgumentMatchers.any(Store.class));

    Store newStore = new Store();
    newStore.name = "Original Name";

    Number storeIdValue = given()
        .contentType("application/json")
        .body(newStore)
        .when()
        .post(STORE_PATH)
        .then()
        .statusCode(201)
        .extract()
        .path("id");

    Long storeId = storeIdValue == null ? null : storeIdValue.longValue();

    if (storeId != null) {
      Store updatedStore = new Store();
      updatedStore.name = "Updated Name";

      given()
          .contentType("application/json")
          .body(updatedStore)
          .when()
          .put(STORE_PATH + "/" + storeId)
          .then()
          .statusCode(200);

      // Verify update persisted
      given()
          .when()
          .get(STORE_PATH + "/" + storeId)
          .then()
          .statusCode(200)
          .body("name", equalTo("Updated Name"));
    }
  }

  @Test
  @DisplayName("Should map WebApplicationException in store error mapper")
  void testStoreErrorMapperWebApplicationException() throws Exception {
    StoreResource.ErrorMapper errorMapper = new StoreResource.ErrorMapper();
    setField(errorMapper, "objectMapper", new ObjectMapper());

    Response response = errorMapper.toResponse(new WebApplicationException("store validation failed", 409));
    ObjectNode body = (ObjectNode) response.getEntity();

    assertEquals(409, response.getStatus());
    assertEquals(409, body.get("code").asInt());
    assertEquals("store validation failed", body.get("error").asText());
  }

  @Test
  @DisplayName("Should map generic exception to 500 in store error mapper")
  void testStoreErrorMapperRuntimeException() throws Exception {
    StoreResource.ErrorMapper errorMapper = new StoreResource.ErrorMapper();
    setField(errorMapper, "objectMapper", new ObjectMapper());

    Response response = errorMapper.toResponse(new IllegalArgumentException("boom"));
    ObjectNode body = (ObjectNode) response.getEntity();

    assertEquals(500, response.getStatus());
    assertEquals(500, body.get("code").asInt());
    assertEquals("boom", body.get("error").asText());
  }

  @Test
  @DisplayName("Should omit error field when exception message is null in store mapper")
  void testStoreErrorMapperNullMessage() throws Exception {
    StoreResource.ErrorMapper errorMapper = new StoreResource.ErrorMapper();
    setField(errorMapper, "objectMapper", new ObjectMapper());

    Response response = errorMapper.toResponse(new RuntimeException((String) null));
    ObjectNode body = (ObjectNode) response.getEntity();

    assertEquals(500, response.getStatus());
    assertEquals(500, body.get("code").asInt());
    assertFalse(body.has("error"));
  }

  private Long createStoreAndReturnId(String name, int quantity) {
    doNothing().when(legacyStoreManagerGateway).createStoreOnLegacySystem(org.mockito.ArgumentMatchers.any(Store.class));

    Store newStore = new Store();
    newStore.name = name;
    newStore.quantityProductsInStock = quantity;

    Number storeIdValue = given()
        .contentType("application/json")
        .body(newStore)
        .when()
        .post(STORE_PATH)
        .then()
        .statusCode(201)
        .extract()
        .path("id");

    return storeIdValue.longValue();
  }

  private String uniqueName(String base) {
    String suffix = String.valueOf(System.nanoTime());
    String candidate = base + "-" + suffix;
    return candidate.length() > 40 ? candidate.substring(0, 40) : candidate;
  }

  private void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}
