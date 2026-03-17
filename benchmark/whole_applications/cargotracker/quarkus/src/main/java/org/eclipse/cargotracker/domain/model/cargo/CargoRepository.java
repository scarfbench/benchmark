package org.eclipse.cargotracker.domain.model.cargo;

public interface CargoRepository {

  Cargo find(TrackingId trackingId);

  void store(Cargo cargo);

  TrackingId nextTrackingId();
}
