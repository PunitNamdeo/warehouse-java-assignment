package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

public class StoreErrorMapperTest {

  @Test
  public void maps_web_application_exception_status_and_message() {
    StoreResource.ErrorMapper mapper = new StoreResource.ErrorMapper();
    mapper.objectMapper = new ObjectMapper();

    Response response = mapper.toResponse(new WebApplicationException("not found", 404));

    assertEquals(404, response.getStatus());
    ObjectNode body = (ObjectNode) response.getEntity();
    assertEquals(404, body.get("code").asInt());
    assertEquals("not found", body.get("error").asText());
    assertNotNull(body.get("exceptionType"));
  }

  @Test
  public void maps_generic_exception_to_500() {
    StoreResource.ErrorMapper mapper = new StoreResource.ErrorMapper();
    mapper.objectMapper = new ObjectMapper();

    Response response = mapper.toResponse(new RuntimeException("boom"));

    assertEquals(500, response.getStatus());
    ObjectNode body = (ObjectNode) response.getEntity();
    assertEquals(500, body.get("code").asInt());
    assertEquals("boom", body.get("error").asText());
  }

  @Test
  public void maps_exception_with_null_message_without_error_field() {
    StoreResource.ErrorMapper mapper = new StoreResource.ErrorMapper();
    mapper.objectMapper = new ObjectMapper();

    Response response = mapper.toResponse(new NullPointerException());

    assertEquals(500, response.getStatus());
    ObjectNode body = (ObjectNode) response.getEntity();
    assertEquals(500, body.get("code").asInt());
    assertNotNull(body.get("exceptionType"));
  }
}
