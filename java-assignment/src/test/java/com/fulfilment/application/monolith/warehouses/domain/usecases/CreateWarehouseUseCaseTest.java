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

public class CreateWarehouseUseCaseTest {

  private CreateWarehouseUseCase useCase;
  private WarehouseStore warehouseStore;
  private LocationResolver locationResolver;

  @BeforeEach
  void setup() {
    warehouseStore = mock(WarehouseStore.class);
    locationResolver = mock(LocationResolver.class);
    useCase = new CreateWarehouseUseCase(warehouseStore, locationResolver);
  }

  @Test
  void testCreateWarehouseSuccess() {
    // Given
    Location validLocation = new Location("ZWOLLE-001", 2, 500);
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-001";
    warehouse.stock = 100;
    warehouse.capacity = 200;
    warehouse.location = "ZWOLLE-001";
    
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);
    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(null);
    when(warehouseStore.getAll()).thenReturn(java.util.List.of());

    // When
    useCase.create(warehouse);

    // Then
    assertNotNull(warehouse.createdAt);
    assertNull(warehouse.archivedAt);
    verify(warehouseStore).create(warehouse);
  }

  @Test
  void testCreateWarehouseLocationNotFound() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-001";
    warehouse.stock = 100;
    warehouse.capacity = 200;
    warehouse.location = "INVALID-LOC";
    
    when(locationResolver.resolveByIdentifier("INVALID-LOC")).thenReturn(null);

    // When & Then
    Exception exception = assertThrows(WebApplicationException.class, () -> {
      useCase.create(warehouse);
    });
    assertTrue(exception.getMessage().contains("Location"));
  }

  @Test
  void testCreateWarehouseDuplicateBusinessUnitCode() {
    // Given
    Location validLocation = new Location("ZWOLLE-001", 2, 500);
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-001";
    warehouse.stock = 100;
    warehouse.capacity = 200;
    warehouse.location = "ZWOLLE-001";
    
    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "WH-001";
    
    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(existingWarehouse);

    // When & Then
    Exception exception = assertThrows(WebApplicationException.class, () -> {
      useCase.create(warehouse);
    });
    assertTrue(exception.getMessage().contains("already exists"));
  }

  @Test
  void testCreateWarehouseCapacityExceedsLocationMax() {
    // Given
    Location validLocation = new Location("ZWOLLE-001", 1, 100); // Max capacity is 100
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-001";
    warehouse.stock = 50;
    warehouse.capacity = 200; // Exceeds location max of 100
    warehouse.location = "ZWOLLE-001";
    
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);
    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(null);

    // When & Then
    Exception exception = assertThrows(WebApplicationException.class, () -> {
      useCase.create(warehouse);
    });
    assertTrue(exception.getMessage().contains("capacity"));
  }

  @Test
  void testCreateWarehouseStockExceedsCapacity() {
    // Given
    Location validLocation = new Location("ZWOLLE-001", 1, 500);
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-001";
    warehouse.stock = 300; // Exceeds capacity
    warehouse.capacity = 200;
    warehouse.location = "ZWOLLE-001";
    
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);
    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(null);

    // When & Then
    Exception exception = assertThrows(WebApplicationException.class, () -> {
      useCase.create(warehouse);
    });
    assertTrue(exception.getMessage().contains("stock"));
  }
}
