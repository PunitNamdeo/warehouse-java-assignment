package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ArchiveWarehouseUseCaseQuarkusCoverageTest {

  @Inject ArchiveWarehouseUseCase useCase;

  @InjectMock WarehouseStore warehouseStore;

  @Test
  void archive_success() {
    Warehouse existing = warehouse("WH-1", "LOC-1", 100, 10);
    Warehouse request = warehouse("WH-1", "LOC-1", 100, 10);
    when(warehouseStore.findByBusinessUnitCode("WH-1")).thenReturn(existing);

    useCase.archive(request);

    assertNotNull(request.archivedAt);
    verify(warehouseStore).update(request);
  }

  @Test
  void archive_not_found() {
    Warehouse request = warehouse("MISSING", "LOC-1", 100, 10);
    when(warehouseStore.findByBusinessUnitCode("MISSING")).thenReturn(null);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.archive(request));

    assertEquals(404, ex.getResponse().getStatus());
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
