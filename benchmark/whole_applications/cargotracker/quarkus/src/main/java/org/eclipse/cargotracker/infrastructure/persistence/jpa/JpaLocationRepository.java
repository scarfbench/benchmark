package org.eclipse.cargotracker.infrastructure.persistence.jpa;

import java.io.Serializable;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.location.LocationRepository;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;

@ApplicationScoped
public class JpaLocationRepository
    implements PanacheRepositoryBase<Location, Long>, LocationRepository, Serializable {

  private static final long serialVersionUID = 1L;

  @Override
  public Location find(UnLocode unLocode) {
    return find("#Location.findByUnLocode", Parameters.with("unLocode", unLocode)).singleResult();
  }

}
