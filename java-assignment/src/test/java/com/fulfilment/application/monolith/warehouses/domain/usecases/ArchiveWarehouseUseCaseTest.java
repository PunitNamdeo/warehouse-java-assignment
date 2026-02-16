package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;

@DisplayName("ArchiveWarehouseUseCase Tests")
public class ArchiveWarehouseUseCaseTest {

  private ArchiveWarehouseUseCase useCase;
  private WarehouseStore warehouseStore;

  @BeforeEach
  void setup() {
    warehouseStore = mock(WarehouseStore.class);
    useCase = new ArchiveWarehouseUseCase(warehouseStore);
  }

  // ============== SUCCESSFUL ARCHIVE TESTS ==============

  @Test
  @DisplayName("Should successfully archive active warehouse")
  void testArchiveWarehouseSuccess() {
    // Given
    Warehouse activeWarehouse = new Warehouse();
    activeWarehouse.businessUnitCode = "WH-001";
    activeWarehouse.stock = 100;
    activeWarehouse.capacity = 200;
    activeWarehouse.location = "ZWOLLE-001";
    activeWarehouse.archivedAt = null; // Active warehouse

    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(activeWarehouse);

    // When
    useCase.archive(activeWarehouse);

    // Then
    assertNotNull(activeWarehouse.archivedAt);
    verify(warehouseStore).update(activeWarehouse);
  }

  @Test
  @DisplayName("Should archive warehouse with zero stock")
  void testArchiveWarehouseZeroStock() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-002";
    warehouse.stock = 0;
    warehouse.capacity = 200;
    warehouse.location = "ZWOLLE-001";
    warehouse.archivedAt = null;

    when(warehouseStore.findByBusinessUnitCode("WH-002")).thenReturn(warehouse);

    // When
    useCase.archive(warehouse);

