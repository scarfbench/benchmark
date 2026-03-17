package org.eclipse.cargotracker.domain.model.voyage;

public interface VoyageRepository {

  Voyage find(VoyageNumber voyageNumber);

}
