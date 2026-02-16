package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

public class StoreErrorMapperCoverageTest {

  @Test
  public void maps_web_application_exception_status_and_message() {
    StoreResource.ErrorMapper mapper = new StoreResource.ErrorMapper();
    mapper.objectMapper = new ObjectMapper();

    Response response = mapper.toResponse(new WebApplicationException("bad request", 400));
    ObjectNode json = (ObjectNode) response.getEntity();

    assertEquals(400, response.getStatus());
    assertEquals(400, json.get("code").asInt());
    assertEquals("bad request", json.get("error").asText());
    assertTrue(json.has("exceptionType"));
  }

  @Test
  public void maps_runtime_exception_without_error_field_when_message_missing() {
    StoreResource.ErrorMapper mapper = new StoreResource.ErrorMapper();
    mapper.objectMapper = new ObjectMapper();

    Response response = mapper.toResponse(new RuntimeException());
    ObjectNode json = (ObjectNode) response.getEntity();

    assertEquals(500, response.getStatus());
    assertEquals(500, json.get("code").asInt());
    assertFalse(json.has("error"));
  }
}
