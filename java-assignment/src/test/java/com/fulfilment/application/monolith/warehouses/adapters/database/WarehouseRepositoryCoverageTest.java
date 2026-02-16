package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class WarehouseRepositoryCoverageTest {

  @Inject WarehouseRepository repository;

  @BeforeEach
  @Transactional
  void clean() {
    repository.deleteAll();
  }

  @Test
  @Transactional
  void create_and_find_active_warehouse() {
    Warehouse warehouse = warehouse("BU-1", "LOC-1", 200, 100);

    repository.create(warehouse);

    Warehouse found = repository.findByBusinessUnitCode("BU-1");
    assertNotNull(found);
    assertEquals("BU-1", found.businessUnitCode);
    assertEquals("LOC-1", found.location);
    assertNotNull(found.createdAt);
    assertNull(found.archivedAt);
  }

  @Test
  @Transactional
  void get_all_returns_only_active() {
    repository.create(warehouse("BU-1", "LOC-1", 200, 100));
    repository.create(warehouse("BU-2", "LOC-1", 300, 120));

    Warehouse toArchive = repository.findByBusinessUnitCode("BU-2");
    toArchive.archivedAt = java.time.LocalDateTime.now();
    repository.update(toArchive);

    List<Warehouse> active = repository.getAll();
    assertEquals(1, active.size());
    assertEquals("BU-1", active.get(0).businessUnitCode);
  }

  @Test
  @Transactional
  void update_existing_warehouse() {
    repository.create(warehouse("BU-3", "LOC-OLD", 200, 100));

    Warehouse update = warehouse("BU-3", "LOC-NEW", 500, 222);
    repository.update(update);

    Warehouse found = repository.findByBusinessUnitCode("BU-3");
    assertEquals("LOC-NEW", found.location);
    assertEquals(500, found.capacity);
    assertEquals(222, found.stock);
  }

  @Test
  @Transactional
  void remove_soft_deletes_warehouse() {
    repository.create(warehouse("BU-4", "LOC-1", 200, 100));

    Warehouse found = repository.findByBusinessUnitCode("BU-4");
    repository.remove(found);

    assertNull(repository.findByBusinessUnitCode("BU-4"));
  }

  @Test
  @Transactional
  void find_by_business_unit_returns_null_when_missing() {
    assertNull(repository.findByBusinessUnitCode("MISSING"));
  }

  @Test
  @Transactional
  void update_non_existing_does_not_fail() {
    repository.update(warehouse("NO-EXIST", "LOC-1", 100, 10));
  }

  @Test
  @Transactional
  void remove_non_existing_does_not_fail() {
    repository.remove(warehouse("NO-EXIST", "LOC-1", 100, 10));
  }

  private Warehouse warehouse(String bu, String location, int capacity, int stock) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = bu;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    return warehouse;
  }
}
