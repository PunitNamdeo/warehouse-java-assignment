package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * Additional integration tests for LocationGateway - Coverage focused
 * Tests various location scenarios and edge cases
 */
@DisplayName("LocationGateway - Extended Coverage Tests")
public class LocationGatewayCoverageTest {

    private LocationGateway locationGateway = new LocationGateway();

    @Test
    @DisplayName("Should resolve ZWOLLE-001 location")
    public void testResolveZwolleLocation() {
        Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");
        
        assertThat(location)
            .isNotNull()
            .hasFieldOrPropertyWithValue("identification", "ZWOLLE-001");
    }

    @Test
    @DisplayName("Should resolve EINDHOVEN-001 location")
    public void testResolveEindhovenLocation() {
        Location location = locationGateway.resolveByIdentifier("EINDHOVEN-001");
        
        assertThat(location)
            .isNotNull()
            .hasFieldOrPropertyWithValue("identification", "EINDHOVEN-001");
    }

    @Test
    @DisplayName("Should resolve AMSTERDAM-001 location")
    public void testResolveAmsterdamLocation() {
        Location location = locationGateway.resolveByIdentifier("AMSTERDAM-001");
        
        assertThat(location)
            .isNotNull();
    }

    @Test
    @DisplayName("Should return null for non-existent location")
    public void testResolveNonExistentLocation() {
        Location location = locationGateway.resolveByIdentifier("FAKE-LOCATION-999");
        
        assertThat(location).isNull();
    }

    @Test
    @DisplayName("Should handle null identifier")
    public void testResolveNullIdentifier() {
        Location location = locationGateway.resolveByIdentifier(null);
        
        assertThat(location).isNull();
    }

    @Test
    @DisplayName("Should handle empty string identifier")
    public void testResolveEmptyIdentifier() {
        Location location = locationGateway.resolveByIdentifier("");
        
        assertThat(location).isNull();
    }

    @Test
    @DisplayName("Should be case sensitive for location codes")
    public void testLocationCodeCaseSensitive() {
        Location uppercase = locationGateway.resolveByIdentifier("AMSTERDAM-001");
        Location lowercase = locationGateway.resolveByIdentifier("amsterdam-001");
        
        assertThat(uppercase).isNotNull();
        assertThat(lowercase).isNull();
    }

    @Test
    @DisplayName("Should have capacity constraints for locations")
    public void testLocationHasCapacityConstraints() {
        Location location = locationGateway.resolveByIdentifier("AMSTERDAM-001");
        
        assertThat(location)
            .isNotNull()
            .hasFieldOrProperty("maxCapacity")
            .hasFieldOrProperty("maxNumberOfWarehouses");
        
        assertThat(location.maxCapacity).isGreaterThan(0);
        assertThat(location.maxNumberOfWarehouses).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should validate warehouse count constraint")
    public void testWarehouseCountConstraint() {
        Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");
        
        assertThat(location)
            .isNotNull();
        
        // ZWOLLE-001 has maxNumberOfWarehouses = 1
        assertThat(location.maxNumberOfWarehouses).isEqualTo(1);
    }

    @Test
    @DisplayName("Should support multiple location queries")
    public void testMultipleLocationQueries() {
        String[] validCodes = {"AMSTERDAM-001", "ZWOLLE-001", "EINDHOVEN-001"};
        
        for (String code : validCodes) {
            Location location = locationGateway.resolveByIdentifier(code);
            assertThat(location)
                .isNotNull()
                .hasFieldOrPropertyWithValue("identification", code);
        }
    }

    @Test
    @DisplayName("Should handle whitespace in identifier")
    public void testWhitespaceInIdentifier() {
        Location location = locationGateway.resolveByIdentifier(" AMSTERDAM-001 ");
        
        // Whitespace in identifier won't match exact string
        assertThat(location).isNull();
    }

    @Test
    @DisplayName("Should identify location with proper attributes")
    public void testLocationAttributes() {
        Location location = locationGateway.resolveByIdentifier("AMSTERDAM-001");
        
        assertThat(location)
            .isNotNull()
            .hasNoNullFieldsOrProperties();
    }

    @Test
    @DisplayName("Should handle multiple warehouse locations")
    public void testMultipleWarehouseLocations() {
        Location location1 = locationGateway.resolveByIdentifier("AMSTERDAM-001");
        Location location2 = locationGateway.resolveByIdentifier("ZWOLLE-002");
        
        // Multiple warehouse locations should resolve correctly
        assertThat(location1).isNotNull();
        assertThat(location2).isNotNull();
        assertThat(location1.identification).isNotEqualTo(location2.identification);
    }

    @Test
    @DisplayName("Should return consistent results for same identifier")
    public void testLocationResolutionConsistency() {
        Location location1 = locationGateway.resolveByIdentifier("AMSTERDAM-001");
        Location location2 = locationGateway.resolveByIdentifier("AMSTERDAM-001");
        
        assertThat(location1)
            .isNotNull()
            .isEqualTo(location2); // Same location should be consistent
    }

    @Test
    @DisplayName("Should verify location identification format")
    public void testLocationIdentificationFormat() {
        Location location = locationGateway.resolveByIdentifier("AMSTERDAM-001");
        
        assertThat(location.identification)
            .contains("-")
            .matches("[A-Z]+-\\d{3}");
    }
}
