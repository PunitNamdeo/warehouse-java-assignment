package com.fulfilment.application.monolith.fulfillment.adapters.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fulfilment.application.monolith.fulfillment.domain.models.WarehouseProductStore;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class WarehouseProductStoreRepositoryCoverageTest {

  @Inject WarehouseProductStoreRepository repository;

  @BeforeEach
  @Transactional
  void clean() {
    repository.deleteAll();
  }

  @Test
  @Transactional
  void create_and_find_association() {
    WarehouseProductStore association = new WarehouseProductStore(1L, 10L, "WH-1");

    repository.create(association);

    WarehouseProductStore found = repository.findAssociation(1L, 10L, "WH-1");
    assertNotNull(found);
    assertEquals(1L, found.productId);
    assertEquals(10L, found.storeId);
    assertEquals("WH-1", found.warehouseBusinessUnitCode);
  }

  @Test
  @Transactional
  void find_by_product_and_store() {
    repository.create(new WarehouseProductStore(1L, 10L, "WH-1"));
    repository.create(new WarehouseProductStore(1L, 10L, "WH-2"));
    repository.create(new WarehouseProductStore(2L, 10L, "WH-1"));

    List<WarehouseProductStore> result = repository.findByProductAndStore(1L, 10L);

    assertEquals(2, result.size());
  }

  @Test
  @Transactional
  void find_by_store() {
    repository.create(new WarehouseProductStore(1L, 20L, "WH-1"));
    repository.create(new WarehouseProductStore(2L, 20L, "WH-2"));
    repository.create(new WarehouseProductStore(3L, 21L, "WH-3"));

    List<WarehouseProductStore> result = repository.findByStore(20L);

    assertEquals(2, result.size());
  }

  @Test
  @Transactional
  void find_by_warehouse() {
    repository.create(new WarehouseProductStore(1L, 30L, "WH-Z"));
    repository.create(new WarehouseProductStore(2L, 31L, "WH-Z"));
    repository.create(new WarehouseProductStore(3L, 32L, "WH-A"));

    List<WarehouseProductStore> result = repository.findByWarehouse("WH-Z");

    assertEquals(2, result.size());
  }

  @Test
  @Transactional
  void remove_association() {
    repository.create(new WarehouseProductStore(7L, 70L, "WH-R"));
    assertNotNull(repository.findAssociation(7L, 70L, "WH-R"));

    repository.remove(7L, 70L, "WH-R");

    assertNull(repository.findAssociation(7L, 70L, "WH-R"));
  }

  @Test
  void db_entity_constructor_and_to_domain_model() {
    DbWarehouseProductStore db = new DbWarehouseProductStore(11L, 22L, "WH-CTOR");
    WarehouseProductStore model = db.toDomainModel();

    assertEquals(11L, model.productId);
    assertEquals(22L, model.storeId);
    assertEquals("WH-CTOR", model.warehouseBusinessUnitCode);
    assertNotNull(model.createdAt);
  }
}
