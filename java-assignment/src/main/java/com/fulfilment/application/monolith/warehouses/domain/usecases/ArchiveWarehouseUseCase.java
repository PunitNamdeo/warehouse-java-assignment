package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void archive(Warehouse warehouse) {
    // Validate warehouse exists
    var existingWarehouse = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (existingWarehouse == null) {
      throw new WebApplicationException(
          "Warehouse with Business Unit Code '"
              + warehouse.businessUnitCode
              + "' not found.",
          404);
    }

    // Set archived timestamp
    warehouse.archivedAt = LocalDateTime.now();
    warehouseStore.update(warehouse);
  }
}

