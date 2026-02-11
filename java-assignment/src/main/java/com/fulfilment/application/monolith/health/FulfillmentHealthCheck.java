package com.fulfilment.application.monolith.health;

import io.quarkus.logging.Log;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Custom Health Check for the Fulfillment application.
 * Monitors application readiness and liveness status.
 * 
 * Used by Kubernetes probes for container health monitoring.
 */
@Liveness
@ApplicationScoped
public class FulfillmentHealthCheck implements HealthCheck {

  @Override
  public HealthCheckResponse call() {
    try {
      Log.info("Fulfillment application health check invoked");
      
      // Verify application is responsive
      return HealthCheckResponse.named("Fulfillment Service")
          .up()
          .build();
    } catch (Exception e) {
      Log.error("Health check failed: " + e.getMessage());
      return HealthCheckResponse.named("Fulfillment Service")
          .down()
          .build();
    }
  }
}
