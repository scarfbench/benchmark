package io.quarkuscoffeeshop.coffeeshop.barista.domain;

import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class BaristaRepository implements PanacheRepository<BaristaItem> {

    @Inject
    @DataSource("barista")
    AgroalDataSource baristaDatasource;

}
