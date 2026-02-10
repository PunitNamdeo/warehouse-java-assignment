package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;

public class ArchiveWarehouseUseCaseTest {

  private ArchiveWarehouseUseCase useCase;
  private WarehouseStore warehouseStore;

  @BeforeEach
  void setup() {
    warehouseStore = mock(WarehouseStore.class);
    useCase = new ArchiveWarehouseUseCase(warehouseStore);
  }

  @Test
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
  void testArchiveAlreadyArchivedWarehouse() {
    // Given - when archiving an already archived warehouse, it just archives again
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
}
