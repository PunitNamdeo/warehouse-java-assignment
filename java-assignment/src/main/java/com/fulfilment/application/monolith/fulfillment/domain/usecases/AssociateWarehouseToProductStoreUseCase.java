package com.fulfilment.application.monolith.fulfillment.domain.usecases;

import com.fulfilment.application.monolith.fulfillment.domain.models.WarehouseProductStore;
import com.fulfilment.application.monolith.fulfillment.domain.ports.WarehouseProductStoreStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class AssociateWarehouseToProductStoreUseCase {

  private static final int MAX_WAREHOUSES_PER_PRODUCT_PER_STORE = 2;
  private static final int MAX_WAREHOUSES_PER_STORE = 3;
  private static final int MAX_PRODUCTS_PER_WAREHOUSE = 5;

  private final WarehouseProductStoreStore warehouseProductStoreStore;

  public AssociateWarehouseToProductStoreUseCase(
      WarehouseProductStoreStore warehouseProductStoreStore) {
    this.warehouseProductStoreStore = warehouseProductStoreStore;
  }

  public void associate(
      Long productId, Long storeId, String warehouseBusinessUnitCode) {

    // Validate association doesn't already exist
    var existing =
        warehouseProductStoreStore.findAssociation(productId, storeId, warehouseBusinessUnitCode);
    if (existing != null) {
      throw new WebApplicationException(
          "Association already exists for Product "
              + productId
              + ", Store "
              + storeId
              + ", Warehouse "
              + warehouseBusinessUnitCode,
          409);
    }

    // Constraint 1: Max 2 warehouses per product per store
    var warehousesForProductStore =
        warehouseProductStoreStore.findByProductAndStore(productId, storeId);
    if (warehousesForProductStore.size() >= MAX_WAREHOUSES_PER_PRODUCT_PER_STORE) {
      throw new WebApplicationException(
          "Product "
              + productId
              + " already has maximum ("
              + MAX_WAREHOUSES_PER_PRODUCT_PER_STORE
              + ") warehouses for Store "
              + storeId,
          409);
    }

    // Constraint 2: Max 3 warehouses per store
    var warehousesForStore = warehouseProductStoreStore.findByStore(storeId);
    var uniqueWarehousesForStore =
        warehousesForStore.stream()
            .map(w -> w.warehouseBusinessUnitCode)
            .distinct()
            .count();

    // Check if this warehouse is new for this store
    var warehouseAlreadyInStore =
        warehousesForStore.stream()
            .anyMatch(w -> w.warehouseBusinessUnitCode.equals(warehouseBusinessUnitCode));

    if (!warehouseAlreadyInStore && uniqueWarehousesForStore >= MAX_WAREHOUSES_PER_STORE) {
      throw new WebApplicationException(
          "Store "
              + storeId
              + " already has maximum ("
              + MAX_WAREHOUSES_PER_STORE
              + ") warehouses",
          409);
    }

    // Constraint 3: Max 5 product types per warehouse
    var productsForWarehouse = warehouseProductStoreStore.findByWarehouse(warehouseBusinessUnitCode);
    var uniqueProductsForWarehouse =
        productsForWarehouse.stream()
            .filter(p -> p.storeId.equals(storeId))
            .map(p -> p.productId)
            .distinct()
            .count();

    var productAlreadyInWarehouse =
        productsForWarehouse.stream()
            .anyMatch(
                p -> p.storeId.equals(storeId) && p.productId.equals(productId));

    if (!productAlreadyInWarehouse && uniqueProductsForWarehouse >= MAX_PRODUCTS_PER_WAREHOUSE) {
      throw new WebApplicationException(
          "Warehouse "
              + warehouseBusinessUnitCode
              + " already has maximum ("
              + MAX_PRODUCTS_PER_WAREHOUSE
              + ") product types",
          409);
    }

    // All validations passed, create the association
    var association = new WarehouseProductStore(productId, storeId, warehouseBusinessUnitCode);
    warehouseProductStoreStore.create(association);
  }

  public void dissociate(Long productId, Long storeId, String warehouseBusinessUnitCode) {
    var existing =
        warehouseProductStoreStore.findAssociation(productId, storeId, warehouseBusinessUnitCode);
    if (existing == null) {
      throw new WebApplicationException(
          "Association not found for Product "
              + productId
              + ", Store "
              + storeId
              + ", Warehouse "
              + warehouseBusinessUnitCode,
          404);
    }
    warehouseProductStoreStore.remove(productId, storeId, warehouseBusinessUnitCode);
  }
}
