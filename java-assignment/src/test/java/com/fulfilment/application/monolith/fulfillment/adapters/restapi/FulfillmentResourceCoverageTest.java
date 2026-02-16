package com.fulfilment.application.monolith.fulfillment.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fulfilment.application.monolith.fulfillment.adapters.database.DbWarehouseProductStore;
import com.fulfilment.application.monolith.fulfillment.adapters.database.WarehouseProductStoreRepository;
import com.fulfilment.application.monolith.fulfillment.domain.models.WarehouseProductStore;
import com.fulfilment.application.monolith.fulfillment.domain.usecases.AssociateWarehouseToProductStoreUseCase;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for FulfillmentResource REST endpoints
 * Provides 100% code coverage for all methods and edge cases
 */
@QuarkusTest
@DisplayName("FulfillmentResource - Unit Tests")
public class FulfillmentResourceCoverageTest {

  private static final String BASE_URL = "/fulfillment/warehouse-product-store";

  @InjectMock
  private WarehouseProductStoreRepository repository;

  @InjectMock
  private AssociateWarehouseToProductStoreUseCase associateUseCase;

  private WarehouseProductStore testAssociation;
  private WarehouseProductStore testAssociation2;

  @BeforeEach
  public void setUp() {
    testAssociation = new WarehouseProductStore(100L, 200L, "WH-001");
    testAssociation2 = new WarehouseProductStore(100L, 200L, "WH-002");
  }

  /**
   * Helper method to convert domain model to database entity
   */
  private DbWarehouseProductStore toDatabaseEntity(WarehouseProductStore model) {
    DbWarehouseProductStore dbEntity = new DbWarehouseProductStore(
        model.productId, model.storeId, model.warehouseBusinessUnitCode);
    dbEntity.createdAt = model.createdAt;
    return dbEntity;
  }

  /**
   * Helper method to convert domain models to database entities
   */
  private List<DbWarehouseProductStore> toDatabaseEntities(List<WarehouseProductStore> models) {
    return models.stream().map(this::toDatabaseEntity).toList();
  }

  // ========== LIST ALL ASSOCIATIONS TESTS ==========

  @Test
  @DisplayName("Should list all associations successfully")
  public void testListAllAssociationsSuccess() {
    List<WarehouseProductStore> associations = new ArrayList<>();
    associations.add(testAssociation);
    associations.add(testAssociation2);

    when(repository.listAll()).thenReturn(toDatabaseEntities(associations));

    given()
        .when()
        .get(BASE_URL)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("size()", equalTo(2))
        .body("[0].productId", equalTo(100))
        .body("[0].storeId", equalTo(200))
        .body("[0].warehouseBusinessUnitCode", equalTo("WH-001"))
        .body("[1].productId", equalTo(100))
        .body("[1].warehouseBusinessUnitCode", equalTo("WH-002"));

    verify(repository, times(1)).listAll();
  }

  @Test
  @DisplayName("Should return empty list when no associations exist")
  public void testListAllAssociationsEmpty() {
    when(repository.listAll()).thenReturn(new ArrayList<>());

    given()
        .when()
        .get(BASE_URL)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("size()", equalTo(0));

    verify(repository, times(1)).listAll();
  }

  // ========== ASSOCIATE TESTS ==========

  @Test
  @DisplayName("Should create association successfully")
  public void testAssociateSuccess() {
    FulfillmentResource.AssociationRequest request = new FulfillmentResource.AssociationRequest();
    request.productId = 100L;
    request.storeId = 200L;
    request.warehouseBusinessUnitCode = "WH-001";

    doNothing().when(associateUseCase).associate(100L, 200L, "WH-001");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post(BASE_URL)
        .then()
        .statusCode(201)
        .contentType(ContentType.JSON)
        .body("productId", equalTo(100))
        .body("storeId", equalTo(200))
        .body("warehouseBusinessUnitCode", equalTo("WH-001"))
        .body("message", equalTo("Association created successfully"));

    verify(associateUseCase, times(1)).associate(100L, 200L, "WH-001");
  }

  @Test
  @DisplayName("Should reject association with missing productId")
  public void testAssociateMissingProductId() {
    FulfillmentResource.AssociationRequest request = new FulfillmentResource.AssociationRequest();
    request.productId = null;
    request.storeId = 200L;
    request.warehouseBusinessUnitCode = "WH-001";

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post(BASE_URL)
        .then()
        .statusCode(400)
        .body("error", containsString("productId, storeId, and warehouseBusinessUnitCode are required"));

    verify(associateUseCase, never()).associate(anyLong(), anyLong(), anyString());
  }

