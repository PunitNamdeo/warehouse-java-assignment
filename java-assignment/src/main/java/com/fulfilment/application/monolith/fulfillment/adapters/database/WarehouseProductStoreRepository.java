package com.fulfilment.application.monolith.fulfillment.adapters.database;

import com.fulfilment.application.monolith.fulfillment.domain.models.WarehouseProductStore;
import com.fulfilment.application.monolith.fulfillment.domain.ports.WarehouseProductStoreStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class WarehouseProductStoreRepository
    implements WarehouseProductStoreStore, PanacheRepository<DbWarehouseProductStore> {

  @Override
  public void create(WarehouseProductStore association) {
    var dbAssociation = new DbWarehouseProductStore();
    dbAssociation.productId = association.productId;
    dbAssociation.storeId = association.storeId;
    dbAssociation.warehouseBusinessUnitCode = association.warehouseBusinessUnitCode;
    dbAssociation.createdAt = association.createdAt;
    this.persist(dbAssociation);
  }

  @Override
  public void remove(Long productId, Long storeId, String warehouseBusinessUnitCode) {
    this.delete(
        "productId = ?1 and storeId = ?2 and warehouseBusinessUnitCode = ?3",
        productId,
        storeId,
        warehouseBusinessUnitCode);
  }

  @Override
  public List<WarehouseProductStore> findByProductAndStore(Long productId, Long storeId) {
    return this.list("productId = ?1 and storeId = ?2", productId, storeId).stream()
        .map(DbWarehouseProductStore::toDomainModel)
        .toList();
  }

  @Override
  public List<WarehouseProductStore> findByStore(Long storeId) {
    return this.list("storeId = ?1", storeId).stream()
        .map(DbWarehouseProductStore::toDomainModel)
        .toList();
  }

  @Override
  public List<WarehouseProductStore> findByWarehouse(String warehouseBusinessUnitCode) {
    return this.list("warehouseBusinessUnitCode = ?1", warehouseBusinessUnitCode).stream()
        .map(DbWarehouseProductStore::toDomainModel)
        .toList();
  }

  @Override
  public WarehouseProductStore findAssociation(
      Long productId, Long storeId, String warehouseBusinessUnitCode) {
    var result =
        this.find(
                "productId = ?1 and storeId = ?2 and warehouseBusinessUnitCode = ?3",
                productId,
                storeId,
                warehouseBusinessUnitCode)
            .firstResultOptional();
    return result.map(DbWarehouseProductStore::toDomainModel).orElse(null);
  }
}
