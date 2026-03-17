package com.example.orderspring.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name="PERSISTENCE_ORDER_VENDOR")
@NamedQueries({
  @NamedQuery(name="findVendorsByPartialName",
              query="SELECT v FROM Vendor v WHERE LOCATE(:name, v.name) > 0"),
  @NamedQuery(name="findVendorByCustomerOrder",
              query="SELECT DISTINCT l.vendorPart.vendor FROM CustomerOrder co, IN(co.lineItems) l WHERE co.orderId = :id ORDER BY l.vendorPart.vendor.name")
})
public class Vendor implements Serializable {
    private static final long serialVersionUID = 2538635007598426330L;

    private int vendorId;
    private String name;
    private String address;
    private String contact;
    private String phone;
    private Collection<VendorPart> vendorParts = new ArrayList<>();

    public Vendor() {}
    public Vendor(int vendorId, String name, String address, String contact, String phone) {
        this.vendorId = vendorId; this.name = name; this.address = address;
        this.contact = contact; this.phone = phone;
    }

    @Id
    public int getVendorId() { return vendorId; }
    public void setVendorId(int vendorId) { this.vendorId = vendorId; }

    @Column(name="VENDORNAME")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    @OneToMany(cascade=CascadeType.ALL, mappedBy="vendor", orphanRemoval = true)
    public Collection<VendorPart> getVendorParts() { return vendorParts; }
    public void setVendorParts(Collection<VendorPart> vendorParts) { this.vendorParts = vendorParts; }

    public void addVendorPart(VendorPart vendorPart) {
        vendorPart.setVendor(this);        // keep both sides in sync
        getVendorParts().add(vendorPart);
    }
}
