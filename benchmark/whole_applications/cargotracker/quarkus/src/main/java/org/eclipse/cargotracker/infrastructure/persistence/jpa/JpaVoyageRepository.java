package org.eclipse.cargotracker.infrastructure.persistence.jpa;

import java.io.Serializable;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.eclipse.cargotracker.domain.model.voyage.VoyageRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;

@ApplicationScoped
public class JpaVoyageRepository
    implements PanacheRepositoryBase<Voyage, Long>, VoyageRepository, Serializable {

  private static final long serialVersionUID = 1L;

  @Override
  public Voyage find(VoyageNumber voyageNumber) {
    return find("#Voyage.findByVoyageNumber", Parameters.with("voyageNumber", voyageNumber))
        .singleResult();
  }

}
