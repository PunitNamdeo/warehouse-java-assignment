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
    @DisplayName("Should resolve ROTTERDAM-001 location")
    public void testResolveRotterdamLocation() {
        Location location = locationGateway.resolveByIdentifier("ROTTERDAM-001");
        
        assertThat(location)
            .isNotNull()
            .hasFieldOrPropertyWithValue("identification", "ROTTERDAM-001");
    }

    @Test
    @DisplayName("Should resolve EINDHOVEN-003 location")
    public void testResolveEindhovenLocation() {
        Location location = locationGateway.resolveByIdentifier("EINDHOVEN-003");
        
        assertThat(location)
            .isNotNull()
            .hasFieldOrPropertyWithValue("identification", "EINDHOVEN-003");
    }

    @Test
    @DisplayName("Should resolve GRONINGEN-001 location")
    public void testResolveGroningenLocation() {
        Location location = locationGateway.resolveByIdentifier("GRONINGEN-001");
        
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
        String[] validCodes = {"AMSTERDAM-001", "ROTTERDAM-001", "EINDHOVEN-003"};
        
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
        
        // Depending on implementation, may return null or trim
        // This tests the actual behavior
        assertThat(location).isNull(); // Assuming no trimming
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
    @DisplayName("Should handle numeric location codes")
    public void testNumericLocationCodes() {
        Location location1 = locationGateway.resolveByIdentifier("AMSTERDAM-001");
        Location location2 = locationGateway.resolveByIdentifier("EINDHOVEN-003");
        
        // Different numeric suffixes should resolve to different locations
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
