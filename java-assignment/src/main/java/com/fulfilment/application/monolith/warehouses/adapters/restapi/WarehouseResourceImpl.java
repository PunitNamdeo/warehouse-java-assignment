package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ArchiveWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ReplaceWarehouseUseCase;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;

/**
 * REST API implementation for Warehouse Management.
 * 
 * Implements all warehouse operations with proper transaction handling and validation:
 * - POST /warehouse - Create new warehouse
 * - GET /warehouse - List all active warehouses
 * - GET /warehouse/{id} - Get specific warehouse by business unit code
 * - DELETE /warehouse/{id} - Archive warehouse
 * - POST /warehouse/{businessUnitCode}/replacement - Replace warehouse with same code
 * 
 * All operations include comprehensive business rule validation:
 * - Business Unit Code uniqueness
 * - Location validation
 * - Capacity constraints per location
 * - Stock vs capacity validation
 * - Maximum warehouses per location constraint
 */
@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject private WarehouseRepository warehouseRepository;

  @Inject private CreateWarehouseUseCase createWarehouseUseCase;

  @Inject private ArchiveWarehouseUseCase archiveWarehouseUseCase;

  @Inject private ReplaceWarehouseUseCase replaceWarehouseUseCase;

  /**
   * List all active (non-archived) warehouses.
   *
   * @return List of all active warehouses
   */
  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    try {
      Log.infof("Retrieving all active warehouses");
      List<Warehouse> warehouses =
          warehouseRepository.getAll().stream()
              .filter(dbWh -> dbWh.archivedAt == null)
              .map(this::toWarehouseResponse)
              .toList();
      Log.infof("Retrieved %d active warehouses", warehouses.size());
      return warehouses;
    } catch (Exception e) {
      Log.errorf(e, "Error retrieving warehouses");
      throw new WebApplicationException("Failed to retrieve warehouses", 500);
    }
  }

  /**
   * Create a new warehouse with comprehensive validation.
   *
   * <p>Validations performed:
   * - Business Unit Code must be unique
   * - Location must be valid and exist
   * - Warehouse capacity must not exceed location max capacity
   * - Stock must not exceed warehouse capacity
   * - Cannot exceed max warehouses per location
   *
   * @param data Warehouse data to create
   * @return Created warehouse with assigned ID
   * @throws WebApplicationException with appropriate HTTP status:
   *     - 400 Bad Request: Invalid location, capacity, or stock
   *     - 409 Conflict: Business Unit Code already exists or max warehouses reached
   */
  @Override
  @Transactional
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    try {
      Log.infof(
          "Creating new warehouse with Business Unit Code: %s at Location: %s",
          data.getBusinessUnitCode(), data.getLocation());

      var domainWarehouse = toDomainModel(data);
      createWarehouseUseCase.create(domainWarehouse);

      Warehouse response = toWarehouseResponse(domainWarehouse);
      Log.infof("Successfully created warehouse: %s", data.getBusinessUnitCode());
      return response;
    } catch (WebApplicationException e) {
      Log.warnf(
          "Validation failed for warehouse creation: %s (HTTP %d)",
          e.getMessage(), e.getResponse().getStatus());
      throw e;
    } catch (Exception e) {
      Log.errorf(e, "Error creating warehouse");
      throw new WebApplicationException("Failed to create warehouse: " + e.getMessage(), 500);
    }
  }

  /**
   * Get a specific warehouse by database ID (primary key).
   *
   * Uses active-only filtering (excludes archived warehouses).
   *
   * @param id Database primary key (Long as String) of the warehouse to retrieve
   * @return Warehouse details if found and not archived
   * @throws WebApplicationException with 404 Not Found if warehouse doesn't exist or is archived, or with 400 if ID is invalid
   */
  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    try {
      Log.infof("Retrieving warehouse with ID: %s", id);
      Long warehouseId;
      try {
        warehouseId = Long.parseLong(id);
      } catch (NumberFormatException e) {
        Log.warnf("Invalid warehouse ID format: %s", id);
        throw new WebApplicationException(
            "Invalid warehouse ID format. ID must be a valid number.", 400);
      }
      
      var dbWarehouse = warehouseRepository.findById(warehouseId);
      if (dbWarehouse == null) {
        Log.warnf("Warehouse with ID '%s' not found", id);
        throw new WebApplicationException(
            "Warehouse with ID '" + id + "' not found.", 404);
      }
      // Check if warehouse is archived
      if (dbWarehouse.archivedAt != null) {
        Log.warnf("Warehouse with ID '%s' is archived", id);
        throw new WebApplicationException(
            "Warehouse with ID '" + id + "' is archived.", 404);
      }
      Log.infof("Successfully retrieved warehouse with ID: %s", id);
      return toWarehouseResponse(dbWarehouse.toWarehouse());
    } catch (WebApplicationException e) {
      throw e;
    } catch (Exception e) {
      Log.errorf(e, "Error retrieving warehouse");
      throw new WebApplicationException("Failed to retrieve warehouse", 500);
    }
  }

  /**
   * Archive a warehouse by marking it as archived (soft delete).
   * 
   * Only active warehouses can be archived. Archives are permanent and cannot be undone.
   * 
   * Archived warehouses:
   * - Are no longer returned in list operations
   * - Cannot be used for new fulfillment associations
   * - Retain historical data in database
   * - Retain their business unit code (preventing code reuse without replacement)
   *
   * @param id Database primary key (Long as String) of the active warehouse to archive
   * @throws WebApplicationException with 404 Not Found if warehouse doesn't exist or is already archived, or with 400 if ID is invalid
   */
  @Override
  @Transactional
  public void archiveAWarehouseUnitByID(String id) {
    try {
      Log.infof("Archiving warehouse with ID: %s", id);
      Long warehouseId;
      try {
        warehouseId = Long.parseLong(id);
      } catch (NumberFormatException e) {
        Log.warnf("Invalid warehouse ID format: %s", id);
        throw new WebApplicationException(
            "Invalid warehouse ID format. ID must be a valid number.", 400);
      }
      
      var dbWarehouse = warehouseRepository.findById(warehouseId);
      if (dbWarehouse == null) {
        Log.warnf("Warehouse with ID '%s' not found for archiving", id);
        throw new WebApplicationException(
            "Warehouse with ID '" + id + "' not found.", 404);
      }
      // Check if warehouse is already archived
      if (dbWarehouse.archivedAt != null) {
        Log.warnf("Warehouse with ID '%s' is already archived", id);
        throw new WebApplicationException(
            "Warehouse with ID '" + id + "' is already archived.", 404);
      }
      var warehouse = dbWarehouse.toWarehouse();
      archiveWarehouseUseCase.archive(warehouse);
      Log.infof("Successfully archived warehouse: %s", id);
    } catch (WebApplicationException e) {
      throw e;
    } catch (Exception e) {
      Log.errorf(e, "Error archiving warehouse");
      throw new WebApplicationException("Failed to archive warehouse", 500);
    }
  }

  /**
   * Replace a warehouse with a new one using the same business unit code.
   *
   * <p>Replacement process:
   * 1. Archives the existing warehouse (soft delete with timestamp)
   * 2. Creates new warehouse with SAME business unit code
   * 3. Validates all constraints for new warehouse
   * 4. Ensures stock matching between old and new warehouse
   *
   * <p>Use case: Warehouse physical upgrade or location change while maintaining code continuity
   *
   * <p>Validations:
   * - Old warehouse must exist
   * - Stock in new warehouse must match old warehouse stock
   * - New warehouse capacity must accommodate the stock
   * - New location must be valid
   * - New capacity must not exceed location max capacity
   *
   * @param businessUnitCode Business Unit Code of warehouse to replace
   * @param data New warehouse data (same businessUnitCode will be assigned)
   * @return Newly created warehouse with the same business unit code
   * @throws WebApplicationException with:
   *     - 404 Not Found: Old warehouse doesn't exist
   *     - 400 Bad Request: Stock mismatch, capacity issues, or invalid location
   */
  @Override
  @Transactional
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {
    try {
      Log.infof("Replacing warehouse with Business Unit Code: %s", businessUnitCode);
      data.setBusinessUnitCode(businessUnitCode);
      var newWarehouse = toDomainModel(data);
      replaceWarehouseUseCase.replace(newWarehouse);
      
      Warehouse response = toWarehouseResponse(newWarehouse);
      Log.infof("Successfully replaced warehouse: %s", businessUnitCode);
      return response;
    } catch (WebApplicationException e) {
      Log.warnf(
          "Validation failed for warehouse replacement: %s (HTTP %d)",
          e.getMessage(), e.getResponse().getStatus());
      throw e;
    } catch (Exception e) {
      Log.errorf(e, "Error replacing warehouse");
      throw new WebApplicationException("Failed to replace warehouse: " + e.getMessage(), 500);
    }
  }

  /**
   * Convert domain warehouse to API response bean.
   * Maps internal domain model to JSON-serializable API response bean.
   */
  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    if (warehouse.id != null) {
      response.setId(String.valueOf(warehouse.id));
    }
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);
    return response;
  }

  /**
   * Convert API request bean to domain model.
   * Maps incoming JSON API request to internal domain model for validation and persistence.
   */
  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomainModel(
      Warehouse apiWarehouse) {
    var warehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    warehouse.businessUnitCode = apiWarehouse.getBusinessUnitCode();
    warehouse.location = apiWarehouse.getLocation();
    warehouse.capacity = apiWarehouse.getCapacity();
    warehouse.stock = apiWarehouse.getStock();
    return warehouse;
  }
}
