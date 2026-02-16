package com.fulfilment.application.monolith.fulfillment.domain.usecases;

import com.fulfilment.application.monolith.fulfillment.domain.models.WarehouseProductStore;
import com.fulfilment.application.monolith.fulfillment.domain.ports.WarehouseProductStoreStore;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AssociateWarehouseToProductStoreUseCase Tests")
public class AssociateWarehouseToProductStoreUseCaseTest {

  private AssociateWarehouseToProductStoreUseCase useCase;
  private WarehouseProductStoreStore warehouseProductStoreStore;

  @BeforeEach
  void setup() {
    warehouseProductStoreStore = mock(WarehouseProductStoreStore.class);
    useCase = new AssociateWarehouseToProductStoreUseCase(warehouseProductStoreStore);
  }

  // ============== SUCCESSFUL ASSOCIATION TESTS ==============

  @Test
  @DisplayName("Should successfully associate warehouse with product and store")
  void testAssociateSuccess() {
    // Given
    Long productId = 1L;
    Long storeId = 1L;
    String warehouseCode = "WH-001";

    when(warehouseProductStoreStore.findAssociation(productId, storeId, warehouseCode))
        .thenReturn(null);
    when(warehouseProductStoreStore.findByProductAndStore(productId, storeId))
        .thenReturn(new ArrayList<>());
    when(warehouseProductStoreStore.findByStore(storeId))
        .thenReturn(new ArrayList<>());
    when(warehouseProductStoreStore.findByWarehouse(warehouseCode))
        .thenReturn(new ArrayList<>());

    // When
    useCase.associate(productId, storeId, warehouseCode);

    // Then
    verify(warehouseProductStoreStore).create(any(WarehouseProductStore.class));
  }

  @Test
  @DisplayName("Should successfully associate when warehouse already exists for store with different product")
  void testAssociateWithExistingWarehouseInStore() {
    // Given
    Long productId = 2L;
    Long storeId = 1L;
    String warehouseCode = "WH-001";

    // Existing association with different product
    WarehouseProductStore existing = new WarehouseProductStore(1L, storeId, warehouseCode);
    List<WarehouseProductStore> existingForStore = new ArrayList<>();
    existingForStore.add(existing);

    when(warehouseProductStoreStore.findAssociation(productId, storeId, warehouseCode))
        .thenReturn(null);
    when(warehouseProductStoreStore.findByProductAndStore(productId, storeId))
        .thenReturn(new ArrayList<>());
    when(warehouseProductStoreStore.findByStore(storeId))
        .thenReturn(existingForStore); // Warehouse already in store
    when(warehouseProductStoreStore.findByWarehouse(warehouseCode))
        .thenReturn(existingForStore);

    // When
    useCase.associate(productId, storeId, warehouseCode);

    // Then - Should allow adding product to existing warehouse-store
    verify(warehouseProductStoreStore).create(any(WarehouseProductStore.class));
  }

  // ============== DUPLICATE ASSOCIATION TESTS ==============

