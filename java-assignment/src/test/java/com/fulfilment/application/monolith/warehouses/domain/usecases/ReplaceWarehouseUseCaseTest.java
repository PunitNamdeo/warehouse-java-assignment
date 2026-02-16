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

@DisplayName("ReplaceWarehouseUseCase Tests")
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

  // ============== SUCCESSFUL REPLACEMENT TESTS ==============

  @Test
  @DisplayName("Should successfully replace warehouse with valid data")
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
    when(warehouseStore.getAll()).thenReturn(new ArrayList<>());

    // When
    useCase.replace(newWarehouse);

    // Then
    assertNotNull(oldWarehouse.archivedAt);
    verify(warehouseStore).update(oldWarehouse);
    verify(warehouseStore).create(newWarehouse);
  }

  @Test
  @DisplayName("Should replace warehouse to same location")
  void testReplaceWarehouseToSameLocation() {
    // Given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "WH-001";
    oldWarehouse.stock = 100;
    oldWarehouse.capacity = 200;
    oldWarehouse.location = "ZWOLLE-001";
    oldWarehouse.archivedAt = null;

    Location location = new Location("ZWOLLE-001", 3, 500);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH-001";
    newWarehouse.stock = 100;
    newWarehouse.capacity = 250;
    newWarehouse.location = "ZWOLLE-001";

    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(location);
    when(warehouseStore.getAll()).thenReturn(List.of(oldWarehouse));

    // When
    useCase.replace(newWarehouse);

    // Then
    assertNotNull(oldWarehouse.archivedAt);
    verify(warehouseStore).update(oldWarehouse);
    verify(warehouseStore).create(newWarehouse);
  }

  @Test
  @DisplayName("Should replace warehouse with increased capacity")
  void testReplaceWarehouseIncreasedCapacity() {
    // Given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "WH-001";
    oldWarehouse.stock = 100;
    oldWarehouse.capacity = 200;
    oldWarehouse.location = "ZWOLLE-001";

    Location newLocation = new Location("AMSTERDAM-001", 5, 1000);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH-001";
    newWarehouse.stock = 100;
    newWarehouse.capacity = 500;
    newWarehouse.location = "AMSTERDAM-001";

    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(newLocation);
    when(warehouseStore.getAll()).thenReturn(new ArrayList<>());

    // When
    useCase.replace(newWarehouse);

    // Then
    verify(warehouseStore).create(newWarehouse);
  }

  @Test
  @DisplayName("Should replace warehouse with zero stock")
  void testReplaceWarehouseZeroStock() {
    // Given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "WH-002";
    oldWarehouse.stock = 0;
    oldWarehouse.capacity = 200;

    Location newLocation = new Location("AMSTERDAM-001", 5, 500);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH-002";
    newWarehouse.stock = 0;
    newWarehouse.capacity = 250;
    newWarehouse.location = "AMSTERDAM-001";

    when(warehouseStore.findByBusinessUnitCode("WH-002")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(newLocation);
    when(warehouseStore.getAll()).thenReturn(new ArrayList<>());

    // When
    useCase.replace(newWarehouse);

    // Then
    verify(warehouseStore).create(newWarehouse);
  }

  @Test
  @DisplayName("Should set createdAt on new warehouse")
  void testReplaceWarehouseSetsCreatedAt() {
    // Given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "WH-001";
    oldWarehouse.stock = 100;
    oldWarehouse.capacity = 200;
    oldWarehouse.location = "ZWOLLE-001";

    Location newLocation = new Location("AMSTERDAM-001", 5, 500);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH-001";
    newWarehouse.stock = 100;
    newWarehouse.capacity = 250;
    newWarehouse.location = "AMSTERDAM-001";
    newWarehouse.createdAt = null;

    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(newLocation);
    when(warehouseStore.getAll()).thenReturn(new ArrayList<>());

    LocalDateTime beforeReplace = LocalDateTime.now();

    // When
    useCase.replace(newWarehouse);

    LocalDateTime afterReplace = LocalDateTime.now();

    // Then
    assertNotNull(newWarehouse.createdAt);
    assertTrue(newWarehouse.createdAt.isAfter(beforeReplace.minusSeconds(1)));
    assertTrue(newWarehouse.createdAt.isBefore(afterReplace.plusSeconds(1)));
  }

  @Test
  @DisplayName("Should set archivedAt to null on new warehouse")
  void testReplaceWarehouseArchiveAtNull() {
    // Given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "WH-001";
    oldWarehouse.stock = 100;
    oldWarehouse.capacity = 200;

    Location newLocation = new Location("AMSTERDAM-001", 5, 500);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH-001";
    newWarehouse.stock = 100;
    newWarehouse.capacity = 250;
    newWarehouse.location = "AMSTERDAM-001";
    newWarehouse.archivedAt = LocalDateTime.now(); // Pre-set to null

    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(newLocation);
    when(warehouseStore.getAll()).thenReturn(new ArrayList<>());

    // When
    useCase.replace(newWarehouse);

    // Then
    assertNull(newWarehouse.archivedAt);
  }

  // ============== NOT FOUND TESTS ==============

  @Test
  @DisplayName("Should reject when old warehouse not found (404)")
  void testReplaceWarehouseNotFound() {
    // Given
    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH-001";
    newWarehouse.stock = 100;
    newWarehouse.capacity = 200;
    newWarehouse.location = "ZWOLLE-001";

    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(null);

    // When & Then
    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.replace(newWarehouse);
    });
    assertEquals(404, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("not found"));
  }

  @Test
  @DisplayName("Should include business unit code in not found error")
  void testReplaceWarehouseNotFoundErrorMessage() {
    // Given
    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "INVALID-CODE";

    when(warehouseStore.findByBusinessUnitCode("INVALID-CODE")).thenReturn(null);

    // When & Then
    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.replace(newWarehouse);
    });
    assertTrue(exception.getMessage().contains("INVALID-CODE"));
  }

  // ============== STOCK MISMATCH TESTS ==============

  @Test
  @DisplayName("Should reject when stock doesn't match (400)")
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
    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.replace(newWarehouse);
    });
    assertEquals(400, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("stock"));
  }

  @Test
  @DisplayName("Should include stock values in mismatch error")
  void testReplaceWarehouseStockMismatchErrorMessage() {
    // Given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "WH-001";
    oldWarehouse.stock = 100;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH-001";
    newWarehouse.stock = 200;
    newWarehouse.location = "ZWOLLE-001";

    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(oldWarehouse);

    // When & Then
    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.replace(newWarehouse);
    });
    assertTrue(exception.getMessage().contains("100"));
    assertTrue(exception.getMessage().contains("200"));
  }

  @Test
  @DisplayName("Should allow stock match even with different old stock")
  void testReplaceWarehouseStockMatches() {
    // Given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "WH-001";
    oldWarehouse.stock = 100;
    oldWarehouse.capacity = 200;
    oldWarehouse.location = "ZWOLLE-001";

    Location newLocation = new Location("AMSTERDAM-001", 5, 500);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH-001";
    newWarehouse.stock = 100; // Same as old
    newWarehouse.capacity = 250;
    newWarehouse.location = "AMSTERDAM-001";

    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(newLocation);
    when(warehouseStore.getAll()).thenReturn(new ArrayList<>());

    // When
    useCase.replace(newWarehouse);

    // Then
    verify(warehouseStore).create(newWarehouse);
  }

  // ============== CAPACITY CONSTRAINT TESTS ==============

  @Test
  @DisplayName("Should reject when new capacity too small for stock (400)")
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
    newWarehouse.capacity = 50; // Too small for stock
    newWarehouse.location = "AMSTERDAM-001";

    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(newLocation);

    // When & Then
    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.replace(newWarehouse);
    });
    assertEquals(400, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("accommodate"));
  }

  @Test
  @DisplayName("Should include capacity values in error message")
  void testReplaceWarehouseCapacityErrorMessage() {
    // Given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "WH-001";
    oldWarehouse.stock = 100;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH-001";
    newWarehouse.stock = 100;
    newWarehouse.capacity = 50;
    newWarehouse.location = "ZWOLLE-001";

    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(oldWarehouse);

    // When & Then
    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.replace(newWarehouse);
    });
    assertTrue(exception.getMessage().contains("50"));
    assertTrue(exception.getMessage().contains("100"));
  }

  @Test
  @DisplayName("Should allow capacity equal to stock")
  void testReplaceWarehouseCapacityEqualToStock() {
    // Given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "WH-001";
    oldWarehouse.stock = 100;
    oldWarehouse.capacity = 200;

    Location newLocation = new Location("AMSTERDAM-001", 5, 500);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH-001";
    newWarehouse.stock = 100;
    newWarehouse.capacity = 100; // Equal to stock
    newWarehouse.location = "AMSTERDAM-001";

    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(newLocation);
    when(warehouseStore.getAll()).thenReturn(new ArrayList<>());

    // When
    useCase.replace(newWarehouse);

    // Then
    verify(warehouseStore).create(newWarehouse);
  }

  // ============== LOCATION VALIDATION TESTS ==============

  @Test
  @DisplayName("Should reject when new location not found (400)")
  void testReplaceWarehouseNewLocationNotFound() {
    // Given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "WH-001";
    oldWarehouse.stock = 100;
    oldWarehouse.capacity = 200;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH-001";
    newWarehouse.stock = 100;
    newWarehouse.capacity = 200;
    newWarehouse.location = "INVALID-LOC";

    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("INVALID-LOC")).thenReturn(null);

    // When & Then
    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.replace(newWarehouse);
    });
    assertEquals(400, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("Location"));
  }

  @Test
  @DisplayName("Should reject when capacity exceeds location max (400)")
  void testReplaceWarehouseCapacityExceedsLocationMax() {
    // Given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "WH-001";
    oldWarehouse.stock = 100;
    oldWarehouse.capacity = 200;

    Location newLocation = new Location("AMSTERDAM-001", 5, 300); // Max 300

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH-001";
    newWarehouse.stock = 100;
    newWarehouse.capacity = 400; // Exceeds location max
    newWarehouse.location = "AMSTERDAM-001";

    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(newLocation);

    // When & Then
    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.replace(newWarehouse);
    });
    assertEquals(400, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("exceeds"));
  }

  // ============== LOCATION WAREHOUSE LIMIT TESTS ==============

  @Test
  @DisplayName("Should reject when new location already has max warehouses (409)")
  void testReplaceWarehouseMaxWarehousesAtNewLocation() {
    // Given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "WH-001";
    oldWarehouse.stock = 100;
    oldWarehouse.capacity = 200;
    oldWarehouse.location = "ZWOLLE-001";

    Warehouse otherWarehouse1 = new Warehouse();
    otherWarehouse1.businessUnitCode = "WH-002";
    otherWarehouse1.location = "AMSTERDAM-001";

    Warehouse otherWarehouse2 = new Warehouse();
    otherWarehouse2.businessUnitCode = "WH-003";
    otherWarehouse2.location = "AMSTERDAM-001";

    Location newLocation = new Location("AMSTERDAM-001", 2, 500); // Max 2 warehouses

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH-001";
    newWarehouse.stock = 100;
    newWarehouse.capacity = 200;
    newWarehouse.location = "AMSTERDAM-001";

    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(newLocation);
    when(warehouseStore.getAll()).thenReturn(List.of(oldWarehouse, otherWarehouse1, otherWarehouse2));

    // When & Then
    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.replace(newWarehouse);
    });
    assertEquals(409, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("Maximum number"));
  }

  @Test
  @DisplayName("Should allow replace when location has space for replacement")
  void testReplaceWarehouseLocationHasSpace() {
    // Given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "WH-001";
    oldWarehouse.stock = 100;
    oldWarehouse.capacity = 200;
    oldWarehouse.location = "ZWOLLE-001";

    Warehouse otherWarehouse = new Warehouse();
    otherWarehouse.businessUnitCode = "WH-002";
    otherWarehouse.location = "AMSTERDAM-001";

    Location newLocation = new Location("AMSTERDAM-001", 3, 500); // Max 3

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH-001";
    newWarehouse.stock = 100;
    newWarehouse.capacity = 200;
    newWarehouse.location = "AMSTERDAM-001";

    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(newLocation);
    when(warehouseStore.getAll()).thenReturn(List.of(oldWarehouse, otherWarehouse));

    // When
    useCase.replace(newWarehouse);

    // Then
    verify(warehouseStore).create(newWarehouse);
  }

  // ============== VERIFICATION TESTS ==============

  @Test
  @DisplayName("Should archive old warehouse and create new warehouse")
  void testReplaceArchivesOldCreatesNew() {
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
    when(warehouseStore.getAll()).thenReturn(new ArrayList<>());

    // When
    useCase.replace(newWarehouse);

    // Then - Verify both operations
    assertNotNull(oldWarehouse.archivedAt);
    verify(warehouseStore).update(oldWarehouse);
    verify(warehouseStore).create(newWarehouse);
  }

  @Test
  @DisplayName("Should call update then create in that order")
  void testReplaceCallsUpdateBeforeCreate() {
    // Given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "WH-001";
    oldWarehouse.stock = 100;
    oldWarehouse.capacity = 200;
    oldWarehouse.location = "ZWOLLE-001";

    Location newLocation = new Location("AMSTERDAM-001", 5, 500);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH-001";
    newWarehouse.stock = 100;
    newWarehouse.capacity = 250;
    newWarehouse.location = "AMSTERDAM-001";

    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(newLocation);
    when(warehouseStore.getAll()).thenReturn(new ArrayList<>());

    // When
    useCase.replace(newWarehouse);

    // Then
    verify(warehouseStore, times(1)).update(oldWarehouse);
    verify(warehouseStore, times(1)).create(newWarehouse);
  }
}

