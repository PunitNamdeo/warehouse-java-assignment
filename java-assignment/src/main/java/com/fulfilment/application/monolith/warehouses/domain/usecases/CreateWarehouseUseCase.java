package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    // Validate Business Unit Code uniqueness
    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw new WebApplicationException(
          "Business Unit Code '" + warehouse.businessUnitCode + "' already exists.", 409);
    }

    // Validate Location exists
    var location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw new WebApplicationException("Location '" + warehouse.location + "' is not valid.", 400);
    }

    // Validate warehouse capacity does not exceed location's max capacity
    if (warehouse.capacity > location.maxCapacity) {
      throw new WebApplicationException(
          "Warehouse capacity "
              + warehouse.capacity
              + " exceeds location's maximum capacity "
              + location.maxCapacity
              + ".",
          400);
    }

    // Validate warehouse can handle the stock
    if (warehouse.stock > warehouse.capacity) {
      throw new WebApplicationException(
          "Warehouse stock " + warehouse.stock + " exceeds its capacity " + warehouse.capacity + ".",
          400);
    }

    // Validate max warehouses per location
    var existingWarehouses =
        warehouseStore.getAll().stream()
            .filter(w -> w.location.equals(warehouse.location))
            .count();

    if (existingWarehouses >= location.maxNumberOfWarehouses) {
      throw new WebApplicationException(
          "Maximum number of warehouses ("
              + location.maxNumberOfWarehouses
              + ") has been reached for location '"
              + warehouse.location
              + "'.",
          409);
    }

    // if all went well, create the warehouse
    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;
    warehouseStore.create(warehouse);
  }
}