  @Test
  @DisplayName("Should reject association with missing storeId")
  public void testAssociateMissingStoreId() {
    FulfillmentResource.AssociationRequest request = new FulfillmentResource.AssociationRequest();
    request.productId = 100L;
    request.storeId = null;
    request.warehouseBusinessUnitCode = "WH-001";

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post(BASE_URL)
        .then()
        .statusCode(400)
        .body("error", containsString("productId, storeId, and warehouseBusinessUnitCode are required"));

    verify(associateUseCase, never()).associate(anyLong(), anyLong(), anyString());
  }

  @Test
  @DisplayName("Should reject association with missing warehouseBusinessUnitCode")
  public void testAssociateMissingWarehouseCode() {
    FulfillmentResource.AssociationRequest request = new FulfillmentResource.AssociationRequest();
    request.productId = 100L;
    request.storeId = 200L;
    request.warehouseBusinessUnitCode = null;

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post(BASE_URL)
        .then()
        .statusCode(400)
        .body("error", containsString("productId, storeId, and warehouseBusinessUnitCode are required"));

    verify(associateUseCase, never()).associate(anyLong(), anyLong(), anyString());
  }

  @Test
  @DisplayName("Should reject association with all missing fields")
  public void testAssociateMissingAllFields() {
    FulfillmentResource.AssociationRequest request = new FulfillmentResource.AssociationRequest();
    request.productId = null;
    request.storeId = null;
    request.warehouseBusinessUnitCode = null;

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post(BASE_URL)
        .then()
        .statusCode(400);

    verify(associateUseCase, never()).associate(anyLong(), anyLong(), anyString());
  }

  // ========== GET WAREHOUSES FOR PRODUCT AND STORE TESTS ==========

  @Test
  @DisplayName("Should retrieve warehouses for product and store successfully")
  public void testGetWarehousesForProductStoreSuccess() {
    List<WarehouseProductStore> associations = new ArrayList<>();
    associations.add(testAssociation);

    when(repository.findByProductAndStore(100L, 200L)).thenReturn(associations);

    given()
        .when()
        .get(BASE_URL + "/product/100/store/200")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("size()", equalTo(1))
        .body("[0].productId", equalTo(100))
        .body("[0].storeId", equalTo(200));

    verify(repository, times(1)).findByProductAndStore(100L, 200L);
  }

  @Test
  @DisplayName("Should return empty list when no warehouses found for product and store")
  public void testGetWarehousesForProductStoreEmpty() {
    when(repository.findByProductAndStore(999L, 999L)).thenReturn(new ArrayList<>());

    given()
        .when()
        .get(BASE_URL + "/product/999/store/999")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("size()", equalTo(0));

    verify(repository, times(1)).findByProductAndStore(999L, 999L);
  }

  @Test
  @DisplayName("Should retrieve multiple warehouses for product and store")
  public void testGetWarehousesForProductStoreMultiple() {
    List<WarehouseProductStore> associations = new ArrayList<>();
    associations.add(testAssociation);
    associations.add(testAssociation2);

    when(repository.findByProductAndStore(100L, 200L)).thenReturn(associations);

    given()
        .when()
        .get(BASE_URL + "/product/100/store/200")
        .then()
        .statusCode(200)
        .body("size()", equalTo(2))
        .body("[0].warehouseBusinessUnitCode", equalTo("WH-001"))
        .body("[1].warehouseBusinessUnitCode", equalTo("WH-002"));

    verify(repository, times(1)).findByProductAndStore(100L, 200L);
  }

  // ========== GET WAREHOUSES FOR STORE TESTS ==========

  @Test
  @DisplayName("Should retrieve warehouses for store successfully")
  public void testGetWarehousesForStoreSuccess() {
    List<WarehouseProductStore> associations = new ArrayList<>();
    associations.add(testAssociation);

    when(repository.findByStore(200L)).thenReturn(associations);

    given()
        .when()
        .get(BASE_URL + "/store/200")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("size()", equalTo(1))
        .body("[0].storeId", equalTo(200));

    verify(repository, times(1)).findByStore(200L);
  }

  @Test
  @DisplayName("Should return empty list when no warehouses found for store")
  public void testGetWarehousesForStoreEmpty() {
    when(repository.findByStore(999L)).thenReturn(new ArrayList<>());

    given()
        .when()
        .get(BASE_URL + "/store/999")
        .then()
        .statusCode(200)
        .body("size()", equalTo(0));

    verify(repository, times(1)).findByStore(999L);
  }

