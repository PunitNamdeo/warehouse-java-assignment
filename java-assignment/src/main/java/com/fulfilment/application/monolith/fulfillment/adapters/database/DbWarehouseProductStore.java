package com.fulfilment.application.monolith.fulfillment.adapters.database;

import com.fulfilment.application.monolith.fulfillment.domain.models.WarehouseProductStore;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "warehouse_product_store",
    uniqueConstraints = {
      @UniqueConstraint(
          columnNames = {"productId", "storeId", "warehouseBusinessUnitCode"},
          name = "uk_warehouse_product_store")
    })
@Cacheable
public class DbWarehouseProductStore {

  @Id @GeneratedValue public Long id;

  public Long productId;

  public Long storeId;

  public String warehouseBusinessUnitCode;

  public LocalDateTime createdAt;

  public DbWarehouseProductStore() {}

  public DbWarehouseProductStore(
      Long productId, Long storeId, String warehouseBusinessUnitCode) {
    this.productId = productId;
    this.storeId = storeId;
    this.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
    this.createdAt = LocalDateTime.now();
  }

  public WarehouseProductStore toDomainModel() {
    var model = new WarehouseProductStore();
    model.productId = this.productId;
    model.storeId = this.storeId;
    model.warehouseBusinessUnitCode = this.warehouseBusinessUnitCode;
    model.createdAt = this.createdAt;
    return model;
  }
}
