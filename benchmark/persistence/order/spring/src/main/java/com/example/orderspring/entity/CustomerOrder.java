package com.example.orderspring.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@Entity
@Table(name="PERSISTENCE_ORDER_CUSTOMERORDER")
@NamedQuery(
    name="findAllOrders",
    query="SELECT co FROM CustomerOrder co " +
          "ORDER BY co.orderId"
)
public class CustomerOrder implements Serializable {
    private static final long serialVersionUID = 6582105865012174694L;

    private Integer orderId;
    private char status;
    private Date lastUpdate;
    private int discount;
    private String shipmentInfo;
    private Collection<LineItem> lineItems = new ArrayList<>();

    public CustomerOrder() {
        this.lastUpdate = new Date();
    }

    public CustomerOrder(Integer orderId, char status, int discount, 
            String shipmentInfo) {
        this.orderId = orderId;
        this.status = status;
        this.discount = discount;
        this.shipmentInfo = shipmentInfo;
        this.lastUpdate = new Date();
    }

    @Id
    public Integer getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public char getStatus() {
        return status;
    }
    
    public void setStatus(char status) {
        this.status = status;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getLastUpdate() {
        return lastUpdate;
    }
    
    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public int getDiscount() {
        return discount;
    }
    
    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public String getShipmentInfo() {
        return shipmentInfo;
    }
    
    public void setShipmentInfo(String shipmentInfo) {
        this.shipmentInfo = shipmentInfo;
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "customerOrder", orphanRemoval = true)
    public Collection<LineItem> getLineItems() {
        return lineItems;
    }
    
    public void setLineItems(Collection<LineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public double calculateAmmount() {
        double amount = 0;
        for (LineItem item : getLineItems()) {
            if (item.getVendorPart() != null) {
                amount += item.getVendorPart().getPrice() * item.getQuantity();
            }
        }
        return (amount * (100 - getDiscount())) / 100.0;
    }

    public double calculateAmount() {
        return calculateAmmount();
    }

    public void addLineItem(LineItem lineItem) {
        lineItem.setCustomerOrder(this);
        this.getLineItems().add(lineItem);
    }

    @Transient
    public int getNextId() {
        return this.lineItems.size() + 1;
    }

    @PrePersist
    @PreUpdate
    private void touchLastUpdate() {
        this.lastUpdate = new Date();
    }
}