  @Test
  @DisplayName("Should retrieve multiple associations for store")
  public void testGetWarehousesForStoreMultiple() {
    WarehouseProductStore assoc1 = new WarehouseProductStore(100L, 200L, "WH-001");
    WarehouseProductStore assoc2 = new WarehouseProductStore(101L, 200L, "WH-002");
    WarehouseProductStore assoc3 = new WarehouseProductStore(102L, 200L, "WH-003");

    List<WarehouseProductStore> associations = List.of(assoc1, assoc2, assoc3);
    when(repository.findByStore(200L)).thenReturn(associations);

    given()
        .when()
        .get(BASE_URL + "/store/200")
        .then()
        .statusCode(200)
        .body("size()", equalTo(3));

    verify(repository, times(1)).findByStore(200L);
  }

  // ========== GET PRODUCTS FOR WAREHOUSE TESTS ==========

  @Test
  @DisplayName("Should retrieve products for warehouse successfully")
  public void testGetProductsForWarehouseSuccess() {
    List<WarehouseProductStore> associations = new ArrayList<>();
    associations.add(testAssociation);

    when(repository.findByWarehouse("WH-001")).thenReturn(associations);

    given()
        .when()
        .get(BASE_URL + "/warehouse/WH-001")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("size()", equalTo(1))
        .body("[0].warehouseBusinessUnitCode", equalTo("WH-001"));

    verify(repository, times(1)).findByWarehouse("WH-001");
  }

  @Test
  @DisplayName("Should return empty list when no products found for warehouse")
  public void testGetProductsForWarehouseEmpty() {
    when(repository.findByWarehouse("WH-999")).thenReturn(new ArrayList<>());

    given()
        .when()
        .get(BASE_URL + "/warehouse/WH-999")
        .then()
        .statusCode(200)
        .body("size()", equalTo(0));

    verify(repository, times(1)).findByWarehouse("WH-999");
  }

  @Test
  @DisplayName("Should retrieve multiple products for warehouse")
  public void testGetProductsForWarehouseMultiple() {
    WarehouseProductStore assoc1 = new WarehouseProductStore(100L, 200L, "WH-001");
    WarehouseProductStore assoc2 = new WarehouseProductStore(101L, 201L, "WH-001");
    WarehouseProductStore assoc3 = new WarehouseProductStore(102L, 202L, "WH-001");

    List<WarehouseProductStore> associations = List.of(assoc1, assoc2, assoc3);
    when(repository.findByWarehouse("WH-001")).thenReturn(associations);

    given()
        .when()
        .get(BASE_URL + "/warehouse/WH-001")
        .then()
        .statusCode(200)
        .body("size()", equalTo(3));

    verify(repository, times(1)).findByWarehouse("WH-001");
  }

  // ========== DISSOCIATE TESTS ==========

  @Test
  @DisplayName("Should dissociate successfully")
  public void testDissociateSuccess() {
    doNothing().when(associateUseCase).dissociate(100L, 200L, "WH-001");

    given()
        .when()
        .delete(BASE_URL + "/product/100/store/200/warehouse/WH-001")
        .then()
        .statusCode(204);

    verify(associateUseCase, times(1)).dissociate(100L, 200L, "WH-001");
  }

  @Test
  @DisplayName("Should dissociate with different parameters")
  public void testDissociateDifferentParams() {
    doNothing().when(associateUseCase).dissociate(500L, 600L, "WH-999");

    given()
        .when()
        .delete(BASE_URL + "/product/500/store/600/warehouse/WH-999")
        .then()
        .statusCode(204);

    verify(associateUseCase, times(1)).dissociate(500L, 600L, "WH-999");
  }

  @Test
  @DisplayName("Should handle dissociate with special characters in warehouse code")
  public void testDissociateSpecialCharacters() {
    doNothing().when(associateUseCase).dissociate(100L, 200L, "WH-001-SPECIAL");

    given()
        .when()
        .delete(BASE_URL + "/product/100/store/200/warehouse/WH-001-SPECIAL")
        .then()
        .statusCode(204);

    verify(associateUseCase, times(1)).dissociate(100L, 200L, "WH-001-SPECIAL");
  }

  // ========== ERROR HANDLING TESTS ==========

