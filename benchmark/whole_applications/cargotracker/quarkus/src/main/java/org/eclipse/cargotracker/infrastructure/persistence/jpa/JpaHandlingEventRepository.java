package org.eclipse.cargotracker.infrastructure.persistence.jpa;

import java.io.Serializable;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.handling.HandlingEventRepository;
import org.eclipse.cargotracker.domain.model.handling.HandlingHistory;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;

@ApplicationScoped
public class JpaHandlingEventRepository
    implements PanacheRepositoryBase<HandlingEvent, Long>, HandlingEventRepository, Serializable {

  private static final long serialVersionUID = 1L;

  @Override
  public void store(HandlingEvent event) {
    persist(event);
  }

  @Override
  public HandlingHistory lookupHandlingHistoryOfCargo(TrackingId trackingId) {
    return new HandlingHistory(
        find("#HandlingEvent.findByTrackingId", Parameters.with("trackingId", trackingId)).list());
  }
}
