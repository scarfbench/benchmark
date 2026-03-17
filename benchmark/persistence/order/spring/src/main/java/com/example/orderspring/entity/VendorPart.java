package com.example.orderspring.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(
    name = "PERSISTENCE_ORDER_VENDOR_PART",
    uniqueConstraints = @UniqueConstraint(columnNames = {"PARTNUMBER", "PARTREVISION"})
)
@NamedQueries({
    @NamedQuery(name = "findAverageVendorPartPrice",
        query = "SELECT AVG(vp.price) FROM VendorPart vp"),
    @NamedQuery(name = "findTotalVendorPartPricePerVendor",
        query = "SELECT SUM(vp.price) FROM VendorPart vp WHERE vp.vendor.vendorId = :id"),
    @NamedQuery(name = "findAllVendorParts",
        query = "SELECT vp FROM VendorPart vp ORDER BY vp.vendorPartNumber")
})
public class VendorPart implements Serializable {
    private static final long serialVersionUID = 4685631589912848921L;

    private Long vendorPartNumber;
    private String description;
    private double price;
    private Part part;
    private Vendor vendor;

    public VendorPart() {}

    public VendorPart(String description, double price, Part part) {
        this.description = description;
        this.price = price;
        setPart(part);
    }

    @TableGenerator(
        name = "vendorPartGen",
        table = "PERSISTENCE_ORDER_SEQUENCE_GENERATOR",
        pkColumnName = "GEN_KEY",
        valueColumnName = "GEN_VALUE",
        pkColumnValue = "VENDOR_PART_ID",
        allocationSize = 10
    )
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "vendorPartGen")
    public Long getVendorPartNumber() { return vendorPartNumber; }
    public void setVendorPartNumber(Long vendorPartNumber) { this.vendorPartNumber = vendorPartNumber; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "PARTNUMBER", referencedColumnName = "PARTNUMBER"),
        @JoinColumn(name = "PARTREVISION", referencedColumnName = "REVISION")
    })
    public Part getPart() { return part; }
    public void setPart(Part part) {
        this.part = part;
        if (part != null && part.getVendorPart() != this) {
            part.setVendorPart(this);
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VENDORID")
    public Vendor getVendor() { return vendor; }
    public void setVendor(Vendor vendor) { this.vendor = vendor; }
}