  @Test
  @DisplayName("Should reject duplicate association (conflict 409)")
  void testAssociateDuplicateConflict() {
    // Given
    Long productId = 1L;
    Long storeId = 1L;
    String warehouseCode = "WH-001";

    WarehouseProductStore existingAssociation = new WarehouseProductStore(productId, storeId, warehouseCode);
    when(warehouseProductStoreStore.findAssociation(productId, storeId, warehouseCode))
        .thenReturn(existingAssociation);

    // When & Then
    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.associate(productId, storeId, warehouseCode);
    });

    assertEquals(409, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("Association already exists"));
    verify(warehouseProductStoreStore, never()).create(any());
  }

  // ============== CONSTRAINT 1: MAX 2 WAREHOUSES PER PRODUCT PER STORE ==============

  @Test
  @DisplayName("Should reject when product already has 2 warehouses for store (409)")
  void testAssociateMaxWarehousesPerProductPerStore() {
    // Given
    Long productId = 1L;
    Long storeId = 1L;
    String warehouseCode = "WH-003";

    // Product already has 2 warehouses for this store
    WarehouseProductStore wh1 = new WarehouseProductStore(productId, storeId, "WH-001");
    WarehouseProductStore wh2 = new WarehouseProductStore(productId, storeId, "WH-002");
    List<WarehouseProductStore> existingAssociations = new ArrayList<>();
    existingAssociations.add(wh1);
    existingAssociations.add(wh2);

    when(warehouseProductStoreStore.findAssociation(productId, storeId, warehouseCode))
        .thenReturn(null);
    when(warehouseProductStoreStore.findByProductAndStore(productId, storeId))
        .thenReturn(existingAssociations);

    // When & Then
    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.associate(productId, storeId, warehouseCode);
    });

    assertEquals(409, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("maximum (2) warehouses"));
    verify(warehouseProductStoreStore, never()).create(any());
  }

  @Test
  @DisplayName("Should allow 2nd warehouse for product-store pair")
  void testAssociateSecondWarehouseForProductStore() {
    // Given
    Long productId = 1L;
    Long storeId = 1L;
    String warehouseCode = "WH-002";

    // Product already has 1 warehouse for this store
    WarehouseProductStore wh1 = new WarehouseProductStore(productId, storeId, "WH-001");
    List<WarehouseProductStore> existingAssociations = new ArrayList<>();
    existingAssociations.add(wh1);

    when(warehouseProductStoreStore.findAssociation(productId, storeId, warehouseCode))
        .thenReturn(null);
    when(warehouseProductStoreStore.findByProductAndStore(productId, storeId))
        .thenReturn(existingAssociations);
    when(warehouseProductStoreStore.findByStore(storeId))
        .thenReturn(existingAssociations);
    when(warehouseProductStoreStore.findByWarehouse(warehouseCode))
        .thenReturn(new ArrayList<>());

    // When
    useCase.associate(productId, storeId, warehouseCode);

    // Then
    verify(warehouseProductStoreStore).create(any(WarehouseProductStore.class));
  }

  // ============== CONSTRAINT 2: MAX 3 WAREHOUSES PER STORE ==============

  @Test
  @DisplayName("Should reject when store already has 3 warehouses (409)")
  void testAssociateMaxWarehousesPerStore() {
    // Given
    Long productId = 4L;
    Long storeId = 1L;
    String warehouseCode = "WH-004";

    // Store already has 3 unique warehouses
    WarehouseProductStore a1 = new WarehouseProductStore(1L, storeId, "WH-001");
    WarehouseProductStore a2 = new WarehouseProductStore(2L, storeId, "WH-002");
    WarehouseProductStore a3 = new WarehouseProductStore(3L, storeId, "WH-003");
    List<WarehouseProductStore> storeAssociations = new ArrayList<>();
    storeAssociations.add(a1);
    storeAssociations.add(a2);
    storeAssociations.add(a3);

    when(warehouseProductStoreStore.findAssociation(productId, storeId, warehouseCode))
        .thenReturn(null);
    when(warehouseProductStoreStore.findByProductAndStore(productId, storeId))
        .thenReturn(new ArrayList<>());
    when(warehouseProductStoreStore.findByStore(storeId))
        .thenReturn(storeAssociations);

    // When & Then
    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.associate(productId, storeId, warehouseCode);
    });

    assertEquals(409, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("maximum (3) warehouses"));
    verify(warehouseProductStoreStore, never()).create(any());
  }

  @Test
  @DisplayName("Should allow 3rd warehouse for store if new warehouse")
  void testAssociateThirdWarehouseForStore() {
    // Given
    Long productId = 3L;
    Long storeId = 1L;
    String warehouseCode = "WH-003";

    // Store already has 2 unique warehouses
    WarehouseProductStore a1 = new WarehouseProductStore(1L, storeId, "WH-001");
    WarehouseProductStore a2 = new WarehouseProductStore(2L, storeId, "WH-002");
    List<WarehouseProductStore> storeAssociations = new ArrayList<>();
    storeAssociations.add(a1);
    storeAssociations.add(a2);

    when(warehouseProductStoreStore.findAssociation(productId, storeId, warehouseCode))
        .thenReturn(null);
    when(warehouseProductStoreStore.findByProductAndStore(productId, storeId))
        .thenReturn(new ArrayList<>());
    when(warehouseProductStoreStore.findByStore(storeId))
        .thenReturn(storeAssociations);
    when(warehouseProductStoreStore.findByWarehouse(warehouseCode))
        .thenReturn(new ArrayList<>());

    // When
    useCase.associate(productId, storeId, warehouseCode);

    // Then
    verify(warehouseProductStoreStore).create(any(WarehouseProductStore.class));
  }

  // ============== CONSTRAINT 3: MAX 5 PRODUCTS PER WAREHOUSE ==============

  @Test
  @DisplayName("Should reject when warehouse already has 5 products for store (409)")
  void testAssociateMaxProductsPerWarehouse() {
    // Given
    Long productId = 6L;
    Long storeId = 1L;
    String warehouseCode = "WH-001";

    // Warehouse already has 5 products for this store
    List<WarehouseProductStore> warehouseAssociations = new ArrayList<>();
    for (int i = 1; i <= 5; i++) {
      warehouseAssociations.add(new WarehouseProductStore((long) i, storeId, warehouseCode));
    }

    when(warehouseProductStoreStore.findAssociation(productId, storeId, warehouseCode))
        .thenReturn(null);
    when(warehouseProductStoreStore.findByProductAndStore(productId, storeId))
        .thenReturn(new ArrayList<>());
    when(warehouseProductStoreStore.findByStore(storeId))
        .thenReturn(warehouseAssociations);
    when(warehouseProductStoreStore.findByWarehouse(warehouseCode))
        .thenReturn(warehouseAssociations);

    // When & Then
    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.associate(productId, storeId, warehouseCode);
    });

    assertEquals(409, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("maximum (5) product types"));
    verify(warehouseProductStoreStore, never()).create(any());
  }

  @Test
  @DisplayName("Should allow 5th product for warehouse in store")
  void testAssociateFifthProductForWarehouse() {
    // Given
    Long productId = 5L;
    Long storeId = 1L;
    String warehouseCode = "WH-001";

    // Warehouse has 4 products for this store
    List<WarehouseProductStore> warehouseAssociations = new ArrayList<>();
    for (int i = 1; i <= 4; i++) {
      warehouseAssociations.add(new WarehouseProductStore((long) i, storeId, warehouseCode));
    }

    when(warehouseProductStoreStore.findAssociation(productId, storeId, warehouseCode))
        .thenReturn(null);
    when(warehouseProductStoreStore.findByProductAndStore(productId, storeId))
        .thenReturn(new ArrayList<>());
    when(warehouseProductStoreStore.findByStore(storeId))
        .thenReturn(warehouseAssociations);
    when(warehouseProductStoreStore.findByWarehouse(warehouseCode))
        .thenReturn(warehouseAssociations);

    // When
    useCase.associate(productId, storeId, warehouseCode);

    // Then
    verify(warehouseProductStoreStore).create(any(WarehouseProductStore.class));
  }

  @Test
  @DisplayName("Should allow adding product when warehouse has products but for different store")
  void testAssociateWithWarehouseProductsForDifferentStore() {
    // Given
    Long productId = 1L;
    Long storeId = 2L;
    String warehouseCode = "WH-001";

    // Warehouse has 5 products but for different store
    List<WarehouseProductStore> warehouseAssociations = new ArrayList<>();
    for (int i = 1; i <= 5; i++) {
      warehouseAssociations.add(new WarehouseProductStore((long) i, 1L, warehouseCode)); // Different store
    }

    when(warehouseProductStoreStore.findAssociation(productId, storeId, warehouseCode))
        .thenReturn(null);
    when(warehouseProductStoreStore.findByProductAndStore(productId, storeId))
        .thenReturn(new ArrayList<>());
    when(warehouseProductStoreStore.findByStore(storeId))
        .thenReturn(new ArrayList<>());
    when(warehouseProductStoreStore.findByWarehouse(warehouseCode))
        .thenReturn(warehouseAssociations);

    // When - Should allow because constraint is per warehouse-store, not just warehouse
    useCase.associate(productId, storeId, warehouseCode);

    // Then
    verify(warehouseProductStoreStore).create(any(WarehouseProductStore.class));
  }

  // ============== DISSOCIATION TESTS ==============

  @Test
  @DisplayName("Should successfully dissociate warehouse from product-store")
  void testDissociateSuccess() {
    // Given
    Long productId = 1L;
    Long storeId = 1L;
    String warehouseCode = "WH-001";

    WarehouseProductStore existing = new WarehouseProductStore(productId, storeId, warehouseCode);
    when(warehouseProductStoreStore.findAssociation(productId, storeId, warehouseCode))
        .thenReturn(existing);

    // When
    useCase.dissociate(productId, storeId, warehouseCode);

    // Then
    verify(warehouseProductStoreStore).remove(productId, storeId, warehouseCode);
  }

  @Test
  @DisplayName("Should reject dissociation of non-existent association (404)")
  void testDissociateNotFound() {
    // Given
    Long productId = 999L;
    Long storeId = 999L;
    String warehouseCode = "WH-INVALID";

    when(warehouseProductStoreStore.findAssociation(productId, storeId, warehouseCode))
        .thenReturn(null);

    // When & Then
    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.dissociate(productId, storeId, warehouseCode);
    });

    assertEquals(404, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("Association not found"));
    verify(warehouseProductStoreStore, never()).remove(any(), any(), any());
  }

  // ============== EDGE CASES ==============

  @Test
  @DisplayName("Should handle multiple warehouses with same warehouse code in store")
  void testAssociateMultipleProductsSameWarehouse() {
    // Given
    Long productId = 2L;
    Long storeId = 1L;
    String warehouseCode = "WH-001";

    WarehouseProductStore p1w1 = new WarehouseProductStore(1L, storeId, warehouseCode);
    List<WarehouseProductStore> storeAssociations = new ArrayList<>();
    storeAssociations.add(p1w1);

    when(warehouseProductStoreStore.findAssociation(productId, storeId, warehouseCode))
        .thenReturn(null);
    when(warehouseProductStoreStore.findByProductAndStore(productId, storeId))
        .thenReturn(new ArrayList<>());
    when(warehouseProductStoreStore.findByStore(storeId))
        .thenReturn(storeAssociations);
    when(warehouseProductStoreStore.findByWarehouse(warehouseCode))
        .thenReturn(storeAssociations);

    // When
    useCase.associate(productId, storeId, warehouseCode);

    // Then
    verify(warehouseProductStoreStore).create(any(WarehouseProductStore.class));
  }

  @Test
  @DisplayName("Should allow product association when reaching limits on different constraints")
  void testAssociateAtConstraintBoundaries() {
    // Given - All constraints at max but allowing new association
    Long productId = 2L;
    Long storeId = 1L;
    String warehouseCode = "WH-002";

    // 1 warehouse for this product-store pair (can add 1 more)
    WarehouseProductStore p2w1 = new WarehouseProductStore(productId, storeId, "WH-001");
    List<WarehouseProductStore> productStoreAssocs = new ArrayList<>();
    productStoreAssocs.add(p2w1);

    // 2 unique warehouses for store (can add 1 more)
    WarehouseProductStore p1w1 = new WarehouseProductStore(1L, storeId, "WH-001");
    WarehouseProductStore p1w2 = new WarehouseProductStore(1L, storeId, "WH-002");
    List<WarehouseProductStore> storeAssocs = new ArrayList<>();
    storeAssocs.add(p1w1);
    storeAssocs.add(p1w2);

    when(warehouseProductStoreStore.findAssociation(productId, storeId, warehouseCode))
        .thenReturn(null);
    when(warehouseProductStoreStore.findByProductAndStore(productId, storeId))
        .thenReturn(productStoreAssocs);
    when(warehouseProductStoreStore.findByStore(storeId))
        .thenReturn(storeAssocs);
    
    List<WarehouseProductStore> warehouseAssocs = new ArrayList<>();
    warehouseAssocs.add(p1w2);
    when(warehouseProductStoreStore.findByWarehouse(warehouseCode))
        .thenReturn(warehouseAssocs);

    // When
    useCase.associate(productId, storeId, warehouseCode);

    // Then
    verify(warehouseProductStoreStore).create(any(WarehouseProductStore.class));
  }
}
