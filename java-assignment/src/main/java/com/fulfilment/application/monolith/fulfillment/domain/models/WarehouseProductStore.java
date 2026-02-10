package com.fulfilment.application.monolith.fulfillment.domain.models;

import java.time.LocalDateTime;

public class WarehouseProductStore {

  public Long productId;

  public Long storeId;

  public String warehouseBusinessUnitCode;

  public LocalDateTime createdAt;

  public WarehouseProductStore() {}

  public WarehouseProductStore(
      Long productId, Long storeId, String warehouseBusinessUnitCode) {
    this.productId = productId;
    this.storeId = storeId;
    this.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
    this.createdAt = LocalDateTime.now();
  }
}
