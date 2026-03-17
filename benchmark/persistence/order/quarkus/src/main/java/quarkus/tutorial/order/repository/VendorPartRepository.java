package quarkus.tutorial.order.repository;

import quarkus.tutorial.order.entity.Part;
import quarkus.tutorial.order.entity.PartKey;
import quarkus.tutorial.order.entity.Vendor;
import quarkus.tutorial.order.entity.VendorPart;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.logging.Logger;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class VendorPartRepository {
    private static final Logger logger = Logger.getLogger(VendorPartRepository.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void createVendorPart(String partNumber, int revision, String description, double price, int vendorId) {
        try {
            PartKey pkey = new PartKey();
            pkey.setPartNumber(partNumber);
            pkey.setRevision(revision);
            Part part = em.find(Part.class, pkey);
            if (part == null) {
                throw new RuntimeException("Part " + partNumber + "-" + revision + " not found");
            }
            Vendor vendor = em.find(Vendor.class, vendorId);
            if (vendor == null) {
                throw new RuntimeException("Vendor ID " + vendorId + " not found");
            }
            VendorPart vendorPart = new VendorPart(description, price, part);
            vendorPart.setVendor(vendor);
            vendor.addVendorPart(vendorPart);
            em.persist(vendorPart);
            logger.info("Created vendor part for part " + partNumber + "-" + revision);
        } catch (Exception e) {
            logger.severe("Failed to create vendor part: " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Double getAvgPrice() {
        try {
            return em.createNamedQuery("findAverageVendorPartPrice", Double.class).getSingleResult();
        } catch (Exception e) {
            logger.severe("Failed to get average vendor part price: " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Double getTotalPricePerVendor(int vendorId) {
        try {
            return em.createNamedQuery("findTotalVendorPartPricePerVendor", Double.class)
                     .setParameter("id", vendorId)
                     .getSingleResult();
        } catch (Exception e) {
            logger.severe("Failed to get total price for vendor ID " + vendorId + ": " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public List<VendorPart> findAllVendorParts() {
        try {
            return em.createNamedQuery("findAllVendorParts", VendorPart.class).getResultList();
        } catch (Exception e) {
            logger.severe("Failed to get all vendor parts: " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}