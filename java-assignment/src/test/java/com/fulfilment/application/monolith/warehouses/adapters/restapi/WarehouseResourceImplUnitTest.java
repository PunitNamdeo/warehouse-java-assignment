package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ArchiveWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ReplaceWarehouseUseCase;
import jakarta.ws.rs.WebApplicationException;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WarehouseResourceImplTest {

  private WarehouseResourceImpl resource;
  private WarehouseRepository warehouseRepository;
  private CreateWarehouseUseCase createWarehouseUseCase;
  private ArchiveWarehouseUseCase archiveWarehouseUseCase;
  private ReplaceWarehouseUseCase replaceWarehouseUseCase;

  @BeforeEach
  void setUp() throws Exception {
    resource = new WarehouseResourceImpl();
    warehouseRepository = mock(WarehouseRepository.class);
    createWarehouseUseCase = mock(CreateWarehouseUseCase.class);
    archiveWarehouseUseCase = mock(ArchiveWarehouseUseCase.class);
    replaceWarehouseUseCase = mock(ReplaceWarehouseUseCase.class);

    setField(resource, "warehouseRepository", warehouseRepository);
    setField(resource, "createWarehouseUseCase", createWarehouseUseCase);
    setField(resource, "archiveWarehouseUseCase", archiveWarehouseUseCase);
    setField(resource, "replaceWarehouseUseCase", replaceWarehouseUseCase);
  }

  @Test
  void list_all_warehouses_success() {
    Warehouse warehouse = domainWarehouse("BU-1", "LOC-1", 100, 10);
    when(warehouseRepository.getAll()).thenReturn(List.of(warehouse));

    List<com.warehouse.api.beans.Warehouse> result = resource.listAllWarehousesUnits();

    assertEquals(1, result.size());
    assertEquals("BU-1", result.get(0).getBusinessUnitCode());
    assertEquals("LOC-1", result.get(0).getLocation());
  }

  @Test
  void list_all_warehouses_multiple_active() {
    Warehouse wh1 = domainWarehouse(1L, "BU-1", "LOC-1", 100, 10);
    Warehouse wh2 = domainWarehouse(2L, "BU-2", "LOC-2", 200, 20);
    Warehouse wh3 = domainWarehouse(3L, "BU-3", "LOC-3", 300, 30);
    when(warehouseRepository.getAll()).thenReturn(List.of(wh1, wh2, wh3));

    List<com.warehouse.api.beans.Warehouse> result = resource.listAllWarehousesUnits();

    assertEquals(3, result.size());
    assertEquals("1", result.get(0).getId());
    assertEquals("2", result.get(1).getId());
    assertEquals("3", result.get(2).getId());
  }

  @Test
  void list_all_warehouses_excludes_archived() {
    var active = domainWarehouse(1L, "BU-1", "LOC-1", 100, 10);
    var archived = domainWarehouse(2L, "BU-ARCHIVED", "LOC-2", 200, 20);
    archived.archivedAt = java.time.LocalDateTime.now();
    when(warehouseRepository.getAll()).thenReturn(List.of(active, archived));

    List<com.warehouse.api.beans.Warehouse> result = resource.listAllWarehousesUnits();

    assertEquals(1, result.size());
    assertEquals("BU-1", result.get(0).getBusinessUnitCode());
  }

  @Test
  void list_all_warehouses_empty() {
    when(warehouseRepository.getAll()).thenReturn(List.of());

    List<com.warehouse.api.beans.Warehouse> result = resource.listAllWarehousesUnits();

    assertEquals(0, result.size());
  }

  @Test
  void list_all_warehouses_failure_maps_to_500() {
    when(warehouseRepository.getAll()).thenThrow(new RuntimeException("boom"));

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.listAllWarehousesUnits());

    assertEquals(500, ex.getResponse().getStatus());
  }

  @Test
  void create_warehouse_success() {
    com.warehouse.api.beans.Warehouse request = apiWarehouse("BU-2", "LOC-2", 200, 20);
    doNothing().when(createWarehouseUseCase).create(org.mockito.ArgumentMatchers.any(Warehouse.class));

    com.warehouse.api.beans.Warehouse response = resource.createANewWarehouseUnit(request);

    assertEquals("BU-2", response.getBusinessUnitCode());
    assertEquals("LOC-2", response.getLocation());
    assertEquals(200, response.getCapacity());
    assertEquals(20, response.getStock());
    verify(createWarehouseUseCase).create(org.mockito.ArgumentMatchers.any(Warehouse.class));
  }

  @Test
  void create_warehouse_with_zero_stock() {
    com.warehouse.api.beans.Warehouse request = apiWarehouse("BU-ZERO", "LOC-2", 200, 0);
    doNothing().when(createWarehouseUseCase).create(org.mockito.ArgumentMatchers.any(Warehouse.class));

    com.warehouse.api.beans.Warehouse response = resource.createANewWarehouseUnit(request);

    assertEquals(0, response.getStock());
    verify(createWarehouseUseCase).create(org.mockito.ArgumentMatchers.any(Warehouse.class));
  }

  @Test
  void create_warehouse_with_full_capacity() {
    com.warehouse.api.beans.Warehouse request = apiWarehouse("BU-FULL", "LOC-2", 500, 500);
    doNothing().when(createWarehouseUseCase).create(org.mockito.ArgumentMatchers.any(Warehouse.class));

    com.warehouse.api.beans.Warehouse response = resource.createANewWarehouseUnit(request);

    assertEquals(500, response.getCapacity());
    assertEquals(500, response.getStock());
  }

  @Test
  void create_warehouse_web_exception_passthrough() {
    com.warehouse.api.beans.Warehouse request = apiWarehouse("BU-3", "LOC-3", 200, 20);
    doThrow(new WebApplicationException("invalid", 400))
        .when(createWarehouseUseCase)
        .create(org.mockito.ArgumentMatchers.any(Warehouse.class));

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.createANewWarehouseUnit(request));

    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  void create_warehouse_unexpected_exception_maps_to_500() {
    com.warehouse.api.beans.Warehouse request = apiWarehouse("BU-3X", "LOC-3", 200, 20);
    doThrow(new RuntimeException("create failed"))
        .when(createWarehouseUseCase)
        .create(org.mockito.ArgumentMatchers.any(Warehouse.class));

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.createANewWarehouseUnit(request));

    assertEquals(500, ex.getResponse().getStatus());
    assertTrue(ex.getMessage().contains("Failed to create warehouse"));
  }

  @Test
  void get_by_id_success() {
    when(warehouseRepository.findById(4L))
        .thenReturn(dbWarehouse(4L, "BU-4", "LOC-4", 300, 30));

    com.warehouse.api.beans.Warehouse response = resource.getAWarehouseUnitByID("4");

    assertEquals("4", response.getId());
    assertEquals("BU-4", response.getBusinessUnitCode());
    assertEquals("LOC-4", response.getLocation());
    assertEquals(300, response.getCapacity());
    assertEquals(30, response.getStock());
  }

  @Test
  void get_by_id_single_digit() {
    when(warehouseRepository.findById(1L))
        .thenReturn(dbWarehouse(1L, "BU-1", "LOC-1", 100, 10));

    com.warehouse.api.beans.Warehouse response = resource.getAWarehouseUnitByID("1");

    assertEquals("1", response.getId());
  }

  @Test
  void get_by_id_large_number() {
    when(warehouseRepository.findById(99999L))
        .thenReturn(dbWarehouse(99999L, "BU-99999", "LOC-99999", 1000, 100));

    com.warehouse.api.beans.Warehouse response = resource.getAWarehouseUnitByID("99999");

    assertEquals("99999", response.getId());
  }

  @Test
  void get_by_id_zero() {
    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.getAWarehouseUnitByID("0"));

    assertEquals(404, ex.getResponse().getStatus());
  }

  @Test
  void get_by_id_negative_number() {
    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.getAWarehouseUnitByID("-1"));

    assertEquals(404, ex.getResponse().getStatus());
  }

  @Test
  void get_by_id_archived_returns_404() {
    var dbWh = dbWarehouse(4L, "BU-4", "LOC-4", 300, 30);
    dbWh.archivedAt = java.time.LocalDateTime.now();
    when(warehouseRepository.findById(4L)).thenReturn(dbWh);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.getAWarehouseUnitByID("4"));

    assertEquals(404, ex.getResponse().getStatus());
    assertTrue(ex.getMessage().contains("archived"));
  }

  @Test
  void get_by_id_not_found() {
    when(warehouseRepository.findById(999L)).thenReturn(null);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.getAWarehouseUnitByID("999"));

    assertEquals(404, ex.getResponse().getStatus());
    assertTrue(ex.getMessage().contains("not found"));
  }

  @Test
  void get_by_id_invalid_format() {
    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.getAWarehouseUnitByID("NOT-A-NUMBER"));

    assertEquals(400, ex.getResponse().getStatus());
    assertTrue(ex.getMessage().contains("Invalid warehouse ID format"));
  }

  @Test
  void get_by_id_with_spaces() {
    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.getAWarehouseUnitByID("  123  "));

    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  void get_by_id_decimal_number() {
    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.getAWarehouseUnitByID("123.45"));

    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  void get_by_id_repository_failure_maps_to_500() {
    when(warehouseRepository.findById(5L))
        .thenThrow(new RuntimeException("db down"));

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.getAWarehouseUnitByID("5"));

    assertEquals(500, ex.getResponse().getStatus());
    assertTrue(ex.getMessage().contains("Failed to retrieve warehouse"));
  }

  @Test
  void archive_success() {
    com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse dbWarehouse = 
        dbWarehouse(5L, "BU-5", "LOC-5", 300, 30);
    when(warehouseRepository.findById(5L)).thenReturn(dbWarehouse);
    doNothing().when(archiveWarehouseUseCase).archive(org.mockito.ArgumentMatchers.any(Warehouse.class));

    resource.archiveAWarehouseUnitByID("5");

    verify(archiveWarehouseUseCase).archive(org.mockito.ArgumentMatchers.any(Warehouse.class));
  }

  @Test
  void archive_first_warehouse() {
    var dbWh = dbWarehouse(1L, "BU-1", "LOC-1", 100, 10);
    when(warehouseRepository.findById(1L)).thenReturn(dbWh);
    doNothing().when(archiveWarehouseUseCase).archive(org.mockito.ArgumentMatchers.any(Warehouse.class));

    resource.archiveAWarehouseUnitByID("1");

    verify(archiveWarehouseUseCase).archive(org.mockito.ArgumentMatchers.any(Warehouse.class));
  }

  @Test
  void archive_full_capacity_warehouse() {
    var dbWh = dbWarehouse(10L, "BU-FULL", "LOC-10", 1000, 1000);
    when(warehouseRepository.findById(10L)).thenReturn(dbWh);
    doNothing().when(archiveWarehouseUseCase).archive(org.mockito.ArgumentMatchers.any(Warehouse.class));

    resource.archiveAWarehouseUnitByID("10");

    verify(archiveWarehouseUseCase).archive(org.mockito.ArgumentMatchers.any(Warehouse.class));
  }

  @Test
  void archive_already_archived_returns_404() {
    var dbWh = dbWarehouse(5L, "BU-5", "LOC-5", 300, 30);
    dbWh.archivedAt = java.time.LocalDateTime.now();
    when(warehouseRepository.findById(5L)).thenReturn(dbWh);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("5"));

    assertEquals(404, ex.getResponse().getStatus());
    assertTrue(ex.getMessage().contains("already archived"));
  }

  @Test
  void archive_not_found() {
    when(warehouseRepository.findById(999L)).thenReturn(null);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("999"));

    assertEquals(404, ex.getResponse().getStatus());
  }

  @Test
  void archive_invalid_format() {
    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("INVALID-ID"));

    assertEquals(400, ex.getResponse().getStatus());
    assertTrue(ex.getMessage().contains("Invalid warehouse ID format"));
  }

  @Test
  void archive_negative_id() {
    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("-5"));

    assertEquals(404, ex.getResponse().getStatus());
  }

  @Test
  void archive_decimal_id() {
    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("5.5"));

    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  void archive_failure_maps_to_500() {
    com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse dbWarehouse = 
        dbWarehouse(6L, "BU-ERR", "LOC-9", 300, 30);
    when(warehouseRepository.findById(6L)).thenReturn(dbWarehouse);
    doThrow(new RuntimeException("archive failed")).when(archiveWarehouseUseCase).archive(org.mockito.ArgumentMatchers.any(Warehouse.class));

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("6"));

    assertEquals(500, ex.getResponse().getStatus());
    assertTrue(ex.getMessage().contains("Failed to archive warehouse"));
  }

  @Test
  void archive_repository_failure() {
    when(warehouseRepository.findById(7L)).thenThrow(new RuntimeException("db connection error"));

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("7"));

    assertEquals(500, ex.getResponse().getStatus());
  }

  @Test
  void replace_success() {
    com.warehouse.api.beans.Warehouse request = apiWarehouse("ignored", "LOC-6", 400, 40);
    doNothing().when(replaceWarehouseUseCase).replace(org.mockito.ArgumentMatchers.any(Warehouse.class));

    com.warehouse.api.beans.Warehouse response =
        resource.replaceTheCurrentActiveWarehouse("BU-6", request);

    assertEquals("BU-6", response.getBusinessUnitCode());
    assertEquals("LOC-6", response.getLocation());
    assertEquals(400, response.getCapacity());
    assertEquals(40, response.getStock());
    verify(replaceWarehouseUseCase).replace(org.mockito.ArgumentMatchers.any(Warehouse.class));
  }

  @Test
  void replace_with_different_location() {
    com.warehouse.api.beans.Warehouse request = apiWarehouse("ignored", "NEW-LOCATION", 500, 50);
    doNothing().when(replaceWarehouseUseCase).replace(org.mockito.ArgumentMatchers.any(Warehouse.class));

    com.warehouse.api.beans.Warehouse response =
        resource.replaceTheCurrentActiveWarehouse("BU-REPLACE", request);

    assertEquals("BU-REPLACE", response.getBusinessUnitCode());
    assertEquals("NEW-LOCATION", response.getLocation());
    assertEquals(500, response.getCapacity());
  }

  @Test
  void replace_with_max_capacity() {
    com.warehouse.api.beans.Warehouse request = apiWarehouse("ignored", "LOC-MAX", 9999, 9999);
    doNothing().when(replaceWarehouseUseCase).replace(org.mockito.ArgumentMatchers.any(Warehouse.class));

    com.warehouse.api.beans.Warehouse response =
        resource.replaceTheCurrentActiveWarehouse("BU-MAX", request);

    assertEquals(9999, response.getCapacity());
    assertEquals(9999, response.getStock());
  }

  @Test
  void replace_with_zero_stock() {
    com.warehouse.api.beans.Warehouse request = apiWarehouse("ignored", "LOC-7Z", 400, 0);
    doNothing().when(replaceWarehouseUseCase).replace(org.mockito.ArgumentMatchers.any(Warehouse.class));

    com.warehouse.api.beans.Warehouse response =
        resource.replaceTheCurrentActiveWarehouse("BU-ZERO", request);

    assertEquals(0, response.getStock());
  }

  @Test
  void replace_failure_maps_to_500() {
    com.warehouse.api.beans.Warehouse request = apiWarehouse("ignored", "LOC-7", 400, 40);
    doThrow(new RuntimeException("replace failed"))
        .when(replaceWarehouseUseCase)
        .replace(org.mockito.ArgumentMatchers.any(Warehouse.class));

    WebApplicationException ex =
        assertThrows(
            WebApplicationException.class,
            () -> resource.replaceTheCurrentActiveWarehouse("BU-7", request));

    assertEquals(500, ex.getResponse().getStatus());
    assertTrue(ex.getMessage().contains("Failed to replace warehouse"));
  }

  @Test
  void replace_web_exception_passthrough() {
    com.warehouse.api.beans.Warehouse request = apiWarehouse("ignored", "LOC-8", 400, 40);
    doThrow(new WebApplicationException("invalid replacement", 400))
        .when(replaceWarehouseUseCase)
        .replace(org.mockito.ArgumentMatchers.any(Warehouse.class));

    WebApplicationException ex =
        assertThrows(
            WebApplicationException.class,
            () -> resource.replaceTheCurrentActiveWarehouse("BU-8", request));

    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  void replace_with_conflict_status() {
    com.warehouse.api.beans.Warehouse request = apiWarehouse("ignored", "LOC-9", 400, 40);
    doThrow(new WebApplicationException("conflict", 409))
        .when(replaceWarehouseUseCase)
        .replace(org.mockito.ArgumentMatchers.any(Warehouse.class));

    WebApplicationException ex =
        assertThrows(
            WebApplicationException.class,
            () -> resource.replaceTheCurrentActiveWarehouse("BU-9", request));

    assertEquals(409, ex.getResponse().getStatus());
  }

  private Warehouse domainWarehouse(Long id, String bu, String location, int capacity, int stock) {
    Warehouse warehouse = new Warehouse();
    warehouse.id = id;
    warehouse.businessUnitCode = bu;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    return warehouse;
  }

  private Warehouse domainWarehouse(String bu, String location, int capacity, int stock) {
    return domainWarehouse(null, bu, location, capacity, stock);
  }

  private com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse dbWarehouse(
      Long id, String bu, String location, int capacity, int stock) {
    var warehouse = new com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse();
    warehouse.id = id;
    warehouse.businessUnitCode = bu;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    warehouse.archivedAt = null;
    return warehouse;
  }

  private com.warehouse.api.beans.Warehouse apiWarehouse(
      String bu, String location, int capacity, int stock) {
    com.warehouse.api.beans.Warehouse warehouse = new com.warehouse.api.beans.Warehouse();
    warehouse.setBusinessUnitCode(bu);
    warehouse.setLocation(location);
    warehouse.setCapacity(capacity);
    warehouse.setStock(stock);
    return warehouse;
  }

  private void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}
