package org.eclipse.cargotracker.application.util;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.arc.profile.UnlessBuildProfile;
import io.quarkus.runtime.Startup;

/** Loads sample data for demo. */
@ApplicationScoped
@Startup
@UnlessBuildProfile("test") // disable for tests
public class SampleDataGenerator {

  @Inject
  private InitLoader loader;

  @PostConstruct
  public void loadSampleData() {
    loader.loadData();
  }

}
