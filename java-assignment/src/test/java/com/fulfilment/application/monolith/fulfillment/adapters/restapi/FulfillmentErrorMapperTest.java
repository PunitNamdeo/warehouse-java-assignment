package com.fulfilment.application.monolith.fulfillment.adapters.restapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

public class FulfillmentErrorMapperTest {

  @Test
  public void maps_web_application_exception_status_and_message() {
    FulfillmentResource.ErrorMapper mapper = new FulfillmentResource.ErrorMapper();
    mapper.objectMapper = new ObjectMapper();

    Response response = mapper.toResponse(new WebApplicationException("conflict", 409));

    assertEquals(409, response.getStatus());
    ObjectNode body = (ObjectNode) response.getEntity();
    assertEquals(409, body.get("code").asInt());
    assertEquals("conflict", body.get("error").asText());
    assertNotNull(body.get("exceptionType"));
  }

  @Test
  public void maps_generic_exception_to_500() {
    FulfillmentResource.ErrorMapper mapper = new FulfillmentResource.ErrorMapper();
    mapper.objectMapper = new ObjectMapper();

    Response response = mapper.toResponse(new RuntimeException("db error"));

    assertEquals(500, response.getStatus());
    ObjectNode body = (ObjectNode) response.getEntity();
    assertEquals(500, body.get("code").asInt());
    assertEquals("db error", body.get("error").asText());
  }

  @Test
  public void maps_exception_without_message() {
    FulfillmentResource.ErrorMapper mapper = new FulfillmentResource.ErrorMapper();
    mapper.objectMapper = new ObjectMapper();

    Response response = mapper.toResponse(new IllegalStateException());

    assertEquals(500, response.getStatus());
    ObjectNode body = (ObjectNode) response.getEntity();
    assertEquals(500, body.get("code").asInt());
    assertNotNull(body.get("exceptionType"));
  }
}
