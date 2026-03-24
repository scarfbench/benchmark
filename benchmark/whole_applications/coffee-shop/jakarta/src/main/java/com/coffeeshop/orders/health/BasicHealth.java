package com.coffeeshop.orders.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import jakarta.enterprise.context.ApplicationScoped;

@Readiness
@ApplicationScoped
public class BasicHealth implements HealthCheck {
    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.up("orders-service");
    }
}
