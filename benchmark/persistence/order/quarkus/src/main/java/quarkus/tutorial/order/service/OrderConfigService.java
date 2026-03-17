package quarkus.tutorial.order.service;

import quarkus.tutorial.order.repository.CustomerOrderRepository;
import quarkus.tutorial.order.repository.LineItemRepository;
import quarkus.tutorial.order.repository.PartRepository;
import quarkus.tutorial.order.repository.VendorPartRepository;
import quarkus.tutorial.order.repository.VendorRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.logging.Logger;

@ApplicationScoped
public class OrderConfigService {
    private static final Logger logger = Logger.getLogger(OrderConfigService.class.getName());

    @Inject
    private PartRepository partRepository;
    @Inject
    private VendorRepository vendorRepository;
    @Inject
    private VendorPartRepository vendorPartRepository;
    @Inject
    private CustomerOrderRepository customerOrderRepository;
    @Inject
    private LineItemRepository lineItemRepository;

    @Transactional
    public void createData() {
        logger.info("Starting dataset initialization");

        // Create Parts
        partRepository.createPart("1234-5678-01", 1, "ABC PART", new java.util.Date(), "PARTQWERTYUIOPASXDCFVGBHNJMKL", null);
        partRepository.createPart("9876-4321-02", 2, "DEF PART", new java.util.Date(), "PARTQWERTYUIOPASXDCFVGBHNJMKL", null);
        partRepository.createPart("5456-6789-03", 3, "GHI PART", new java.util.Date(), "PARTQWERTYUIOPASXDCFVGBHNJMKL", null);
        partRepository.createPart("ABCD-XYZW-FF", 5, "XYZ PART", new java.util.Date(), "PARTQWERTYUIOPASXDCFVGBHNJMKL", null);
        partRepository.createPart("SDFG-ERTY-BN", 7, "BOM PART", new java.util.Date(), "PARTQWERTYUIOPASXDCFVGBHNJMKL", null);

        // Add Parts to BOM
        partRepository.addPartToBillOfMaterial("SDFG-ERTY-BN", 7, "1234-5678-01", 1);
        partRepository.addPartToBillOfMaterial("SDFG-ERTY-BN", 7, "9876-4321-02", 2);
        partRepository.addPartToBillOfMaterial("SDFG-ERTY-BN", 7, "5456-6789-03", 3);
        partRepository.addPartToBillOfMaterial("SDFG-ERTY-BN", 7, "ABCD-XYZW-FF", 5);

        // Create Vendors
        vendorRepository.createVendor(100, "WidgetCorp", "111 Main St., Anytown, KY 99999", "Mr. Jones", "888-777-9999");
        vendorRepository.createVendor(200, "Gadget, Inc.", "123 State St., Sometown, MI 88888", "Mrs. Smith", "866-345-6789");

        // Create VendorParts
        vendorPartRepository.createVendorPart("1234-5678-01", 1, "PART1", 100.00, 100);
        vendorPartRepository.createVendorPart("9876-4321-02", 2, "PART2", 10.44, 200);
        vendorPartRepository.createVendorPart("5456-6789-03", 3, "PART3", 76.23, 200);
        vendorPartRepository.createVendorPart("ABCD-XYZW-FF", 5, "PART4", 55.19, 100);
        vendorPartRepository.createVendorPart("SDFG-ERTY-BN", 7, "PART5", 345.87, 100);

        // Create Orders and LineItems
        Integer orderId = 1111;
        customerOrderRepository.createOrder(orderId, 'N', 10, "333 New Court, New City, CA 90000");
        lineItemRepository.addLineItem(orderId, "1234-5678-01", 1, 3);
        lineItemRepository.addLineItem(orderId, "9876-4321-02", 2, 5);
        lineItemRepository.addLineItem(orderId, "ABCD-XYZW-FF", 5, 7);

        orderId = 4312;
        customerOrderRepository.createOrder(orderId, 'N', 0, "333 New Court, New City, CA 90000");
        lineItemRepository.addLineItem(orderId, "SDFG-ERTY-BN", 7, 1);
        lineItemRepository.addLineItem(orderId, "ABCD-XYZW-FF", 5, 3);
        lineItemRepository.addLineItem(orderId, "1234-5678-01", 1, 15);

        logger.info("Order data initialization completed successfully");
    }
}