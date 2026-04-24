package com.ibm.webapi.apis;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class HealthEndpoint implements HealthCheck {

	@Override
	public HealthCheckResponse call() {
		return HealthCheckResponse.named("web-api").status(true).build();
	}
}
