package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * Unit tests for LocationGateway.
 * Tests cover positive scenarios, negative scenarios, and boundary conditions.
 * 
 * Case Study Context: Location serves as reference data that ensures geographical
 * consistency in the fulfillment network. Each location has capacity constraints
 * that impact cost allocation (shared warehouse costs distributed across multiple
 * stores in the same location).
 */
@DisplayName("LocationGateway Tests")
public class LocationGatewayTest {

  private LocationGateway locationGateway = new LocationGateway();

  // ============= POSITIVE TEST SCENARIOS =============

  @Test
  @DisplayName("Should resolve ZWOLLE-001 location successfully")
  public void testWhenResolveExistingLocationShouldReturn() {
    // given
    LocationGateway locationGateway = new LocationGateway();

    // when
    Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

    // then
    assertThat(location).isNotNull();
    assertEquals(location.identification, "ZWOLLE-001");
    assertEquals(location.maxNumberOfWarehouses, 1);
    assertEquals(location.maxCapacity, 40);
  }

  @Test
  @DisplayName("Should resolve AMSTERDAM-001 location successfully")
  public void testResolveAmsterdamLocation() {
    // given-when
    Location location = locationGateway.resolveByIdentifier("AMSTERDAM-001");

    // then
    assertThat(location)
        .isNotNull()
        .hasFieldOrPropertyWithValue("identification", "AMSTERDAM-001");
    assertThat(location.maxNumberOfWarehouses).isGreaterThan(0);
    assertThat(location.maxCapacity).isGreaterThan(0);
  }

  @Test
  @DisplayName("Should resolve TILBURG-001 location successfully")
  public void testResolveRotterdamLocation() {
    // when
    Location location = locationGateway.resolveByIdentifier("TILBURG-001");

    // then
    assertThat(location).isNotNull();
    assertThat(location.identification).isEqualTo("TILBURG-001");
  }

  @Test
  @DisplayName("Should resolve EINDHOVEN-001 location")
  public void testResolveNewYorkLocation() {
    // when
    Location location = locationGateway.resolveByIdentifier("EINDHOVEN-001");

    // then
    assertThat(location)
        .isNotNull()
        .extracting("identification")
        .isEqualTo("EINDHOVEN-001");
  }

  @Test
  @DisplayName("Should resolve VETSBY-001 location")
  public void testResolveLosAngelesLocation() {
    // when
    Location location = locationGateway.resolveByIdentifier("VETSBY-001");

    // then
    assertThat(location).isNotNull();
    assertThat(location.identification).matches("[A-Z0-9-]+");
  }

  // ============= NEGATIVE TEST SCENARIOS =============

  @Test
  @DisplayName("Should return null for non-existent location")
  public void testWhenResolveNonExistentLocationShouldReturnNull() {
    // when
    Location location = locationGateway.resolveByIdentifier("INVALID-999");

    // then
    assertThat(location).isNull();
  }

  @Test
  @DisplayName("Should return null for empty identifier")
  public void testWhenResolveEmptyIdentifierShouldReturnNull() {
    // when
    Location location = locationGateway.resolveByIdentifier("");

    // then
    assertThat(location).isNull();
  }

  @Test
  @DisplayName("Should return null for null identifier")
  public void testWhenResolveNullIdentifierShouldReturnNull() {
    // when
    Location location = locationGateway.resolveByIdentifier(null);

    // then
    assertThat(location).isNull();
  }

  @Test
  @DisplayName("Should handle case-sensitive identifier lookup")
  public void testWhenResolveLowercaseIdentifierShouldReturnNull() {
    // when - location code is uppercase
    Location location = locationGateway.resolveByIdentifier("zwolle-001");

    // then - should not match (case-sensitive)
    assertThat(location).isNull();
  }

  @Test
  @DisplayName("Should not match partial identifiers")
  public void testPartialIdentifierDoesNotMatch() {
    // when
    Location location = locationGateway.resolveByIdentifier("ZWOLLE");

    // then
    assertThat(location).isNull();
  }

  // ============= BOUNDARY CONDITION TESTS =============

  @Test
  @DisplayName("Should handle whitespace in identifier")
  public void testIdentifierWithWhitespace() {
    // when
    Location location = locationGateway.resolveByIdentifier(" ZWOLLE-001 ");

    // then - should not match due to whitespace
    assertThat(location).isNull();
  }

  @Test
  @DisplayName("All predefined locations should have valid capacity")
  public void testAllLocationsHaveValidCapacity() {
    // given valid location codes
    String[] validCodes = {"AMSTERDAM-001", "TILBURG-001", "ZWOLLE-001", 
                          "EINDHOVEN-001", "VETSBY-001"};

    // when-then
    for (String code : validCodes) {
      Location location = locationGateway.resolveByIdentifier(code);
      assertThat(location)
          .isNotNull()
          .withFailMessage("Location %s should have valid capacity", code)
          .satisfies(l -> assertThat(l.maxCapacity).isGreaterThan(0));
    }
  }

  @Test
  @DisplayName("Location identifier should follow expected format")
  public void testLocationIdentifierFormat() {
    // when
    Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

    // then
    assertThat(location.identification)
        .matches("[A-Z0-9-]+")
        .contains("-");
  }
}

