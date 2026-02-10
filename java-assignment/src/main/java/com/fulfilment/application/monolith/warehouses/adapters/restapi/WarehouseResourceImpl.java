package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ArchiveWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ReplaceWarehouseUseCase;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject private WarehouseRepository warehouseRepository;

  @Inject private CreateWarehouseUseCase createWarehouseUseCase;

  @Inject private ArchiveWarehouseUseCase archiveWarehouseUseCase;

  @Inject private ReplaceWarehouseUseCase replaceWarehouseUseCase;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  @Transactional
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    var domainWarehouse = toDomainModel(data);
    createWarehouseUseCase.create(domainWarehouse);
    return toWarehouseResponse(domainWarehouse);
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    var warehouse = warehouseRepository.findByBusinessUnitCode(id);
    if (warehouse == null) {
      throw new jakarta.ws.rs.WebApplicationException(
          "Warehouse with Business Unit Code '" + id + "' not found.", 404);
    }
    return toWarehouseResponse(warehouse);
  }

  @Override
  @Transactional
  public void archiveAWarehouseUnitByID(String id) {
    var warehouse = warehouseRepository.findByBusinessUnitCode(id);
    if (warehouse == null) {
      throw new jakarta.ws.rs.WebApplicationException(
          "Warehouse with Business Unit Code '" + id + "' not found.", 404);
    }
    archiveWarehouseUseCase.archive(warehouse);
  }

  @Override
  @Transactional
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {
    data.setBusinessUnitCode(businessUnitCode);
    var newWarehouse = toDomainModel(data);
    replaceWarehouseUseCase.replace(newWarehouse);
    return toWarehouseResponse(newWarehouse);
  }

  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomainModel(
      Warehouse apiWarehouse) {
    var warehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    warehouse.businessUnitCode = apiWarehouse.getBusinessUnitCode();
    warehouse.location = apiWarehouse.getLocation();
    warehouse.capacity = apiWarehouse.getCapacity();
    warehouse.stock = apiWarehouse.getStock();
    return warehouse;
  }
}
