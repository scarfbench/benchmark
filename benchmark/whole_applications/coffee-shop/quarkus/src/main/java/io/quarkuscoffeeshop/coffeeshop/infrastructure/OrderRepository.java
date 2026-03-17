package io.quarkuscoffeeshop.coffeeshop.infrastructure;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkuscoffeeshop.coffeeshop.domain.Order;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OrderRepository implements PanacheRepository<Order> {

    public Order findById(final String id) {
        return Order.findById(id);
    }
}
