package quarkus.tutorial.order.repository;

import quarkus.tutorial.order.entity.Vendor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class VendorRepository {
    private static final Logger logger = Logger.getLogger(VendorRepository.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void createVendor(int vendorId, String name, String address, String contact, String phone) {
        try {
            Vendor vendor = new Vendor(vendorId, name, address, contact, phone);
            em.persist(vendor);
            logger.info("Created vendor ID " + vendorId);
        } catch (Exception e) {
            logger.severe("Failed to create vendor ID " + vendorId + ": " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public List<String> locateVendorsByPartialName(String name) {
        try {
            List<Vendor> vendors = em.createNamedQuery("findVendorsByPartialName", Vendor.class)
                                     .setParameter("name", name)
                                     .getResultList();
            List<String> names = new ArrayList<>();
            for (Vendor vendor : vendors) {
                names.add(vendor.getName());
            }
            return names;
        } catch (Exception e) {
            logger.severe("Failed to locate vendors by name " + name + ": " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public String reportVendorsByOrder(Integer orderId) {
        try {
            List<Vendor> vendors = em.createNamedQuery("findVendorByCustomerOrder", Vendor.class)
                                     .setParameter("id", orderId)
                                     .getResultList();
            StringBuilder report = new StringBuilder();
            for (Vendor vendor : vendors) {
                report.append(vendor.getVendorId()).append(' ')
                      .append(vendor.getName()).append(' ')
                      .append(vendor.getContact()).append('\n');
            }
            return report.toString();
        } catch (Exception e) {
            logger.severe("Failed to report vendors for order ID " + orderId + ": " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}