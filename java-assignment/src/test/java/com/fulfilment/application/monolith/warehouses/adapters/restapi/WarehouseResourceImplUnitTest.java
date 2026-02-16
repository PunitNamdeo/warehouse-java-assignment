package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

class WarehouseResourceImplUnitTest {

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
    verify(createWarehouseUseCase).create(org.mockito.ArgumentMatchers.any(Warehouse.class));
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
    when(warehouseRepository.findByBusinessUnitCode("BU-4"))
        .thenReturn(domainWarehouse("BU-4", "LOC-4", 300, 30));

    com.warehouse.api.beans.Warehouse response = resource.getAWarehouseUnitByID("BU-4");

    assertEquals("BU-4", response.getBusinessUnitCode());
    assertEquals("LOC-4", response.getLocation());
  }

  @Test
  void get_by_id_not_found() {
    when(warehouseRepository.findByBusinessUnitCode("MISSING")).thenReturn(null);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.getAWarehouseUnitByID("MISSING"));

    assertEquals(404, ex.getResponse().getStatus());
    assertTrue(ex.getMessage().contains("not found"));
  }

  @Test
  void get_by_id_repository_failure_maps_to_500() {
    when(warehouseRepository.findByBusinessUnitCode("BROKEN"))
        .thenThrow(new RuntimeException("db down"));

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.getAWarehouseUnitByID("BROKEN"));

    assertEquals(500, ex.getResponse().getStatus());
    assertTrue(ex.getMessage().contains("Failed to retrieve warehouse"));
  }

  @Test
  void archive_success() {
    Warehouse warehouse = domainWarehouse("BU-5", "LOC-5", 300, 30);
    when(warehouseRepository.findByBusinessUnitCode("BU-5")).thenReturn(warehouse);
    doNothing().when(archiveWarehouseUseCase).archive(warehouse);

    resource.archiveAWarehouseUnitByID("BU-5");

    verify(archiveWarehouseUseCase).archive(warehouse);
  }

  @Test
  void archive_not_found() {
    when(warehouseRepository.findByBusinessUnitCode("BU-X")).thenReturn(null);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("BU-X"));

    assertEquals(404, ex.getResponse().getStatus());
  }

  @Test
  void archive_failure_maps_to_500() {
    Warehouse warehouse = domainWarehouse("BU-ERR", "LOC-9", 300, 30);
    when(warehouseRepository.findByBusinessUnitCode("BU-ERR")).thenReturn(warehouse);
    doThrow(new RuntimeException("archive failed")).when(archiveWarehouseUseCase).archive(warehouse);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("BU-ERR"));

    assertEquals(500, ex.getResponse().getStatus());
    assertTrue(ex.getMessage().contains("Failed to archive warehouse"));
  }

  @Test
  void replace_success() {
    com.warehouse.api.beans.Warehouse request = apiWarehouse("ignored", "LOC-6", 400, 40);
    doNothing().when(replaceWarehouseUseCase).replace(org.mockito.ArgumentMatchers.any(Warehouse.class));

    com.warehouse.api.beans.Warehouse response =
        resource.replaceTheCurrentActiveWarehouse("BU-6", request);

    assertEquals("BU-6", response.getBusinessUnitCode());
    assertEquals("LOC-6", response.getLocation());
    verify(replaceWarehouseUseCase).replace(org.mockito.ArgumentMatchers.any(Warehouse.class));
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

  private Warehouse domainWarehouse(String bu, String location, int capacity, int stock) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = bu;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
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
