package quarkus.tutorial.order.repository;

import quarkus.tutorial.order.entity.CustomerOrder;
import quarkus.tutorial.order.entity.LineItem;
import quarkus.tutorial.order.entity.Part;
import quarkus.tutorial.order.entity.PartKey;
import quarkus.tutorial.order.entity.VendorPart;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.logging.Logger;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class LineItemRepository {
    private static final Logger logger = Logger.getLogger(LineItemRepository.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void addLineItem(Integer orderId, String partNumber, int revision, int quantity) {
        try {
            CustomerOrder order = em.find(CustomerOrder.class, orderId);
            if (order == null) {
                throw new RuntimeException("Order ID " + orderId + " not found");
            }
            PartKey pkey = new PartKey();
            pkey.setPartNumber(partNumber);
            pkey.setRevision(revision);
            Part part = em.find(Part.class, pkey);
            if (part == null) {
                throw new RuntimeException("Part " + partNumber + "-" + revision + " not found");
            }
            VendorPart vendorPart = part.getVendorPart();
            LineItem lineItem = new LineItem(order, quantity, vendorPart);
            order.addLineItem(lineItem);
            em.persist(lineItem);
            logger.info("Added line item for order ID " + orderId + ", part " + partNumber);
        } catch (Exception e) {
            logger.severe("Failed to add line item for order ID " + orderId + ": " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public List<LineItem> getLineItems(int orderId) {
        try {
            return em.createNamedQuery("findLineItemsByOrderId", LineItem.class)
                     .setParameter("orderId", orderId)
                     .getResultList();
        } catch (Exception e) {
            logger.severe("Failed to get line items for order ID " + orderId + ": " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public int countAllItems() {
        try {
            return em.createNamedQuery("findAllLineItems", LineItem.class)
                     .getResultList()
                     .size();
        } catch (Exception e) {
            logger.severe("Failed to count line items: " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}