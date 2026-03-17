package org.eclipse.cargotracker.application;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** Loads sample data for demo. */
@ApplicationScoped
@Startup
public class BookingServiceTestDataGenerator {

  @Inject
  private TestDataGenerator generator;

  @PostConstruct
  public void loadSampleData() {
    // can't have @Transactional for calls inside the same class
    generator.loadData();
  }

}
