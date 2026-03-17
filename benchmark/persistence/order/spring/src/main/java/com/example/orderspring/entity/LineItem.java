package com.example.orderspring.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@IdClass(LineItemKey.class)
@Entity
@Table(name="PERSISTENCE_ORDER_LINEITEM")
@NamedQueries({
  @NamedQuery(name="findAllLineItems", query="SELECT l FROM LineItem l"),
  @NamedQuery(name="findLineItemsByOrderId",
              query="SELECT l FROM LineItem l WHERE l.customerOrder.orderId = :orderId ORDER BY l.itemId"),
  @NamedQuery(name="findLineItemById",
              query="SELECT DISTINCT l FROM LineItem l WHERE l.itemId = :itemId AND l.customerOrder.orderId = :orderId")
})
public class LineItem implements Serializable {
    private static final long serialVersionUID = 3229188813505619743L;

    private int itemId;
    private int quantity;
    private VendorPart vendorPart;
    private CustomerOrder customerOrder;

    public LineItem() { }

    public LineItem(CustomerOrder order, int quantity, VendorPart vendorPart) {
        this.customerOrder = order;
        this.itemId = order.getNextId();
        this.quantity = quantity;
        this.vendorPart = vendorPart;
    }

    @Id
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="VENDORPARTNUMBER")
    public VendorPart getVendorPart() { return vendorPart; }
    public void setVendorPart(VendorPart vendorPart) { this.vendorPart = vendorPart; }

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ORDERID")
    public CustomerOrder getCustomerOrder() { return customerOrder; }
    public void setCustomerOrder(CustomerOrder customerOrder) { this.customerOrder = customerOrder; }
}
