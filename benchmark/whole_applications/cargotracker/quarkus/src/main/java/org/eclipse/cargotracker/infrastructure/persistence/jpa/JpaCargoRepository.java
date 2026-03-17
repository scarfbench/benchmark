package org.eclipse.cargotracker.infrastructure.persistence.jpa;

import java.io.Serializable;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.CargoRepository;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.infrastructure.events.cdi.CargoUpdated;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;

@ApplicationScoped
public class JpaCargoRepository
    implements PanacheRepositoryBase<Cargo, Long>, CargoRepository, Serializable {

  private static final long serialVersionUID = 1L;

  @Inject
  private Logger logger;

  @Inject
  @CargoUpdated
  private Event<Cargo> cargoUpdated;

  @Override
  public Cargo find(TrackingId trackingId) {
    Cargo cargo;

    try {
      cargo =
          find("#Cargo.findByTrackingId", Parameters.with("trackingId", trackingId)).singleResult();
    } catch (NoResultException e) {
      logger.log(Level.FINE, "Find called on non-existant tracking ID.", e);
      cargo = null;
    }

    return cargo;
  }

  @Override
  public void store(Cargo cargo) {
    // TODO [Clean Code] See why cascade is not working correctly for legs.
    cargo.getItinerary().getLegs().forEach(leg -> getEntityManager().persist(leg));

    persist(cargo);

    cargoUpdated.fireAsync(cargo);
  }

  @Override
  public TrackingId nextTrackingId() {
    String random = UUID.randomUUID().toString().toUpperCase();

    return new TrackingId(random.substring(0, random.indexOf("-")));
  }
}
