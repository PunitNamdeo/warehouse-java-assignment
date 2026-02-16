package com.fulfilment.application.monolith.fulfillment.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.fulfillment.domain.models.WarehouseProductStore;
import com.fulfilment.application.monolith.fulfillment.domain.ports.WarehouseProductStoreStore;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class AssociateWarehouseToProductStoreUseCaseQuarkusCoverageTest {

  @Inject AssociateWarehouseToProductStoreUseCase useCase;

  @InjectMock WarehouseProductStoreStore warehouseProductStoreStore;

  @Test
  void associate_success() {
    when(warehouseProductStoreStore.findAssociation(1L, 1L, "WH-1")).thenReturn(null);
    when(warehouseProductStoreStore.findByProductAndStore(1L, 1L)).thenReturn(List.of());
    when(warehouseProductStoreStore.findByStore(1L)).thenReturn(List.of());
    when(warehouseProductStoreStore.findByWarehouse("WH-1")).thenReturn(List.of());
    doNothing().when(warehouseProductStoreStore).create(any(WarehouseProductStore.class));

    useCase.associate(1L, 1L, "WH-1");

    verify(warehouseProductStoreStore).create(any(WarehouseProductStore.class));
  }

  @Test
  void associate_duplicate_conflict() {
    when(warehouseProductStoreStore.findAssociation(1L, 1L, "WH-1"))
        .thenReturn(new WarehouseProductStore(1L, 1L, "WH-1"));

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.associate(1L, 1L, "WH-1"));

    assertEquals(409, ex.getResponse().getStatus());
    verify(warehouseProductStoreStore, never()).create(any());
  }

  @Test
  void associate_max_warehouses_per_product_store_conflict() {
    when(warehouseProductStoreStore.findAssociation(1L, 1L, "WH-3")).thenReturn(null);
    when(warehouseProductStoreStore.findByProductAndStore(1L, 1L))
        .thenReturn(List.of(
            new WarehouseProductStore(1L, 1L, "WH-1"),
            new WarehouseProductStore(1L, 1L, "WH-2")));

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.associate(1L, 1L, "WH-3"));

    assertEquals(409, ex.getResponse().getStatus());
  }

  @Test
  void associate_max_warehouses_per_store_conflict_when_new_warehouse() {
    when(warehouseProductStoreStore.findAssociation(10L, 1L, "WH-4")).thenReturn(null);
    when(warehouseProductStoreStore.findByProductAndStore(10L, 1L)).thenReturn(List.of());
    when(warehouseProductStoreStore.findByStore(1L))
        .thenReturn(List.of(
            new WarehouseProductStore(1L, 1L, "WH-1"),
            new WarehouseProductStore(2L, 1L, "WH-2"),
            new WarehouseProductStore(3L, 1L, "WH-3")));

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.associate(10L, 1L, "WH-4"));

    assertEquals(409, ex.getResponse().getStatus());
  }

  @Test
  void associate_max_products_per_warehouse_conflict() {
    when(warehouseProductStoreStore.findAssociation(6L, 1L, "WH-1")).thenReturn(null);
    when(warehouseProductStoreStore.findByProductAndStore(6L, 1L)).thenReturn(List.of());
    when(warehouseProductStoreStore.findByStore(1L))
        .thenReturn(List.of(new WarehouseProductStore(1L, 1L, "WH-1")));
    when(warehouseProductStoreStore.findByWarehouse("WH-1"))
        .thenReturn(List.of(
            new WarehouseProductStore(1L, 1L, "WH-1"),
            new WarehouseProductStore(2L, 1L, "WH-1"),
            new WarehouseProductStore(3L, 1L, "WH-1"),
            new WarehouseProductStore(4L, 1L, "WH-1"),
            new WarehouseProductStore(5L, 1L, "WH-1")));

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.associate(6L, 1L, "WH-1"));

    assertEquals(409, ex.getResponse().getStatus());
  }

  @Test
  void dissociate_success() {
    when(warehouseProductStoreStore.findAssociation(1L, 1L, "WH-1"))
        .thenReturn(new WarehouseProductStore(1L, 1L, "WH-1"));
    doNothing().when(warehouseProductStoreStore).remove(1L, 1L, "WH-1");

    useCase.dissociate(1L, 1L, "WH-1");

    verify(warehouseProductStoreStore).remove(1L, 1L, "WH-1");
  }

  @Test
  void dissociate_not_found() {
    when(warehouseProductStoreStore.findAssociation(1L, 1L, "WH-1")).thenReturn(null);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.dissociate(1L, 1L, "WH-1"));

    assertEquals(404, ex.getResponse().getStatus());
  }
}
