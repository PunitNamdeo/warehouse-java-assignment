package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    // Find the old warehouse by business unit code
    var oldWarehouse = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);

    if (oldWarehouse == null) {
      throw new WebApplicationException(
          "Warehouse with Business Unit Code '"
              + newWarehouse.businessUnitCode
              + "' not found.",
          404);
    }

    // Validate stock matching
    if (!oldWarehouse.stock.equals(newWarehouse.stock)) {
      throw new WebApplicationException(
          "Stock mismatch: new warehouse stock "
              + newWarehouse.stock
              + " does not match old warehouse stock "
              + oldWarehouse.stock
              + ".",
          400);
    }

    // Validate new warehouse capacity can accommodate the stock
    if (newWarehouse.capacity < newWarehouse.stock) {
      throw new WebApplicationException(
          "New warehouse capacity "
              + newWarehouse.capacity
              + " cannot accommodate stock "
              + newWarehouse.stock
              + ".",
          400);
    }

    // Validate new location exists
    var location = locationResolver.resolveByIdentifier(newWarehouse.location);
    if (location == null) {
      throw new WebApplicationException(
          "Location '" + newWarehouse.location + "' is not valid.", 400);
    }

    // Validate new warehouse capacity does not exceed location's max capacity
    if (newWarehouse.capacity > location.maxCapacity) {
      throw new WebApplicationException(
          "Warehouse capacity "
              + newWarehouse.capacity
              + " exceeds location's maximum capacity "
              + location.maxCapacity
              + ".",
          400);
    }

    // Archive the old warehouse
    oldWarehouse.archivedAt = LocalDateTime.now();
    warehouseStore.update(oldWarehouse);

    // Create the new warehouse with the same business unit code
    newWarehouse.createdAt = LocalDateTime.now();
    newWarehouse.archivedAt = null;
    warehouseStore.create(newWarehouse);
  }
}

