package com.fulfilment.application.monolith.health;

import io.quarkus.logging.Log;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Custom Health Check for the Fulfillment application.
 * Monitors application readiness and liveness status.
 */
@Liveness
@ApplicationScoped
public class FulfillmentHealthCheck implements HealthCheck {

  @Override
  public HealthCheckResponse call() {
    Log.infof("Fulfillment application health check invoked");
    
    try {
      // Verify application is responsive
      return HealthCheckResponse.up("Fulfillment Service")
          .withData("status", "OPERATIONAL")
          .withData("timestamp", System.currentTimeMillis())
          .build();
    } catch (Exception e) {
      Log.errorf("Health check failed: %s", e.getMessage());
      return HealthCheckResponse.down("Fulfillment Service")
          .withData("error", e.getMessage())
          .build();
    }
  }
}
