package com.fulfilment.application.monolith.fulfillment.domain.ports;

import com.fulfilment.application.monolith.fulfillment.domain.models.WarehouseProductStore;
import java.util.List;

public interface WarehouseProductStoreStore {

  void create(WarehouseProductStore association);

  void remove(Long productId, Long storeId, String warehouseBusinessUnitCode);

  List<WarehouseProductStore> findByProductAndStore(Long productId, Long storeId);

  List<WarehouseProductStore> findByStore(Long storeId);

  List<WarehouseProductStore> findByWarehouse(String warehouseBusinessUnitCode);

  WarehouseProductStore findAssociation(
      Long productId, Long storeId, String warehouseBusinessUnitCode);
}
