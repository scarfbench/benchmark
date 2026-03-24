package com.coffeeshop.orders.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

import com.coffeeshop.common.domain.OrderStatus;

@Entity
@Table(name = "orders")
public class OrderEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String customer;

  @Column(nullable = false)
  private String item;

  @Column(nullable = false)
  private int quantity;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status = OrderStatus.PLACED;

  @Column(name = "created", nullable = false)
  private OffsetDateTime created;

  @Column(name = "updated", nullable = false)
  private OffsetDateTime updated;

  @PrePersist
  void prePersist() {
    OffsetDateTime now = OffsetDateTime.now();
    this.created = now;
    this.updated = now;
  }

  @PreUpdate
  void preUpdate() {
    this.updated = OffsetDateTime.now();
  }

  // getters & setters
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getCustomer() { return customer; }
  public void setCustomer(String customer) { this.customer = customer; }

  public String getItem() { return item; }
  public void setItem(String item) { this.item = item; }

  public int getQuantity() { return quantity; }
  public void setQuantity(int quantity) { this.quantity = quantity; }

  public OrderStatus getStatus() { return status; }
  public void setStatus(OrderStatus status) { this.status = status; }

  public OffsetDateTime getCreated() { return created; }
  public void setCreated(OffsetDateTime created) { this.created = created; }

  public OffsetDateTime getUpdated() { return updated; }
  public void setUpdated(OffsetDateTime updated) { this.updated = updated; }
}
