package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReplaceWarehouseUseCaseTest {

  private ReplaceWarehouseUseCase useCase;
  private WarehouseStore warehouseStore;
  private LocationGateway locationGateway;
  private CreateWarehouseUseCase createWarehouseUseCase;
  private ArchiveWarehouseUseCase archiveWarehouseUseCase;

  @BeforeEach
  void setup() {
    warehouseStore = mock(WarehouseStore.class);
    locationGateway = mock(LocationGateway.class);
    createWarehouseUseCase = mock(CreateWarehouseUseCase.class);
    archiveWarehouseUseCase = mock(ArchiveWarehouseUseCase.class);
    useCase = new ReplaceWarehouseUseCase(warehouseStore, locationGateway, createWarehouseUseCase, archiveWarehouseUseCase);
  }

  @Test
  void testReplaceWarehouseSuccess() {
    // Given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "WH-001";
    oldWarehouse.stock = 100;
    oldWarehouse.capacity = 200;
    oldWarehouse.location = "ZWOLLE-001";
    oldWarehouse.archivedAt = null;

    Location newLocation = new Location();
    newLocation.identification = "AMSTERDAM-001";
    newLocation.maximumCapacity = 500;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH-001";
    newWarehouse.stock = 100; // Same stock
    newWarehouse.capacity = 250;
    newWarehouse.location = "AMSTERDAM-001";
    newWarehouse.archivedAt = null;

    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(oldWarehouse);
    when(locationGateway.resolveByIdentifier("AMSTERDAM-001")).thenReturn(newLocation);
    when(createWarehouseUseCase.create("WH-001", 100, 250, "AMSTERDAM-001")).thenReturn(newWarehouse);

    // When
    Warehouse result = useCase.replace("WH-001", 100, 250, "AMSTERDAM-001");

    // Then
    assertNotNull(result);
    assertEquals("WH-001", result.businessUnitCode);
    assertEquals("AMSTERDAM-001", result.location);
    verify(archiveWarehouseUseCase).archive("WH-001");
    verify(createWarehouseUseCase).create("WH-001", 100, 250, "AMSTERDAM-001");
  }

  @Test
  void testReplaceWarehouseNotFound() {
    // Given
    when(warehouseStore.findByBusinessUnitCode("INVALID-WH")).thenReturn(null);

    // When & Then
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      useCase.replace("INVALID-WH", 100, 200, "ZWOLLE-001");
    });
    assertTrue(exception.getMessage().contains("not found"));
  }

  @Test
  void testReplaceWarehouseStockMismatch() {
    // Given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "WH-001";
    oldWarehouse.stock = 100;
    oldWarehouse.capacity = 200;

    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(oldWarehouse);

    // When & Then - Stock 150 doesn't match old stock 100
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      useCase.replace("WH-001", 150, 200, "ZWOLLE-001");
    });
    assertTrue(exception.getMessage().contains("stock"));
  }

  @Test
  void testReplaceWarehouseNewCapacityTooSmall() {
    // Given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "WH-001";
    oldWarehouse.stock = 100;
    oldWarehouse.capacity = 200;

    Location newLocation = new Location();
    newLocation.identification = "AMSTERDAM-001";
    newLocation.maximumCapacity = 500;

    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(oldWarehouse);
    when(locationGateway.resolveByIdentifier("AMSTERDAM-001")).thenReturn(newLocation);

    // When & Then - New capacity 50 is less than stock 100
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      useCase.replace("WH-001", 100, 50, "AMSTERDAM-001");
    });
    assertTrue(exception.getMessage().contains("capacity"));
  }
}
