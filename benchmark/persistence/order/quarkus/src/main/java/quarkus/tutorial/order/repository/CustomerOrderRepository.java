package quarkus.tutorial.order.repository;

import quarkus.tutorial.order.entity.CustomerOrder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.logging.Logger;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CustomerOrderRepository {
    private static final Logger logger = Logger.getLogger(CustomerOrderRepository.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void createOrder(Integer orderId, char status, int discount, String shipmentInfo) {
        try {
            CustomerOrder order = new CustomerOrder(orderId, status, discount, shipmentInfo);
            em.persist(order);
            logger.info("Created order ID " + orderId);
        } catch (Exception e) {
            logger.severe("Failed to create order ID " + orderId + ": " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public List<CustomerOrder> getOrders() {
        try {
            return em.createNamedQuery("findAllOrders", CustomerOrder.class).getResultList();
        } catch (Exception e) {
            logger.severe("Failed to get orders: " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Transactional
    public void removeOrder(Integer orderId) {
        try {
            CustomerOrder order = em.find(CustomerOrder.class, orderId);
            if (order != null) {
                em.remove(order);
                logger.info("Removed order ID " + orderId);
            }
        } catch (Exception e) {
            logger.severe("Failed to remove order ID " + orderId + ": " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public double getOrderPrice(Integer orderId) {
        try {
            CustomerOrder order = em.find(CustomerOrder.class, orderId);
            if (order == null) {
                throw new RuntimeException("Order ID " + orderId + " not found");
            }
            return order.calculateAmmount();
        } catch (Exception e) {
            logger.severe("Failed to get price for order ID " + orderId + ": " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Transactional
    public void adjustOrderDiscount(int adjustment) {
        try {
            List<CustomerOrder> orders = em.createNamedQuery("findAllOrders", CustomerOrder.class).getResultList();
            for (CustomerOrder order : orders) {
                int newDiscount = order.getDiscount() + adjustment;
                order.setDiscount(Math.max(newDiscount, 0));
                em.merge(order);
            }
            logger.info("Adjusted discount by " + adjustment);
        } catch (Exception e) {
            logger.severe("Failed to adjust discounts: " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}