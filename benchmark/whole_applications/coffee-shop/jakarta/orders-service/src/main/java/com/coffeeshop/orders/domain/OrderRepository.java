package com.coffeeshop.orders.domain;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class OrderRepository {

  @PersistenceContext(unitName = "ordersPU")
  EntityManager em;

  @Transactional
  public Long save(OrderEntity e){
    em.persist(e);
    return e.getId();
  }

  public OrderEntity find(long id){
    return em.find(OrderEntity.class, id);
  }
}