    // Then
    assertNotNull(warehouse.archivedAt);
    verify(warehouseStore).update(warehouse);
  }

  @Test
  @DisplayName("Should archive warehouse with full capacity")
  void testArchiveWarehouseFullCapacity() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-003";
    warehouse.stock = 500;
    warehouse.capacity = 500;
    warehouse.location = "ZWOLLE-001";
    warehouse.archivedAt = null;

    when(warehouseStore.findByBusinessUnitCode("WH-003")).thenReturn(warehouse);

    // When
    useCase.archive(warehouse);

    // Then
    assertNotNull(warehouse.archivedAt);
    verify(warehouseStore).update(warehouse);
  }

  @Test
  @DisplayName("Should archive warehouse and preserve business unit code")
  void testArchiveWarehousePreservesCode() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "PRESERVE-CODE";
    warehouse.stock = 100;
    warehouse.capacity = 200;
    warehouse.location = "ZWOLLE-001";
    warehouse.archivedAt = null;

    when(warehouseStore.findByBusinessUnitCode("PRESERVE-CODE")).thenReturn(warehouse);

    // When
    useCase.archive(warehouse);

    // Then
    assertEquals("PRESERVE-CODE", warehouse.businessUnitCode);
    verify(warehouseStore).update(warehouse);
  }

  @Test
  @DisplayName("Should archive warehouse and preserve location")
  void testArchiveWarehousePreservesLocation() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-004";
    warehouse.stock = 100;
    warehouse.capacity = 200;
    warehouse.location = "AMSTERDAM-001";
    warehouse.archivedAt = null;

    when(warehouseStore.findByBusinessUnitCode("WH-004")).thenReturn(warehouse);

    // When
    useCase.archive(warehouse);

    // Then
    assertEquals("AMSTERDAM-001", warehouse.location);
    verify(warehouseStore).update(warehouse);
  }

  @Test
  @DisplayName("Should archive warehouse with createdAt timestamp")
  void testArchiveWarehouseWithCreatedAt() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-005";
    warehouse.stock = 100;
    warehouse.capacity = 200;
    warehouse.location = "ZWOLLE-001";
    warehouse.createdAt = LocalDateTime.now().minusDays(10);
    warehouse.archivedAt = null;

    when(warehouseStore.findByBusinessUnitCode("WH-005")).thenReturn(warehouse);

    // When
    useCase.archive(warehouse);

    // Then
    assertNotNull(warehouse.createdAt);
    assertNotNull(warehouse.archivedAt);
    assertTrue(warehouse.archivedAt.isAfter(warehouse.createdAt));
    verify(warehouseStore).update(warehouse);
  }

  @Test
  @DisplayName("Should set archivedAt timestamp when archiving")
  void testArchiveWarehouseSetsArchivedAtTimestamp() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-006";
    warehouse.stock = 100;
    warehouse.capacity = 200;
    warehouse.location = "ZWOLLE-001";
    warehouse.archivedAt = null;

    when(warehouseStore.findByBusinessUnitCode("WH-006")).thenReturn(warehouse);
    LocalDateTime beforeArchive = LocalDateTime.now();

    // When
    useCase.archive(warehouse);
    LocalDateTime afterArchive = LocalDateTime.now();

    // Then
    assertNotNull(warehouse.archivedAt);
    assertTrue(warehouse.archivedAt.isAfter(beforeArchive.minusSeconds(1)));
    assertTrue(warehouse.archivedAt.isBefore(afterArchive.plusSeconds(1)));
  }

  // ============== NOT FOUND TESTS ==============

  @Test
  @DisplayName("Should reject archiving non-existent warehouse (404)")
  void testArchiveWarehouseNotFound() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "INVALID-WH";
    warehouse.archivedAt = null;

    when(warehouseStore.findByBusinessUnitCode("INVALID-WH")).thenReturn(null);

    // When & Then
    Exception exception = assertThrows(WebApplicationException.class, () -> {
      useCase.archive(warehouse);
    });
    assertTrue(exception.getMessage().contains("not found"));
  }

  @Test
  @DisplayName("Should return 404 status for non-existent warehouse")
  void testArchiveWarehouseNotFoundStatus() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "NOT-EXIST";

    when(warehouseStore.findByBusinessUnitCode("NOT-EXIST")).thenReturn(null);

    // When & Then
    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.archive(warehouse);
    });
    assertEquals(404, exception.getResponse().getStatus());
  }

  @Test
  @DisplayName("Should include business unit code in error message")
  void testArchiveWarehouseNotFoundErrorMessage() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "ERROR-CODE-123";

    when(warehouseStore.findByBusinessUnitCode("ERROR-CODE-123")).thenReturn(null);

    // When & Then
    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.archive(warehouse);
    });
    assertTrue(exception.getMessage().contains("ERROR-CODE-123"));
  }

  // ============== ALREADY ARCHIVED TESTS ==============

  @Test
  @DisplayName("Should re-archive already archived warehouse")
  void testArchiveAlreadyArchivedWarehouse() {
    // Given - warehouse already has archivedAt set
    Warehouse archivedWarehouse = new Warehouse();
    archivedWarehouse.businessUnitCode = "WH-001";
    archivedWarehouse.archivedAt = LocalDateTime.now().minusHours(1);

    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(archivedWarehouse);

    // When - archive again (implementation allows this)
    useCase.archive(archivedWarehouse);

    // Then
    assertNotNull(archivedWarehouse.archivedAt);
    verify(warehouseStore).update(archivedWarehouse);
  }

  @Test
  @DisplayName("Should allow archiving warehouse that was already archived")
  void testArchiveMultipleTimes() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-007";
    warehouse.stock = 100;
    warehouse.capacity = 200;
    warehouse.location = "ZWOLLE-001";
    warehouse.archivedAt = LocalDateTime.now().minusDays(5);

    LocalDateTime previousArchiveTime = warehouse.archivedAt;

    when(warehouseStore.findByBusinessUnitCode("WH-007")).thenReturn(warehouse);

    // When
    useCase.archive(warehouse);

    // Then
    assertNotNull(warehouse.archivedAt);
    // Archive time is updated to new time (not preserved from before)
    assertTrue(warehouse.archivedAt.isAfter(previousArchiveTime));
    verify(warehouseStore).update(warehouse);
  }

  // ============== CALL VERIFICATION TESTS ==============

  @Test
  @DisplayName("Should call warehouseStore update exactly once")
  void testArchiveCallsUpdateOnce() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-008";
    warehouse.stock = 100;
    warehouse.capacity = 200;
    warehouse.location = "ZWOLLE-001";
    warehouse.archivedAt = null;

    when(warehouseStore.findByBusinessUnitCode("WH-008")).thenReturn(warehouse);

    // When
    useCase.archive(warehouse);

    // Then
    verify(warehouseStore, times(1)).update(warehouse);
  }

  @Test
  @DisplayName("Should call update with modified warehouse object")
  void testArchiveUpdatesCorrectWarehouse() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-009";
    warehouse.stock = 100;
    warehouse.capacity = 200;
    warehouse.location = "ZWOLLE-001";
    warehouse.archivedAt = null;

    when(warehouseStore.findByBusinessUnitCode("WH-009")).thenReturn(warehouse);

    // When
    useCase.archive(warehouse);

    // Then
    verify(warehouseStore).update(argThat(w -> w.businessUnitCode.equals("WH-009")));
  }

  // ============== EDGE CASES ==============

  @Test
  @DisplayName("Should handle warehouse with null stock")
  void testArchiveWarehouseNullStock() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-010";
    warehouse.stock = null;
    warehouse.capacity = 200;
    warehouse.location = "ZWOLLE-001";
    warehouse.archivedAt = null;

    when(warehouseStore.findByBusinessUnitCode("WH-010")).thenReturn(warehouse);

    // When
    useCase.archive(warehouse);

    // Then
    assertNotNull(warehouse.archivedAt);
    verify(warehouseStore).update(warehouse);
  }

  @Test
  @DisplayName("Should handle warehouse with special characters in code")
  void testArchiveWarehouseSpecialCharacterCode() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-@#$-001";
    warehouse.stock = 100;
    warehouse.capacity = 200;
    warehouse.location = "ZWOLLE-001";
    warehouse.archivedAt = null;

    when(warehouseStore.findByBusinessUnitCode("WH-@#$-001")).thenReturn(warehouse);

    // When
    useCase.archive(warehouse);

    // Then
    assertNotNull(warehouse.archivedAt);
    verify(warehouseStore).update(warehouse);
  }

  @Test
  @DisplayName("Should preserve stock value when archiving")
  void testArchivePreservesStock() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-011";
    warehouse.stock = 12345;
    warehouse.capacity = 50000;
    warehouse.location = "ZWOLLE-001";
    warehouse.archivedAt = null;

    when(warehouseStore.findByBusinessUnitCode("WH-011")).thenReturn(warehouse);

    // When
    useCase.archive(warehouse);

    // Then
    assertEquals(12345, warehouse.stock);
    verify(warehouseStore).update(warehouse);
  }

  @Test
  @DisplayName("Should preserve capacity value when archiving")
  void testArchivePreservesCapacity() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-012";
    warehouse.stock = 100;
    warehouse.capacity = 54321;
    warehouse.location = "ZWOLLE-001";
    warehouse.archivedAt = null;

    when(warehouseStore.findByBusinessUnitCode("WH-012")).thenReturn(warehouse);

    // When
    useCase.archive(warehouse);

    // Then
    assertEquals(54321, warehouse.capacity);
    verify(warehouseStore).update(warehouse);
  }
}
