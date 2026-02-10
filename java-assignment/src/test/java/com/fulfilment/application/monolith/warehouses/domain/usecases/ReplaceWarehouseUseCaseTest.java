package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReplaceWarehouseUseCaseTest {

  private ReplaceWarehouseUseCase useCase;
  private WarehouseStore warehouseStore;
  private LocationResolver locationResolver;

  @BeforeEach
  void setup() {
    warehouseStore = mock(WarehouseStore.class);
    locationResolver = mock(LocationResolver.class);
    useCase = new ReplaceWarehouseUseCase(warehouseStore, locationResolver);
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

    Location newLocation = new Location("AMSTERDAM-001", 5, 500);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH-001";
    newWarehouse.stock = 100;
    newWarehouse.capacity = 250;
    newWarehouse.location = "AMSTERDAM-001";

    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(newLocation);
    when(warehouseStore.getAll()).thenReturn(java.util.List.of());

    // When
    useCase.replace(newWarehouse);

    // Then
    assertNotNull(oldWarehouse.archivedAt);
    verify(warehouseStore).update(oldWarehouse);
    verify(warehouseStore).create(newWarehouse);
  }

  @Test
  void testReplaceWarehouseNotFound() {
    // Given
    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH-001";
    newWarehouse.stock = 100;
    newWarehouse.capacity = 200;
    newWarehouse.location = "ZWOLLE-001";

    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(null);

    // When & Then
    Exception exception = assertThrows(WebApplicationException.class, () -> {
      useCase.replace(newWarehouse);
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

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH-001";
    newWarehouse.stock = 150;
    newWarehouse.capacity = 200;
    newWarehouse.location = "ZWOLLE-001";

    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(oldWarehouse);

    // When & Then
    Exception exception = assertThrows(WebApplicationException.class, () -> {
      useCase.replace(newWarehouse);
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

    Location newLocation = new Location("AMSTERDAM-001", 5, 500);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH-001";
    newWarehouse.stock = 100;
    newWarehouse.capacity = 50;
    newWarehouse.location = "AMSTERDAM-001";

    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(newLocation);

    // When & Then
    Exception exception = assertThrows(WebApplicationException.class, () -> {
      useCase.replace(newWarehouse);
    });
    assertTrue(exception.getMessage().contains("accommodate"));
  }
}

