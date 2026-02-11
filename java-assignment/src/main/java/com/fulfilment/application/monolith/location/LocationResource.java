package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Path("location")
@ApplicationScoped
@Produces("application/json")
public class LocationResource {

  @Inject
  private LocationGateway locationGateway;

  @GET
  @Path("{id}")
  public Response resolveLocation(@PathParam("id") String id) {
    Location location = locationGateway.resolveByIdentifier(id);
    
    if (location == null) {
      return Response.status(404)
          .entity(new ErrorResponse(
              "jakarta.ws.rs.NotFoundException",
              404,
              "Location with id '" + id + "' not found"))
          .build();
    }
    
    return Response.ok(new LocationDto(
        location.identification,
        location.maxNumberOfWarehouses,
        location.maxCapacity))
        .build();
  }

  public static class LocationDto {
    public String identification;
    public Integer maxNumberOfWarehouses;
    public Integer maxCapacity;

    public LocationDto(String identification, Integer maxNumberOfWarehouses, Integer maxCapacity) {
      this.identification = identification;
      this.maxNumberOfWarehouses = maxNumberOfWarehouses;
      this.maxCapacity = maxCapacity;
    }
  }

  public static class ErrorResponse {
    public String exceptionType;
    public Integer code;
    public String error;

    public ErrorResponse(String exceptionType, Integer code, String error) {
      this.exceptionType = exceptionType;
      this.code = code;
      this.error = error;
    }
  }
}
