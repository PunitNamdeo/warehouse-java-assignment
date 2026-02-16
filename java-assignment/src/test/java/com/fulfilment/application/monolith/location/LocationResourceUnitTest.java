package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import jakarta.ws.rs.core.Response;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class LocationResourceUnitTest {

  @Test
  void resolve_location_success() throws Exception {
    LocationResource resource = new LocationResource();
    LocationGateway gateway = mock(LocationGateway.class);
    setField(resource, "locationGateway", gateway);

    Location location = new Location("ZWOLLE-001", 2, 500);
    when(gateway.resolveByIdentifier("ZWOLLE-001")).thenReturn(location);

    Response response = resource.resolveLocation("ZWOLLE-001");

    assertEquals(200, response.getStatus());
    LocationResource.LocationDto dto = (LocationResource.LocationDto) response.getEntity();
    assertEquals("ZWOLLE-001", dto.identification);
    assertEquals(2, dto.maxNumberOfWarehouses);
    assertEquals(500, dto.maxCapacity);
  }

  @Test
  void resolve_location_not_found() throws Exception {
    LocationResource resource = new LocationResource();
    LocationGateway gateway = mock(LocationGateway.class);
    setField(resource, "locationGateway", gateway);

    when(gateway.resolveByIdentifier("UNKNOWN")).thenReturn(null);

    Response response = resource.resolveLocation("UNKNOWN");

    assertEquals(404, response.getStatus());
    LocationResource.ErrorResponse error = (LocationResource.ErrorResponse) response.getEntity();
    assertEquals("jakarta.ws.rs.NotFoundException", error.exceptionType);
    assertEquals(404, error.code);
    assertTrue(error.error.contains("UNKNOWN"));
  }

  @Test
  void dto_and_errorresponse_constructors() {
    LocationResource.LocationDto dto = new LocationResource.LocationDto("A", 1, 10);
    assertEquals("A", dto.identification);
    assertEquals(1, dto.maxNumberOfWarehouses);
    assertEquals(10, dto.maxCapacity);

    LocationResource.ErrorResponse error =
        new LocationResource.ErrorResponse("type", 400, "bad request");
    assertEquals("type", error.exceptionType);
    assertEquals(400, error.code);
    assertEquals("bad request", error.error);
    assertNotNull(error);
  }

  private void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}
