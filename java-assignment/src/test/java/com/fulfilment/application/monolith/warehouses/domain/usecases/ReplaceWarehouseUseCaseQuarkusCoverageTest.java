package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
public class ReplaceWarehouseUseCaseQuarkusCoverageTest {

  @Inject ReplaceWarehouseUseCase useCase;

  @InjectMock WarehouseStore warehouseStore;

  @InjectMock LocationResolver locationResolver;

  @Test
  void replace_success() {
    Warehouse oldWarehouse = warehouse("WH-1", "OLD", 200, 100);
    Warehouse replacement = warehouse("WH-1", "NEW", 250, 100);
    Location newLocation = new Location("NEW", 3, 500);

    when(warehouseStore.findByBusinessUnitCode("WH-1")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("NEW")).thenReturn(newLocation);
    when(warehouseStore.getAll()).thenReturn(List.of(oldWarehouse));

    useCase.replace(replacement);

    assertNotNull(oldWarehouse.archivedAt);
    assertNotNull(replacement.createdAt);
    assertEquals(null, replacement.archivedAt);
    verify(warehouseStore).update(oldWarehouse);
    verify(warehouseStore).create(replacement);
  }

  @Test
  void replace_not_found() {
    Warehouse replacement = warehouse("MISSING", "NEW", 250, 100);
    when(warehouseStore.findByBusinessUnitCode("MISSING")).thenReturn(null);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.replace(replacement));

    assertEquals(404, ex.getResponse().getStatus());
    verify(warehouseStore, never()).create(replacement);
  }

  @Test
  void replace_stock_mismatch() {
    Warehouse oldWarehouse = warehouse("WH-1", "OLD", 200, 90);
    Warehouse replacement = warehouse("WH-1", "NEW", 250, 100);

    when(warehouseStore.findByBusinessUnitCode("WH-1")).thenReturn(oldWarehouse);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.replace(replacement));

    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  void replace_capacity_less_than_stock() {
    Warehouse oldWarehouse = warehouse("WH-1", "OLD", 200, 100);
    Warehouse replacement = warehouse("WH-1", "NEW", 99, 100);

    when(warehouseStore.findByBusinessUnitCode("WH-1")).thenReturn(oldWarehouse);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.replace(replacement));

    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  void replace_invalid_location() {
    Warehouse oldWarehouse = warehouse("WH-1", "OLD", 200, 100);
    Warehouse replacement = warehouse("WH-1", "NEW", 250, 100);

    when(warehouseStore.findByBusinessUnitCode("WH-1")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("NEW")).thenReturn(null);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.replace(replacement));

    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  void replace_capacity_exceeds_location_max() {
    Warehouse oldWarehouse = warehouse("WH-1", "OLD", 200, 100);
    Warehouse replacement = warehouse("WH-1", "NEW", 700, 100);
    Location newLocation = new Location("NEW", 3, 500);

    when(warehouseStore.findByBusinessUnitCode("WH-1")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("NEW")).thenReturn(newLocation);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.replace(replacement));

    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  void replace_max_warehouses_reached_in_new_location() {
    Warehouse oldWarehouse = warehouse("WH-1", "OLD", 200, 100);
    Warehouse replacement = warehouse("WH-1", "NEW", 250, 100);
    Location newLocation = new Location("NEW", 2, 500);

    Warehouse existing1 = warehouse("WH-2", "NEW", 150, 80);
    Warehouse existing2 = warehouse("WH-3", "NEW", 180, 90);

    when(warehouseStore.findByBusinessUnitCode("WH-1")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("NEW")).thenReturn(newLocation);
    when(warehouseStore.getAll()).thenReturn(List.of(existing1, existing2));

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.replace(replacement));

    assertEquals(409, ex.getResponse().getStatus());
    assertTrue(ex.getMessage().contains("Maximum number of warehouses"));
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