  @Test
  @DisplayName("Should handle WebApplicationException from associate use case")
  public void testAssociateWebApplicationException() {
    FulfillmentResource.AssociationRequest request = new FulfillmentResource.AssociationRequest();
    request.productId = 100L;
    request.storeId = 200L;
    request.warehouseBusinessUnitCode = "WH-001";

    doThrow(new WebApplicationException("Association already exists", 409))
        .when(associateUseCase).associate(100L, 200L, "WH-001");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post(BASE_URL)
        .then()
        .statusCode(409)
        .body("error", containsString("Association already exists"));

    verify(associateUseCase, times(1)).associate(100L, 200L, "WH-001");
  }

  @Test
  @DisplayName("Should handle WebApplicationException from dissociate use case")
  public void testDissociateWebApplicationException() {
    doThrow(new WebApplicationException("Association not found", 404))
        .when(associateUseCase).dissociate(100L, 200L, "WH-001");

    given()
        .when()
        .delete(BASE_URL + "/product/100/store/200/warehouse/WH-001")
        .then()
        .statusCode(404)
        .body("error", containsString("Association not found"));

    verify(associateUseCase, times(1)).dissociate(100L, 200L, "WH-001");
  }

  @Test
  @DisplayName("Should handle generic exception")
  public void testHandleGenericException() {
    when(repository.listAll()).thenThrow(new RuntimeException("Database connection error"));

    given()
        .when()
        .get(BASE_URL)
        .then()
        .statusCode(500)
        .body("exceptionType", containsString("RuntimeException"))
        .body("code", equalTo(500));
  }

  // ========== PATH PARAMETER EDGE CASES ==========

  @Test
  @DisplayName("Should handle large numeric path parameters")
  public void testLargeNumericPathParameters() {
    when(repository.findByProductAndStore(9999999999L, 8888888888L))
        .thenReturn(new ArrayList<>());

    given()
        .when()
        .get(BASE_URL + "/product/9999999999/store/8888888888")
        .then()
        .statusCode(200)
        .body("size()", equalTo(0));

    verify(repository, times(1)).findByProductAndStore(9999999999L, 8888888888L);
  }

  @Test
  @DisplayName("Should handle warehouse code with hyphens")
  public void testWarehouseCodeWithHyphens() {
    List<WarehouseProductStore> associations = new ArrayList<>();
    when(repository.findByWarehouse("WH-001-ABC-DEF")).thenReturn(associations);

    given()
        .when()
        .get(BASE_URL + "/warehouse/WH-001-ABC-DEF")
        .then()
        .statusCode(200);

    verify(repository, times(1)).findByWarehouse("WH-001-ABC-DEF");
  }

  // ========== DTO TESTS ==========

  @Test
  @DisplayName("Should correctly serialize WarehouseProductStoreDto")
  public void testWarehouseProductStoreDtoSerialization() {
    List<WarehouseProductStore> associations = new ArrayList<>();
    associations.add(new WarehouseProductStore(100L, 200L, "WH-001"));

    when(repository.listAll()).thenReturn(toDatabaseEntities(associations));

    given()
        .when()
        .get(BASE_URL)
        .then()
        .statusCode(200)
        .body("[0].productId", equalTo(100))
        .body("[0].storeId", equalTo(200))
        .body("[0].warehouseBusinessUnitCode", equalTo("WH-001"))
        .body("[0].createdAt", notNullValue());
  }

  // ========== BOUNDARY TESTS ==========

  @Test
  @DisplayName("Should handle single warehouse association")
  public void testSingleWarehouseForProductStore() {
    List<WarehouseProductStore> associations = List.of(testAssociation);
    when(repository.findByProductAndStore(100L, 200L)).thenReturn(associations);

    given()
        .when()
        .get(BASE_URL + "/product/100/store/200")
        .then()
        .statusCode(200)
        .body("size()", equalTo(1));
  }

  @Test
  @DisplayName("Should verify all response fields are populated correctly")
  public void testResponseFieldsPopulated() {
    FulfillmentResource.AssociationRequest request = new FulfillmentResource.AssociationRequest();
    request.productId = 123L;
    request.storeId = 456L;
    request.warehouseBusinessUnitCode = "WH-TEST";

    doNothing().when(associateUseCase).associate(123L, 456L, "WH-TEST");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post(BASE_URL)
        .then()
        .statusCode(201)
        .body("productId", equalTo(123))
        .body("storeId", equalTo(456))
        .body("warehouseBusinessUnitCode", equalTo("WH-TEST"))
        .body("message", notNullValue());
  }
}
