package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

public class LegacyStoreManagerGatewayCoverageTest {

  @Test
  void create_store_on_legacy_system_executes() {
    LegacyStoreManagerGateway gateway = new LegacyStoreManagerGateway();
    Store store = new Store();
    store.name = "legacy-create-store";
    store.quantityProductsInStock = 10;

    assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
  }

  @Test
  void update_store_on_legacy_system_executes() {
    LegacyStoreManagerGateway gateway = new LegacyStoreManagerGateway();
    Store store = new Store();
    store.name = "legacy-update-store";
    store.quantityProductsInStock = 20;

    assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(store));
  }
}
