package com.fulfilment.application.monolith.stores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import org.jboss.logging.Logger;

/**
 * REST endpoint for Store management.
 * 
 * Endpoints:
 * - GET /store - List all stores
 * - GET /store/{id} - Get store by ID
 * - POST /store - Create new store
 * - PUT /store/{id} - Update entire store
 * - PATCH /store/{id} - Partial update store
 * - DELETE /store/{id} - Delete store
 * 
 * Exception Handling:
 * - 400 Bad Request: Invalid input (null name, missing fields)
 * - 404 Not Found: Store does not exist
 * - 409 Conflict: Business logic constraint violated
 * - 422 Unprocessable Entity: Request contains invalid data
 * - 500 Internal Server Error: Unexpected error
 * 
 * Logging:
 * - INFO: Business events (store created, retrieved, deleted)
 * - WARN: Validation failures, constraint violations
 * - ERROR: System errors, exceptions
 */
@Path("store")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class StoreResource {

  @Inject 
  LegacyStoreManagerGateway legacyStoreManagerGateway;

  @Inject 
  EntityManager entityManager;

  @Inject
  TransactionSynchronizationRegistry transactionSynchronizationRegistry;

  private static final Logger LOGGER = Logger.getLogger(StoreResource.class.getName());

  /**
   * Retrieve all stores sorted by name.
   * 
   * @return List of all stores
   */
  @GET
  public List<Store> get() {
    try {
      Log.infof("Retrieving all stores");
      List<Store> stores = Store.listAll(Sort.by("name"));
      Log.infof("Retrieved %d stores", stores.size());
      return stores;
    } catch (Exception e) {
      Log.errorf("Error retrieving stores: %s", e.getMessage());
      throw new WebApplicationException("Failed to retrieve stores", 500);
    }
  }

  /**
   * Retrieve a single store by ID.
   * 
   * @param id Store ID
   * @return Store entity
   * @throws WebApplicationException if store not found (404)
   */
  @GET
  @Path("{id}")
  public Store getSingle(Long id) {
    try {
      validateId(id);
      Log.infof("Retrieving store with ID: %d", id);
      
      Store entity = Store.findById(id);
      if (entity == null) {
        Log.warnf("Store with ID %d not found", id);
        throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
      }
      
      Log.infof("Successfully retrieved store: %s (ID: %d)", entity.name, id);
      return entity;
    } catch (WebApplicationException e) {
      throw e;
    } catch (Exception e) {
      Log.errorf(e, "Error retrieving store with ID %d", id);
      throw new WebApplicationException("Failed to retrieve store", 500);
    }
  }

  /**
   * Create a new store.
   * 
   * @param store Store entity to create
   * @return Response with created store (201)
   * @throws WebApplicationException if validation fails (400, 422)
   */
  @POST
  @Transactional
  public Response create(Store store) {
    try {
      // Input validation
      validateStoreInput(store);
      
      if (store.id != null) {
        Log.warnf("Attempted to create store with pre-set ID: %d", store.id);
        throw new WebApplicationException("Id was invalidly set on request.", 422);
      }

      if (store.name == null || store.name.trim().isEmpty()) {
        Log.warnf("Attempted to create store with null or empty name");
        throw new WebApplicationException("Store name is required and cannot be empty", 400);
      }

      Log.infof("Creating new store: %s", store.name);
      store.persist();
      entityManager.flush();

      // Register callback to execute AFTER successful transaction commit
      transactionSynchronizationRegistry.registerInterposedSynchronization(
          new Synchronization() {
            @Override
            public void afterCompletion(int status) {
              // Only execute if transaction committed successfully (status = STATUS_COMMITTED)
              if (status == Status.STATUS_COMMITTED) {
                try {
                  Log.infof("Transaction committed, notifying legacy system for store: %s", store.name);
                  legacyStoreManagerGateway.createStoreOnLegacySystem(store);
                  Log.infof("Successfully notified legacy system for store: %s (ID: %d)", store.name, store.id);
                } catch (Exception legacyError) {
                  // This is AFTER commit, so we cannot rollback DB. We must fail the operation.
                  Log.errorf(legacyError, "CRITICAL: Legacy system notification failed after DB commit for store %s. Data inconsistency detected!", store.name);
                  throw new RuntimeException("Legacy system notification failed after database commit", legacyError);
                }
              }
            }

            @Override
            public void beforeCompletion() {
              // Not needed for this use case
            }
          });

      return Response.ok(store).status(201).build();
    } catch (WebApplicationException e) {
      throw e;
    } catch (PersistenceException e) {
      Log.errorf(e, "Database error creating store");
      throw new WebApplicationException("Database error creating store", 500);
    } catch (Exception e) {
      Log.errorf(e, "Unexpected error creating store");
      throw new WebApplicationException("Failed to create store", 500);
    }
  }

  /**
   * Update an entire store (all fields).
   * 
   * @param id Store ID to update
   * @param updatedStore Updated store entity
   * @return Updated store
   * @throws WebApplicationException if validation fails or store not found
   */
  @PUT
  @Path("{id}")
  @Transactional
  public Store update(Long id, Store updatedStore) {
    try {
      validateId(id);
      validateStoreInput(updatedStore);

      if (updatedStore.name == null || updatedStore.name.trim().isEmpty()) {
        Log.warnf("Attempted to update store %d with null or empty name", id);
        throw new WebApplicationException("Store Name was not set on request.", 422);
      }

      Log.infof("Updating store ID %d", id);
      Store entity = Store.findById(id);

      if (entity == null) {
        Log.warnf("Store with ID %d not found for update", id);
        throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
      }

      String oldName = entity.name;
      entity.name = updatedStore.name;
      entity.quantityProductsInStock = updatedStore.quantityProductsInStock;
      
      entityManager.flush();

      // Register callback to execute AFTER successful transaction commit
      transactionSynchronizationRegistry.registerInterposedSynchronization(
          new Synchronization() {
            @Override
            public void afterCompletion(int status) {
              // Only execute if transaction committed successfully (status = STATUS_COMMITTED)
              if (status == Status.STATUS_COMMITTED) {
                try {
                  Log.infof("Transaction committed, notifying legacy system for store update: %s (ID: %d)", entity.name, id);
                  legacyStoreManagerGateway.updateStoreOnLegacySystem(entity);
                  Log.infof("Successfully notified legacy system for store update: %s (ID: %d)", entity.name, id);
                } catch (Exception legacyError) {
                  // This is AFTER commit, so we cannot rollback DB. We must fail.
                  Log.errorf(legacyError, "CRITICAL: Legacy system notification failed after DB commit for store %d update. Data inconsistency detected!", id);
                  throw new RuntimeException("Legacy system notification failed after database commit", legacyError);
                }
              }
            }

            @Override
            public void beforeCompletion() {
              // Not needed for this use case
            }
          });

      return entity;
    } catch (WebApplicationException e) {
      throw e;
    } catch (PersistenceException e) {
      Log.errorf(e, "Database error updating store %d", id);
      throw new WebApplicationException("Database error updating store", 500);
    } catch (Exception e) {
      Log.errorf(e, "Unexpected error updating store %d", id);
      throw new WebApplicationException("Failed to update store", 500);
    }
  }

  /**
   * Partial update of a store (only provided fields).
   * 
   * @param id Store ID to patch
   * @param updatedStore Partial store entity
   * @return Updated store
   * @throws WebApplicationException if validation fails or store not found
   */
  @PATCH
  @Path("{id}")
  @Transactional
  public Store patch(Long id, Store updatedStore) {
    try {
      validateId(id);

      Log.infof("Patching store ID %d", id);
      Store entity = Store.findById(id);

      if (entity == null) {
        Log.warnf("Store with ID %d not found for patch", id);
        throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
      }

      if (updatedStore.name != null && !updatedStore.name.trim().isEmpty()) {
        String oldName = entity.name;
        entity.name = updatedStore.name;
        Log.infof("Patched store ID %d name from '%s' to '%s'", id, oldName, updatedStore.name);
      }

      if (updatedStore.quantityProductsInStock >= 0) {
        entity.quantityProductsInStock = updatedStore.quantityProductsInStock;
      }

      entityManager.flush();

      // Register callback to execute AFTER successful transaction commit
      transactionSynchronizationRegistry.registerInterposedSynchronization(
          new Synchronization() {
            @Override
            public void afterCompletion(int status) {
              // Only execute if transaction committed successfully (status = STATUS_COMMITTED)
              if (status == Status.STATUS_COMMITTED) {
                try {
                  Log.infof("Transaction committed, notifying legacy system for store patch: %s (ID: %d)", entity.name, id);
                  legacyStoreManagerGateway.updateStoreOnLegacySystem(entity);
                  Log.infof("Successfully notified legacy system for store patch: %s (ID: %d)", entity.name, id);
                } catch (Exception legacyError) {
                  // This is AFTER commit, so we cannot rollback DB. We must fail.
                  Log.errorf(legacyError, "CRITICAL: Legacy system notification failed after DB commit for store %d patch. Data inconsistency detected!", id);
                  throw new RuntimeException("Legacy system notification failed after database commit", legacyError);
                }
              }
            }

            @Override
            public void beforeCompletion() {
              // Not needed for this use case
            }
          });

      return entity;
    } catch (WebApplicationException e) {
      throw e;
    } catch (PersistenceException e) {
      Log.errorf(e, "Database error patching store %d", id);
      throw new WebApplicationException("Database error patching store", 500);
    } catch (Exception e) {
      Log.errorf(e, "Unexpected error patching store %d", id);
      throw new WebApplicationException("Failed to patch store", 500);
    }
  }

  /**
   * Delete a store by ID.
   * 
   * @param id Store ID to delete
   * @return 204 No Content response
   * @throws WebApplicationException if store not found (404)
   */
  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(Long id) {
    try {
      validateId(id);
      
      Log.infof("Deleting store with ID %d", id);
      Store entity = Store.findById(id);
      
      if (entity == null) {
        Log.warnf("Store with ID %d not found for deletion", id);
        throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
      }

      String storeName = entity.name;
      entity.delete();
      Log.infof("Successfully deleted store: %s (ID: %d)", storeName, id);
      
      return Response.status(204).build();
    } catch (WebApplicationException e) {
      throw e;
    } catch (PersistenceException e) {
      Log.errorf(e, "Database error deleting store %d", id);
      throw new WebApplicationException("Database error deleting store", 500);
    } catch (Exception e) {
      Log.errorf(e, "Unexpected error deleting store %d", id);
      throw new WebApplicationException("Failed to delete store", 500);
    }
  }

  /**
   * Validate store input.
   * 
   * @param store Store to validate
   * @throws WebApplicationException if validation fails
   */
  private void validateStoreInput(Store store) {
    if (store == null) {
      Log.warnf("Null store object provided");
      throw new WebApplicationException("Store object cannot be null", 400);
    }
  }

  /**
   * Validate ID parameter.
   * 
   * @param id ID to validate
   * @throws WebApplicationException if ID is invalid
   */
  private void validateId(Long id) {
    if (id == null || id <= 0) {
      throw new WebApplicationException("Invalid store ID: " + id, 400);
    }
  }

  /**
   * Error mapper for all exceptions.
   * Converts exceptions to proper HTTP responses.
   */
  @Provider
  public static class ErrorMapper implements ExceptionMapper<Exception> {

    @Inject 
    ObjectMapper objectMapper;

    @Override
    public Response toResponse(Exception exception) {
      LOGGER.error("Failed to handle request", exception);

      int code = 500;
      if (exception instanceof WebApplicationException) {
        code = ((WebApplicationException) exception).getResponse().getStatus();
      }

      ObjectNode exceptionJson = objectMapper.createObjectNode();
      exceptionJson.put("exceptionType", exception.getClass().getName());
      exceptionJson.put("code", code);

      if (exception.getMessage() != null) {
        exceptionJson.put("error", exception.getMessage());
      }

      return Response.status(code).entity(exceptionJson).build();
    }
  }
}
