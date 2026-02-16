package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@DisplayName("CreateWarehouseUseCase Tests")
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

  // ============== SUCCESSFUL CREATION TESTS ==============

  @Test
  @DisplayName("Should successfully create warehouse with valid data")
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
    when(warehouseStore.getAll()).thenReturn(new ArrayList<>());

    // When
    useCase.create(warehouse);

    // Then
    assertNotNull(warehouse.createdAt);
    assertNull(warehouse.archivedAt);
    verify(warehouseStore).create(warehouse);
  }

  @Test
  @DisplayName("Should create warehouse with zero stock")
  void testCreateWarehouseZeroStock() {
    // Given
    Location validLocation = new Location("ZWOLLE-001", 2, 500);
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-002";
    warehouse.stock = 0;
    warehouse.capacity = 200;
    warehouse.location = "ZWOLLE-001";
    
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);
    when(warehouseStore.findByBusinessUnitCode("WH-002")).thenReturn(null);
    when(warehouseStore.getAll()).thenReturn(new ArrayList<>());

    // When
    useCase.create(warehouse);

    // Then
    assertEquals(0, warehouse.stock);
    verify(warehouseStore).create(warehouse);
  }

  @Test
  @DisplayName("Should create warehouse with capacity equal to location max")
  void testCreateWarehouseCapacityEqualToLocationMax() {
    // Given
    Location validLocation = new Location("ZWOLLE-001", 2, 300);
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-003";
    warehouse.stock = 100;
    warehouse.capacity = 300; // Equal to location max
    warehouse.location = "ZWOLLE-001";
    
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);
    when(warehouseStore.findByBusinessUnitCode("WH-003")).thenReturn(null);
    when(warehouseStore.getAll()).thenReturn(new ArrayList<>());

    // When
    useCase.create(warehouse);

    // Then
    verify(warehouseStore).create(warehouse);
  }

  @Test
  @DisplayName("Should create warehouse with stock equal to capacity")
  void testCreateWarehouseStockEqualToCapacity() {
    // Given
    Location validLocation = new Location("ZWOLLE-001", 2, 500);
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-004";
    warehouse.stock = 200;
    warehouse.capacity = 200; // Stock equals capacity
    warehouse.location = "ZWOLLE-001";
    
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);
    when(warehouseStore.findByBusinessUnitCode("WH-004")).thenReturn(null);
    when(warehouseStore.getAll()).thenReturn(new ArrayList<>());

    // When
    useCase.create(warehouse);

    // Then
    verify(warehouseStore).create(warehouse);
  }

  @Test
  @DisplayName("Should create second warehouse at location")
  void testCreateSecondWarehouseAtLocation() {
    // Given
    Location validLocation = new Location("ZWOLLE-001", 2, 500);
    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "WH-001";
    existingWarehouse.location = "ZWOLLE-001";
    
    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH-005";
    newWarehouse.stock = 100;
    newWarehouse.capacity = 200;
    newWarehouse.location = "ZWOLLE-001";
    
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);
    when(warehouseStore.findByBusinessUnitCode("WH-005")).thenReturn(null);
    when(warehouseStore.getAll()).thenReturn(List.of(existingWarehouse));

    // When
    useCase.create(newWarehouse);

    // Then
    verify(warehouseStore).create(newWarehouse);
  }

  // ============== LOCATION VALIDATION TESTS ==============

  @Test
  @DisplayName("Should reject warehouse creation when location not found (400)")
  void testCreateWarehouseLocationNotFound() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-001";
    warehouse.stock = 100;
    warehouse.capacity = 200;
    warehouse.location = "INVALID-LOC";
    
    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("INVALID-LOC")).thenReturn(null);

    // When & Then
    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.create(warehouse);
    });
    assertEquals(400, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("Location"));
    assertTrue(exception.getMessage().contains("not valid"));
  }

  @Test
  @DisplayName("Should include location code in error message")
  void testCreateWarehouseLocationNotFoundErrorMessage() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-001";
    warehouse.location = "UNKNOWN-LOC";
    
    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("UNKNOWN-LOC")).thenReturn(null);

    // When & Then
    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.create(warehouse);
    });
    assertTrue(exception.getMessage().contains("UNKNOWN-LOC"));
  }

  // ============== BUSINESS UNIT CODE VALIDATION TESTS ==============

  @Test
  @DisplayName("Should reject duplicate business unit code (409)")
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
    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.create(warehouse);
    });
    assertEquals(409, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("already exists"));
  }

  @Test
  @DisplayName("Should include code in duplicate error message")
  void testCreateWarehouseDuplicateCodeErrorMessage() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "DUPLICATE-WH";
    
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "DUPLICATE-WH";
    
    when(warehouseStore.findByBusinessUnitCode("DUPLICATE-WH")).thenReturn(existing);

    // When & Then
    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.create(warehouse);
    });
    assertTrue(exception.getMessage().contains("DUPLICATE-WH"));
  }

  // ============== CAPACITY CONSTRAINT TESTS ==============

  @Test
  @DisplayName("Should reject when warehouse capacity exceeds location max (400)")
  void testCreateWarehouseCapacityExceedsLocationMax() {
    // Given
    Location validLocation = new Location("ZWOLLE-001", 1, 100); // Max capacity is 100
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-001";
    warehouse.stock = 50;
    warehouse.capacity = 200; // Exceeds location max of 100
    warehouse.location = "ZWOLLE-001";
    
    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);

    // When & Then
    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.create(warehouse);
    });
    assertEquals(400, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("capacity"));
    assertTrue(exception.getMessage().contains("exceeds"));
  }

  @Test
  @DisplayName("Should include capacity values in error message")
  void testCreateWarehouseCapacityExceedsErrorMessage() {
    // Given
    Location validLocation = new Location("ZWOLLE-001", 1, 100);
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-001";
    warehouse.stock = 10;
    warehouse.capacity = 150;
    warehouse.location = "ZWOLLE-001";
    
    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);

    // When & Then
    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.create(warehouse);
    });
    assertTrue(exception.getMessage().contains("150"));
    assertTrue(exception.getMessage().contains("100"));
  }

  // ============== STOCK CONSTRAINT TESTS ==============

  @Test
  @DisplayName("Should reject when stock exceeds capacity (400)")
  void testCreateWarehouseStockExceedsCapacity() {
    // Given
    Location validLocation = new Location("ZWOLLE-001", 1, 500);
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-001";
    warehouse.stock = 300; // Exceeds capacity
    warehouse.capacity = 200;
    warehouse.location = "ZWOLLE-001";
    
    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);

    // When & Then
    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.create(warehouse);
    });
    assertEquals(400, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("stock"));
  }

  @Test
  @DisplayName("Should include stock values in error message")
  void testCreateWarehouseStockExceedsErrorMessage() {
    // Given
    Location validLocation = new Location("ZWOLLE-001", 1, 500);
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-001";
    warehouse.stock = 250;
    warehouse.capacity = 200;
    warehouse.location = "ZWOLLE-001";
    
    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);

    // When & Then
    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.create(warehouse);
    });
    assertTrue(exception.getMessage().contains("250"));
    assertTrue(exception.getMessage().contains("200"));
  }

  // ============== LOCATION WAREHOUSE LIMIT TESTS ==============

  @Test
  @DisplayName("Should reject when location already has max warehouses (409)")
  void testCreateWarehouseMaxWarehousesPerLocation() {
    // Given
    Location validLocation = new Location("ZWOLLE-001", 1, 500); // Max 1 warehouse
    
    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "WH-001";
    existingWarehouse.location = "ZWOLLE-001";
    
    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH-002";
    newWarehouse.stock = 100;
    newWarehouse.capacity = 200;
    newWarehouse.location = "ZWOLLE-001";
    
    when(warehouseStore.findByBusinessUnitCode("WH-002")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);
    when(warehouseStore.getAll()).thenReturn(List.of(existingWarehouse));

    // When & Then
    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.create(newWarehouse);
    });
    assertEquals(409, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("Maximum number"));
  }

  @Test
  @DisplayName("Should allow creating warehouse when below location limit")
  void testCreateWarehouseBelowMaximumAtLocation() {
    // Given
    Location validLocation = new Location("ZWOLLE-001", 3, 500); // Max 3 warehouses
    
    Warehouse existing1 = new Warehouse();
    existing1.businessUnitCode = "WH-001";
    existing1.location = "ZWOLLE-001";
    
    Warehouse existing2 = new Warehouse();
    existing2.businessUnitCode = "WH-002";
    existing2.location = "ZWOLLE-001";
    
    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH-003";
    newWarehouse.stock = 100;
    newWarehouse.capacity = 200;
    newWarehouse.location = "ZWOLLE-001";
    
    when(warehouseStore.findByBusinessUnitCode("WH-003")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);
    when(warehouseStore.getAll()).thenReturn(List.of(existing1, existing2));

    // When
    useCase.create(newWarehouse);

    // Then
    verify(warehouseStore).create(newWarehouse);
  }

  @Test
  @DisplayName("Should set createdAt timestamp on warehouse")
  void testCreateWarehouseSetsTimestamp() {
    // Given
    Location validLocation = new Location("ZWOLLE-001", 2, 500);
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-001";
    warehouse.stock = 100;
    warehouse.capacity = 200;
    warehouse.location = "ZWOLLE-001";
    
    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);
    when(warehouseStore.getAll()).thenReturn(new ArrayList<>());

    LocalDateTime beforeCreate = LocalDateTime.now();

    // When
    useCase.create(warehouse);

    LocalDateTime afterCreate = LocalDateTime.now();

    // Then
    assertNotNull(warehouse.createdAt);
    assertTrue(warehouse.createdAt.isAfter(beforeCreate.minusSeconds(1)));
    assertTrue(warehouse.createdAt.isBefore(afterCreate.plusSeconds(1)));
  }

  @Test
  @DisplayName("Should set archivedAt to null on new warehouse")
  void testCreateWarehouseArchiveAtNull() {
    // Given
    Location validLocation = new Location("ZWOLLE-001", 2, 500);
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-001";
    warehouse.stock = 100;
    warehouse.capacity = 200;
    warehouse.location = "ZWOLLE-001";
    warehouse.archivedAt = LocalDateTime.now(); // Pre-set to verify it's cleared
    
    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);
    when(warehouseStore.getAll()).thenReturn(new ArrayList<>());

    // When
    useCase.create(warehouse);

    // Then
    assertNull(warehouse.archivedAt);
  }

  @Test
  @DisplayName("Should handle warehouses at different locations")
  void testCreateWarehouseAtDifferentLocation() {
    // Given
    Location location1 = new Location("LOCATION-001", 2, 500);
    Location location2 = new Location("LOCATION-002", 2, 500);
    
    Warehouse warehouseAtLocation1 = new Warehouse();
    warehouseAtLocation1.businessUnitCode = "WH-001";
    warehouseAtLocation1.location = "LOCATION-001";
    
    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH-002";
    newWarehouse.stock = 100;
    newWarehouse.capacity = 200;
    newWarehouse.location = "LOCATION-002";
    
    when(warehouseStore.findByBusinessUnitCode("WH-002")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("LOCATION-002")).thenReturn(location2);
    when(warehouseStore.getAll()).thenReturn(List.of(warehouseAtLocation1));

    // When
    useCase.create(newWarehouse);

    // Then
    verify(warehouseStore).create(newWarehouse);
  }
}
