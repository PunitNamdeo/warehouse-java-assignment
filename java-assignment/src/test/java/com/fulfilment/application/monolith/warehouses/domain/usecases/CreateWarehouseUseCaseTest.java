package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CreateWarehouseUseCaseTest {

  private CreateWarehouseUseCase useCase;
  private WarehouseStore warehouseStore;
  private LocationGateway locationGateway;

  @BeforeEach
  void setup() {
    warehouseStore = mock(WarehouseStore.class);
    locationGateway = mock(LocationGateway.class);
    useCase = new CreateWarehouseUseCase(warehouseStore, locationGateway);
  }

  @Test
  void testCreateWarehouseSuccess() {
    // Given
    Location validLocation = new Location();
    validLocation.identification = "ZWOLLE-001";
    validLocation.maximumCapacity = 500;
    
    when(locationGateway.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);
    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(null);

    // When
    Warehouse result = useCase.create("WH-001", 100, 200, "ZWOLLE-001");

    // Then
    assertNotNull(result);
    assertEquals("WH-001", result.businessUnitCode);
    assertEquals(100, result.stock);
    assertEquals(200, result.capacity);
    assertEquals("ZWOLLE-001", result.location);
    assertNull(result.archivedAt);
    verify(warehouseStore).create(result);
  }

  @Test
  void testCreateWarehouseLocationNotFound() {
    // Given
    when(locationGateway.resolveByIdentifier("INVALID-LOC")).thenReturn(null);

    // When & Then
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      useCase.create("WH-001", 100, 200, "INVALID-LOC");
    });
    assertTrue(exception.getMessage().contains("Location"));
  }

  @Test
  void testCreateWarehouseDuplicateBusinessUnitCode() {
    // Given
    Location validLocation = new Location();
    validLocation.identification = "ZWOLLE-001";
    validLocation.maximumCapacity = 500;
    
    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "WH-001";
    
    when(locationGateway.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);
    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(existingWarehouse);

    // When & Then
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      useCase.create("WH-001", 100, 200, "ZWOLLE-001");
    });
    assertTrue(exception.getMessage().contains("already exists"));
  }

  @Test
  void testCreateWarehouseCapacityExceedsLocationMax() {
    // Given
    Location validLocation = new Location();
    validLocation.identification = "ZWOLLE-001";
    validLocation.maximumCapacity = 100; // Max capacity is 100
    
    when(locationGateway.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);
    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(null);

    // When & Then - Capacity 200 exceeds location max 100
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      useCase.create("WH-001", 150, 200, "ZWOLLE-001");
    });
    assertTrue(exception.getMessage().contains("capacity"));
  }

  @Test
  void testCreateWarehouseStockExceedsCapacity() {
    // Given
    Location validLocation = new Location();
    validLocation.identification = "ZWOLLE-001";
    validLocation.maximumCapacity = 500;
    
    when(locationGateway.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);
    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(null);

    // When & Then - Stock 300 exceeds capacity 200
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      useCase.create("WH-001", 300, 200, "ZWOLLE-001");
    });
    assertTrue(exception.getMessage().contains("stock"));
  }
}
