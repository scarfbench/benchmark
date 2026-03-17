package quarkus.tutorial.order.repository;

import quarkus.tutorial.order.entity.Part;
import quarkus.tutorial.order.entity.PartKey;
import quarkus.tutorial.order.entity.VendorPart;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PartRepository {
    private static final Logger logger = Logger.getLogger(PartRepository.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void createPart(String partNumber, int revision, String description,
                          Date revisionDate, String specification, Serializable drawing) {
        try {
            Part part = new Part(partNumber, revision, description, revisionDate, specification, drawing);
            em.persist(part);
            logger.info("Created part " + partNumber + "-" + revision);
        } catch (Exception e) {
            logger.severe("Failed to create part " + partNumber + "-" + revision + ": " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public List<Part> getAllParts() {
        try {
            return em.createNamedQuery("findAllParts", Part.class).getResultList();
        } catch (Exception e) {
            logger.severe("Failed to get all parts: " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Transactional
    public void addPartToBillOfMaterial(String bomPartNumber, int bomRevision, String partNumber, int revision) {
        try {
            PartKey bomKey = new PartKey();
            bomKey.setPartNumber(bomPartNumber);
            bomKey.setRevision(bomRevision);
            Part bom = em.find(Part.class, bomKey);
            if (bom == null) {
                throw new RuntimeException("BOM part " + bomPartNumber + "-" + bomRevision + " not found");
            }
            PartKey partKey = new PartKey();
            partKey.setPartNumber(partNumber);
            partKey.setRevision(revision);
            Part part = em.find(Part.class, partKey);
            if (part == null) {
                throw new RuntimeException("Part " + partNumber + "-" + revision + " not found");
            }
            bom.getParts().add(part);
            part.setBomPart(bom);
            em.merge(bom);
            logger.info("Added part " + partNumber + " to BOM " + bomPartNumber);
        } catch (Exception e) {
            logger.severe("Failed to add part to BOM: " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public double getBillOfMaterialPrice(String bomPartNumber, int bomRevision) {
        try {
            PartKey bomKey = new PartKey();
            bomKey.setPartNumber(bomPartNumber);
            bomKey.setRevision(bomRevision);
            Part bom = em.find(Part.class, bomKey);
            if (bom == null) {
                throw new RuntimeException("BOM part " + bomPartNumber + "-" + bomRevision + " not found");
            }
            double price = 0.0;
            for (Part part : bom.getParts()) {
                VendorPart vendorPart = part.getVendorPart();
                price += vendorPart.getPrice();
            }
            return price;
        } catch (Exception e) {
            logger.severe("Failed to get BOM price: " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}