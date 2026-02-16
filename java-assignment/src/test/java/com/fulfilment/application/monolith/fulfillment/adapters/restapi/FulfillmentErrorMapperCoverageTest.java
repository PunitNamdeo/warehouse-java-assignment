package com.fulfilment.application.monolith.fulfillment.adapters.restapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

public class FulfillmentErrorMapperCoverageTest {

  @Test
  public void maps_web_application_exception() {
    FulfillmentResource.ErrorMapper mapper = new FulfillmentResource.ErrorMapper();
    mapper.objectMapper = new ObjectMapper();

    Response response = mapper.toResponse(new WebApplicationException("conflict", 409));
    ObjectNode json = (ObjectNode) response.getEntity();

    assertEquals(409, response.getStatus());
    assertEquals(409, json.get("code").asInt());
    assertEquals("conflict", json.get("error").asText());
    assertTrue(json.has("exceptionType"));
  }

  @Test
  public void maps_generic_exception_without_message() {
    FulfillmentResource.ErrorMapper mapper = new FulfillmentResource.ErrorMapper();
    mapper.objectMapper = new ObjectMapper();

    Response response = mapper.toResponse(new RuntimeException());
    ObjectNode json = (ObjectNode) response.getEntity();

    assertEquals(500, response.getStatus());
    assertEquals(500, json.get("code").asInt());
    assertFalse(json.has("error"));
  }
}
