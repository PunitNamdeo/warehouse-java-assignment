package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.list("archivedAt is null").stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    var dbWarehouse = new DbWarehouse();
    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.createdAt = LocalDateTime.now();
    dbWarehouse.archivedAt = null;
    this.persist(dbWarehouse);
  }

  @Override
  public void update(Warehouse warehouse) {
    var dbWarehouse =
        this.find("businessUnitCode", warehouse.businessUnitCode).firstResultOptional();

    if (dbWarehouse.isPresent()) {
      var entity = dbWarehouse.get();
      entity.location = warehouse.location;
      entity.capacity = warehouse.capacity;
      entity.stock = warehouse.stock;
      entity.archivedAt = warehouse.archivedAt;
    }
  }

  @Override
  public void remove(Warehouse warehouse) {
    var dbWarehouse =
        this.find("businessUnitCode", warehouse.businessUnitCode).firstResultOptional();

    if (dbWarehouse.isPresent()) {
      var entity = dbWarehouse.get();
      entity.archivedAt = LocalDateTime.now();
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    try {
      var dbWarehouse =
          this.find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResult();
      if (dbWarehouse != null) {
        return ((DbWarehouse) dbWarehouse).toWarehouse();
      }
    } catch (NoResultException e) {
      // Return null if not found
    }
    return null;
  }
}

