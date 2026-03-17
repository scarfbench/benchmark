package com.example.orderspring.service;

import com.example.orderspring.entity.*;
import com.example.orderspring.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@Transactional
public class OrderService {
    
    @Autowired
    private PartRepository partRepository;
    
    @Autowired
    private CustomerOrderRepository customerOrderRepository;
    
    @Autowired
    private VendorRepository vendorRepository;
    
    @Autowired
    private VendorPartRepository vendorPartRepository;
    
    @Autowired
    private LineItemRepository lineItemRepository;
    
    private static final Logger logger = Logger.getLogger("order.service.OrderService");
    
    public void createPart(String partNumber, int revision, String description, 
                          java.util.Date revisionDate, String specification, Serializable drawing) {
        try {
            Part part = new Part(partNumber, revision, description, revisionDate, specification, drawing);
            logger.log(Level.INFO, "Created part {0}-{1}", new Object[]{partNumber, revision});
            partRepository.save(part);
            logger.log(Level.INFO, "Persisted part {0}-{1}", new Object[]{partNumber, revision});
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error creating part", ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    public List<Part> getAllParts() {
        return partRepository.findAllParts();
    }

    public void addPartToBillOfMaterial(String bomPartNumber, int bomRevision, 
                                       String partNumber, int revision) {
        logger.log(Level.INFO, "BOM part number: {0}", bomPartNumber);
        logger.log(Level.INFO, "BOM revision: {0}", bomRevision);
        logger.log(Level.INFO, "Part number: {0}", partNumber);
        logger.log(Level.INFO, "Part revision: {0}", revision);
        try {
            PartKey bomKey = new PartKey();
            bomKey.setPartNumber(bomPartNumber);
            bomKey.setRevision(bomRevision);
            
            Part bom = partRepository.findByPartNumberAndRevision(bomPartNumber, bomRevision).orElse(null);
            if (bom != null) {
                logger.log(Level.INFO, "BOM Part found: {0}", bom.getPartNumber());
                
                Part part = partRepository.findByPartNumberAndRevision(partNumber, revision).orElse(null);
                if (part != null) {
                    logger.log(Level.INFO, "Part found: {0}", part.getPartNumber());
                    bom.getParts().add(part);
                    part.setBomPart(bom);
                    partRepository.save(bom);
                    partRepository.save(part);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error adding part to BOM", e);
        }
    }
    
    public void createVendor(int vendorId, String name, String address, String contact, String phone) {
        try {
            Vendor vendor = new Vendor(vendorId, name, address, contact, phone);
            vendorRepository.save(vendor);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating vendor", e);
            throw new RuntimeException(e);
        }
    }
    
    public void createVendorPart(String partNumber, int revision, String description, 
                                double price, int vendorId) {
        try {
            Part part = partRepository.findByPartNumberAndRevision(partNumber, revision).orElse(null);
            if (part != null) {
                VendorPart vendorPart = new VendorPart(description, price, part);
                vendorPartRepository.save(vendorPart);
                
                Vendor vendor = vendorRepository.findById(vendorId).orElse(null);
                if (vendor != null) {
                    vendor.addVendorPart(vendorPart);
                    vendorPart.setVendor(vendor);
                    vendorRepository.save(vendor);
                    vendorPartRepository.save(vendorPart);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating vendor part", e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Transactional
    public void createOrder(Integer orderId, char status, int discount, String shipmentInfo) {
        if (orderId == null) throw new IllegalArgumentException("Order ID is required");
        if (customerOrderRepository.existsById(orderId)) {
            throw new IllegalStateException("Order ID " + orderId + " already exists");
        }
        customerOrderRepository.save(new CustomerOrder(orderId, status, discount, shipmentInfo));
    }

    public List<CustomerOrder> getOrders() {
        try {
            return customerOrderRepository.findAllOrders();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting orders", e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    public void addLineItem(Integer orderId, String partNumber, int revision, int quantity) {
        try {
            CustomerOrder order = customerOrderRepository.findById(orderId).orElse(null);
            if (order != null) {
                logger.log(Level.INFO, "Found order ID {0}", orderId);
                
                Part part = partRepository.findByPartNumberAndRevision(partNumber, revision).orElse(null);
                if (part != null && part.getVendorPart() != null) {
                    LineItem lineItem = new LineItem(order, quantity, part.getVendorPart());
                    order.addLineItem(lineItem);
                    lineItemRepository.save(lineItem);
                    customerOrderRepository.save(order);
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Couldn't add {0} to order ID {1}.", new Object[]{partNumber, orderId});
            throw new RuntimeException(e.getMessage());
        }
    }
    
    public double getBillOfMaterialPrice(String bomPartNumber, int bomRevision, 
                                       String partNumber, int revision) {
        double price = 0.0;
        try {
            Part bom = partRepository.findByPartNumberAndRevision(bomPartNumber, bomRevision).orElse(null);
            if (bom != null) {
                Collection<Part> parts = bom.getParts();
                for (Part part : parts) {
                    VendorPart vendorPart = part.getVendorPart();
                    if (vendorPart != null) {
                        price += vendorPart.getPrice();
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error calculating BOM price", e);
            throw new RuntimeException(e.getMessage());
        }
        return price;
    }
    
    public double getOrderPrice(Integer orderId) {
        double price = 0.0;
        try {
            CustomerOrder order = customerOrderRepository.findById(orderId).orElse(null);
            if (order != null) {
                price = order.calculateAmount();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error calculating order price", e);
            throw new RuntimeException(e.getMessage());
        }
        return price;
    }
    
    public void adjustOrderDiscount(int adjustment) {
        try {
            List<CustomerOrder> orders = customerOrderRepository.findAllOrders();
            for (CustomerOrder order : orders) {
                int newDiscount = order.getDiscount() + adjustment;
                order.setDiscount((newDiscount > 0) ? newDiscount : 0);
                customerOrderRepository.save(order);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error adjusting order discount", e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    public Double getAvgPrice() {
        try {
            return vendorPartRepository.findAverageVendorPartPrice();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting average price", e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    public Double getTotalPricePerVendor(int vendorId) {
        try {
            return vendorPartRepository.findTotalVendorPartPricePerVendor(vendorId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting total price per vendor", e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    public List<String> locateVendorsByPartialName(String name) {
        List<String> names = new ArrayList<>();
        try {
            List<Vendor> vendors = vendorRepository.findVendorsByPartialName(name);
            for (Vendor vendor : vendors) {
                names.add(vendor.getName());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error locating vendors", e);
            throw new RuntimeException(e.getMessage());
        }
        return names;
    }
    
    public int countAllItems() {
        int count = 0;
        try {
            count = lineItemRepository.findAllLineItems().size();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error counting items", e);
            throw new RuntimeException(e.getMessage());
        }
        return count;
    }

    public List<LineItem> getLineItems(int orderId) {
        try {
            return lineItemRepository.findLineItemsByOrderId(orderId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting line items", e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Transactional
    public void removeOrder(Integer orderId) {
        lineItemRepository.deleteAllByOrderId(orderId);
        customerOrderRepository.deleteById(orderId);
    }
    
    public String reportVendorsByOrder(Integer orderId) {
        StringBuilder report = new StringBuilder();
        try {
            List<Vendor> vendors = vendorRepository.findVendorByCustomerOrder(orderId);
            for (Vendor vendor : vendors) {
                report.append(vendor.getVendorId()).append(' ')
                        .append(vendor.getName()).append(' ')
                        .append(vendor.getContact()).append('\n');
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error reporting vendors by order", e);
            throw new RuntimeException(e);
        }
        return report.toString();
    }
    
    // Additional methods for JSF integration
    public List<CustomerOrder> getAllOrders() {
        return getOrders();
    }
    
    public List<LineItem> getLineItemsByOrderId(Integer orderId) {
        return getLineItems(orderId);
    }
    
    public List<Vendor> findVendorsByName(String name) {
        try {
            return vendorRepository.findVendorsByPartialName(name);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding vendors by name", e);
            throw new RuntimeException(e.getMessage());
        }
    }
}
