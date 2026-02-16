package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class CreateWarehouseUseCaseQuarkusCoverageTest {

  @Inject CreateWarehouseUseCase useCase;

  @InjectMock WarehouseStore warehouseStore;

  @InjectMock LocationResolver locationResolver;

  @Test
  void create_success() {
    Warehouse warehouse = warehouse("WH-1", "LOC-1", 200, 100);
    Location location = new Location("LOC-1", 3, 500);

    when(warehouseStore.findByBusinessUnitCode("WH-1")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("LOC-1")).thenReturn(location);
    when(warehouseStore.getAll()).thenReturn(List.of());

    useCase.create(warehouse);

    assertNotNull(warehouse.createdAt);
    assertEquals(null, warehouse.archivedAt);
    verify(warehouseStore).create(warehouse);
  }

  @Test
  void create_duplicate_bu_conflict() {
    Warehouse warehouse = warehouse("WH-1", "LOC-1", 200, 100);

    when(warehouseStore.findByBusinessUnitCode("WH-1")).thenReturn(warehouse("WH-1", "LOC-1", 100, 20));

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.create(warehouse));

    assertEquals(409, ex.getResponse().getStatus());
    verify(warehouseStore, never()).create(warehouse);
  }

  @Test
  void create_invalid_location() {
    Warehouse warehouse = warehouse("WH-1", "BAD-LOC", 200, 100);

    when(warehouseStore.findByBusinessUnitCode("WH-1")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("BAD-LOC")).thenReturn(null);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.create(warehouse));

    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  void create_capacity_exceeds_location_max() {
    Warehouse warehouse = warehouse("WH-1", "LOC-1", 700, 100);
    Location location = new Location("LOC-1", 3, 500);

    when(warehouseStore.findByBusinessUnitCode("WH-1")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("LOC-1")).thenReturn(location);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.create(warehouse));

    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  void create_stock_exceeds_capacity() {
    Warehouse warehouse = warehouse("WH-1", "LOC-1", 100, 101);
    Location location = new Location("LOC-1", 3, 500);

    when(warehouseStore.findByBusinessUnitCode("WH-1")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("LOC-1")).thenReturn(location);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.create(warehouse));

    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  void create_max_warehouses_reached_conflict() {
    Warehouse warehouse = warehouse("WH-3", "LOC-1", 200, 100);
    Location location = new Location("LOC-1", 2, 500);

    Warehouse existing1 = warehouse("WH-1", "LOC-1", 150, 80);
    Warehouse existing2 = warehouse("WH-2", "LOC-1", 150, 80);

    when(warehouseStore.findByBusinessUnitCode("WH-3")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("LOC-1")).thenReturn(location);
    when(warehouseStore.getAll()).thenReturn(List.of(existing1, existing2));

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.create(warehouse));

    assertEquals(409, ex.getResponse().getStatus());
  }

  private Warehouse warehouse(String bu, String location, int capacity, int stock) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = bu;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    return warehouse;
  }
}
