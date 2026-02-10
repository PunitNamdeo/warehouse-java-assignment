package com.fulfilment.application.monolith.fulfillment.adapters.restapi;

import com.fulfilment.application.monolith.fulfillment.adapters.database.WarehouseProductStoreRepository;
import com.fulfilment.application.monolith.fulfillment.domain.usecases.AssociateWarehouseToProductStoreUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import java.util.Map;
import org.jboss.logging.Logger;

@Path("fulfillment/warehouse-product-store")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class FulfillmentResource {

  @Inject private WarehouseProductStoreRepository repository;

  @Inject private AssociateWarehouseToProductStoreUseCase associateUseCase;

  private static final Logger LOGGER = Logger.getLogger(FulfillmentResource.class.getName());

  @POST
  @Transactional
  public Response associate(AssociationRequest request) {
    if (request.productId == null || request.storeId == null || request.warehouseBusinessUnitCode == null) {
      throw new WebApplicationException(
          "productId, storeId, and warehouseBusinessUnitCode are required", 400);
    }

    associateUseCase.associate(request.productId, request.storeId, request.warehouseBusinessUnitCode);

    return Response.ok()
        .entity(
            Map.of(
                "productId",
                request.productId,
                "storeId",
                request.storeId,
                "warehouseBusinessUnitCode",
                request.warehouseBusinessUnitCode,
                "message",
                "Association created successfully"))
        .status(201)
        .build();
  }

  @GET
  @Path("product/{productId}/store/{storeId}")
  public Response getWarehousesForProductStore(
      @PathParam("productId") Long productId, @PathParam("storeId") Long storeId) {
    var associations = repository.findByProductAndStore(productId, storeId);
    return Response.ok(associations).build();
  }

  @GET
  @Path("store/{storeId}")
  public Response getWarehousesForStore(@PathParam("storeId") Long storeId) {
    var associations = repository.findByStore(storeId);
    return Response.ok(associations).build();
  }

  @GET
  @Path("warehouse/{warehouseCode}")
  public Response getProductsForWarehouse(@PathParam("warehouseCode") String warehouseCode) {
    var associations = repository.findByWarehouse(warehouseCode);
    return Response.ok(associations).build();
  }

  @DELETE
  @Path("product/{productId}/store/{storeId}/warehouse/{warehouseCode}")
  @Transactional
  public Response dissociate(
      @PathParam("productId") Long productId,
      @PathParam("storeId") Long storeId,
      @PathParam("warehouseCode") String warehouseCode) {
    associateUseCase.dissociate(productId, storeId, warehouseCode);
    return Response.status(204).build();
  }

  public static class AssociationRequest {
    public Long productId;
    public Long storeId;
    public String warehouseBusinessUnitCode;
  }

  @Provider
  public static class ErrorMapper implements ExceptionMapper<Exception> {

    @Inject ObjectMapper objectMapper;

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
